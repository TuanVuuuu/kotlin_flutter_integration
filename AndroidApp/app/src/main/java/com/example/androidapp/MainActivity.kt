package com.example.androidapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.androidapp.databinding.ActivityMainBinding
import com.example.androidapp.tab.TabFragment

/**
 * Activity chính của ứng dụng, quản lý Bottom Navigation và các Fragment
 * Theo mô hình MVVM
 */
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    
    private lateinit var flutterViewManager: FlutterViewManager
    private lateinit var bottomNavAdapter: BottomNavigationAdapter
    private lateinit var viewModel: MainViewModel
    private lateinit var tutorialManager: TutorialManager
    
    // ViewBinding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Khởi tạo ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Khởi tạo ViewModel
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        
        // Khởi tạo FlutterViewManager
        flutterViewManager = FlutterViewManager.getInstance(this)
        
        // Khởi tạo TutorialManager
        tutorialManager = TutorialManager.getInstance(this)
        
        // Reset tutorial status cho HomeFragment để kiểm tra (chỉ dùng cho debug)
        tutorialManager.resetTutorialStatus("home_tutorial")
        
        // Reset tutorial status cho NotificationFragment để kiểm tra (chỉ dùng cho debug)
        tutorialManager.resetTutorialStatus("notification_tutorial")
        
        // Khởi tạo Bottom Navigation
        setupBottomNavigation()
        
        // Observe ViewModel data
        observeViewModel()
        
        // Ensure default tab is selected after everything is initialized
        selectDefaultTab()
    }
    
    /**
     * Thiết lập Bottom Navigation và các listener
     */
    private fun setupBottomNavigation() {
        // Lấy danh sách các tab layout
        val tabLayouts = listOf(
            binding.tab1,
            binding.tab2,
            binding.tab3
        )
        
        // Khởi tạo adapter với các fragment mặc định
        bottomNavAdapter = BottomNavigationAdapter.createWithDefaultFragments(
            this,
            supportFragmentManager,
            binding.fragmentContainer.id,
            tabLayouts
        )
        
        // Thiết lập sự kiện click cho các tab
        binding.tab1.setOnClickListener {
            bottomNavAdapter.switchToTab(0)
            viewModel.setCurrentTabIndex(0)
        }
        
        binding.tab2.setOnClickListener {
            bottomNavAdapter.switchToTab(1)
            viewModel.setCurrentTabIndex(1)
        }
        
        binding.tab3.setOnClickListener {
            bottomNavAdapter.switchToTab(2)
            viewModel.setCurrentTabIndex(2)
        }
    }
    
    /**
     * Observe data changes từ ViewModel
     */
    private fun observeViewModel() {
        viewModel.getCurrentTabIndex().observe(this) { tabIndex ->
            // Chuyển đến tab tương ứng khi ViewModel thay đổi
            bottomNavAdapter.switchToTab(tabIndex)
        }
    }
    
    /**
     * Chọn tab mặc định sau khi mọi thứ đã được khởi tạo
     */
    private fun selectDefaultTab() {
        // Đảm bảo tab mặc định được chọn và các listener được gọi
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                bottomNavAdapter.switchToTab(bottomNavAdapter.defaultTabIndex)
            } catch (e: Exception) {
                Log.e(TAG, "Error switching to default tab: ${e.message}", e)
            }
        }, 500) // Increased delay to ensure fragments are fully initialized
    }
    
    /**
     * Cung cấp khả năng reset tất cả tutorials để test
     */
    fun resetAllTutorials() {
        Log.d(TAG, "Resetting all tutorials for testing")
        // Reset tutorials for each fragment
        tutorialManager.resetTutorialStatus("home_tutorial")
        tutorialManager.resetTutorialStatus("notification_tutorial")
        
        // Gọi lại tutorial của fragment hiện tại
        val currentFragment = bottomNavAdapter.getCurrentFragment()
        if (currentFragment is TabFragment) {
            currentFragment.onTabSelected()
        }
    }
    
    override fun onBackPressed() {
        // With our new TutorialManager implementation, just call super directly
        super.onBackPressed()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Giải phóng tài nguyên khi activity bị hủy
        flutterViewManager.dispose()
    }
}
