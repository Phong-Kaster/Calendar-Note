package com.example.skeleton.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.Density
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.util.NetworkUtil
import kotlinx.coroutines.launch
import java.util.Locale


val LocalLocale = staticCompositionLocalOf { Locale.getDefault() }
val LocalNavController = staticCompositionLocalOf<NavController?> { null }
//val LocalTheme = compositionLocalOf { DarkCustomizedTheme }

open class CoreFragment : Fragment() {

//    @Inject
//    lateinit var settingRepository: SettingRepositoryImpl
    protected open val TAG: String = this.javaClass.simpleName

    /** Dark Mode*/
    private var enableDarkMode by mutableStateOf(true)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupDarkMode()
        return ComposeView(requireActivity()).apply {
            setViewCompositionStrategy(strategy = ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CompositionLocalProvider(
                    LocalNavController provides findNavController(),
                    LocalLocale provides requireActivity().resources.configuration.locales[0],
                    LocalDensity provides Density(LocalDensity.current.density, 1f),
//                    LocalTheme provides if (enableDarkMode) DarkCustomizedTheme else LightCustomizedTheme,
                    *compositionLocalProvider().toTypedArray()
                ) {
                    MyApplicationTheme() {
                        ComposeView()
                    }
                }
            }
        }
    }

    @Composable
    protected open fun compositionLocalProvider(): List<ProvidedValue<*>> {
        return listOf()
    }

    @Composable
    open fun ComposeView() {
    }

     fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

     fun isInternetConnected(): Boolean {
        return NetworkUtil.isInternetConnected(context = requireContext())
    }

     fun trackEvent(name: String) {}

    private fun setupDarkMode() {
        lifecycleScope.launch {
//            settingRepository.enableDarkModeFlow().collect {
//                enableDarkMode = it
//            }
        }
    }
}