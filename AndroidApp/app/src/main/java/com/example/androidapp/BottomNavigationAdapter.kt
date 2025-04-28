package com.example.androidapp

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.androidapp.tab.TabFragment

/**
 * Adapter để quản lý các tab trong Bottom Navigation và các Fragment tương ứng
 */
class BottomNavigationAdapter(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val containerResId: Int,
    private val tabLayouts: List<LinearLayout>,
    private val onTabSelected: ((position: Int) -> Unit)? = null
) {

    private val fragments = mutableListOf<Fragment>()
    private var currentTabIndex = -1 // Start with -1 to ensure first tab selection works
    private var isInitialized = false
    
    /**
     * Builder cho BottomNavigationAdapter
     */
    class Builder(
        private val context: Context,
        private val fragmentManager: FragmentManager,
        private val containerResId: Int
    ) {
        private val tabLayouts = mutableListOf<LinearLayout>()
        private val fragments = mutableListOf<Fragment>()
        private var onTabSelected: ((position: Int) -> Unit)? = null
        private var defaultTabIndex: Int = 0
        
        /**
         * Thêm tab vào adapter
         */
        fun addTab(tabLayout: LinearLayout, fragment: Fragment): Builder {
            tabLayouts.add(tabLayout)
            fragments.add(fragment)
            return this
        }
        
        /**
         * Thiết lập callback khi tab được chọn
         */
        fun setOnTabSelectedListener(listener: (position: Int) -> Unit): Builder {
            onTabSelected = listener
            return this
        }
        
        /**
         * Thiết lập tab mặc định
         */
        fun setDefaultTab(index: Int): Builder {
            defaultTabIndex = index
            return this
        }
        
        /**
         * Xây dựng và trả về BottomNavigationAdapter đã khởi tạo
         */
        fun build(): BottomNavigationAdapter {
            val adapter = BottomNavigationAdapter(
                context,
                fragmentManager,
                containerResId,
                tabLayouts,
                onTabSelected
            )
            
            // Thêm các fragments đã đăng ký
            fragments.forEach { fragment ->
                adapter.addFragment(fragment)
            }
            
            // Khởi tạo adapter
            adapter.initialize()
            
            // ĐÃ THAY ĐỔI: Lưu tab mặc định nhưng không chuyển tab ngay
            // Cho phép MainActivity quyết định khi nào là thời điểm thích hợp để chuyển tab
            adapter.defaultTabIndex = defaultTabIndex
            
            return adapter
        }
    }

    /**
     * Thêm một fragment vào adapter
     */
    fun addFragment(fragment: Fragment) {
        fragments.add(fragment)
    }

    /**
     * Khởi tạo adapter với các fragments đã được thêm
     */
    fun initialize() {
        if (isInitialized) return
        
        // Thiết lập sự kiện click cho các tab
        tabLayouts.forEachIndexed { index, tab ->
            tab.setOnClickListener {
                if (index != currentTabIndex) {
                    switchToTab(index)
                    onTabSelected?.invoke(index)
                }
            }
            
            // Reset background to ensure proper initial state
            tab.setBackgroundResource(android.R.drawable.list_selector_background)
        }
        
        // Kiểm tra nếu không có fragment nào thì không thực hiện transaction
        if (fragments.isEmpty()) {
            isInitialized = true
            return
        }
        
        // Thêm tất cả Fragment vào container và ẩn chúng
        val transaction = fragmentManager.beginTransaction()
        fragments.forEach { fragment ->
            transaction.add(containerResId, fragment).hide(fragment)
        }
        transaction.commit()
        
        isInitialized = true
    }

    /**
     * Chuyển đến tab được chọn
     */
    fun switchToTab(index: Int) {
        if (!isInitialized || index >= fragments.size || fragments.isEmpty()) return
        
        // Skip if already on this tab (but allow first selection when currentTabIndex is -1)
        if (index == currentTabIndex && currentTabIndex != -1) return
        
        Log.d("BottomNavigationAdapter", "Switching to tab $index")
        
        // Hiển thị fragment được chọn và ẩn các fragment khác
        val transaction = fragmentManager.beginTransaction()
        
        if (currentTabIndex != -1 && currentTabIndex < fragments.size) {
            transaction.hide(fragments[currentTabIndex])
        }
        
        transaction.show(fragments[index])
        transaction.commit()
        
        // Cập nhật UI của bottom navigation
        updateTabIndicator(index)
        
        // Lưu lại tab hiện tại
        currentTabIndex = index
        
        // Gọi callback nếu có
        onTabSelected?.invoke(index)
        
        // Kích hoạt hàm onTabSelected trong fragment nếu có
        val fragment = fragments[index]
        if (fragment is TabFragment) {
            Log.d("BottomNavigationAdapter", "Calling onTabSelected for fragment ${fragment.javaClass.simpleName}")
            try {
                // Use Handler to defer onTabSelected call to ensure the fragment is fully ready
                // Tăng delay từ 0 lên 300ms để đảm bảo fragment đã hoàn toàn gắn kết và có context
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        if (fragment.isAdded && fragment.context != null) {
                            fragment.onTabSelected()
                        } else {
                            Log.e("BottomNavigationAdapter", "Fragment is not ready for onTabSelected")
                        }
                    } catch (e: Exception) {
                        Log.e("BottomNavigationAdapter", "Error in onTabSelected for ${fragment.javaClass.simpleName}: ${e.message}", e)
                    }
                }, 300)
            } catch (e: Exception) {
                Log.e("BottomNavigationAdapter", "Failed to post onTabSelected for ${fragment.javaClass.simpleName}: ${e.message}", e)
            }
        } else {
            Log.d("BottomNavigationAdapter", "Fragment ${fragment.javaClass.simpleName} does not implement TabFragment")
        }
    }
    
    /**
     * Cập nhật giao diện tab khi chuyển tab
     */
    private fun updateTabIndicator(selectedIndex: Int) {
        // Đảm bảo selectedIndex hợp lệ
        if (selectedIndex < 0 || selectedIndex >= tabLayouts.size) return
        
        // Reset màu của tất cả tab
        tabLayouts.forEach { tab ->
            tab.setBackgroundResource(android.R.drawable.list_selector_background)
        }
        
        // Đổi màu tab được chọn
        tabLayouts[selectedIndex].setBackgroundColor(
            context.resources.getColor(android.R.color.holo_blue_light, null)
        )
    }
    
    /**
     * Trả về Fragment hiện tại đang hiển thị
     */
    fun getCurrentFragment(): Fragment? {
        return if (fragments.isNotEmpty() && currentTabIndex >= 0 && currentTabIndex < fragments.size) {
            fragments[currentTabIndex]
        } else null
    }
    
    // Thêm biến để lưu tab mặc định
    var defaultTabIndex: Int = 0
        private set
    
    companion object {
        /**
         * Tạo adapter với các fragment mặc định cho ứng dụng
         */
        fun createWithDefaultFragments(
            context: Context, 
            fragmentManager: FragmentManager,
            containerResId: Int,
            tabLayouts: List<LinearLayout>
        ): BottomNavigationAdapter {
            val builder = Builder(context, fragmentManager, containerResId)
            
            // Thêm các tab và fragment tương ứng
            builder.addTab(tabLayouts[0], HomeFragment())
                  .addTab(tabLayouts[1], FlutterFragment())
                  .addTab(tabLayouts[2], NotificationFragment())
                  .setDefaultTab(0) // Tab mặc định là tab đầu tiên
            
            return builder.build()
        }
    }
} 