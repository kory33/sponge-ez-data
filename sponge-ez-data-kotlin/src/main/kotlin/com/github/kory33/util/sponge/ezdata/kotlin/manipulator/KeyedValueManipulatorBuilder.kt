package com.github.kory33.util.sponge.ezdata.kotlin.manipulator

import com.github.kory33.util.sponge.ezdata.kotlin.internal.extensions.optionalFlatIf
import com.github.kory33.util.sponge.ezdata.kotlin.internal.extensions.optionalIf
import org.spongepowered.api.data.DataHolder
import org.spongepowered.api.data.DataView
import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder
import org.spongepowered.api.data.persistence.AbstractDataBuilder
import org.spongepowered.api.data.value.BaseValue
import java.util.*

@Suppress("UNCHECKED_CAST")
abstract class KeyedValueManipulatorBuilder<M: KeyedValueManipulator<M, I>, I: ImmutableKeyedValueManipulator<I, M>>(
        mClass: Class<M>,
        supportedVersion: Int): AbstractDataBuilder<M>(mClass, supportedVersion), DataManipulatorBuilder<M, I> {

    abstract val requiredKeys: Collection<Key<*>>

    override fun buildContent(container: DataView): Optional<M> {
        return optionalFlatIf (requiredKeys.all { container.contains(it) }) {
            val valuePairs = requiredKeys.associate { it to container[it.query].get() }

            // valueの型がkeyのelementTokenに全て一致しているならば
            optionalIf (valuePairs.allTypesMatch()) {
                create().apply {
                    valuePairs.forEach { (key, value) ->
                        // 型一致チェックにより、とあるEが存在しkey: Key<out BaseValue<E>>で、value: Eである。
                        // よってkeyValueMap[key]はValue<E>?となり、この操作は安全となる。
                        set(key as Key<out BaseValue<Any>>, value)
                    }
                }
            }
        }
    }

    override fun createFrom(dataHolder: DataHolder): Optional<M> = create().fill(dataHolder)

}