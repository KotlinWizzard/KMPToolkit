package io.github.kotlinwizzard.kmptoolkit.image.processing

import io.github.kotlinwizzard.kmptoolkit.image.camera.ui.toByteArray
import io.github.kotlinwizzard.kmptoolkit.image.camera.ui.toNSData
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
import kotlin.math.PI
import kotlin.math.abs

actual object ImageEditor {
    actual suspend fun applyBrightness(
        bitmapData: ByteArray,
        factor: Float
    ): ByteArray {
        val mapped = factor.coerceIn(-1F,1F)
        return applyCIColorControls(bitmapData, brightness = mapped)
    }

    actual suspend fun applyGreyscale(bitmapData: ByteArray): ByteArray {
        return applyFilter(bitmapData, "CIPhotoEffectMono")
    }

    actual suspend fun applySepia(bitmapData: ByteArray): ByteArray {
        return applyFilter(bitmapData, "CISepiaTone") { filter ->
            filter.setValue(1.3, forKey = kCIInputIntensityKey)
        }
    }

    actual suspend fun applyContrast(bitmapData: ByteArray, factor: Float): ByteArray {
        val clamped = factor.coerceIn(-1f, 1f)
        val contrast = when {
            clamped <= 0f -> (clamped + 1f)
            else -> (clamped * 2) + 1f
        }
        return applyCIColorControls(bitmapData, contrast = contrast)
    }

    actual suspend fun applySaturation(bitmapData: ByteArray, factor: Float): ByteArray {
        val clamped = factor.coerceIn(-1f, 1f)
        val saturation = when {
            clamped <= 0f -> (clamped + 1f)
            else -> (clamped * 5) + 1f
        }
        return applyCIColorControls(bitmapData, saturation = saturation)
    }

    actual suspend fun applyExposure(bitmapData: ByteArray, ev: Float): ByteArray {
        return applyFilter(bitmapData, "CIExposureAdjust") { filter ->
            val clamped = ev.coerceIn(-1F,1F)
            val mappedEv =  clamped.times(5)
            filter.setValue(mappedEv.toDouble(), forKey = "inputEV")
        }
    }

    actual suspend fun applyTemperature(bitmapData: ByteArray, temperature: Float): ByteArray {
        val clamped = temperature.coerceIn(-1f, 1f).times(100)
        val isNegative = clamped<=0
        val positiveDegrees = abs(clamped)
        val mappedDegree = 6500 - (positiveDegrees * 45.0)
        val (neutralDegree,targetDegree) = when(isNegative){
            true -> mappedDegree to 0.0
            false -> 0.0 to mappedDegree
        }
        return applyFilter(bitmapData, "CITemperatureAndTint") { filter ->
            val neutral = CIVector(x = neutralDegree,  0.0)
            val target = CIVector(x = targetDegree,0.0)
            filter.setValue(neutral, forKey = "inputNeutral")
            filter.setValue(target, forKey = "inputTargetNeutral")
        }
    }

    actual suspend fun applyHue(bitmapData: ByteArray, angleDegrees: Float): ByteArray {
        val radians = ((angleDegrees % 360f + 360f) % 360f) * PI.toFloat() / 180f
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