import androidx.compose.ui.window.ComposeUIViewController
import di.KoinInit
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    KoinInit.init {}
    return ComposeUIViewController { App() }
}
