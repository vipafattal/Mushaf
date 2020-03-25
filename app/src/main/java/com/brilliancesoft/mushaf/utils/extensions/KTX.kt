
/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.brilliancesoft.mushaf.utils.extensions

import android.animation.Animator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableString
import android.util.Size
import android.util.SizeF
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import java.io.File
import java.io.Serializable


inline fun View.updatePadding(
    @Px left: Int = paddingLeft,
    @Px top: Int = paddingTop,
    @Px right: Int = paddingRight,
    @Px bottom: Int = paddingBottom
) {
    setPadding(left, top, right, bottom)
}


inline var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }


inline fun CharSequence.toSpannable(): Spannable = SpannableString.valueOf(this)


inline operator fun ViewGroup.plusAssign(view: View) = addView(view)



inline operator fun Rect.component1() = this.left
inline operator fun Rect.component2() = this.top
inline operator fun Rect.component3() = this.right
inline operator fun Rect.component4() = this.bottom


fun Drawable.toBitmap(
    @Px width: Int = intrinsicWidth,
    @Px height: Int = intrinsicHeight,
    config: Bitmap.Config? = null
): Bitmap {

    if (this is BitmapDrawable) {
        if (config == null || bitmap.config == config) {
            // Fast-path to return original. Bitmap.createScaledBitmap will do this check, but it
            // involves allocation and two jumps into native code so we perform the check ourselves.
            if (width == intrinsicWidth && height == intrinsicHeight) {
                return bitmap
            }
            return Bitmap.createScaledBitmap(bitmap, width, height, true)
        }
    }

    val (oldLeft, oldTop, oldRight, oldBottom) = bounds

    val bitmap = Bitmap.createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
    setBounds(0, 0, width, height)
    draw(Canvas(bitmap))

    setBounds(oldLeft, oldTop, oldRight, oldBottom)
    return bitmap
}

fun Uri.toFile(): File {
    require(scheme == "file") { "Uri lacks 'file' scheme: $this" }
    return File(requireNotNull(path) { "Uri path is null: $this" })
}


inline fun Animator.addListener(
    crossinline onEnd: (animator: Animator) -> Unit = {},
    crossinline onStart: (animator: Animator) -> Unit = {},
    crossinline onCancel: (animator: Animator) -> Unit = {},
    crossinline onRepeat: (animator: Animator) -> Unit = {}
): Animator.AnimatorListener {
    val listener = object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animator: Animator) = onRepeat(animator)
        override fun onAnimationEnd(animator: Animator) = onEnd(animator)
        override fun onAnimationCancel(animator: Animator) = onCancel(animator)
        override fun onAnimationStart(animator: Animator) = onStart(animator)
    }
    addListener(listener)
    return listener
}

inline fun Animator.doOnEnd(crossinline action: (animator: Animator) -> Unit) =
    addListener(onEnd = action)

fun bundleOf(vararg pairs: Pair<String, Any?>) = Bundle(pairs.size).apply {
    for ((key, value) in pairs) {
        when (value) {
            null -> putString(key, null) // Any nullable type will suffice.

            // Scalars
            is Boolean -> putBoolean(key, value)
            is Byte -> putByte(key, value)
            is Char -> putChar(key, value)
            is Double -> putDouble(key, value)
            is Float -> putFloat(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Short -> putShort(key, value)

            // References
            is Bundle -> putBundle(key, value)
            is CharSequence -> putCharSequence(key, value)
            is Parcelable -> putParcelable(key, value)

            // Scalar arrays
            is BooleanArray -> putBooleanArray(key, value)
            is ByteArray -> putByteArray(key, value)
            is CharArray -> putCharArray(key, value)
            is DoubleArray -> putDoubleArray(key, value)
            is FloatArray -> putFloatArray(key, value)
            is IntArray -> putIntArray(key, value)
            is LongArray -> putLongArray(key, value)
            is ShortArray -> putShortArray(key, value)

            // Reference arrays
            is Array<*> -> {
                val componentType = value::class.java.componentType!!
                @Suppress("UNCHECKED_CAST") // Checked by reflection.
                when {
                    Parcelable::class.java.isAssignableFrom(componentType) -> {
                        putParcelableArray(key, value as Array<Parcelable>)
                    }
                    String::class.java.isAssignableFrom(componentType) -> {
                        putStringArray(key, value as Array<String>)
                    }
                    CharSequence::class.java.isAssignableFrom(componentType) -> {
                        putCharSequenceArray(key, value as Array<CharSequence>)
                    }
                    Serializable::class.java.isAssignableFrom(componentType) -> {
                        putSerializable(key, value)
                    }
                    else -> {
                        val valueType = componentType.canonicalName
                        throw IllegalArgumentException(
                            "Illegal value array type $valueType for key \"$key\"")
                    }
                }
            }

            // Last resort. Also we must check this after Array<*> as all arrays are serializable.
            is Serializable -> putSerializable(key, value)

            else -> {
                if (Build.VERSION.SDK_INT >= 18 && value is Binder) {
                    putBinder(key, value)
                } else if (Build.VERSION.SDK_INT >= 21 && value is Size) {
                    putSize(key, value)
                } else if (Build.VERSION.SDK_INT >= 21 && value is SizeF) {
                    putSizeF(key, value)
                } else {
                    val valueType = value.javaClass.canonicalName
                    throw IllegalArgumentException("Illegal value type $valueType for key \"$key\"")
                }
            }
        }
    }
}