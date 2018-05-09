package com.github.kory33.util.sponge.ezdata.kotlin.manipulator

import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.manipulator.DataManipulator
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator
import org.spongepowered.api.data.value.mutable.Value

abstract class SingleKeyedValueData<T, M: KeyedValueManipulator<M, I>, I: ImmutableDataManipulator<I, M>>(
        value: T,
        private val key: Key<Value<T>>): KeyedValueManipulator<M, I>() {

    init {
        addKeyValuePair(key, value)
    }

    var rawValue: T
        get() = get(key).get()
        set(value) {
            set(key, value)
        }

}

abstract class ImmutableSingleKeyedValueData<out T, I: ImmutableDataManipulator<I, M>, M: DataManipulator<M, I>>(
        value: T,
        private val key: Key<Value<T>>): ImmutableKeyedValueManipulator<I, M>() {

    init {
        addKeyValuePair(key, value)
    }

    val rawValue: T
        get() = get(key).get()

}
