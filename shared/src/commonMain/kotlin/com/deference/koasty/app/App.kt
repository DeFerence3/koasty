package com.deference.koasty.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deference.koasty.KoastDisplayMode
import com.deference.koasty.KoastManager
import com.deference.koasty.KoastPosition
import com.deference.koasty.KoastyHostConfig
import com.deference.koasty.KoastyProvider
import com.deference.koasty.app.theme.KoastyDemoTheme
import kotlin.math.roundToInt

@Composable
@Preview
fun App() {
    KoastyDemoTheme {
        val koastManager = remember { KoastManager() }

        val vm = viewModel { AppViewModel() }
        val config by vm.config.collectAsStateWithLifecycle()

        KoastyProvider(
            koastManager,
            config = config.copy(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
        ) {
            Scaffold { paddingValues ->
                SettingsScreen(
                    config = config,
                    onAction = vm::onAction,
                    koastManager = koastManager,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    config: KoastyHostConfig,
    onAction: (AppAction) -> Unit,
    koastManager: KoastManager,
    modifier: Modifier = Modifier
) {
    var count by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Button(
            onClick = {
                count++
                koastManager.show("Koast $count", "Change a setting and show another")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Show koast")
        }

        Section("Display mode")
        RadioRow(
            label = "Sequential",
            selected = config.displayMode == KoastDisplayMode.Sequential,
            onClick = { onAction(AppAction.DisplayMode(KoastDisplayMode.Sequential)) }
        )
        RadioRow(
            label = "Stacked",
            selected = config.displayMode != KoastDisplayMode.Sequential,
            onClick = { onAction(AppAction.DisplayMode(KoastDisplayMode.Stacked())) }
        )

        Section("Position")
        KoastPosition.entries.forEach { position ->
            RadioRow(
                label = position.name,
                selected = config.position == position,
                onClick = { onAction(AppAction.Position(position)) }
            )
        }

        Section("Toggles")
        CheckBoxRow(
            label = "Compact",
            checked = config.compact,
            onToggle = { onAction(AppAction.Compact) }
        )
        CheckBoxRow(
            label = "Respect Safe Area",
            checked = config.respectSafeArea,
            onToggle = { onAction(AppAction.RespectSafeArea) }
        )
        CheckBoxRow(
            label = "Horizontal Swipe",
            checked = config.horizontalSwipeEnabled,
            onToggle = { onAction(AppAction.HorizontalSwipe) }
        )
        CheckBoxRow(
            label = "Swipe Up",
            checked = config.swipeUpEnabled,
            onToggle = { onAction(AppAction.SwipeUp) }
        )
        CheckBoxRow(
            label = "Show Compact Dismiss Button",
            checked = config.showCompactDismissButton,
            onToggle = { onAction(AppAction.CompactDismissButton) }
        )

        Section("Values")
        ValueSlider(
            label = "Edge Padding",
            value = config.edgePadding.value,
            range = 0f..48f,
            onChange = { onAction(AppAction.EdgePadding(it)) }
        )
        ValueSlider(
            label = "Max Width",
            value = config.maxWidth.value,
            range = 240f..720f,
            onChange = { onAction(AppAction.MaxWidth(it)) }
        )
        ValueSlider(
            label = "Min Height",
            value = config.minHeight.value,
            range = 40f..160f,
            onChange = { onAction(AppAction.MinHeight(it)) }
        )
    }
}

@Composable
private fun Section(title: String) {
    HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun RadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Text(
            text = label,
            modifier = Modifier
                .padding(start = 8.dp, top = 12.dp, bottom = 12.dp)
        )
    }
}

@Composable
private fun CheckBoxRow(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Checkbox,
                onValueChange = { onToggle() }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null
        )
        Text(
            text = label,
            modifier = Modifier
                .padding(start = 8.dp, top = 12.dp, bottom = 12.dp)
        )
    }
}

@Composable
private fun ValueSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column {
        Text("$label: ${value.roundToInt()} dp")
        Slider(
            value = value.coerceIn(range),
            onValueChange = onChange,
            valueRange = range
        )
    }
}
