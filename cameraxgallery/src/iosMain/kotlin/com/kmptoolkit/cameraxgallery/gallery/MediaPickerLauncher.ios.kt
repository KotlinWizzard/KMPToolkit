package com.kmptoolkit.cameraxgallery.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.Foundation.NSData
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerConfigurationSelectionOrdered
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.darwin.NSObject
import platform.darwin.dispatch_group_create
import platform.darwin.dispatch_group_enter
import platform.darwin.dispatch_group_leave
import platform.darwin.dispatch_group_notify
import platform.posix.memcpy

@Composable
internal actual fun launchMediaPicker(
    onResult: (List<Pair<ByteArray, MediaPickerMediaType>>) -> Unit,
    mediaPickerStatus: MediaPickerStatus.LaunchRequested,
    mediaPickerLauncherState: MediaPickerLauncherState
) {
    val scope = rememberCoroutineScope()
    startMediaPickerLauncher(
        selectionMode = mediaPickerStatus.mediaPickerSelectionMode,
        selectionType = mediaPickerStatus.mediaPickerMediaSelectionType,
        mediaPickerLauncherState = mediaPickerLauncherState,
        scope = scope,
        onResult = onResult
    )
}


@OptIn(ExperimentalForeignApi::class)
@Composable
internal fun startMediaPickerLauncher(
    selectionMode: MediaPickerSelectionMode,
    selectionType:MediaPickerSelectionType,
    mediaPickerLauncherState: MediaPickerLauncherState,
    scope: CoroutineScope,
    onResult: (List<Pair<ByteArray, MediaPickerMediaType>>) -> Unit,
){
    val delegate =
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(
                picker: PHPickerViewController,
                didFinishPicking: List<*>,
            ) {
                picker.dismissViewControllerAnimated(flag = true, completion = null)
                @Suppress("UNCHECKED_CAST")
                val results = didFinishPicking as List<PHPickerResult>

                val dispatchGroup = dispatch_group_create()
                val data = mutableListOf<Pair<ByteArray,MediaPickerMediaType>>()

                for (result in results) {
                    dispatch_group_enter(dispatchGroup)
                    result.itemProvider.loadDataRepresentationForTypeIdentifier(
                        typeIdentifier = "public.image",
                    ) { nsData, _ ->
                        scope.launch(Dispatchers.Main) {
                            nsData?.let {
                                val image = UIImage.imageWithData(it)
                                val bytes = image?.toByteArray()
                                if (bytes != null) {
                                    data.add(bytes to MediaPickerMediaType.Image)
                                }
                                dispatch_group_leave(dispatchGroup)
                            }
                        }
                    }
                    result.itemProvider.loadDataRepresentationForTypeIdentifier(
                        typeIdentifier = "public.movie",
                    ) { nsData, _ ->
                        scope.launch(Dispatchers.Main) {
                            nsData?.let {
                                val videoBytes = it.toByteArray()
                                if(videoBytes!=null) {
                                    data.add(videoBytes to MediaPickerMediaType.Video)
                                }
                                dispatch_group_leave(dispatchGroup)
                            }
                        }
                    }
                }

                dispatch_group_notify(dispatchGroup, platform.darwin.dispatch_get_main_queue()) {
                    scope.launch(Dispatchers.Main) {
                        onResult(data)
                    }
                }
            }
        }
    LaunchMediaPicker(
        delegate = delegate,
        mediaPickerMediaSelectionType = selectionType,
        mediaPickerSelectionMode = selectionMode,
        mediaPickerLauncherState = mediaPickerLauncherState
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray():ByteArray?{
    return this.bytes().toByteArray(this.length())
}

@OptIn(ExperimentalForeignApi::class)
private fun CPointer<out CPointed>?.toByteArray(size:ULong): ByteArray? {
    if (this == null) return null
    val length = size.toInt()
    val byteArray = ByteArray(length)
    memcpy(byteArray.refTo(0), this, length.toULong())
    return byteArray
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toByteArray(compressionQuality: Double=1.0): ByteArray {
    val validCompressionQuality = compressionQuality.coerceIn(0.0, 1.0)
    val jpegData = UIImageJPEGRepresentation(this, validCompressionQuality)!!
    return ByteArray(jpegData.length.toInt()).apply {
        memcpy(this.refTo(0), jpegData.bytes, jpegData.length)
    }
}

@Composable
internal fun LaunchMediaPicker(
    delegate: PHPickerViewControllerDelegateProtocol,
    mediaPickerMediaSelectionType: MediaPickerSelectionType,
    mediaPickerSelectionMode: MediaPickerSelectionMode,
    mediaPickerLauncherState: MediaPickerLauncherState
) {
    val status = mediaPickerLauncherState.status
    LaunchedEffect(status) {
        when (status) {
            MediaPickerLauncherStatus.LaunchRequested -> {
                val pickerController = createPHPickerViewController(
                    delegate = delegate,
                    mode = mediaPickerSelectionMode,
                    type = mediaPickerMediaSelectionType
                )
                UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                    pickerController,
                    true,
                    null,
                )
                mediaPickerLauncherState.launch()
            }

            else -> Unit
        }
    }
}


private fun createPHPickerViewController(
    delegate: PHPickerViewControllerDelegateProtocol,
    mode: MediaPickerSelectionMode,
    type: MediaPickerSelectionType
): PHPickerViewController {
    val pickerViewController =
        PHPickerViewController(
            configuration =
            getPhPickerConfiguration(
                mode = mode,
                type = type
            )
        )
    pickerViewController.delegate = delegate
    return pickerViewController
}


private fun getPhPickerConfiguration(mode: MediaPickerSelectionMode, type: MediaPickerSelectionType): PHPickerConfiguration {
   return PHPickerConfiguration().apply {
        val limit = when(mode){
            is MediaPickerSelectionMode.Multiple -> (mode.maxSelection.takeIf { it != MediaPickerSelectionMode.INFINITY } ?: 0).toLong()
            MediaPickerSelectionMode.Single -> 1L
        }
        val filter = when(type){
            MediaPickerSelectionType.Image -> PHPickerFilter.imagesFilter
            MediaPickerSelectionType.Video -> PHPickerFilter.videosFilter
            MediaPickerSelectionType.Combined -> PHPickerFilter.anyFilterMatchingSubfilters(listOf( PHPickerFilter.imagesFilter, PHPickerFilter.videosFilter))
        }
        setSelectionLimit(selectionLimit = limit)
        setFilter(filter = filter)
        setSelection(selection = PHPickerConfigurationSelectionOrdered)
    }
}