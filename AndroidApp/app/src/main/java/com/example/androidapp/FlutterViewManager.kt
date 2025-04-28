package com.example.androidapp

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel

/**
 * Manager để quản lý toàn bộ logic liên quan đến Flutter
 */
class FlutterViewManager private constructor(private val context: Context) {
    
    private val redFlutterEngine: FlutterEngine by lazy {
        FlutterEngine(context).apply {
            navigationChannel.setInitialRoute("red_square")
            dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
            FlutterEngineCache.getInstance().put(RED_ENGINE_ID, this)
        }
    }
    
    private val blueFlutterEngine: FlutterEngine by lazy {
        FlutterEngine(context).apply {
            navigationChannel.setInitialRoute("blue_square")
            dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
            FlutterEngineCache.getInstance().put(BLUE_ENGINE_ID, this)
        }
    }
    
    private val methodChannel: MethodChannel by lazy {
        MethodChannel(redFlutterEngine.dartExecutor, FLUTTER_CHANNEL_NAME)
    }
    
    /**
     * Thêm một FlutterView vào container với engine màu đỏ
     */
    fun attachRedFlutterView(container: FrameLayout): FlutterView {
        val flutterView = FlutterView(context)
        flutterView.attachToFlutterEngine(redFlutterEngine)
        container.addView(flutterView)
        return flutterView
    }
    
    /**
     * Thêm một FlutterView vào container với engine màu xanh
     */
    fun attachBlueFlutterView(container: FrameLayout): FlutterView {
        val flutterView = FlutterView(context)
        flutterView.attachToFlutterEngine(blueFlutterEngine)
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
        redFlutterEngine.destroy()
        blueFlutterEngine.destroy()
        FlutterEngineCache.getInstance().remove(RED_ENGINE_ID)
        FlutterEngineCache.getInstance().remove(BLUE_ENGINE_ID)
    }
    
    companion object {
        private const val RED_ENGINE_ID = "red_engine"
        private const val BLUE_ENGINE_ID = "blue_engine"
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