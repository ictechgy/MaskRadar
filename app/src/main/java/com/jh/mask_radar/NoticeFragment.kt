package com.jh.mask_radar

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlin.system.exitProcess

class NoticeFragment : Fragment() {
    private val hideHandler = Handler()     //Runnable 실행 핸들러

    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {  //UI가리기 Runnable
        val flags =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity?.window?.decorView?.systemUiVisibility = flags
        (activity as? AppCompatActivity)?.supportActionBar?.hide()  //actionBar도 가리기
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        exitButtonBar?.visibility = View.VISIBLE
    }
    private var visible: Boolean = false    //UI가 보이는지의 여부 (Nav 등)
    private val hideRunnable = Runnable { hide() }  //hide()실행 Runnable

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */

    private var confirmButton : TextView? = null
    private var exitButton: Button? = null
    private var fullscreenContent: View? = null
    private var exitButtonBar: View? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true  //뷰 최초 생성완료시에는 UI가 보이는 상태

        confirmButton = view.findViewById(R.id.confirm_button)
        exitButton = view.findViewById(R.id.exit_button)
        fullscreenContent = view.findViewById(R.id.fullscreen_content)
        exitButtonBar = view.findViewById(R.id.exit_button_bar)
        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent?.setOnClickListener { toggle() }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        confirmButton?.setOnClickListener {
            show()
            val pref : SharedPreferences = view.context.getSharedPreferences(getString(R.string.preference_map_fragment), Context.MODE_PRIVATE)
            val edit = pref.edit()
            edit.putBoolean("needNotice", false)
            edit.apply()    //더이상 이 프래그먼트는 보지 않아도 됨
            val navController : NavController = Navigation.findNavController(view)
            navController.popBackStack()
        }
        exitButton?.setOnClickListener {
            //앱 종료
            activity?.finishAffinity()
            System.runFinalization()
            exitProcess(0)
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)        //프래그먼트 생성 뒤 잠깐의 시간 뒤에 UI 가리기
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Clear the systemUiVisibility flag
        activity?.window?.decorView?.systemUiVisibility = 0
        show()  //프래그먼트가 onPause()상태 들어오면 UI 보이기
    }

    override fun onDestroy() {
        super.onDestroy()
        confirmButton = null
        exitButton = null
        fullscreenContent = null
        exitButtonBar = null
    }

    private fun toggle() {
        if (visible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        exitButtonBar?.visibility = View.GONE
        visible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())     //약간의 시간 뒤에 UI 숨기기
   }

    @Suppress("InlinedApi")
    private fun show() {
        // Show the system bar
        fullscreenContent?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        visible = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
        //(activity as? AppCompatActivity)?.supportActionBar?.show() //ActionBar는 계속 안보이게.
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}