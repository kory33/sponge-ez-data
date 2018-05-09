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
     * [Key]と[Value]の間の写像
     *
     * 各エントリに関して、`Key<V>`, `V` の `V` は一致する。
     * この性質は外部からの変更に限り[addKeyValuePair]により保証される。
     *
     * このクラス内での操作は気を付けること。
     */
    private val keyValueMap: MutableMap<Key<out Value<*>>, ImmutableValue<*>> = HashMap()

    protected fun <E, V: Value<E>> addKeyValuePair(key: Key<V>, value: ImmutableValue<E>) {
        keyValueMap[key] = value
    }

    protected fun <E> addKeyValuePair(key: Key<Value<E>>, value: E) {
        addKeyValuePair(key, Sponge.getRegistry().valueFactory.createValue(key, value).asImmutable())
    }

    override fun supports(key: Key<*>?) = keyValueMap.containsKey(key)

    override fun getKeys(): Set<Key<*>> = keyValueMap.keys

    override fun <E : Any?> get(key: Key<out BaseValue<E>>?): Optional<E> = getValue(key).map { it.get() }

    override fun <E : Any?, V : BaseValue<E>?> getValue(key: Key<V>?): Optional<V> {
        // HashMap.getは型変数を気にしないためここで例外は出ない
        val value = keyValueMap[key as Key<out Value<*>>]

        // keyValueMapの制約より、keyの型Key<V>の中のVはvalueの型に一致する
        // よってこのキャストは正しい
        return Optional.ofNullable(value as V)
    }

    override fun getValues(): Set<ImmutableValue<*>> = keyValueMap.values.toSet()

    override fun toContainer(): DataContainer = DataContainer.createNew().also { container ->
        keyValueMap.forEach { key, value -> container.set(key.query, value.get()) }
    }

}