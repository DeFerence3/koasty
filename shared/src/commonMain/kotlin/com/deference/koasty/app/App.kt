package com.deference.koasty.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        var style by remember { mutableStateOf(SampleStyle.Default) }
        KoastyProvider(
            koastManager,
            config = sampleConfig(style).copy(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                compact = true,
                respectSafeArea = false
            ),
            koastContent = if (style == SampleStyle.CustomLayout) {
                { koast -> SampleLayout(koast) }
            } else null,
            stackHandle = if (style == SampleStyle.StackedCustomHandle) {
                { state -> SampleStackHandle(state) }
            } else null,
        ){
			val samples = remember { getSamples(koastManager) { style = it } }
            Scaffold(
                modifier = Modifier
                    .safeContentPadding()
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                ) {
                    Section("Display Mode")
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LazyVerticalGrid(
                            modifier = Modifier
                                .fillMaxWidth(0.6f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            columns = GridCells.Adaptive(minSize = 128.dp),
                        ) {
                            items(samples) {
                                Button(onClick = { it.second() }) {
                                    Text(it.first)
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun Section(
    title: String
){
    Text(title)
}