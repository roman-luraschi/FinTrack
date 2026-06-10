package com.fintrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.fintrack.app.feature.mercadopago.MercadoPagoOAuthDeepLinkDispatcher
import com.fintrack.app.feature.security.presentation.AppLockGate
import com.fintrack.app.feature.security.presentation.AppLockViewModel
import com.fintrack.core.designsystem.theme.FinTrackTheme
import com.fintrack.navigation.FinTrackNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var mercadoPagoOAuthDeepLinkDispatcher: MercadoPagoOAuthDeepLinkDispatcher

    private val appLockViewModel: AppLockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleOAuthDeepLink(intent)
        enableEdgeToEdge()
        setContent {
            FinTrackTheme {
                AppLockGate(
                    viewModel = appLockViewModel,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    FinTrackNavHost(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOAuthDeepLink(intent)
    }

    private fun handleOAuthDeepLink(intent: Intent?) {
        mercadoPagoOAuthDeepLinkDispatcher.dispatch(intent?.data)
    }

    override fun onResume() {
        super.onResume()
        appLockViewModel.onAppResumed()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        appLockViewModel.lockWhenBackgrounded()
    }
}
