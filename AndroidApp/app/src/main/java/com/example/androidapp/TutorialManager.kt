package com.example.androidapp

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

/**
 * Manager class for handling tutorials across the app
 */
class TutorialManager private constructor(private val appContext: Context) {
    private val TAG = "TutorialManager"
    
    private val PREFS_NAME = "tutorial_prefs"
    private val TUTORIAL_DELAY = 500L // Delay before showing tutorial
    
    private val sharedPreferences: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())
    
    private var currentTutorial: Tutorial? = null
    private var currentStep = 0
    private var overlayView: TutorialOverlayView? = null
    
    // Store a weak reference to an activity context
    private var activityRef: WeakReference<Activity>? = null
    
    /**
     * Interface for callback events related to tutorial
     */
    interface TutorialCallback {
        // Callback methods can be added here if needed
    }
    
    init {
        Log.d(TAG, "Initializing TutorialManager")
        sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    companion object {
        private var instance: TutorialManager? = null
        
        @JvmStatic
        fun getInstance(context: Context): TutorialManager {
            if (instance == null) {
                instance = TutorialManager(context.applicationContext)
                Log.d("TutorialManager", "Created new instance")
            }
            
            // Always update activity reference if it's an activity
            if (context is Activity) {
                instance?.updateActivityReference(context)
            }
            
            return instance!!
        }
    }
    
    /**
     * Update the activity reference
     */
    fun updateActivityReference(activity: Activity) {
        Log.d(TAG, "Updating activity reference to ${activity.javaClass.simpleName}")
        activityRef = WeakReference(activity)
    }
    
    /**
     * Get current activity context or null
     */
    private fun getActivityContext(): Activity? {
        val activity = activityRef?.get()
        if (activity == null || activity.isFinishing) {
            Log.e(TAG, "Activity reference is null or activity is finishing")
            return null
        }
        return activity
    }
    
    /**
     * Check if tutorial has been shown before
     */
    fun hasShownTutorial(tutorialId: String): Boolean {
        val hasShown = sharedPreferences.getBoolean("tutorial_shown_$tutorialId", false)
        Log.d(TAG, "Tutorial $tutorialId has shown before: $hasShown")
        return hasShown
    }
    
    /**
     * Mark tutorial as shown
     */
    fun markTutorialAsShown(tutorialId: String) {
        Log.d(TAG, "Marking tutorial $tutorialId as shown")
        sharedPreferences.edit().putBoolean("tutorial_shown_$tutorialId", true).apply()
    }
    
    /**
     * Reset tutorial shown status
     */
    fun resetTutorialStatus(tutorialId: String) {
        Log.d(TAG, "Resetting tutorial status for $tutorialId")
        sharedPreferences.edit().putBoolean("tutorial_shown_$tutorialId", false).apply()
        try {
            Toast.makeText(appContext, "Tutorial $tutorialId reset", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast in resetTutorialStatus: ${e.message}")
        }
    }
    
    /**
     * Create a simplified tutorial with 2 steps
     */
    fun createTutorial(
        activity: Activity,
        tutorialId: String,
        steps: List<TutorialStepInput>
    ): Tutorial {
        Log.d(TAG, "Creating tutorial with ID: $tutorialId and ${steps.size} steps")
        updateActivityReference(activity)
        val tutorialSteps = steps.map {
            if (it.view.width == 0 || it.view.height == 0) {
                Log.e(TAG, "Warning: Target view has zero width or height: ${it.view.width}x${it.view.height}")
            }
            TutorialStep(it.view, "${it.title}\n\n${it.message}")
        }
        return Tutorial(tutorialId, tutorialSteps)
    }
    
    /**
     * Show a tutorial
     */
    fun showTutorial(tutorial: Tutorial) {
        try {
            Log.d(TAG, "Preparing to show tutorial: ${tutorial.id}")
            
            // Check if tutorial has been shown before
            if (hasShownTutorial(tutorial.id)) {
                Log.d(TAG, "Tutorial ${tutorial.id} has already been shown")
                return
            }
            
            // Create overlay if needed
            if (overlayView == null) {
                Log.d(TAG, "Creating new TutorialOverlayView")
                overlayView = TutorialOverlayView(appContext)
            }
            
            // Show a toast to inform user about tutorial activation
            try {
                Toast.makeText(appContext, "Tutorial activated", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error showing toast for tutorial activation: ${e.message}")
            }
            
            // Add a delay to ensure UI is properly laid out
            handler.postDelayed({
                try {
                    startTutorial(tutorial)
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting tutorial: ${e.message}", e)
                    try {
                        Toast.makeText(appContext, "Error starting tutorial: ${e.message}", Toast.LENGTH_SHORT).show()
                    } catch (t: Exception) {
                        Log.e(TAG, "Error showing toast for tutorial error: ${t.message}")
                    }
                }
            }, TUTORIAL_DELAY)
        } catch (e: Exception) {
            Log.e(TAG, "Error queuing tutorial: ${e.message}", e)
            try {
                Toast.makeText(appContext, "Error preparing tutorial: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (t: Exception) {
                Log.e(TAG, "Error showing toast for tutorial preparation error: ${t.message}")
            }
        }
    }
    
    /**
     * Start showing a tutorial
     */
    private fun startTutorial(tutorial: Tutorial) {
        Log.d(TAG, "Starting tutorial: ${tutorial.id}")
        try {
            // Check if a tutorial is already active
            if (currentTutorial != null) {
                Log.d(TAG, "Another tutorial is already active, ending it first")
                endTutorial()
            }
            
            // Set as active tutorial
            currentTutorial = tutorial
            currentStep = 0
            
            // Get activity from weak reference
            val activity = getActivityContext()
            if (activity == null) {
                Log.e(TAG, "Activity reference is null or activity is finishing, cannot show tutorial")
                Toast.makeText(appContext, "Error: Cannot show tutorial - no valid activity", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Create new overlay for each tutorial to ensure clean state
            Log.d(TAG, "Creating new TutorialOverlayView")
            overlayView = TutorialOverlayView(activity) // Use activity context instead of application context
            
            // Add overlay to the activity
            val overlay = overlayView ?: run {
                Log.e(TAG, "Overlay view is null")
                return
            }
            
            // Remove overlay from parent if it already has one
            if (overlay.parent != null) {
                Log.d(TAG, "Removing overlay from existing parent")
                (overlay.parent as? ViewGroup)?.removeView(overlay)
            }
            
            // Add overlay to root view
            val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
                rootView.addView(overlay)
                Log.d(TAG, "Added overlay view to root")
            
            // KHÔNG thiết lập dismiss listener ở đây, thay vào đó sẽ thiết lập trong showStep
            // để tránh bị ghi đè
            
            // Show first step
            showStep(0)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting tutorial: ${e.message}", e)
            Toast.makeText(appContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Show a specific step in the tutorial
     */
    private fun showStep(stepIndex: Int) {
        Log.d(TAG, "Showing step $stepIndex")
        try {
            val tutorial = currentTutorial ?: run {
                Log.e(TAG, "No tutorial is active")
                return
            }
            
            if (stepIndex < 0 || stepIndex >= tutorial.steps.size) {
                Log.e(TAG, "Invalid step index: $stepIndex (total steps: ${tutorial.steps.size})")
                return
            }
            
            currentStep = stepIndex
            val step = tutorial.steps[stepIndex]
            
            // Get the view for this step
            val targetView = step.targetView
            if (targetView == null) {
                Log.e(TAG, "Target view is null")
                nextStep() // Skip to next step
                return
            }
            
            if (!isViewAttachedToWindow(targetView)) {
                Log.e(TAG, "Target view is not attached to window")
                nextStep() // Skip to next step
                return
            }
            
            // View is valid, check its visibility and size
            if (!targetView.isShown) {
                Log.e(TAG, "Target view is not visible")
                nextStep() // Skip to next step
                return
            }
            
            if (targetView.width == 0 || targetView.height == 0) {
                Log.e(TAG, "Target view has zero size: ${targetView.width}x${targetView.height}")
                nextStep() // Skip to next step
                return
            }
            
            Log.d(TAG, "Setting target view: ${targetView.javaClass.simpleName} with message: ${step.message}")
            Log.d(TAG, "Target view size: ${targetView.width}x${targetView.height}")
            
            // Set the target view in overlay
            val overlay = overlayView
            if (overlay == null) {
                Log.e(TAG, "Overlay view is null")
                return
            }
            
            // Đảm bảo overlay được đính kèm vào parent view
            val activity = getActivityContext()
            if (activity != null && overlay.parent == null) {
                Log.w(TAG, "Overlay is not attached to window, adding it back")
                val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
                try {
                    rootView.addView(overlay)
                } catch (e: Exception) {
                    Log.e(TAG, "Error adding overlay back to window: ${e.message}")
                    // Nếu không thêm được, tạo lại tutorial
                    startTutorial(tutorial)
                    return
                }
            }
            
            // Xác định vị trí tooltip (trên hoặc dưới target)
            val screenHeight = activity?.resources?.displayMetrics?.heightPixels ?: 0
            val targetLocation = IntArray(2)
            targetView.getLocationOnScreen(targetLocation)
            val targetY = targetLocation[1]
            
            // Nếu target nằm ở nửa dưới màn hình, hiển thị tooltip bên trên
            val position = if (targetY > screenHeight / 2) 
                TutorialOverlayView.Position.ABOVE 
            else 
                TutorialOverlayView.Position.BELOW
            
            // Thiết lập lại dismiss listener TRƯỚC khi hiển thị tooltip mới
            val finalActivity = activity // Capture for lambda
            overlay.setOnDismissListener {
                Log.d(TAG, "Dismiss listener triggered from showStep(), step: $currentStep")
                handler.post {
                    try {
                        Log.d(TAG, "Handler calling nextStep()")
                        nextStep()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in dismiss listener callback: ${e.message}", e)
                        try {
                            Log.d(TAG, "Retrying nextStep() after error")
                            // Thử lại nếu có lỗi
                            nextStep()
                        } catch (t: Exception) {
                            Log.e(TAG, "Second attempt to run nextStep also failed: ${t.message}", t)
                            // Thử cách cuối cùng - gọi trực tiếp showStep với step tiếp theo
                            try {
                                showStep(currentStep + 1)
                            } catch (e3: Exception) {
                                Log.e(TAG, "All attempts to continue tutorial failed", e3)
                            }
                        }
                    }
                }
            }
            
            // Hiển thị tooltip sau khi đã thiết lập listener
            overlay.post {
                Log.d(TAG, "Showing tooltip for step $currentStep with position $position")
                overlay.showTooltip(targetView, step.message, position)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing step: ${e.message}", e)
        }
    }
    
    /**
     * Move to the next step or end tutorial
     */
    private fun nextStep() {
        Log.d(TAG, "Moving to next step, current step: $currentStep")
        val stepStartTime = System.currentTimeMillis()
        
        try {
            val tutorial = currentTutorial ?: run {
                Log.e(TAG, "No tutorial is active")
                return
            }
            
            val nextStepIndex = currentStep + 1
            Log.d(TAG, "Total steps: ${tutorial.steps.size}, next step: $nextStepIndex")
            
            if (nextStepIndex < tutorial.steps.size) {
                Log.d(TAG, "Moving to step $nextStepIndex")
                
                // Nếu overlay đã bị hủy, cần tạo mới
                if (overlayView == null || overlayView?.parent == null) {
                    Log.w(TAG, "Overlay was lost, recreating tutorial")
                    startTutorial(tutorial)
                    return
                }
                
                // Đảm bảo xóa tooltip cũ trước khi hiển thị mới
                overlayView?.cleanup()
                
                // Delay nhỏ trước khi hiển thị tooltip mới
                handler.postDelayed({
                    Log.d(TAG, "Delayed showStep($nextStepIndex) called after ${System.currentTimeMillis() - stepStartTime}ms")
                    showStep(nextStepIndex)
                }, 100)
            } else {
                Log.d(TAG, "End of tutorial reached")
                endTutorial()
                markTutorialAsShown(tutorial.id)
                Toast.makeText(appContext, "Tutorial completed", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error moving to next step: ${e.message}", e)
            
            // Tìm cách khôi phục trạng thái khi có lỗi
            try {
                endTutorial() // Kết thúc tutorial hiện tại để tránh lỗi khôi phục được
            } catch (t: Exception) {
                Log.e(TAG, "Error ending tutorial after nextStep failure: ${t.message}", t)
            }
        }
    }
    
    /**
     * End the current tutorial
     */
    fun endTutorial() {
        Log.d(TAG, "Ending tutorial")
        try {
            val activity = getActivityContext()
            if (activity == null) {
                Log.d(TAG, "Activity is null or finishing, skipping cleanup")
                // Vẫn cần cleanup overlay ngay cả khi activity null
                overlayView?.cleanup()
                overlayView = null
                currentTutorial = null
                currentStep = 0
                return
            }
            
            val overlay = overlayView
            if (overlay != null) {
                Log.d(TAG, "Cleaning up overlay")
                
                // Đảm bảo xóa targetView để tránh hiển thị highlight
                activity.runOnUiThread {
                    try {
                        // Nếu overlay còn parent, xóa khỏi parent
                        if (overlay.parent != null) {
                            val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
                            rootView.removeView(overlay)
                            Log.d(TAG, "Removed overlay view from root")
                        }
                        
                        // Clean up resources in the overlay
                        overlay.cleanup()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error removing overlay on UI thread: ${e.message}", e)
                        
                        // Vẫn cần cleanup ngay cả khi có lỗi
                        try {
                            overlay.cleanup()
                        } catch (t: Exception) {
                            Log.e(TAG, "Error cleaning up overlay: ${t.message}", t)
                        }
                    }
                }
            }
            
            overlayView = null
            currentTutorial = null
            currentStep = 0
            
            Log.d(TAG, "Tutorial ended successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error ending tutorial: ${e.message}", e)
            try {
                // Vẫn cần cleanup ngay cả khi có lỗi
                overlayView?.cleanup()
            } catch (t: Exception) {
                Log.e(TAG, "Error cleaning up overlay in exception handler: ${t.message}", t)
            }
            
            overlayView = null
            currentTutorial = null
            currentStep = 0
        }
    }
    
    /**
     * Data class for a tutorial step
     */
    data class TutorialStep(
        val targetView: View?,
        val message: String
    )
    
    /**
     * Data class for a tutorial
     */
    data class Tutorial(
        val id: String,
        val steps: List<TutorialStep>,
        val onComplete: (() -> Unit)? = null
    )
    
    /**
     * Data class for a pending tutorial
     */
    data class PendingTutorial(
        val fragment: Fragment,
        val tutorial: Tutorial
    )
    
    /**
     * Check if a tutorial is currently active
     */
    fun isTutorialActive(): Boolean {
        return currentTutorial != null
    }
    
    /**
     * Check if a specific tutorial is currently active
     */
    fun isTutorialActive(tutorialId: String): Boolean {
        val tutorial = currentTutorial ?: return false
        return tutorial.id == tutorialId
    }
    
    /**
     * Cancel the current tutorial
     */
    fun cancelTutorial() {
        try {
            Log.d(TAG, "Attempting to cancel tutorial")
            if (isTutorialActive()) {
                Log.d(TAG, "Active tutorial found, cancelling")
                endTutorial()
                try {
                    Toast.makeText(appContext, "Tutorial cancelled", Toast.LENGTH_SHORT).show()
                } catch (t: Exception) {
                    Log.e(TAG, "Error showing toast for tutorial cancellation: ${t.message}")
                }
            } else {
                Log.d(TAG, "No active tutorial to cancel")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling tutorial: ${e.message}", e)
            currentTutorial = null
            currentStep = 0
        }
    }
    
    /**
     * Get the ID of the currently active tutorial or null if no tutorial is active
     */
    fun getCurrentTutorialId(): String? {
        return currentTutorial?.id
    }
    
    /**
     * Check if a view is attached to a window
     */
    private fun isViewAttachedToWindow(view: View): Boolean {
        return view.isAttachedToWindow
    }
    
    data class TutorialStepInput(val view: View, val title: String, val message: String)
} 