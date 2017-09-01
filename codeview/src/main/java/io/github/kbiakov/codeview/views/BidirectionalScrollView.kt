package io.github.kbiakov.codeview.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec.makeMeasureSpec
import android.widget.HorizontalScrollView
import io.github.kbiakov.codeview.dpToPx

/**
 * @class BidirectionalScrollView
 *
 * Combines vertical & horizontal scroll to implement bidirectional
 * scrolling behavior (like a map view, for example).
 *
 * @author Kirill Biakov
 */
class BidirectionalScrollView : HorizontalScrollView {

    private var currentX = 0
    private var currentY = 0
    private var isMoved = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentX = event.rawX.toInt()
                currentY = event.rawY.toInt()
                return super.dispatchTouchEvent(event)
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = Math.abs(currentX - event.rawX)
                val deltaY = Math.abs(currentY - event.rawY)
                scroll(event)

                val movedOnDistance = dpToPx(context, 2)
                if (deltaX > movedOnDistance || deltaY > movedOnDistance)
                    isMoved = true
            }
            MotionEvent.ACTION_UP -> {
                if (!isMoved)
                    return super.dispatchTouchEvent(event)
                isMoved = false
            }
            MotionEvent.ACTION_CANCEL -> isMoved = false
        }
        return true
    }

    private fun scroll(event: MotionEvent) {
        val x2 = event.rawX.toInt()
        val y2 = event.rawY.toInt()
        val posX = currentX - x2
        val posY = currentY - y2
        scrollBy(posX, posY)

        currentX = x2
        currentY = y2
    }

    override fun measureChild(child: View, parentWidthMeasureSpec: Int, parentHeightMeasureSpec: Int) {
        val measureSpecZero = makeMeasureSpec(0)
        child.measure(measureSpecZero, measureSpecZero)
    }

    override fun measureChildWithMargins(child: View,
                                         parentWidthMeasureSpec: Int, widthUsed: Int,
                                         parentHeightMeasureSpec: Int, heightUsed: Int) {
        val params = child.layoutParams as MarginLayoutParams
        val childWidthMeasureSpec = makeMeasureSpec(params.leftMargin + params.rightMargin)
        val childHeightMeasureSpec = makeMeasureSpec(params.topMargin + params.bottomMargin)
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    private fun makeMeasureSpec(size: Int) = makeMeasureSpec(size, MeasureSpec.UNSPECIFIED)
}
