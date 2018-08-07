package com.github.kory33.util.sponge.ezdata.kotlin.manipulator

import com.github.kory33.util.sponge.ezdata.kotlin.extensions.internal.optionalIf
import com.github.kory33.util.sponge.ezdata.kotlin.extensions.internal.orElseFill
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.DataContainer
import org.spongepowered.api.data.DataHolder
import org.spongepowered.api.data.DataView
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
 * Gets a value or an object corresponding to the given value key.
 */
@Suppress("UNCHECKED_CAST")
internal fun <T> DataView.getCorrespondingValue(key: Key<out BaseValue<T>>): Optional<T> {
    return (container.getObject(key.query, key.elementToken.rawType) as Optional<T>).orElseFill {
        container[key.query].flatMap { raw ->
            optionalIf(key.elementToken.isSupertypeOf(raw.javaClass)) {
                raw
            } as Optional<T>
        }
    }
}

/**
 * Class of mutable data manipulators in which every internal data is managed through its corresponding [Key].
 *
 * Within the subclass constructor, [addKeyValuePair] has to be called in order to add a value which
 * will then be serialized to [DataContainer].
 *
 * Implementation of [equals] and [hashCode] methods assume that no fields other than those within a key-map
 * are taken into account for equality check. If that is not the case, [equals], [hashCode] and [toContainer]
 * would need to be overrode.
 *
 * @author kory33
 */
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

    final override fun <E : Any?, V : BaseValue<E>> getValue(key: Key<V>?): Optional<V> {
        val value = keyValueMap[key as Key<*>]

        // If key: Key<out BaseValue<E>> for some E, then value: Value<E>.
        // ~Also, Value<E> ⊆ V therefore this line is safe.~
        // TODO how do we know Value<E> ⊆ V??
        // Moreover, since this implementation is final, this line ensures that this method only returns
        // Optional<out Value<E>>
        return Optional.ofNullable(value as? V)
    }

    final override fun <E : Any?> get(key: Key<out BaseValue<E>>): Optional<E> = getValue(key).map { it.get() }

    override fun getValues(): Set<ImmutableValue<*>> = keyValueMap.values.map { it.asImmutable() }.toSet()

    override fun <E : Any?> set(key: Key<out BaseValue<E>>, value: E): M {
        // this is safe because getValue only returns Optional<out Value<E>>
        getValue(key).map { (it as Value<E>).set(value) }

        // This cast is safe if the derived type's M is derived type itself.
        return this as M
    }

    fun fromView(view: DataView): Optional<M> {
        val valuePairs = keyValueMap.keys.associate { it to view.getCorrespondingValue(it as Key<BaseValue<Any>>) }

        // if all the values corresponding to the queries are present
        return optionalIf(valuePairs.all { (_, value) -> value.isPresent }) {
            val unwrappedValuePairs = valuePairs.mapValues { it.value.get() }.toList()

            // The last cast is safe if the derived type's M is derived type itself.
            unwrappedValuePairs.fold(this) { accum, (key, value) ->
                // If, for some E, key: Key<out Value<E>> then keyValueMap[key]: Value<E> by the
                // property of keyValueMap. By type-match check, unwrappedValue: E for the same E.
                // therefore this line does not cause any heap pollution.
                key as Key<Value<Any>>
                accum.set(key, value)
            } as M
        }
    }

    override fun from(container: DataContainer): Optional<M> = fromView(container)

    override fun fill(dataHolder: DataHolder, overlap: MergeFunction): Optional<M> {
        val merged = overlap.merge(this, copy().from(dataHolder.toContainer()).orElse(null))

        // as long as the same restriction of keyValueMap is satisfied in `merged`, this is safe.
        keyValueMap.putAll(merged.keyValueMap)

        // This cast is safe if the derived type's M is derived type itself.
        return Optional.of(this as M)
    }

    override fun supports(key: Key<*>?) = keyValueMap.containsKey(key)

    override fun getKeys(): Set<Key<out BaseValue<*>>> = keyValueMap.keys

    override fun toContainer(): DataContainer = DataContainer.createNew().also { container ->
        keyValueMap.forEach { key, value -> container.set(key.query, value.get()) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyedValueManipulator<*, *>

        if (keyValueMap != other.keyValueMap) return false

        return true
    }

    override fun hashCode(): Int {
        return keyValueMap.hashCode()
    }

}