package com.ladablocker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.ladablocker.ui.AppViewModel
import com.ladablocker.ui.LadaBlockerApp
import com.ladablocker.ui.theme.LadaBlockerTheme

class MainActivity : ComponentActivity() {

    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LadaBlockerTheme {
                LadaBlockerApp(vm)
            }
        }
    }
}