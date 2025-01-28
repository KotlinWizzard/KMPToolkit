package screens.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import com.kmptoolkit.cameraxgallery.camera.state.CameraCaptureState
import com.kmptoolkit.cameraxgallery.camera.state.rememberCameraState
import com.kmptoolkit.cameraxgallery.camera.ui.CameraCaptureButton
import com.kmptoolkit.cameraxgallery.camera.ui.CameraPreviewLayout
import com.kmptoolkit.core.presentation.theme.ToolkitDarkScreen
import com.kmptoolkit.core.presentation.theme.ToolkitScaffold
import presentation.BackButtonToolbar

class CameraScreen : Screen {
    @Composable
    override fun Content() {
        ToolkitDarkScreen {
            ToolkitScaffold(topBar = {
                BackButtonToolbar("Camera")
            }) { it ->
                val cameraState = rememberCameraState(onCapture = { res ->
                    println("TEST_CAMERA: onCapture: $res")
                })

                    CameraPreviewLayout(
                        Modifier.fillMaxSize().padding(top = it.calculateTopPadding()),
                        cameraState = cameraState,
                        bottomBar = {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val captureState = cameraState.captureState
                                val text = if (captureState is CameraCaptureState.Video) {
                                    captureState.minuteSecondsText ?: ""
                                } else {
                                    ""
                                }
                                Text("${cameraState.cameraCaptureMode}: $text", color = Color.White)
                                CameraCaptureButton(cameraState = cameraState)
                            }
                        })

            }
        }
    }
}