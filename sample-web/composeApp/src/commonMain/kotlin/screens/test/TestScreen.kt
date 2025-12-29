package screens.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitScaffold
import presentation.BackButtonToolbar

class TestScreen: Screen {
    @Composable
    override fun Content() {
        ToolkitScaffold(topBar = {
            BackButtonToolbar("Pdf")
        }) {
            Box(Modifier.padding(it)) {
                Text("Test")
            }

        }
    }
}