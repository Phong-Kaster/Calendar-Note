package com.example.skeleton

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.skeleton.core.CoreActivity
import com.example.skeleton.data.notification.AlarmNotifier
import com.example.skeleton.data.notification.GreetingNotifier
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : CoreActivity() {
    private lateinit var navController: NavController

    /**
     * The daily hello. Reached through Koin rather than built here, because it is the **one**
     * instance in the app: the `Mutex` that stops two greetings going out at once lives inside it, so
     * a second copy made here would hold a second, useless lock.
     */
    private val greetingNotifier: GreetingNotifier by inject()

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

    /**
     * "The app is on screen now."
     *
     * `onStart` is this app's foreground signal. It fires on a cold launch, when the user comes back
     * to the app from somewhere else — and also on every configuration change, because a rotation or
     * a switch in the in-app language picker recreates the activity and starts it again. That third
     * case is on purpose rather than tolerated: [GreetingNotifier.greetIfFirstForegroundToday] is
     * built to be asked on every single foreground and to answer "already done today" itself, so
     * there is nothing to decide here.
     *
     * (`ProcessLifecycleOwner` would be the tidier signal, and it is not used: it comes from
     * `androidx.lifecycle:lifecycle-process`, a dependency this app does not have.)
     *
     * The greeting is launched rather than awaited because it reads from storage, and blocking the
     * main thread on a disk read is how a launch turns into a freeze. `lifecycleScope` ties that
     * coroutine to this activity, so an app closed a moment after opening does not leave work
     * running behind it.
     */
    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            greetingNotifier.greetIfFirstForegroundToday()
        }
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