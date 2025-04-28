package com.example.androidapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.androidapp.R
import kotlin.math.max
import kotlin.math.min

/**
 * Custom tooltip view with an arrow that points to a target
 */
class TooltipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val TAG = "TooltipView"
    
    // Views will be initialized in init block
    private var cardView: CardView? = null
    private var textView: TextView? = null
    
    // Arrow properties
    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE  // Same as card background
        style = Paint.Style.FILL
        setShadowLayer(4f, 0f, 2f, Color.parseColor("#33000000"))
    }
    
    private val arrowPath = Path()
    private val ARROW_HEIGHT = 20f
    private val ARROW_WIDTH = 40f
    
    // Position of the target (to know where to point the arrow)
    private var targetCenterX = 0f
    private var targetTop = 0f
    private var targetBottom = 0f
    
    // Card margin to make room for the arrow
    private val cardMargin = ARROW_HEIGHT.toInt() + 4
    
    // Additional properties
    private var isAboveTarget = false
    
    init {
        try {
            Log.d(TAG, "Initializing TooltipView")
            
            // Set layout params to wrap content
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            
            // Inflate the card layout
            val view = LayoutInflater.from(context).inflate(R.layout.tooltip_layout, this, true)
            
            // Get references to the views
            cardView = findViewById(R.id.tooltip_card)
            textView = findViewById(R.id.tooltip_text)
            
            if (cardView == null) {
                Log.e(TAG, "ERROR: CardView not found with ID tooltip_card")
            }
            
            if (textView == null) {
                Log.e(TAG, "ERROR: TextView not found with ID tooltip_text")
            }
            
            // Set padding to make room for the arrow
            setPadding(0, cardMargin, 0, cardMargin)
            
            // Make the background transparent (only the card and arrow will be visible)
            setBackgroundColor(Color.TRANSPARENT)
            
            // Enable custom drawing
            setWillNotDraw(false)
            
            Log.d(TAG, "TooltipView initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TooltipView: ${e.message}", e)
        }
    }
    
    /**
     * Set the target position to point the arrow at
     */
    fun setTargetPosition(centerX: Float, top: Float, bottom: Float, isAboveTarget: Boolean = false) {
        Log.d(TAG, "Setting target position: centerX=$centerX, top=$top, bottom=$bottom, isAboveTarget=$isAboveTarget")
        targetCenterX = centerX
        targetTop = top
        targetBottom = bottom
        
        // Save whether tooltip is above target (for arrow direction)
        this.isAboveTarget = isAboveTarget
        
        invalidate()
    }
    
    /**
     * Set the tooltip text
     */
    fun setText(text: String) {
        Log.d(TAG, "Setting tooltip text: $text")
        if (textView == null) {
            Log.e(TAG, "ERROR: Cannot set text, TextView is null")
            return
        }
        textView?.text = text
        
        // Request layout to update size
        post {
            requestLayout()
        }
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        
        try {
            val width = measuredWidth
            val height = measuredHeight
            
            Log.d(TAG, "onMeasure: width=$width, height=$height")
            
            // Make sure tooltip has proper size to account for arrow
            setMeasuredDimension(width, height + cardMargin * 2)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onMeasure: ${e.message}", e)
        }
    }
    
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d(TAG, "onLayout: changed=$changed, bounds=[$left,$top,$right,$bottom]")
        
        // Make sure cardView is properly positioned
        if (cardView != null) {
            val card = cardView!!
            card.layout(
                card.left,
                cardMargin,
                card.right,
                bottom - top - cardMargin
            )
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        try {
            if (cardView == null) {
                Log.e(TAG, "ERROR: Cannot draw, CardView is null")
                return
            }
            
            // Get the position of the card view
            val cardRect = RectF()
            cardRect.left = cardView!!.left.toFloat()
            cardRect.top = cardView!!.top.toFloat()
            cardRect.right = cardView!!.right.toFloat()
            cardRect.bottom = cardView!!.bottom.toFloat()
            
            Log.d(TAG, "Card position: $cardRect")
            Log.d(TAG, "Target: centerX=$targetCenterX, top=$targetTop, bottom=$targetBottom")
            
            // Calculate arrow position
            drawArrow(canvas, cardRect)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDraw: ${e.message}", e)
        }
    }
    
    private fun drawArrow(canvas: Canvas, cardRect: RectF) {
        try {
            // Calculate the arrow base x position (centered with target)
            val selfLocation = IntArray(2)
            getLocationOnScreen(selfLocation)
            
            // Adjust target center X to be relative to tooltip position
            val adjustedTargetCenterX = max(
                cardRect.left + ARROW_WIDTH / 2 + 10,
                min(cardRect.right - ARROW_WIDTH / 2 - 10, 
                    targetCenterX - selfLocation[0])
            )
            
            Log.d(TAG, "Drawing arrow at adjusted x=$adjustedTargetCenterX, isAboveTarget=$isAboveTarget")
            
            arrowPath.reset()
            
            // If tooltip is above the target, arrow points down
            if (isAboveTarget) {
                // Position arrow at the bottom of the card
                arrowPath.moveTo(adjustedTargetCenterX, cardRect.bottom + ARROW_HEIGHT)
                arrowPath.lineTo(adjustedTargetCenterX - ARROW_WIDTH / 2, cardRect.bottom)
                arrowPath.lineTo(adjustedTargetCenterX + ARROW_WIDTH / 2, cardRect.bottom)
                arrowPath.close()
                
                Log.d(TAG, "Drawing down arrow at x=$adjustedTargetCenterX, bottom=${cardRect.bottom}")
            } 
            // Otherwise arrow points up
            else {
                // Position arrow at the top of the card
                arrowPath.moveTo(adjustedTargetCenterX, cardRect.top - ARROW_HEIGHT)
                arrowPath.lineTo(adjustedTargetCenterX - ARROW_WIDTH / 2, cardRect.top)
                arrowPath.lineTo(adjustedTargetCenterX + ARROW_WIDTH / 2, cardRect.top)
                arrowPath.close()
                
                Log.d(TAG, "Drawing up arrow at x=$adjustedTargetCenterX, top=${cardRect.top}")
            }
            
            // Draw the arrow
            canvas.drawPath(arrowPath, arrowPaint)
        } catch (e: Exception) {
            Log.e(TAG, "Error drawing arrow: ${e.message}", e)
        }
    }
    
    // Helper function to constrain value between min and max
    private fun max(a: Float, b: Float): Float = if (a > b) a else b
    private fun min(a: Float, b: Float): Float = if (a < b) a else b
} 