package com.github.kory33.util.sponge.ezdata.kotlin.manipulator

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
 * 全てのkey-valueペアにおいてvalueの型がkeyのelementTokenに一致しているかを判定する関数
 */
internal fun Map<Key<*>, Any>.allTypesMatch() = all { (key, value) -> key.elementToken.rawType.isInstance(value) }

@Suppress("UNCHECKED_CAST")
abstract class KeyedValueManipulator<M: KeyedValueManipulator<M, I>, I: ImmutableDataManipulator<I, M>>: DataManipulator<M, I> {

    /**
     * [Key]と[Value]の間の写像
     *
     * 各エントリ`(k, v)`に関して、`k = v.getKey()`でなければならない。
     *
     * この性質は外部からの変更に限り[addValue]により保証される。
     * 直接操作するときは制約を壊してはならない。
     */
    private val keyValueMap: MutableMap<Key<*>, Value<*>> = HashMap()

    protected fun <E, V: Value<E>> addValue(value: V) {
        keyValueMap[value.key] = value
    }

    protected fun <E> addKeyValuePair(key: Key<Value<E>>, value: E) {
        addValue(Sponge.getRegistry().valueFactory.createValue(key, value))
    }

    override fun from(container: DataContainer): Optional<M> {
        val valuePairs = keyValueMap.keys.associate { it to container[it.query] }

        // 全てのキーのクエリに対応する値が入っていた場合
        return optionalFlatIf (valuePairs.all { (_, value) -> value.isPresent }) {
            val unwrappedValuePairs = valuePairs.mapValues { it.value.get() }

            optionalIf (unwrappedValuePairs.allTypesMatch()) {
                unwrappedValuePairs.forEach { key, unwrappedValue ->
                    // keyValueMap.keys.contains(key)なので、keyValueMap[key]はnullではない。
                    // また、key: Key<out BaseValue<E>>なら、keyValueMapの性質によりkeyValueMap[key]: Value<E> であり、
                    // 型一致チェックによって unwrappedValue: E である。
                    // よってこの操作は例外を起こさず安全である。
                    (keyValueMap[key] as Value<Any>).set(unwrappedValue)
                }

                // 派生クラスの型変数Mが派生クラス自身であるならばこのキャストは安全である
                this as M
            }
        }
    }

    override fun fill(dataHolder: DataHolder, overlap: MergeFunction): Optional<M> {
        val merged = overlap.merge(this, copy().from(dataHolder.toContainer()).orElse(null))

        // merged側でのkeyValueMapの性質が保証されているならばこの操作は安全になる
        keyValueMap.putAll(merged.keyValueMap)

        // 派生クラスの型変数Mが派生クラス自身であるならばこのキャストは安全である
        return Optional.of(this as M)
    }

    override fun supports(key: Key<*>?) = keyValueMap.containsKey(key)

    override fun getKeys(): Set<Key<out BaseValue<*>>> = keyValueMap.keys

    final override fun <E : Any?> get(key: Key<out BaseValue<E>>): Optional<E> = getValue(key).map { it.get() }

    final override fun <E : Any?, V : BaseValue<E>> getValue(key: Key<V>?): Optional<V> {
        val value = keyValueMap[key as Key<*>]

        // keyValueMapの制約より、key: Key<out BaseValue<E>>ならば、value: Value<E>である。
        // Value<E>: Vであるから、このキャストは安全である。
        // さらに、この実装によりgetValueがOptional<out Value<E>>しか返さないことが保証できる。
        return Optional.ofNullable(value as? V)
    }

    override fun getValues(): Set<ImmutableValue<*>> = keyValueMap.values.map { it.asImmutable() }.toSet()

    override fun <E : Any?> set(key: Key<out BaseValue<E>>, value: E): M {
        // getValueはOptional<out Value<E>>しか返さないので、このキャストは安全である
        getValue(key).map { (it as Value<E>).set(value) }

        // 派生クラスの型変数Mが派生クラス自身であるならばこのキャストは安全である
        return this as M
    }

    override fun toContainer(): DataContainer = DataContainer.createNew().also { container ->
        keyValueMap.forEach { key, value -> container.set(key.query, value.get()) }
    }

}