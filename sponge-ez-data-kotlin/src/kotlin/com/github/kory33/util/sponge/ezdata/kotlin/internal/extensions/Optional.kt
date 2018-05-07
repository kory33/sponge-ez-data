package com.github.kory33.util.sponge.ezdata.kotlin.internal.extensions

import java.util.*

inline fun <T> optionalIf(condition: Boolean, supplier: () -> T): Optional<T> =
        if (condition) Optional.of(supplier()) else Optional.empty()

inline fun <T> optionalFlatIf(condition: Boolean, supplier: () -> Optional<T>): Optional<T> =
        optionalIf(condition, supplier).flatMap { it }
