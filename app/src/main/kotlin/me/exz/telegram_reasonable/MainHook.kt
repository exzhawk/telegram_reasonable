package me.exz.telegram_reasonable

import android.util.Log
import android.view.View
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class MainHook : IXposedHookLoadPackage {
    private val TAG = "telegram_reasonable"

    private val packageName = "org.telegram.messenger"

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (packageName == lpparam.packageName) {
            hookUnmute(lpparam)
            hookSwipe(lpparam)
            hookDoubleTap(lpparam)
            hookPullNext(lpparam)
        }
    }

    /**
     * no chat list swipe gesture
     *
     * effect: chat list swipe gesture will show action icon but do nothing.
     *
     * you may select chat list swipe gesture for normal chat,
     * but swipe on archived chat always unarchive it and there is no easy undo.
     */
    private fun hookSwipe(lpparam: LoadPackageParam) {
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
                    Log.i(TAG, "no chat list swipe gesture")
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
                    Log.i(TAG, "no chat list swipe gesture")
                    return Float.MAX_VALUE
                }
            }
        )
    }

    /**
     * no unmute button in channel
     *
     * effect: make unmute button unusable.
     *
     * that big unmute button is easy to tap accidentally,
     * and re-mute is inconvenient.
     */
    private fun hookUnmute(lpparam: LoadPackageParam) {
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
                        Log.i(TAG, "no unmute button in channel")
                        bottomOverlayChatText.visibility = View.INVISIBLE
                    }
                }
            }
        })
    }

    /**
     * no double tap quick reaction
     *
     * effect: make double tap do nothing. (but still considered as a single tap)
     *
     * double tap quick reaction is easy to trigger accidentally,
     * and send unnecessary notification to other user
     */

    private fun hookDoubleTap(lpparam: LoadPackageParam) {
        val hookClass = lpparam.classLoader.loadClass("org.telegram.ui.ChatActivity$12") ?: return
        Log.i(TAG, "found chat activity")
        XposedHelpers.findAndHookMethod(
            hookClass,
            "hasDoubleTap",
            "android.view.View",
            Int::class.java,
            object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Boolean {
                    Log.i(TAG, "no double tap quick reaction")
                    return false
                }
            })
    }

    /**
     * no pull up to go to the next channel
     */
    private fun hookPullNext(lpparam: LoadPackageParam) {
        val hookClass =
            lpparam.classLoader.loadClass("org.telegram.ui.ChatPullingDownDrawable") ?: return
        Log.i(TAG, "found ChatPullingDownDrawable activity")
        XposedHelpers.findAndHookMethod(
            hookClass,
            "getNextUnreadDialog",
            Long::class.java,
            Int::class.java,
            Int::class.java,
            Boolean::class.java,
            IntArray::class.java,
            object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Any? {
                    Log.i(TAG, "no pull next")
                    return null
                }
            })
    }

}