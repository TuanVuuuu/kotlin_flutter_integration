package com.example.androidapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Notification(val id: Int, val title: String, val message: String)

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        loadNotifications()
    }
    
    private fun loadNotifications() {
        _isLoading.value = true
        
        // Simulate loading notifications from a data source
        val notificationList = listOf(
            Notification(1, "Welcome", "Welcome to the notification screen"),
            Notification(2, "New message", "You have a new message from Flutter")
        )
        
        // Simulate network delay
        android.os.Handler().postDelayed({
            _notifications.value = notificationList
            _isLoading.value = false
        }, 500)
    }
    
    fun sendMessage(message: String) {
        // Could update the notification list or perform other operations
        val newNotification = Notification(
            id = (_notifications.value?.size ?: 0) + 3,
            title = "Sent",
            message = "You sent: $message"
        )
        
        val currentList = _notifications.value?.toMutableList() ?: mutableListOf()
        currentList.add(newNotification)
        _notifications.value = currentList
    }
} 