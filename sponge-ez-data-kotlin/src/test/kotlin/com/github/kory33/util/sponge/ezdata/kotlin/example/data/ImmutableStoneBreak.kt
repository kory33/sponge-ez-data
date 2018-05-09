package com.github.kory33.util.sponge.ezdata.kotlin.example.data

import com.github.kory33.util.sponge.ezdata.kotlin.manipulator.ImmutableSingleKeyedValueData

class ImmutableStoneBreak(value: Int):
        ImmutableSingleKeyedValueData<Int, ImmutableStoneBreak, StoneBreak>(value, Keys.STONE_BREAK) {

    override fun getContentVersion() = StoneBreak.CONTENT_VERSION

    override fun asMutable() = StoneBreak(rawValue)

}