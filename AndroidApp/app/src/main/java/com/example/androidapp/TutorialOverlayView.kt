package com.example.androidapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.androidapp.R
import kotlin.math.max
import kotlin.math.min
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.core.animation.doOnEnd
import androidx.core.view.doOnLayout

/**
 * Custom view to display tutorial overlays with tooltips
 */
class TutorialOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val TAG = "TutorialOverlayView"
    
    // Constants
    private val SCREEN_MARGIN = 32 // Margin from screen edges
    private val TOOLTIP_MARGIN = 16 // Margin from target view
    private val highlightPadding = 8f // Padding around highlighted view
    
    // Default color values
    private val DEFAULT_DIM_COLOR = Color.parseColor("#99000000") // 60% black
    private val DEFAULT_HIGHLIGHT_RADIUS = 15f
    
    // Configurable properties
    private var dimColor: Int = DEFAULT_DIM_COLOR
    private var highlightRadius: Float = DEFAULT_HIGHLIGHT_RADIUS
    private var tooltipTextColor: Int = Color.parseColor("#333333")
    private var tooltipBackgroundColor: Int = Color.WHITE
    
    // Paint objects for drawing
    private val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = dimColor
        style = Paint.Style.FILL
    }
    
    private val transparentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    
    // Paint for dashed border
    private val dashedBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(12f, 12f), 0f)
    }
    
    // Views to highlight
    private var targetView: View? = null
    private var tooltipView: TooltipView? = null
    
    // Tooltips
    private val targetRect = Rect()
    private var onDismissListener: (() -> Unit)? = null
    private var dismissInProgress = false
    private var isDismissed = false
    
    // Position enum
    enum class Position {
        ABOVE, BELOW
    }
    
    // Biến lưu vị trí mong muốn cho tooltip
    private var lastPosition: Position = Position.BELOW
    
    init {
        try {
            Log.d(TAG, "Initializing TutorialOverlayView")
            
            // Set layer type for proper rendering of transparent hole
            setLayerType(LAYER_TYPE_HARDWARE, null)
            
            // Ensure we will draw our own content
            setWillNotDraw(false)
            
            // Set transparent background to handle touch events
            setBackgroundColor(Color.TRANSPARENT)
            
            // Set Z index to ensure overlay stays on top
            elevation = 1000f
            
            // Create tooltip view
            tooltipView = TooltipView(context)
            tooltipView?.visibility = View.INVISIBLE
            
            // Set layout params to wrap content
            tooltipView?.layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            
            // Add tooltip to overlay
            tooltipView?.let { addView(it) }
            
            // Make entire overlay clickable to capture all touches
            isClickable = true
            isFocusable = true
            
            Log.d(TAG, "TutorialOverlayView initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TutorialOverlayView: ${e.message}", e)
        }
    }
    
    /**
     * Configure the appearance of the overlay
     */
    fun configure(
        dimColor: Int = this.dimColor,
        highlightRadius: Float = this.highlightRadius,
        tooltipTextColor: Int = this.tooltipTextColor,
        tooltipBackgroundColor: Int = this.tooltipBackgroundColor
    ) {
        this.dimColor = dimColor
        this.highlightRadius = highlightRadius
        this.tooltipTextColor = tooltipTextColor
        this.tooltipBackgroundColor = tooltipBackgroundColor
        
        // Update paint objects
        dimPaint.color = dimColor
        
        // If tooltip exists, update its colors
        tooltipView?.let {
            // Remove existing tooltip
            removeView(it)
            
            // Create new tooltip with updated colors
            tooltipView = TooltipView(context)
            tooltipView?.let { newTooltip ->
                newTooltip.visibility = View.INVISIBLE
                newTooltip.layoutParams = LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                addView(newTooltip)
                
                // If we have a target view and message, show immediately
                if (targetView != null) {
                    targetView?.getGlobalVisibleRect(targetRect)
                }
            }
        }
        
        invalidate()
    }
    
    /**
     * Show a tooltip for the specified target view
     */
    fun showTooltip(
        target: View,
        text: String,
        position: Position = Position.BELOW,
        onDismiss: (() -> Unit)? = null,
        customTextColor: Int = tooltipTextColor,
        customBackgroundColor: Int = tooltipBackgroundColor
    ) {
        try {
            Log.d(TAG, "Showing tooltip with text: '$text', position: $position")
            
            // LƯU VỊ TRÍ TOOLTIP
            lastPosition = position
            
            // Remove old tooltip if exists
            tooltipView?.let { removeView(it) }
            
            // Save references
            targetView = target
            if (onDismiss != null) {
                onDismissListener = onDismiss
            }
            
            // Get target position using the same method as setTargetView
            val locationOnScreen = IntArray(2)
            target.getLocationOnScreen(locationOnScreen)
            
            // Lấy vị trí của overlay
            val overlayLocation = IntArray(2)
            getLocationOnScreen(overlayLocation)
            
            // Cập nhật lại vị trí của target
            targetRect.set(
                locationOnScreen[0] - overlayLocation[0],
                locationOnScreen[1] - overlayLocation[1],
                locationOnScreen[0] - overlayLocation[0] + target.width,
                locationOnScreen[1] - overlayLocation[1] + target.height
            )
            
            Log.d(TAG, "showTooltip - ViewOnScreen: (${locationOnScreen[0]}, ${locationOnScreen[1]}), OverlayOnScreen: (${overlayLocation[0]}, ${overlayLocation[1]})")
            Log.d(TAG, "showTooltip - Target rect: $targetRect")
            
            // Create tooltip
            tooltipView = TooltipView(context).apply {
                setText(text)
                layoutParams = LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                
                // Set custom colors if needed
                val tv = findViewById<TextView>(R.id.tooltip_text)
                tv?.setTextColor(customTextColor)
                
                val cardView = findViewById<CardView>(R.id.tooltip_card)
                cardView?.setCardBackgroundColor(customBackgroundColor)
                
                // Start invisible for animation
                alpha = 0f
                visibility = View.INVISIBLE
            }
            addView(tooltipView)
            
            // Position the tooltip after layout and start animation
            tooltipView?.post { 
                Log.d(TAG, "Positioning tooltip with position: $position")
                positionTooltip(position)
                // Animate in after positioning
                animateTooltipIn()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing tooltip: ${e.message}", e)
        }
    }
    
    /**
     * Position the tooltip relative to the target
     */
    private fun positionTooltip(position: Position) {
        val tooltip = tooltipView ?: return
        try {
            val tooltipWidth = tooltip.measuredWidth
            val tooltipHeight = tooltip.measuredHeight
            val screenWidth = width
            val screenHeight = height
            val targetCenterX = targetRect.left + targetRect.width() / 2f
            
            // Log kích thước để debug
            Log.d(TAG, "Positioning tooltip: screen=${screenWidth}x${screenHeight}, target=$targetRect, tooltip=${tooltipWidth}x${tooltipHeight}")
            
            // Horizontal position (centered with target)
            var tooltipX = (targetCenterX - tooltipWidth / 2).toInt()
            
            // Ensure tooltip is not cut off by screen edges
            // Add extra padding from screen edges (15dp instead of just SCREEN_MARGIN)
            val horizontalScreenMargin = SCREEN_MARGIN + dpToPx(15)
            tooltipX = tooltipX.coerceIn(horizontalScreenMargin, screenWidth - tooltipWidth - horizontalScreenMargin)
            
            // Increase vertical margin to create more separation
            val verticalMargin = TOOLTIP_MARGIN + dpToPx(10)
            
            // Calculate available space above and below target
            val spaceBelow = screenHeight - targetRect.bottom - verticalMargin - tooltipHeight
            val spaceAbove = targetRect.top - verticalMargin - tooltipHeight
            
            Log.d(TAG, "Space calculation: above=$spaceAbove, below=$spaceBelow")
            
            // Determine vertical position with enhanced logic
            val tooltipY = when {
                // If specified position is BELOW and there's enough space
                position == Position.BELOW && spaceBelow >= 0 -> {
                    Log.d(TAG, "Positioning BELOW target as requested")
                    (targetRect.bottom + verticalMargin).toInt()
                }
                
                // If specified position is ABOVE and there's enough space
                position == Position.ABOVE && spaceAbove >= 0 -> {
                    Log.d(TAG, "Positioning ABOVE target as requested")
                    (targetRect.top - verticalMargin - tooltipHeight).toInt()
                }
                
                // Automatic positioning based on available space
                spaceBelow >= tooltipHeight -> {
                    Log.d(TAG, "Auto positioning BELOW target")
                    (targetRect.bottom + verticalMargin).toInt()
                }
                
                spaceAbove >= tooltipHeight -> {
                    Log.d(TAG, "Auto positioning ABOVE target")
                    (targetRect.top - verticalMargin - tooltipHeight).toInt()
                }
                
                // If there's more space below than above, place below but adjust to fit
                spaceBelow > spaceAbove && spaceBelow > dpToPx(50) -> {
                    Log.d(TAG, "Positioning at bottom of screen (limited space)")
                    screenHeight - tooltipHeight - horizontalScreenMargin
                }
                
                // If there's more space above than below, place above but adjust to fit
                spaceAbove > spaceBelow && spaceAbove > dpToPx(50) -> {
                    Log.d(TAG, "Positioning at top of screen (limited space)")
                    horizontalScreenMargin
                }
                
                // Fallback: center on screen as last resort
                else -> {
                    Log.d(TAG, "CENTERING tooltip on screen (very limited space)")
                    (screenHeight - tooltipHeight) / 2
                }
            }
            
            // Set final position
            tooltip.x = tooltipX.toFloat()
            tooltip.y = tooltipY.toFloat()
            
            // Update the arrow direction in the tooltip
            val isAboveTarget = tooltipY + tooltipHeight < targetRect.top
            tooltip.setTargetPosition(
                targetCenterX,
                targetRect.top.toFloat(),
                targetRect.bottom.toFloat(),
                isAboveTarget
            )
            
            Log.d(TAG, "Tooltip final position: x=$tooltipX, y=$tooltipY")
        } catch (e: Exception) {
            Log.e(TAG, "Error in positionTooltip: ${e.message}", e)
        }
    }
    
    /**
     * Convert dp to pixels
     */
    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        try {
            // Get screen dimensions
            val displayMetrics = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            
            // Set dimensions to match screen for overlay
            setMeasuredDimension(screenWidth, screenHeight)
            
            // Measure tooltip - constrain width to 85% of screen width
            val maxWidth = (screenWidth * 0.85f).toInt()
            tooltipView?.measure(
                MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            
            Log.d(TAG, "Tooltip measured: ${tooltipView?.measuredWidth}x${tooltipView?.measuredHeight}")
            
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(screenHeight, MeasureSpec.EXACTLY)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in onMeasure: ${e.message}", e)
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
    
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        
        // Position tooltip relative to target view
        if (tooltipView != null && targetView != null) {
            // Cập nhật vị trí target nếu cần
            targetView?.let { view ->
                // Lấy vị trí của view trên màn hình
                val locationOnScreen = IntArray(2)
                view.getLocationOnScreen(locationOnScreen)
                
                // Lấy vị trí của overlay trên màn hình
                val overlayLocation = IntArray(2)
                getLocationOnScreen(overlayLocation)
                
                // Cập nhật lại vị trí của target
                targetRect.set(
                    locationOnScreen[0] - overlayLocation[0],  // X tương đối
                    locationOnScreen[1] - overlayLocation[1],  // Y tương đối
                    locationOnScreen[0] - overlayLocation[0] + view.width,
                    locationOnScreen[1] - overlayLocation[1] + view.height
                )
                
                Log.d(TAG, "onLayout update - targetRect: $targetRect")
            }
            
            // Sử dụng vị trí đã lưu
            positionTooltip(lastPosition)
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        if (targetView == null) return
        try {
            canvas.drawColor(Color.TRANSPARENT)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)
            val rectF = RectF(
                targetRect.left - highlightPadding,
                targetRect.top - highlightPadding,
                targetRect.right + highlightPadding,
                targetRect.bottom + highlightPadding
            )
            canvas.drawRoundRect(rectF, highlightRadius, highlightRadius, transparentPaint)

            // Dashline cách vùng sáng 10dp
            val dashlineOffset = dpToPx(10).toFloat()
            val dashRectF = RectF(
                rectF.left - dashlineOffset,
                rectF.top - dashlineOffset,
                rectF.right + dashlineOffset,
                rectF.bottom + dashlineOffset
            )
            canvas.drawRoundRect(dashRectF, highlightRadius + dashlineOffset, highlightRadius + dashlineOffset, dashedBorderPaint)

            super.onDraw(canvas)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDraw: ", e)
        }
    }
    
    private fun isViewAttachedToWindow(view: View): Boolean {
        return view.isAttachedToWindow
    }
    
    /**
     * Dismiss the tooltip (with animation, then callback)
     */
    fun dismissTooltip() {
        if (dismissInProgress || isDismissed) {
            Log.d(TAG, "Dismiss already in progress or tooltip already dismissed")
            return
        }
        
        dismissInProgress = true
        isDismissed = true
        
        Log.d(TAG, "Dismissing tooltip, callback will be invoked: ${onDismissListener != null}")
        
        try {
            val tooltip = tooltipView ?: run {
                Log.d(TAG, "tooltipView is null, invoking callback directly")
                // Quan trọng: Xóa target view để xóa highlight
                targetView = null
                invalidate()
                dismissInProgress = false
                onDismissListener?.invoke()
                return
            }
            
            animateTooltipOut {
                try {
                    removeView(tooltip)
                    tooltipView = null
                    // Quan trọng: Xóa target view để xóa highlight
                    targetView = null
                    invalidate()
                    
                    Log.d(TAG, "Animation completed, invoking dismiss callback")
                    
                    // Đảm bảo callback được gọi trên main thread
                    post {
                        try {
                            onDismissListener?.invoke()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in onDismissListener: ${e.message}", e)
                        }
                        dismissInProgress = false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in tooltip dismiss callback: ${e.message}", e)
                    dismissInProgress = false
                    // Quan trọng: Xóa target view để xóa highlight
                    targetView = null
                    invalidate()
                    onDismissListener?.invoke() // Try to invoke callback even if there was an error
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing tooltip: ${e.message}", e)
            if (tooltipView != null) {
                removeView(tooltipView)
                tooltipView = null
            }
            
            // Quan trọng: Xóa target view để xóa highlight
            targetView = null
            invalidate()
            
            // Đảm bảo callback luôn được gọi ngay cả khi có lỗi
            post {
                onDismissListener?.invoke()
                dismissInProgress = false
            }
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            
            // Trước tiên kiểm tra xem có bấm vào tooltip không
            val cardView = tooltipView?.findViewById<CardView>(R.id.tooltip_card)
            if (cardView != null) {
                val cardLocation = IntArray(2)
                cardView.getLocationOnScreen(cardLocation)
                val overlayLocation = IntArray(2)
                getLocationOnScreen(overlayLocation)
                val left = cardLocation[0] - overlayLocation[0]
                val top = cardLocation[1] - overlayLocation[1]
                val right = left + cardView.width
                val bottom = top + cardView.height
                
                if (x in left.toFloat()..right.toFloat() && y in top.toFloat()..bottom.toFloat()) {
                    // Bấm vào tooltip, không làm gì cả
                    Log.d(TAG, "Touch inside tooltip card, ignore dismiss")
                    return true
                }
            }
            
            // Bấm ra ngoài tooltip - luôn dismiss và gọi callback
            Log.d(TAG, "Touch outside tooltip, dismissing")
            dismissTooltip()
            return true
        }
        return super.onTouchEvent(event)
    }
    
    // Clean up resources
    fun cleanup() {
        Log.d(TAG, "Cleaning up resources")
        
        // Hủy animation nếu đang chạy
        tooltipView?.animate()?.cancel()
        
        // Xóa tất cả views
        removeAllViews()
        
        // Xóa references
        targetView = null
        tooltipView = null
        onDismissListener = null
        
        // Reset state
        dismissInProgress = false
        isDismissed = false
        
        // Cập nhật lại giao diện để xóa highlights
        invalidate()
        
        Log.d(TAG, "Cleanup completed")
    }

    /**
     * Set the target view to highlight
     */
    fun setTargetView(view: View, message: String) {
        try {
            if (view.width <= 0 || view.height <= 0) {
                Log.e(TAG, "Target view has zero size: ${view.width}x${view.height}")
                return
            }
            
            targetView = view
            
            // Lấy vị trí của view trên màn hình
            val locationOnScreen = IntArray(2)
            view.getLocationOnScreen(locationOnScreen)
            
            // Lấy vị trí của overlay trên màn hình
            val overlayLocation = IntArray(2)
            getLocationOnScreen(overlayLocation)
            
            // Tính toán vị trí tương đối với overlay
            targetRect.set(
                locationOnScreen[0] - overlayLocation[0],  // X tương đối
                locationOnScreen[1] - overlayLocation[1],  // Y tương đối
                locationOnScreen[0] - overlayLocation[0] + view.width,
                locationOnScreen[1] - overlayLocation[1] + view.height
            )
            
            Log.d(TAG, "ViewOnScreen: (${locationOnScreen[0]}, ${locationOnScreen[1]}), OverlayOnScreen: (${overlayLocation[0]}, ${overlayLocation[1]})")
            Log.d(TAG, "Updated target rect relative to overlay: $targetRect")
            
            // Prepare tooltip
            showTooltip(view, message)
            
            // Refresh drawing
            invalidate()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting target view: ${e.message}", e)
        }
    }

    /**
     * Set the on dismiss listener
     */
    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    /**
     * Show animation for the tooltip
     */
    private fun animateTooltipIn() {
        tooltipView?.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(300)
                .withEndAction {
                    Log.d(TAG, "Tooltip animation completed")
                }
                .start()
        }
    }

    /**
     * Hide animation for the tooltip
     */
    private fun animateTooltipOut(onComplete: () -> Unit) {
        tooltipView?.apply {
            animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    visibility = View.INVISIBLE
                    onComplete()
                }
                .start()
        } ?: onComplete()
    }
} 