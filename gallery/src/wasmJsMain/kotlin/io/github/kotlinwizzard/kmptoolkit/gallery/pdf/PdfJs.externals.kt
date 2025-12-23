@file:JsModule("pdfjs-dist/legacy/build/pdf.js")
package io.github.kotlinwizzard.kmptoolkit.gallery.pdf



import org.khronos.webgl.ArrayBuffer
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.js.Promise



external object GlobalWorkerOptions {
    var workerSrc: String
}
external fun getDocument(src: GetDocumentSrc): PdfLoadingTask

external interface GetDocumentSrc

@JsFun("buffer => ({ data: buffer })")
external fun getDocumentSrcFromBuffer(buffer: ArrayBuffer): GetDocumentSrc


external interface PdfLoadingTask {
    @OptIn(ExperimentalWasmJsInterop::class)
    val promise: Promise<PdfDocument>
}

@OptIn(ExperimentalWasmJsInterop::class)
external interface PdfDocument: JsAny {
    val numPages: Int
    fun getPage(pageNumber: Int): Promise<PdfPage>
}

@OptIn(ExperimentalWasmJsInterop::class)
external interface PdfPage:JsAny {
    fun getViewport(params: JsAny = definedExternally): PdfViewport
    fun render(params: JsAny): PdfRenderTask
}

external interface PdfViewport {
    val width: Double
    val height: Double
}

external interface PdfRenderTask {
    @OptIn(ExperimentalWasmJsInterop::class)
    val promise: Promise<JsAny>
}


@JsFun("(page, scale) => page.getViewport({ scale: scale })")
external fun getViewport(page: PdfPage, scale: Double): PdfViewport

// page.render({ canvasContext: ctx, viewport })
@JsFun("(page, ctx, viewport) => page.render({ canvasContext: ctx, viewport: viewport })")
external fun renderPage(page: PdfPage, ctx: CanvasRenderingContext2D, viewport: PdfViewport): PdfRenderTask
