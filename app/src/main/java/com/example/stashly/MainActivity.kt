package com.example.stashly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.stashly.ui.AuthViewModel
import com.example.stashly.ui.StashViewModel
import com.example.stashly.ui.navigation.AppNavigation
import com.example.stashly.ui.theme.StashlyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val stashViewModel = ViewModelProvider(this, StashViewModel.Factory(application))[StashViewModel::class.java]
        val authViewModel = ViewModelProvider(this, AuthViewModel.Factory(application))[AuthViewModel::class.java]

        setContent {
            StashlyTheme {
                AppNavigation(
                    stashViewModel = stashViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
