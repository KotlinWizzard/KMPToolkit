package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing

import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreImage.CIContext
import platform.CoreImage.CIFilter
import platform.CoreImage.CIImage
import platform.CoreImage.CIVector
import platform.CoreImage.createCGImage
import platform.CoreImage.filterWithName
import platform.CoreImage.kCIInputAngleKey
import platform.CoreImage.kCIInputBrightnessKey
import platform.CoreImage.kCIInputContrastKey
import platform.CoreImage.kCIInputImageKey
import platform.CoreImage.kCIInputIntensityKey
import platform.CoreImage.kCIInputSaturationKey
import platform.Foundation.setValue
import platform.UIKit.UIImage

actual object ImageEditor {
    actual suspend fun applyBrightness(
        bitmapData: ByteArray,
        factor: Float
    ): ByteArray {
        val mapped = (factor - 1.0f).coerceIn(-1.0f, 1.0f)
        return applyCIColorControls(bitmapData, brightness = mapped)
    }

    actual suspend fun applyGreyscale(bitmapData: ByteArray): ByteArray {
        return applyFilter(bitmapData, "CIPhotoEffectMono")
    }

    actual suspend fun applySepia(bitmapData: ByteArray): ByteArray {
        return applyFilter(bitmapData, "CISepiaTone") { filter ->
            filter.setValue(1.0, forKey = kCIInputIntensityKey)
        }
    }

    actual suspend fun applyContrast(bitmapData: ByteArray, factor: Float): ByteArray {
        return applyCIColorControls(bitmapData, contrast = factor)
    }

    actual suspend fun applySaturation(bitmapData: ByteArray, factor: Float): ByteArray {
        return applyCIColorControls(bitmapData, saturation = factor)
    }

    actual suspend fun applyExposure(bitmapData: ByteArray, ev: Float): ByteArray {
        return applyFilter(bitmapData, "CIExposureAdjust") { filter ->
            filter.setValue(ev.toDouble(), forKey = "inputEV")
        }
    }

    actual suspend fun applyTemperature(bitmapData: ByteArray, temperature: Float): ByteArray {
        val clamped = temperature.coerceIn(-100f, 100f)
        val targetK = 6500.0 + (clamped * 15.0) // linearer Bereich ±1500K
        return applyFilter(bitmapData, "CITemperatureAndTint") { filter ->
            val neutral = CIVector(x = 6500.0,  0.0)
            val target = CIVector(x = targetK, 0.0)
            filter.setValue(neutral, forKey = "inputNeutral")
            filter.setValue(target, forKey = "inputTargetNeutral")
        }
    }

    actual suspend fun applyHue(bitmapData: ByteArray, angleDegrees: Float): ByteArray {
        val radians = ((angleDegrees % 360f) * kotlin.math.PI / 180f).coerceIn(- kotlin.math.PI, kotlin.math.PI)
        return applyFilter(bitmapData, "CIHueAdjust") { filter ->
            filter.setValue(radians, forKey = kCIInputAngleKey)
        }
    }

    private fun applyCIColorControls(
        data: ByteArray,
        brightness: Float? = null,
        contrast: Float? = null,
        saturation: Float? = null
    ): ByteArray {
        return applyFilter(data, "CIColorControls") { filter ->
            brightness?.let { filter.setValue(it.toDouble(), forKey = kCIInputBrightnessKey) }
            contrast?.let { filter.setValue(it.toDouble(), forKey = kCIInputContrastKey) }
            saturation?.let { filter.setValue(it.toDouble(), forKey = kCIInputSaturationKey) }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun applyFilter(
        data: ByteArray,
        filterName: String,
        configure: (CIFilter) -> Unit = {}
    ): ByteArray {
        val nsData = data.toNSData()
        val uiImage = UIImage(data = nsData)
        val ciImage = CIImage(nsData)
        val filter = CIFilter.filterWithName(filterName) as CIFilter
        filter.setValue(ciImage, forKey = kCIInputImageKey)
        configure(filter)
        val outputImage = filter.outputImage ?: return data
        val context = CIContext()
        val cgImage = context.createCGImage(outputImage, fromRect = outputImage.extent) ?: return data
        val newUIImage = UIImage.imageWithCGImage(cgImage)
        return newUIImage.toByteArray()
    }
}