package me.exz.telegram_reasonable

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Field

class MainHook : IXposedHookLoadPackage {
    private val TAG = "telegram_reasonable"

    private val packageName = "org.telegram.messenger"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (packageName == lpparam.packageName) {
            hookUnmute(lpparam)
            hookSwipe(lpparam)
            hookDoubleTap(lpparam)
        }
    }

    private fun hookSwipe(lpparam: XC_LoadPackage.LoadPackageParam) {
        val hookClass =
            lpparam.classLoader.loadClass("org.telegram.ui.DialogsActivity\$SwipeController")
                ?: return
        Log.i(TAG, "found swipe controller")
        XposedHelpers.findAndHookMethod(
            hookClass,
            "getSwipeThreshold",
            "androidx.recyclerview.widget.RecyclerView\$ViewHolder",
            object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Float {
                    Log.i(TAG, "no swipe")
                    return 1.01f
                }
            }
        )
        XposedHelpers.findAndHookMethod(
            hookClass,
            "getSwipeEscapeVelocity",
            Float::class.java,
            object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Float {
                    Log.i(TAG, "no swipe")
                    return Float.MAX_VALUE
                }
            }
        )
    }

    private fun hookUnmute(lpparam: XC_LoadPackage.LoadPackageParam) {
        val hookClass = lpparam.classLoader.loadClass("org.telegram.ui.ChatActivity") ?: return
        Log.i(TAG, "found chat activity")
        XposedHelpers.findAndHookMethod(hookClass, "updateBottomOverlay", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                Log.i(TAG, "after hooked method")
                val bottomOverlayChatTextField =
                    XposedHelpers.findField(param.thisObject.javaClass, "bottomOverlayChatText")
                val bottomOverlayChatText: View? =
                    bottomOverlayChatTextField.get(param.thisObject) as? View
                if (bottomOverlayChatText != null) {
                    if (bottomOverlayChatText.contentDescription == "UNMUTE") {
                        Log.i(TAG, "no unmute")
                        bottomOverlayChatText.visibility = View.INVISIBLE
                    }
                }
            }
        })
    }

    private fun hookDoubleTap(lpparam: XC_LoadPackage.LoadPackageParam) {
        val hookClass = lpparam.classLoader.loadClass("org.telegram.ui.ChatActivity$10") ?: return
        Log.i(TAG, "found chat activity")
        XposedHelpers.findAndHookMethod(
            hookClass,
            "hasDoubleTap",
            "android.view.View",
            Int::class.java,
            object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Boolean {
                    Log.i(TAG, "no double tap")
                    return false;
                }
            })
    }

}