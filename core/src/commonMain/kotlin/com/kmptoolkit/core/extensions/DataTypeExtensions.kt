package com.kmptoolkit.core.extensions

import androidx.compose.runtime.MutableState
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

fun Float.convertMeterToKilometer() = div(1000)

fun Float.convertKilometerToMeter() = times(1000)

fun Double.toRadians() = this * PI / 180.0

fun Double.toDegree() = this * 180.0 / PI

fun Double.roundWithDecimalPoints(points: Int): Double {
    val factor = 10.0.pow(points)
    return (this * factor).roundToInt() / factor
}

fun Float.roundWithDecimalPoints(points: Int): Float =
    this.toDouble().roundWithDecimalPoints(points).toFloat()


fun MutableState<Boolean>.toggle() {
    this.value = !this.value
}

fun Boolean.toggle() = !this



@OptIn(ExperimentalTime::class)
fun DurationUnit.millis(value: Int) = Duration.convert(value.toDouble(), this, DurationUnit.MILLISECONDS).toLong()

