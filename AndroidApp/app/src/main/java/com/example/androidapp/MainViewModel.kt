package com.example.androidapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel cho MainActivity theo mô hình MVVM
 */
class MainViewModel : ViewModel() {
    // LiveData để quản lý tab hiện tại
    private val currentTabIndex = MutableLiveData<Int>().apply {
        // Mặc định tab đầu tiên được chọn
        value = 0
    }
    
    /**
     * Lấy LiveData của tab hiện tại
     */
    fun getCurrentTabIndex(): LiveData<Int> = currentTabIndex
    
    /**
     * Cập nhật tab hiện tại
     */
    fun setCurrentTabIndex(index: Int) {
        if (index in 0..2) { // Chỉ cho phép các giá trị hợp lệ
            currentTabIndex.value = index
        }
    }
} 