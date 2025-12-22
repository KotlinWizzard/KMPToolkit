import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import di.KoinInit

fun main() =
    application {
        KoinInit.init { }
        Window(
            onCloseRequest = ::exitApplication,
            title = "KMPToolkit",
        ) {
            App()
        }
    }

@Composable
fun AppDesktopPreview() {
    App()
}
