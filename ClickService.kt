package com.fazenda50.screen
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
class ClickService : AccessibilityService() {
    companion object { var instance: ClickService? = null }
    override fun onServiceConnected() { instance=this; super.onServiceConnected() }
    override fun onAccessibilityEvent(e: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    fun click(xRatio: Float, yRatio: Float){
        val x = resources.displayMetrics.widthPixels * xRatio
        val y = resources.displayMetrics.heightPixels * yRatio
        val path = Path().apply { moveTo(x,y) }
        val b = GestureDescription.Builder(); b.addStroke(GestureDescription.StrokeDescription(path,0,100))
        dispatchGesture(b.build(),null,null)
    }
}
