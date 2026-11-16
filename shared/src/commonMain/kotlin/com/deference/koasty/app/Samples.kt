package com.deference.koasty.app

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deference.koasty.KoastDisplayMode
import com.deference.koasty.KoastManager
import com.deference.koasty.KoastPosition
import com.deference.koasty.KoastRequest
import com.deference.koasty.KoastyHostConfig
import kotlin.time.Duration.Companion.seconds

internal enum class SampleStyle { Default, Stacked, CustomConfig, Leading, CustomLayout }

internal fun sampleConfig(style: SampleStyle): KoastyHostConfig = when (style) {
	SampleStyle.Stacked -> KoastyHostConfig(
		displayMode = KoastDisplayMode.Stacked(maxVisible = 3),
	)
	SampleStyle.CustomConfig -> KoastyHostConfig(
		position = KoastPosition.TOP_CENTER,
		compact = true,
		containerColor = Color(0xFF263238),
		contentColor = Color(0xFFECEFF1),
		shape = RoundedCornerShape(24.dp),
		shadowElevation = 16.dp,
		contentPadding = PaddingValues(24.dp),
		titleStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
		messageStyle = TextStyle(fontSize = 13.sp),
		horizontalSwipeEnabled = true,
		swipeUpEnabled = false,
		displayMode = KoastDisplayMode.Stacked(maxVisible = 3),
		enterAnimationSpec = tween(400),
		exitAnimationSpec = tween(400),
	)
	else -> KoastyHostConfig()
}

@Composable
internal fun SampleLeading(koast: KoastRequest) {
	Text("●", modifier = Modifier.padding(end = 12.dp))
}

@Composable
internal fun SampleLayout(koast: KoastRequest) {
	Column(modifier = Modifier.padding(16.dp)) {
		Text("★ ${koast.title}", color = Color.White, fontWeight = FontWeight.Bold)
		koast.message?.let { Text(it, color = Color.White) }
	}
}

internal fun getSamples(
	koastManager: KoastManager,
	setStyle: (SampleStyle) -> Unit,
) = listOf(
	"Normal" to {
		koastManager.show(
			"Alert",
			"This is a normal koast, dismissable by swiping " +
					"and automatically fades after a duration"
		)
	},
	"Persistent" to {
		koastManager.showPersistent(
			"Persistent Koast",
			"This koast won't dismiss by its own." +
					"only programmatically "
		)
	},
	"Dismiss Persistent" to {
		koastManager.dismiss()
	},
	"Alias" to {
		koastManager.koast("Alias", "Created with koast()")
	},
	"Custom Duration" to {
		koastManager.show("Connected", "Hides after 8 seconds", duration = 8.seconds)
	},
	"Title Only" to {
		koastManager.show("Title only koast")
	},
	"Dismissible" to {
		koastManager.showDismissible("Dismissible", "Swipe me away")
	},
	"Non Dismissible" to {
		koastManager.showNonDismissible("Non Dismissible", "Cannot swipe, fades automatically")
	},
	"Until Dismissed" to {
		koastManager.showUntilDismissed("You're offline", "Swipe or dismiss in code")
	},
	"Clickable" to {
		koastManager.showClickable(
			"Update available",
			"Tap to see another koast",
			onClick = { koastManager.show("Clicked", "Click handled") }
		)
	},
	"Click Keeps Open" to {
		koastManager.showClickable(
			"Stay open",
			"Tap won't dismiss me",
			dismissOnClick = false,
			onClick = {}
		)
	},
	"Queue Koasts" to {
		koastManager.show("First", "Shown first")
		koastManager.show("Second", "Waits until the first is gone")
		koastManager.show("Third", "First in, first out")
	},
	"Dismiss All" to {
		koastManager.dismissAll()
	},
	"Default Style" to {
		setStyle(SampleStyle.Default)
		koastManager.show("Default", "Default host config")
	},
	"Stacked Style" to {
		setStyle(SampleStyle.Stacked)
		koastManager.show("Stacked", "Queued koasts stack like cards")
		koastManager.show("Next up", "Moves forward when the front one leaves")
		koastManager.show("Third", "Last visible card")
		koastManager.show("Fourth", "Waits off-screen until there is room")
	},
	"Custom Config" to {
		setStyle(SampleStyle.CustomConfig)
		koastManager.show("Custom Config", "Colors, shape, position, animation")
	},
	"Leading Content" to {
		setStyle(SampleStyle.Leading)
		koastManager.show("Leading", "Leading icon slot")
	},
	"Custom Layout" to {
		setStyle(SampleStyle.CustomLayout)
		koastManager.show("Custom Layout", "Fully custom koastContent")
	},
)