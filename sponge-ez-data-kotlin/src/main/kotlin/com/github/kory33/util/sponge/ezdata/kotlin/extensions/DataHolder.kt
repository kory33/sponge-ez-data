package com.github.kory33.util.sponge.ezdata.kotlin.extensions

import com.github.kory33.util.sponge.ezdata.kotlin.manipulator.SingleKeyedValueData
import org.spongepowered.api.data.DataHolder
import org.spongepowered.api.data.DataTransactionResult
import org.spongepowered.api.data.manipulator.DataManipulator
import java.util.*

/**
 * Transforms the value in the [DataHolder] whose container class is [T] using [function].
 * Instance of [T] present in the [DataHolder] is fetched using [DataHolder.getOrCreate].
 */
inline fun <reified T: DataManipulator<*, *>>
        DataHolder.transformContainer(crossinline function: (T) -> T): DataTransactionResult =
        getOrCreate(T::class.java)
                .map { offer(function(it)) }
                .orElse(DataTransactionResult.failNoData())

/**
 * Obtain single value corresponding to the given [SingleKeyedValueData]
 */
inline fun <R, reified T: SingleKeyedValueData<R, T, *>> DataHolder.getSingleValue(): Optional<R> =
        getOrCreate(T::class.java).map { it.rawValue }

/**
 * Transforms the underlying value corresponding to the given [SingleKeyedValueData]
 */
inline fun <R, reified T: SingleKeyedValueData<R, T, *>> DataHolder.transformSingleValue(crossinline function: (R) -> R) =
        transformContainer<T> { it.apply { it.rawValue = function(it.rawValue) } }
