package com.github.kory33.util.sponge.ezdata.kotlin.manipulator

import com.github.kory33.util.sponge.ezdata.kotlin.extensions.internal.optionalFlatIf
import com.github.kory33.util.sponge.ezdata.kotlin.extensions.internal.optionalIf
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.DataContainer
import org.spongepowered.api.data.DataHolder
import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.manipulator.DataManipulator
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator
import org.spongepowered.api.data.merge.MergeFunction
import org.spongepowered.api.data.value.BaseValue
import org.spongepowered.api.data.value.immutable.ImmutableValue
import org.spongepowered.api.data.value.mutable.Value
import java.util.*
import kotlin.collections.HashMap

/**
 * Checks if, for every key-value pair, value's type matches key's elementToken
 */
internal fun Map<Key<*>, Any>.allTypesMatch() = all { (key, value) -> key.elementToken.rawType.isInstance(value) }

@Suppress("UNCHECKED_CAST")
abstract class KeyedValueManipulator<M: KeyedValueManipulator<M, I>, I: ImmutableDataManipulator<I, M>>: DataManipulator<M, I> {

    /**
     * Projection of [Value] onto [Key].
     *
     * Specifically, for every entry (k, v),
     * there has to be some type `E` such that (k, v): (Key<out BaseValue<E>>, Value<E>).
     *
     * This property should hold by the type restriction given by [addKeyValuePair].
     */
    private val keyValueMap: MutableMap<Key<*>, Value<*>> = HashMap()

    protected fun <E> addValue(value: Value<E>) {
        keyValueMap[value.key] = value
    }

    protected fun <E> addKeyValuePair(key: Key<Value<E>>, value: E) {
        addValue(Sponge.getRegistry().valueFactory.createValue(key, value))
    }

    override fun from(container: DataContainer): Optional<M> {
        val valuePairs = keyValueMap.keys.associate { it to container[it.query] }

        // if all the values corresponding to the queries are present
        return optionalFlatIf(valuePairs.all { (_, value) -> value.isPresent }) {
            val unwrappedValuePairs = valuePairs.mapValues { it.value.get() }

            optionalIf(unwrappedValuePairs.allTypesMatch()) {
                unwrappedValuePairs.forEach { key, unwrappedValue ->
                    // keyValueMap.keys.contains(key) therefore keyValueMap[key] is never null.
                    // Also, if, for some E, key: Key<out BaseValue<E>> then keyValueMap[key]: Value<E> by the
                    // property of keyValueMap. By type-match check, unwrappedValue: E for the same E.
                    // therefore this line does not cause any heap pollution.
                    (keyValueMap[key] as Value<in Any>).set(unwrappedValue)
                }

                // This cast is safe if the derived type's M is derived type itself.
                this as M
            }
        }
    }

    override fun fill(dataHolder: DataHolder, overlap: MergeFunction): Optional<M> {
        val merged = overlap.merge(this, copy().from(dataHolder.toContainer()).orElse(null))

        // as long as the same restriction of keyValueMap is satisfied in `merged`, this is safe.
        keyValueMap.putAll(merged.keyValueMap)

        // This cast is safe if the derived type's M is derived type itself.
        return Optional.of(this as M)
    }

    override fun supports(key: Key<*>?) = keyValueMap.containsKey(key)

    override fun getKeys(): Set<Key<out BaseValue<*>>> = keyValueMap.keys

    final override fun <E : Any?> get(key: Key<out BaseValue<E>>): Optional<E> = getValue(key).map { it.get() }

    final override fun <E : Any?, V : BaseValue<E>> getValue(key: Key<V>?): Optional<V> {
        val value = keyValueMap[key as Key<*>]

        // If key: Key<out BaseValue<E>> for some E, then value: Value<E>.
        // ~Also, Value<E> ⊆ V therefore this line is safe.~
        // TODO how do we know Value<E> ⊆ V??
        // Moreover, since this implementation is final, this line ensures that this method only returns
        // Optional<out Value<E>>
        return Optional.ofNullable(value as? V)
    }

    override fun getValues(): Set<ImmutableValue<*>> = keyValueMap.values.map { it.asImmutable() }.toSet()

    override fun <E : Any?> set(key: Key<out BaseValue<E>>, value: E): M {
        // this is safe because getValue only returns Optional<out Value<E>>
        getValue(key).map { (it as Value<E>).set(value) }

        // This cast is safe if the derived type's M is derived type itself.
        return this as M
    }

    override fun toContainer(): DataContainer = DataContainer.createNew().also { container ->
        keyValueMap.forEach { key, value -> container.set(key.query, value.get()) }
    }

}