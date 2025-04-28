package com.example.androidapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.androidapp.databinding.FragmentNotificationBinding
import com.example.androidapp.tab.TabFragment

/**
 * Fragment quản lý phần thông báo
 */
class NotificationFragment : Fragment(), TabFragment, TutorialManager.TutorialCallback {
    private val TAG = "NotificationFragment"
    
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: NotificationViewModel
    
    // Use lazy initialization like in HomeFragment
    private val tutorialManager by lazy { TutorialManager.getInstance(requireContext()) }
    
    // ID for this fragment's tutorial
    private val NOTIFICATION_TUTORIAL_ID = "notification_tutorial"
    
    // Flag to track if we've already checked for tutorial
    private var hasCheckedTutorial = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(NotificationViewModel::class.java)
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        
        // Reset the tutorial check flag when view is created
        hasCheckedTutorial = false
    }
    
    private fun setupUI() {
        // Set up the send message button
        binding.btnSendMessage.setOnClickListener {
            val message = binding.editTextMessage.text.toString()
            Log.d(TAG, "Sending message: $message")
            
            // Call viewModel method
            viewModel.sendMessage(message)
            
            // Clear input field
            binding.editTextMessage.text.clear()
            
            // Show confirmation
            Toast.makeText(context, "Message sent: $message", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTabSelected() {
        Log.d(TAG, "Notification tab selected")
        
        try {
            // Only check for tutorial once per tab selection
            if (!hasCheckedTutorial) {
                hasCheckedTutorial = true
                
                // Show a toast for debugging
                Toast.makeText(context, "Notification tab selected", Toast.LENGTH_SHORT).show()
                
                // Delay tutorial check to ensure view is ready
                Handler(Looper.getMainLooper()).postDelayed({
                    showTutorialIfNeeded()
                }, 300)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onTabSelected: ${e.message}", e)
            Toast.makeText(context, "Error in tab selection: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showTutorialIfNeeded() {
        Log.d(TAG, "showTutorialIfNeeded called")
        
        try {
            // Check if tutorial has been shown before
            val hasShown = tutorialManager.hasShownTutorial(NOTIFICATION_TUTORIAL_ID)
            Log.d(TAG, "Has tutorial been shown before: $hasShown")
            
            if (hasShown) {
                Log.d(TAG, "Tutorial already shown, skipping")
                return
            }
            
            // Check if fragment is added
            if (!isAdded) {
                Log.e(TAG, "Fragment is not added, skipping tutorial")
                return
            }
            
            // Create and show a toast for debugging - check if context is available
            val context = context
            if (context != null) {
                Toast.makeText(context, "Preparing to show notification tutorial", Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "Context is null, cannot show Toast")
            }
            
            // Wait for views to be properly laid out
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    // Check if fragment is still attached and has context
                    if (!isAdded || context == null) {
                        Log.e(TAG, "Fragment is no longer attached or context is null")
                        return@postDelayed
                    }
                    
                    // Force reset tutorial status to ensure it shows
                    tutorialManager.resetTutorialStatus(NOTIFICATION_TUTORIAL_ID)
                    
                    // Check if input field exists
                    if (binding.editTextMessage == null || binding.btnSendMessage == null) {
                        Log.e(TAG, "Tutorial targets are null")
                        if (context != null) {
                            Toast.makeText(context, "Cannot show tutorial: target views not found", Toast.LENGTH_SHORT).show()
                        }
                        return@postDelayed
                    }
                    
                    // Create tutorial with two steps
                    val steps = listOf(
                        TutorialManager.TutorialStepInput(binding.editTextMessage, "Nhập tin nhắn", "Nhập nội dung tin nhắn vào đây"),
                        TutorialManager.TutorialStepInput(binding.btnSendMessage, "Gửi tin nhắn", "Bấm vào đây để gửi tin nhắn")
                    )
                    val tutorial = tutorialManager.createTutorial(
                        requireActivity(),
                        NOTIFICATION_TUTORIAL_ID,
                        steps
                    )
                    
                    // Show the tutorial
                    Log.d(TAG, "Tutorial created, now showing")
                    tutorialManager.showTutorial(tutorial)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating/showing tutorial: ${e.message}", e)
                    val ctx = context
                    if (ctx != null) {
                        Toast.makeText(ctx, "Error showing tutorial: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }, 500)
        } catch (e: Exception) {
            Log.e(TAG, "Exception in showTutorialIfNeeded: ${e.message}", e)
            val ctx = context
            if (ctx != null) {
                Toast.makeText(ctx, "Error checking tutorial: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onTabUnselected() {
        Log.d(TAG, "onTabUnselected")
        try {
            // Cancel any active tutorials when tab is unselected
            if (context != null) {
                TutorialManager.getInstance(requireContext()).cancelTutorial()
                Log.d(TAG, "Cancelled tutorial on tab unselection")
            } else {
                Log.d(TAG, "Context is null, skipping tutorial cancellation")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling tutorial on tab unselection: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        // Clear binding to avoid memory leaks
        _binding = null
    }
} 