package com.example.androidapp

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import io.flutter.embedding.android.RenderMode

/**
 * Manager để quản lý toàn bộ logic liên quan đến Flutter
 */
class FlutterViewManager private constructor(private val context: Context) {


    private val scenarioEngine: FlutterEngine by lazy {
        FlutterEngine(context).apply {
            navigationChannel.setInitialRoute("scenario_view")
            dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
            FlutterEngineCache.getInstance().put(SCENARIO_ENGINE_ID, this)
        }
    }

    
    private val methodChannel: MethodChannel by lazy {
        // MethodChannel(redFlutterEngine.dartExecutor, FLUTTER_CHANNEL_NAME)
        MethodChannel(scenarioEngine.dartExecutor, FLUTTER_CHANNEL_NAME)
    }
    
    /**
     * Thêm một FlutterView vào container với engine màu đỏ
     */
    fun attachRedFlutterView(container: FrameLayout): FlutterView {
        val flutterView = FlutterView(context, RenderMode.texture)
        flutterView.attachToFlutterEngine(scenarioEngine)
        
        // Đảm bảo FlutterView có thể nhận sự kiện touch
        flutterView.isClickable = true
        flutterView.isFocusable = true
        flutterView.isFocusableInTouchMode = true
        
        container.addView(flutterView)
        return flutterView
    }
    
    /**
     * Thêm một FlutterView vào container với engine màu xanh
     */
    fun attachBlueFlutterView(container: FrameLayout): FlutterView {
        val flutterView = FlutterView(context, RenderMode.texture)
        flutterView.attachToFlutterEngine(scenarioEngine)
        
        // Đảm bảo FlutterView có thể nhận sự kiện touch
        flutterView.isClickable = true
        flutterView.isFocusable = true
        flutterView.isFocusableInTouchMode = true
        
        container.addView(flutterView)
        return flutterView
    }
    
    /**
     * Gửi tin nhắn đến Flutter thông qua MethodChannel
     */
    fun sendMessage(message: String) {
        methodChannel.invokeMethod("showMessage", message)
    }
    
    /**
     * Giải phóng tài nguyên khi không cần nữa
     */
    fun dispose() {
        FlutterEngineCache.getInstance().remove(SCENARIO_ENGINE_ID)
    }
    
    companion object {
        private const val SCENARIO_ENGINE_ID = "scenario_engine"
        private const val FLUTTER_CHANNEL_NAME = "flutter_module_channel"
        
        @Volatile
        private var INSTANCE: FlutterViewManager? = null
        
        fun getInstance(context: Context): FlutterViewManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FlutterViewManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
} 