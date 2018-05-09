package com.github.kory33.util.sponge.exdata.kotlin.example.data

import com.github.kory33.util.sponge.ezdata.kotlin.manipulator.SingleKeyedValueData

class StoneBreak(value: Int): SingleKeyedValueData<Int, StoneBreak, ImmutableStoneBreak>(value, Keys.STONE_BREAK) {

    override fun asImmutable() = ImmutableStoneBreak(rawValue)

    override fun copy() = StoneBreak(rawValue)

    override fun getContentVersion() = CONTENT_VERSION

    companion object {
        const val CONTENT_VERSION = 1
    }

}