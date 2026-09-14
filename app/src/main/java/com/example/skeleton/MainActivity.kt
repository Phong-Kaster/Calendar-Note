package com.example.skeleton

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.skeleton.core.CoreActivity
import com.example.skeleton.data.notification.AlarmNotifier

class MainActivity : CoreActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupComponent()

        // `savedInstanceState == null` is what tells a genuinely fresh launch apart from this same
        // instance being recreated for a config change (a rotation, or a language change through
        // AppLocalesMetadataHolderService) — `getIntent()` still answers the same notification
        // intent on every recreation, so without this guard a single tap would throw the user back
        // onto Alarms after every rotation for as long as the activity instance lives.
        if (savedInstanceState == null) openAlarmsIfRequested(intent = intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openAlarmsIfRequested(intent = intent)
    }

    private fun setupComponent() {
        // Get nav fragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    /**
     * Opens the Alarms list when this activity was (re)started by a tap on an alarm's notification.
     *
     * `AlarmNotifier`'s pending intent carries `CLEAR_TOP`/`SINGLE_TOP`, so a tap reaches this
     * activity through [onNewIntent] when it is already on top and through [onCreate] otherwise —
     * both call this the same way. Navigating through **`R.id.toAlarms`**, the same tab-swap action
     * `CoreBottomBar` uses — not the raw `alarmsFragment` destination — is what keeps this safe to
     * call from any screen: its `popUpTo="@id/homeFragment"` collapses whatever was on top (a
     * half-written alarm in the editor, Settings) down to Home before pushing Alarms, and its
     * `launchSingleTop` is what stops a second tap while Alarms is already showing from pushing a
     * duplicate of it.
     */
    private fun openAlarmsIfRequested(intent: Intent?) {
        if (intent?.action != AlarmNotifier.ACTION_OPEN_ALARMS) return

        navController.navigate(R.id.toAlarms)
    }
}