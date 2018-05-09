package com.github.kory33.util.sponge.ezdata.kotlin.extensions.internal


inline fun <T> List<T>.mapLast(transform: (T) -> T) = dropLast(1) + transform(last())
