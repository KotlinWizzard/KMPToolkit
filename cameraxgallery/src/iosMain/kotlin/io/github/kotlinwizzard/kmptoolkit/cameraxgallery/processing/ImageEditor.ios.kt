package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing

import platform.CoreImage.CIFilter
import platform.CoreImage.filterWithName
import platform.Foundation.setValue

actual object ImageEditor {
    actual suspend fun applyBrightness(
        bitmapData: ByteArray,
        factor: Float
    ): ByteArray {
        return applyCIColorControls(bitmapData, brightness = factor)
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
        return applyFilter(bitmapData, "CITemperatureAndTint") { filter ->
            val neutral = CIVector(x = 6500.0, y = 0.0)
            val target = CIVector(x = 6500.0 + temperature, y = 0.0)
            filter.setValue(neutral, forKey = "inputNeutral")
            filter.setValue(target, forKey = "inputTargetNeutral")
        }
    }

    actual suspend fun applyHue(bitmapData: ByteArray, angleDegrees: Float): ByteArray {
        val angleRadians = angleDegrees * kotlin.math.PI / 180f
        return applyFilter(bitmapData, "CIHueAdjust") { filter ->
            filter.setValue(angleRadians, forKey = kCIInputAngleKey)
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

    private fun applyFilter(
        data: ByteArray,
        filterName: String,
        configure: (CIFilter) -> Unit = {}
    ): ByteArray {
        val nsData = data.toNSData()
        val uiImage = UIImage(data = nsData) ?: return data
        val ciImage = CIImage(image = uiImage) ?: return data
        val filter = CIFilter.filterWithName(filterName) as CIFilter
        filter.setValue(ciImage, forKey = kCIInputImageKey)
        configure(filter)
        val outputImage = filter.outputImage ?: return data
        val context = CIContext()
        val cgImage = context.createCGImage(outputImage, fromRect = outputImage.extent) ?: return data
        val newUIImage = UIImage.imageWithCGImage(cgImage)
        val pngData = newUIImage.pngData() ?: return data
        return pngData.toByteArray()
    }
}