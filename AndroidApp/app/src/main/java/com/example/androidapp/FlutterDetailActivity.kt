package com.example.androidapp

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import io.flutter.embedding.android.FlutterView

/**
 * Activity hiển thị màn hình Flutter chi tiết
 * Theo mô hình MVVM
 */
class FlutterDetailActivity : AppCompatActivity() {
    private lateinit var flutterViewManager: FlutterViewManager
    private var flutterView: FlutterView? = null
    private lateinit var viewModel: FlutterDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flutter_detail)
        
        // Khởi tạo ViewModel
        viewModel = androidx.lifecycle.ViewModelProvider(this).get(FlutterDetailViewModel::class.java)
        
        // Khởi tạo FlutterViewManager
        flutterViewManager = FlutterViewManager.getInstance(this)
        
        // Thiết lập Flutter View
        setupFlutterView()
        
        // Thiết lập sự kiện cho nút quay lại
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish() // Đóng activity và quay lại màn hình trước đó
        }
        
        // Observe ViewModel data
        observeViewModel()
    }
    
    private fun observeViewModel() {
        // Observe data changes từ ViewModel
        viewModel.getFlutterMessage().observe(this) { _ ->
            // Xử lý message từ ViewModel nếu cần
        }
    }
    
    private fun setupFlutterView() {
        // Lấy container và đính kèm Flutter view
        val container = findViewById<FrameLayout>(R.id.flutterContainer)
        // Sử dụng Flutter view màu xanh cho màn hình chi tiết
        flutterView = flutterViewManager.attachBlueFlutterView(container)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Tách FlutterView khỏi engine khi activity bị hủy
        flutterView?.detachFromFlutterEngine()
    }
} 