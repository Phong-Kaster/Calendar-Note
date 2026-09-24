package com.example.skeleton

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.skeleton.core.CoreActivity

/**
 * The one and only activity of the app. It hosts the navigation graph, and every screen is a
 * fragment inside it.
 *
 * It also listens for the "open Music" ticket from the media notification: when the intent
 * carries [EXTRA_OPEN_MUSIC] = true, it jumps to the Music screen.
 *
 * Example: `Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_OPEN_MUSIC, true)`.
 * @author Phong-Kaster
 */
class MainActivity : CoreActivity() {
    private lateinit var navController: NavController

    /**
     * Shows the layout, finds the nav controller, and on a fresh start (not a rotation /
     * process restore) handles the "open Music" ticket.
     * @param savedInstanceState Saved state, or null on a fresh start.
     * @author Phong-Kaster
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupComponent()
        if (savedInstanceState != null) return
        handleOpenMusicIntent(intent = intent)
    }

    /**
     * Called when the app is already open and the notification is tapped (the notification uses
     * SINGLE_TOP + CLEAR_TOP, so this same activity is reused instead of a second copy).
     * @param intent The new intent that reached this activity.
     * @author Phong-Kaster
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOpenMusicIntent(intent = intent)
    }

    /**
     * Finds the [NavHostFragment] in the layout and keeps its [NavController].
     * @author Phong-Kaster
     */
    private fun setupComponent() {
        // Get nav fragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    /**
     * If the intent asks for Music, go to the Music screen.
     *
     * The global action `toMusic` pops everything back to the graph root and uses
     * launchSingleTop, so Music ends up the only screen and one Back leaves the app.
     * @param intent The intent to check, e.g. the one built by the media notification.
     * @author Phong-Kaster
     */
    private fun handleOpenMusicIntent(intent: Intent?) {
        val shouldOpenMusic = intent?.getBooleanExtra(EXTRA_OPEN_MUSIC, false) ?: false
        if (!shouldOpenMusic) return
        // Music is already on screen (e.g. a cold start, Music is the start screen): nothing to do.
        if (navController.currentDestination?.id == R.id.musicFragment) return
        navController.navigate(R.id.toMusic)
    }

    companion object {
        /**
         * Boolean intent extra: true means "open the app on the Music screen".
         * @author Phong-Kaster
         */
        const val EXTRA_OPEN_MUSIC = "extra_open_music"
    }
}
