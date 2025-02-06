package presentation

import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import io.github.kotlinwizzard.kmptoolkit.core.extensions.currentOrThrow

@Composable
fun BackButtonToolbar(text:String){
    val nav = LocalNavigator.currentOrThrow
    TopAppBar(navigationIcon = {
        Button(onClick = {
            nav.pop()
        }){
            Text("Back")
        }
    },title = {
        Text(text)
    })
}