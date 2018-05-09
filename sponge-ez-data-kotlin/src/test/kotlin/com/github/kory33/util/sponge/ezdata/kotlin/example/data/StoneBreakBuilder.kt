package com.github.kory33.util.sponge.ezdata.kotlin.example.data

import com.github.kory33.util.sponge.ezdata.kotlin.manipulator.KeyedValueManipulatorBuilder

class StoneBreakBuilder:
        KeyedValueManipulatorBuilder<StoneBreak, ImmutableStoneBreak>(
                StoneBreak::class.java,
                StoneBreak.CONTENT_VERSION
        ) {

    override val requiredKeys = setOf(Keys.STONE_BREAK)

    override fun create() = StoneBreak(0)

}