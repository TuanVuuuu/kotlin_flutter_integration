package com.example.androidapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import android.util.Log

/**
 * Activity hiển thị màn hình Flutter chi tiết
 * Theo mô hình MVVM
 */
class FlutterDetailActivity : AppCompatActivity() {
    private val TAG = "FlutterDetailActivity"
    private val ENGINE_ID = "detail_engine"
    private val CHANNEL_NAME = "com.example.androidapp/detail"
    
    private lateinit var viewModel: FlutterDetailViewModel
    private var flutterFragment: FlutterFragment? = null
    private lateinit var methodChannel: MethodChannel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flutter_detail)
        
        // Khởi tạo ViewModel
        viewModel = androidx.lifecycle.ViewModelProvider(this).get(FlutterDetailViewModel::class.java)
        
        // Thiết lập Flutter Engine
        setupFlutterEngine()
        
        // Thiết lập Flutter Fragment
        setupFlutterFragment()
        
        // Thiết lập sự kiện cho nút quay lại
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish() // Đóng activity và quay lại màn hình trước đó
        }
        
        // Observe ViewModel data
        observeViewModel()
    }
    
    private fun setupFlutterEngine() {
        // Tạo FlutterEngine mới nếu chưa có trong cache
        if (FlutterEngineCache.getInstance().get(ENGINE_ID) == null) {
            val flutterEngine = FlutterEngine(applicationContext).apply {
                navigationChannel.setInitialRoute("detail_view")
                dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
                
                // Thiết lập MethodChannel để giao tiếp với Flutter
                methodChannel = MethodChannel(dartExecutor.binaryMessenger, CHANNEL_NAME)
                methodChannel.setMethodCallHandler { call, result ->
                    when (call.method) {
                        "getSharedData" -> {
                            // Lấy data từ scenario engine (engine chính)
                            val scenarioEngine = FlutterEngineCache.getInstance().get("scenario_engine")
                            if (scenarioEngine != null) {
                                val scenarioChannel = MethodChannel(scenarioEngine.dartExecutor.binaryMessenger, "com.example.androidapp/scenario")
                                scenarioChannel.invokeMethod("getCurrentData", null, object : MethodChannel.Result {
                                    override fun success(data: Any?) {
                                        Log.d(TAG, "Got data from scenario engine: $data")
                                        result.success(data)
                                    }
                                    
                                    override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                                        Log.e(TAG, "Error getting data from scenario engine: $errorMessage")
                                        result.error(errorCode, errorMessage, errorDetails)
                                    }
                                    
                                    override fun notImplemented() {
                                        Log.e(TAG, "Method not implemented in scenario engine")
                                        result.notImplemented()
                                    }
                                })
                            } else {
                                result.error("NO_ENGINE", "Scenario engine not found", null)
                            }
                        }
                        "sendDataToScenario" -> {
                            // Gửi data đến scenario engine
                            val data = call.arguments
                            val scenarioEngine = FlutterEngineCache.getInstance().get("scenario_engine")
                            if (scenarioEngine != null) {
                                val scenarioChannel = MethodChannel(scenarioEngine.dartExecutor.binaryMessenger, "com.example.androidapp/scenario")
                                scenarioChannel.invokeMethod("receiveData", data, object : MethodChannel.Result {
                                    override fun success(data: Any?) {
                                        Log.d(TAG, "Data sent to scenario engine successfully")
                                        result.success(data)
                                    }
                                    
                                    override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                                        Log.e(TAG, "Error sending data to scenario engine: $errorMessage")
                                        result.error(errorCode, errorMessage, errorDetails)
                                    }
                                    
                                    override fun notImplemented() {
                                        Log.e(TAG, "Method not implemented in scenario engine")
                                        result.notImplemented()
                                    }
                                })
                            } else {
                                result.error("NO_ENGINE", "Scenario engine not found", null)
                            }
                        }
                        else -> result.notImplemented()
                    }
                }
            }
            FlutterEngineCache.getInstance().put(ENGINE_ID, flutterEngine)
        } else {
            // Nếu engine đã tồn tại, lấy MethodChannel từ engine đó
            val existingEngine = FlutterEngineCache.getInstance().get(ENGINE_ID)
            methodChannel = MethodChannel(existingEngine!!.dartExecutor.binaryMessenger, CHANNEL_NAME)
        }
    }
    
    private fun setupFlutterFragment() {
        // Tạo FlutterFragment với engine đã cache
        flutterFragment = FlutterFragment
            .withCachedEngine(ENGINE_ID)
            .shouldAttachEngineToActivity(true)
            .build<FlutterFragment>()

        // Thêm FlutterFragment vào container
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.flutterContainer, flutterFragment!!)
            .commit()
    }
    
    private fun observeViewModel() {
        // Observe data changes từ ViewModel
        viewModel.getFlutterMessage().observe(this) { message ->
            // Gửi message đến Flutter thông qua MethodChannel
            methodChannel.invokeMethod("receiveMessage", message)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up engine if this is the last activity using it
        if (FlutterEngineCache.getInstance().get(ENGINE_ID) != null) {
            FlutterEngineCache.getInstance().get(ENGINE_ID)?.destroy()
            FlutterEngineCache.getInstance().remove(ENGINE_ID)
        }
    }
} 