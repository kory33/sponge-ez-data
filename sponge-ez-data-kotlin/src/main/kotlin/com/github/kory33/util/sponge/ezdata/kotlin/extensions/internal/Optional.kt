package com.github.kory33.util.sponge.ezdata.kotlin.extensions.internal

import java.util.*

inline fun <T> optionalIf(condition: Boolean, supplier: () -> T): Optional<T> =
        if (condition) Optional.of(supplier()) else Optional.empty()

inline fun <T> optionalFlatIf(condition: Boolean, supplier: () -> Optional<T>): Optional<T> =
        optionalIf(condition, supplier).flatMap { it }

inline fun <T> Optional<T>.orElseFill(other: () -> Optional<T>) = if(isPresent) this else other()

fun <T> Optional<T>.asNullable(): T? = orElse(null)
