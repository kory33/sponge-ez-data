package com.github.kory33.util.sponge.ezdata.kotlin.manipulator

import org.spongepowered.api.data.DataHolder
import org.spongepowered.api.data.DataView
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder
import org.spongepowered.api.data.persistence.AbstractDataBuilder
import java.util.*

@Suppress("UNCHECKED_CAST")
abstract class KeyedValueManipulatorBuilder<M: KeyedValueManipulator<M, I>, I: ImmutableKeyedValueManipulator<I, M>>(
        mClass: Class<M>,
        supportedVersion: Int): AbstractDataBuilder<M>(mClass, supportedVersion), DataManipulatorBuilder<M, I> {

    override fun buildContent(container: DataView): Optional<M> = create().fromView(container)

    override fun createFrom(dataHolder: DataHolder): Optional<M> = create().fill(dataHolder)

}
