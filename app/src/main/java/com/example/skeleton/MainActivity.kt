package com.example.skeleton

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.skeleton.core.CoreActivity

/**
 * The only activity of the app. It hosts the navigation graph (one Fragment per screen).
 *
 * When it is opened from the media notification, the intent carries [EXTRA_OPEN_MUSIC] and the
 * activity jumps to the Music tab. The extra is removed after use so it only works once.
 *
 * @author Phong-Kaster
 */
class MainActivity : CoreActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupComponent()
        // Only a fresh start reads the extra; after a rotation the same old intent comes back.
        if (savedInstanceState == null) handleOpenMusicIntent(intent = intent)
    }

    /**
     * Called when the running activity is reused (for example, the user taps the notification
     * while the app is already open).
     *
     * @author Phong-Kaster
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOpenMusicIntent(intent = intent)
    }

    private fun setupComponent() {
        // Get nav fragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    /**
     * If [intent] asks to open the Music tab, removes that request and goes to Music
     * (nothing happens when Music is already showing).
     *
     * @author Phong-Kaster
     */
    private fun handleOpenMusicIntent(intent: Intent?) {
        if (intent == null) return
        if (!intent.getBooleanExtra(EXTRA_OPEN_MUSIC, false)) return
        // Opening the app again from Recents replays the old intent; that is not a notification tap.
        if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) return
        intent.removeExtra(EXTRA_OPEN_MUSIC)
        navigateToMusic()
    }

    /**
     * Goes to the Music tab with the global action `toMusic`, unless Music is already on screen.
     *
     * @author Phong-Kaster
     */
    private fun navigateToMusic() {
        if (navController.currentDestination?.id == R.id.musicFragment) return
        try {
            navController.navigate(R.id.toMusic)
        } catch (e: Exception) {
            Log.e(TAG, "navigateToMusic() failed", e)
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        /** Intent extra (Boolean): true means "show the Music tab when you open". */
        const val EXTRA_OPEN_MUSIC = "extra_open_music"
    }
}
