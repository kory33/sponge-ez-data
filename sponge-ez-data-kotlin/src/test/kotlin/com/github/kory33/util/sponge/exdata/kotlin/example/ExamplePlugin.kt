package com.github.kory33.util.sponge.exdata.kotlin.example

import com.github.kory33.util.sponge.exdata.kotlin.example.data.ImmutableStoneBreak
import com.github.kory33.util.sponge.exdata.kotlin.example.data.StoneBreak
import com.github.kory33.util.sponge.exdata.kotlin.example.data.StoneBreakBuilder
import com.github.kory33.util.sponge.ezdata.kotlin.buildPartialRegistration
import com.github.kory33.util.sponge.ezdata.kotlin.extensions.transformSingleValue
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.Order
import org.spongepowered.api.event.block.ChangeBlockEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import javax.inject.Inject

@Plugin(id = ExamplePlugin.ID, name = "Ez-data Example Plugin")
class ExamplePlugin {

    @Inject lateinit var container: PluginContainer

    @Listener
    fun onInit(event: GameInitializationEvent) {
        buildPartialRegistration(StoneBreak::class.java, ImmutableStoneBreak::class.java, StoneBreakBuilder())
                .buildAndRegister(container)
    }

    @Listener(order=Order.POST)
    fun onStoneBreak(event: ChangeBlockEvent.Break) {
        val causePlayer = event.cause.mapNotNull { it as? Player }.firstOrNull() ?: return

        val brokeStoneNumber = event.transactions
                .filter { it.isValid && it.original.state.type == BlockTypes.STONE }
                .size

        causePlayer.transformSingleValue<Int, StoneBreak> {
            val result = it + brokeStoneNumber
            result.also { println(it) }
        }
   }

    companion object {
        const val ID = "ezdataexampleplugin"
    }

}