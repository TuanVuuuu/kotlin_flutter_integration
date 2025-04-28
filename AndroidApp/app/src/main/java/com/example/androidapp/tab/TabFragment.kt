package com.example.androidapp.tab

/**
 * Interface for fragments that can respond to tab selection events
 */
interface TabFragment {
    /**
     * Called when the tab containing this fragment is selected
     */
    fun onTabSelected()
    
    /**
     * Called when the tab containing this fragment is unselected
     */
    fun onTabUnselected()
} 