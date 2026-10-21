package com.deference.koasty.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
            config = sampleConfig(style),
            leadingContent = if (style == SampleStyle.Leading) {
                { koast -> SampleLeading(koast) }
            } else null,
            koastContent = if (style == SampleStyle.CustomLayout) {
                { koast -> SampleLayout(koast) }
            } else null,
        ){
			val samples = remember { getSamples(koastManager) { style = it } }
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