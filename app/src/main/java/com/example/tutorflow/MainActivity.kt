package com.example.tutorflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tutorflow.ui.navigation.AppNavigation
import com.example.tutorflow.ui.theme.TutorFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TutorFlowTheme {
                AppNavigation()
            }
        }
    }
}
