@file:JsModule("pdfjs-dist/legacy/build/pdf.worker.min.js")
package io.github.kotlinwizzard.kmptoolkit.gallery.pdf

//external val pdfWorkerSrc: String

@JsFun("() => new URL('pdfjs-dist/legacy/build/pdf.worker.min.js', import.meta.url).toString()")
external fun pdfWorkerUrl(): String