package com.deference.koasty.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.deference.koasty.KoastManager
import com.deference.koasty.KoastyProvider
import com.deference.koasty.app.theme.KoastyDemoTheme

@Composable
@Preview
fun App() {
    KoastyDemoTheme {
        val koastManager = remember {
            KoastManager()
        }
        KoastyProvider(
            koastManager
        ){
            Scaffold(
                modifier = Modifier
                    .safeContentPadding()
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(onClick = {
                            koastManager.show(
                                "Test",
                                "Testing Koasty"
                            )
                        }) {
                            Text("Click me!")
                        }
                    }
                }
            }
        }
    }
}