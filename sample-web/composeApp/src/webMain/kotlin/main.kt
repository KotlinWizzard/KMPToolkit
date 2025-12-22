import App
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import di.KoinInit

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    KoinInit.init {
        ComposeViewport {
            App()
        }
    }
}