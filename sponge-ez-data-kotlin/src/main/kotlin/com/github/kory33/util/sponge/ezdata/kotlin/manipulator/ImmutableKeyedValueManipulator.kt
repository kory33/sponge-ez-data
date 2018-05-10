package com.github.kory33.util.sponge.ezdata.kotlin.manipulator

import org.spongepowered.api.Sponge
import org.spongepowered.api.data.DataContainer
import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.manipulator.DataManipulator
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator
import org.spongepowered.api.data.value.BaseValue
import org.spongepowered.api.data.value.immutable.ImmutableValue
import org.spongepowered.api.data.value.mutable.Value
import java.util.*

@Suppress("UNCHECKED_CAST")
abstract class ImmutableKeyedValueManipulator<I: ImmutableDataManipulator<I, M>, M: DataManipulator<M, I>>: ImmutableDataManipulator<I, M> {

    /**
     * Projection of [ImmutableValue] onto [Key].
     *
     * Specifically, for every entry (k, v),
     * there has to be some type `E` such that (k, v): (Key<out BaseValue<E>>, ImmutableValue<E>).
     *
     * This property should hold by the type restriction given by [addKeyValuePair]
     */
    private val keyValueMap: MutableMap<Key<*>, ImmutableValue<*>> = HashMap()

    protected fun <E> addValue(value: ImmutableValue<E>) {
        keyValueMap[value.key] = value
    }

    protected fun <E> addKeyValuePair(key: Key<Value<E>>, value: E) {
        addValue(Sponge.getRegistry().valueFactory.createValue(key, value).asImmutable())
    }

    override fun supports(key: Key<*>?) = keyValueMap.containsKey(key)

    override fun getKeys(): Set<Key<*>> = keyValueMap.keys

    override fun <E : Any?> get(key: Key<out BaseValue<E>>?): Optional<E> = getValue(key).map { it.get() }

    override fun <E : Any?, V : BaseValue<E>?> getValue(key: Key<V>?): Optional<V> {
        val value = keyValueMap[key as Key<*>]

        // If key: Key<out BaseValue<E>> for some E, then value: ImmutableValue<E>.
        // ~Also, ImmutableValue<E> ⊆ V therefore this line is safe.~
        // TODO how do we know ImmutableValue<E> ⊆ V??
        return Optional.ofNullable(value as V)
    }

    override fun getValues(): Set<ImmutableValue<*>> = keyValueMap.values.toSet()

    override fun toContainer(): DataContainer = DataContainer.createNew().also { container ->
        keyValueMap.forEach { key, value -> container.set(key.query, value.get()) }
    }

}