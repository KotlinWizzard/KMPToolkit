package io.github.kotlinwizzard.kmptoolkit.image.processing

import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.PI

actual suspend fun ImageRotation.Companion.rotateImage(
    byteArray: ByteArray,
    rotateBy: ImageRotation
): ByteArray {
    val normalized = normalizeDegrees(rotateBy.rotation)

    if (normalized == 0) return byteArray

    val srcBitmap = decodeJpegToImageBitmap(byteArray)
    val srcW = srcBitmap.width
    val srcH = srcBitmap.height

    val (dstW, dstH) = if (normalized == 90 || normalized == 270) srcH to srcW else srcW to srcH

    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = dstW
    canvas.height = dstH

    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
    ctx.save()
    ctx.translate(dstW / 2.0, dstH / 2.0)
    ctx.rotate(normalized * PI / 180.0)
    ctx.drawImage(srcBitmap, (-srcW / 2.0), (-srcH / 2.0))
    ctx.restore()

    val outBlob = canvasToJpegBlob(canvas, quality = 0.92)
    return blobToByteArray(outBlob)
}