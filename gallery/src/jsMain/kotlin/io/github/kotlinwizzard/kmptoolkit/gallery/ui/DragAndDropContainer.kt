package io.github.kotlinwizzard.kmptoolkit.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.WebElementView
import io.github.kotlinwizzard.kmptoolkit.core.extensions.clickableWithoutRipple
import io.github.kotlinwizzard.kmptoolkit.core.service.media.LocalCache
import io.github.kotlinwizzard.kmptoolkit.gallery.chooseFile
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.files.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DragAndDropContainer(
    modifier: Modifier,
    pickFilesOnClick: Boolean,
    onClick: (coroutineScope: CoroutineScope) -> Unit,
    onDrop: (coroutineScope: CoroutineScope, files: List<File>) -> Unit
) {
    val document = document
    val coroutineScope = rememberCoroutineScope()
    val clickModifier = if (pickFilesOnClick) Modifier.clickableWithoutRipple(onClick = {
        onClick(coroutineScope)
    }) else Modifier
    val divRef = remember { mutableStateOf<HTMLDivElement?>(null) }
    WebElementView(
        modifier = modifier.then(clickModifier),
        factory = {
            val div = (document.createElement("div") as HTMLDivElement)
            div.style.setProperty("pointer-events", "none")
            divRef.value = div
            div
        })

    LaunchedEffect(divRef.value) {
        divRef.value?.let { div ->
            val parent = div.parentElement as? HTMLElement
            parent?.style?.setProperty("pointer-events", "none")
        }
    }

    DisposableEffect(divRef.value) {
        val wrapper = divRef.value ?: return@DisposableEffect onDispose { }

        val dragOverListener: (org.w3c.dom.events.Event) -> Unit = { evt ->
            val e = evt.unsafeCast<DragEvent>()

            val rect = wrapper.getBoundingClientRect()
            val x = e.clientX.toDouble()
            val y = e.clientY.toDouble()

            val inside = x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom
            if (inside) {
                e.preventDefault()
                e.asDynamic().dataTransfer?.dropEffect = "copy"
            }
        }

        val dropListener: (org.w3c.dom.events.Event) -> Unit = { evt ->
            val e = evt.unsafeCast<DragEvent>()

            val rect = wrapper.getBoundingClientRect()
            val x = e.clientX.toDouble()
            val y = e.clientY.toDouble()

            val inside = x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom
            if (inside) {

                e.preventDefault()

                val dt = e.dataTransfer
                if(dt!=null) {
                    val files = dt.files

                    val filtered = buildList {
                        for (i in 0 until files.length) {
                            val f = files.item(i) ?: continue
                            add(f)
                        }
                    }
                    onDrop(coroutineScope, filtered)
                }
            }
        }

        document.addEventListener("dragover", dragOverListener)
        document.addEventListener("drop", dropListener)

        onDispose {
            document.removeEventListener("dragover", dragOverListener)
            document.removeEventListener("drop", dropListener)
        }
    }

}