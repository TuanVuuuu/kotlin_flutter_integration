package com.example.androidapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.Toast
import com.example.androidapp.databinding.FragmentHomeBinding
import com.example.androidapp.tab.TabFragment

/**
 * Fragment home, theo mô hình MVVM
 */
class HomeFragment : Fragment(), TabFragment, TutorialManager.TutorialCallback {
    private val TAG = "HomeFragment"
    
    private lateinit var viewModel: HomeViewModel
    
    // ViewBinding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    // ID của tutorial cho màn hình Home - use consistent ID
    private val HOME_TUTORIAL_ID = "home_tutorial"
    
    // Manager cho tutorial - initialized early to avoid null issues
    private lateinit var tutorialManager: TutorialManager
    
    // Biến đánh dấu đã kiểm tra hiển thị tutorial
    private var hasCheckedTutorial = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Creating HomeFragment")
        // Khởi tạo ViewBinding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        
        // Khởi tạo ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        return view
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: View created")
        
        setupUI()
        
        // Initialize TutorialManager with Activity context
        try {
            tutorialManager = TutorialManager.getInstance(requireActivity())
            Log.d(TAG, "TutorialManager initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TutorialManager: ${e.message}", e)
        }
        
        // Log views for debugging
        Log.d(TAG, "btnItem1 visibility: ${binding.btnItem1.visibility}")
        Log.d(TAG, "btnItem2 visibility: ${binding.btnItem2.visibility}")
        
        // Initialize hasCheckedTutorial to false whenever view is created
        hasCheckedTutorial = false
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Fragment resumed, visibility: $isVisible")
        
        // Update the activity reference whenever the fragment resumes
        if (this::tutorialManager.isInitialized) {
            try {
                val activity = activity
                if (activity != null && !activity.isFinishing) {
                    tutorialManager.updateActivityReference(activity)
                    Log.d(TAG, "Updated TutorialManager activity reference in onResume")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating TutorialManager activity reference: ${e.message}", e)
            }
        }
    }
    
    private fun setupUI() {
        // Setup UI elements
        binding.btnItem1.setOnClickListener {
            Log.d(TAG, "btnItem1 clicked")
            if (viewModel.prepareForFlutterScreen()) {
                startActivity(Intent(requireContext(), FlutterDetailActivity::class.java))
            }
        }
        
        binding.btnItem2.setOnClickListener {
            Log.d(TAG, "btnItem2 clicked")
            Toast.makeText(requireContext(), "Item 2 clicked", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnItem3.setOnClickListener {
            Log.d(TAG, "btnItem3 clicked")
            Toast.makeText(requireContext(), "Item 3 clicked", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showTutorialIfNeeded() {
        Log.d(TAG, "showTutorialIfNeeded called")
        
        try {
            // Check TutorialManager is initialized
            if (!this::tutorialManager.isInitialized) {
                Log.e(TAG, "tutorialManager not initialized")
                try {
                    tutorialManager = TutorialManager.getInstance(requireActivity())
                    Log.d(TAG, "TutorialManager initialized")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize TutorialManager: ${e.message}", e)
                    return
                }
            }
            
            // Update activity reference
            val currentActivity = activity
            if (currentActivity != null && !currentActivity.isFinishing) {
                tutorialManager.updateActivityReference(currentActivity)
                Log.d(TAG, "Updated TutorialManager activity reference")
            }
            
            // Debug button states
            val btn1 = binding.btnItem1
            val btn2 = binding.btnItem2
            val btn3 = binding.btnItem3
            
            Log.d(TAG, "Button btnItem1 is attached: ${btn1.isAttachedToWindow}")
            Log.d(TAG, "Button btnItem2 is attached: ${btn2.isAttachedToWindow}")
            Log.d(TAG, "Button btnItem3 is attached: ${btn3.isAttachedToWindow}")
            
            Log.d(TAG, "Button btnItem1 visibility: ${btn1.visibility}")
            Log.d(TAG, "Button btnItem2 visibility: ${btn2.visibility}")
            Log.d(TAG, "Button btnItem3 visibility: ${btn3.visibility}")
            
            // Check tutorial shown status
            val hasShown = tutorialManager.hasShownTutorial(HOME_TUTORIAL_ID)
            Log.d(TAG, "Has tutorial been shown before: $hasShown")
            
            if (hasShown) {
                Log.d(TAG, "Tutorial already shown, skipping")
                return
            }
            
            // Check fragment visibility but ALLOW it to continue even if not visible
            // This is to debug why isVisible might be false when we expect it to be true
            if (!isVisible) {
                Log.d(TAG, "Fragment visibility is FALSE but continuing anyway for testing")
            } else {
                Log.d(TAG, "Fragment is visible")
            }
            
            if (!isAdded) {
                Log.e(TAG, "Fragment is not added, skipping tutorial")
                return
            }
            
            // Create and show a toast to debug - check if context is available
            val context = context
            if (context != null) {
                Toast.makeText(context, "Preparing to show tutorial", Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "Context is null, cannot show Toast")
            }
            
            // Wait for view to be properly laid out - use a longer delay
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    // Check if fragment is still attached
                    if (!isAdded || context == null) {
                        Log.e(TAG, "Fragment is no longer attached or context is null")
                        return@postDelayed
                    }
                    
                    // Ensure we have a valid activity
                    val fragmentActivity = activity
                    if (fragmentActivity == null || fragmentActivity.isFinishing) {
                        Log.e(TAG, "Activity is null or finishing, cannot show tutorial")
                        return@postDelayed
                    }
                    
                    // Update activity reference one more time
                    tutorialManager.updateActivityReference(fragmentActivity)
                    
                    // Force reset tutorial status to ensure it shows
                    tutorialManager.resetTutorialStatus(HOME_TUTORIAL_ID)
                    
                    val steps = listOf(
                        TutorialManager.TutorialStepInput(binding.btnItem1, "Mở màn hình Flutter", "Bấm vào đây để mở màn hình Flutter"),
                        TutorialManager.TutorialStepInput(binding.btnItem2, "Chức năng 2", "Bấm vào đây để sử dụng chức năng 2"),
                        TutorialManager.TutorialStepInput(binding.btnItem3, "Chức năng 3", "Bấm vào đây để sử dụng chức năng 3")

                        // Thêm các bước khác nếu muốn
                    )
                    val tutorial = tutorialManager.createTutorial(
                        fragmentActivity,
                        HOME_TUTORIAL_ID,
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
            }, 500) // Increased delay to 500ms
        } catch (e: Exception) {
            Log.e(TAG, "Exception in showTutorialIfNeeded: ${e.message}", e)
            val ctx = context
            if (ctx != null) {
                Toast.makeText(ctx, "Error checking tutorial: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        // Xóa binding để tránh memory leak
        _binding = null
    }
    
    /**
     * Phương thức để reset tutorial (thường dùng cho testing)
     */
    fun resetTutorial() {
        Log.d(TAG, "resetTutorial called")
        tutorialManager.resetTutorialStatus(HOME_TUTORIAL_ID)
        Toast.makeText(context, "Tutorial reset, will show on next tab selection", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Called when the tab containing this fragment is selected
     */
    override fun onTabSelected() {
        Log.d(TAG, "onTabSelected - HomeFragment")
        showTutorialIfNeeded()
    }

    /**
     * Called when the tab containing this fragment is unselected
     */
    override fun onTabUnselected() {
        Log.d(TAG, "onTabUnselected - HomeFragment")
        try {
            if (this::tutorialManager.isInitialized) {
                Log.d(TAG, "Calling tutorialManager.cancelTutorial()")
                tutorialManager.cancelTutorial()
                Log.d(TAG, "Cancelled tutorial on tab unselection")
            } else {
                Log.d(TAG, "TutorialManager not initialized, skipping tutorial cancellation")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling tutorial on tab unselection: ${e.message}", e)
        }
    }
} 