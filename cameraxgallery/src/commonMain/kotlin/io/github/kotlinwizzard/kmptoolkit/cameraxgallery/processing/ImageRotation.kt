package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing

enum class ImageRotation(val rotation:Int) {
    DegreeNegative90(-90),
    DegreeNegative180(-180),
    DegreeNegative270(-270),
    DegreeNegative360(-360),
    Degree90(90),
    Degree180(180),
    Degree270(270),
    Degree360(360);
    companion object
}

expect fun ImageRotation.Companion.rotateImage(byteArray: ByteArray, rotateBy:ImageRotation):ByteArray