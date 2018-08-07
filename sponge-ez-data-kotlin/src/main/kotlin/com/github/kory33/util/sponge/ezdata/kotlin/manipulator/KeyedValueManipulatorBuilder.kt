package com.github.kory33.util.sponge.ezdata.kotlin.manipulator

import com.github.kory33.util.sponge.ezdata.kotlin.extensions.internal.mappend
import com.github.kory33.util.sponge.ezdata.kotlin.extensions.internal.optionalIf
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

    abstract val requiredKeys: Collection<Key<out BaseValue<*>>>

    override fun buildContent(container: DataView): Optional<M> {
        return requiredKeys.fold(Optional.of(create())) { accum, key ->
            accum.flatMap { manipulator ->
                (container.getObject(key.query, key.elementToken.rawType) as Optional<Any>).mappend {
                    container[key.query].flatMap { raw ->
                        optionalIf(key.elementToken.isSupertypeOf(raw.javaClass)) {
                            raw
                        }
                    }
                }.map { value ->
                    // This is valid since there must exist a type T for which value: T and key: Key<out BaseValue<T>>.
                    manipulator.set(key as Key<BaseValue<Any>>, value)
                }
            }
        }
    }

    override fun createFrom(dataHolder: DataHolder): Optional<M> = create().fill(dataHolder)

}
