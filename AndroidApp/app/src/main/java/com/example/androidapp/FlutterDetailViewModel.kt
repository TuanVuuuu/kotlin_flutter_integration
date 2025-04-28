package com.example.androidapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel cho FlutterDetailActivity theo mô hình MVVM
 */
class FlutterDetailViewModel : ViewModel() {
    // LiveData để quản lý message từ Flutter
    private val flutterMessage = MutableLiveData<String>().apply {
        // Giá trị mặc định
        value = ""
    }
    
    /**
     * Lấy LiveData của message từ Flutter
     */
    fun getFlutterMessage(): LiveData<String> = flutterMessage
    
    /**
     * Cập nhật message từ Flutter
     */
    fun setFlutterMessage(message: String) {
        flutterMessage.value = message
    }
    
    /**
     * Xử lý message gửi đến Flutter
     */
    fun sendMessageToFlutter(message: String): Boolean {
        // Có thể thêm logic xác thực trước khi gửi
        return if (message.isNotEmpty()) {
            // Thực hiện gửi message
            true
        } else {
            false
        }
    }
} 