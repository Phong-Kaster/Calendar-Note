package com.example.skeleton.ui.util

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController

/**
 * Small helpers that make navigating between fragments safe: if the navigation fails
 * (for example the user taps twice very fast), we print the error instead of crashing the app.
 *
 * Example:
 * ```
 * safeNavigate(destination = R.id.nowPlayingFragment)
 * safeNavigateUp()
 * ```
 * @author Phong-Kaster
 */
object NavigationUtil {

    /**
     * Go to another screen by its id. Any error is printed, not thrown.
     * @param destination is the id of the screen that we go to
     * @param bundle is the optional data we send to that screen
     * @param navOptions is the optional animation / back stack rules
     * @author Phong-Kaster
     */
    fun Fragment.safeNavigate(destination: Int, bundle: Bundle? = null, navOptions: NavOptions? = null) {
        try {
            findNavController().navigate(destination, bundle, navOptions, null)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * Go back to the previous screen. Any error is printed, not thrown.
     * @author Phong-Kaster
     */
    fun Fragment.safeNavigateUp() {
        try {
            findNavController().navigateUp()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
