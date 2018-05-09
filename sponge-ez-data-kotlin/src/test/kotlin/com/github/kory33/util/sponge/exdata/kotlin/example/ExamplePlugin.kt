package com.github.kory33.util.sponge.exdata.kotlin.example

import com.github.kory33.util.sponge.exdata.kotlin.example.data.ImmutableStoneBreak
import com.github.kory33.util.sponge.exdata.kotlin.example.data.StoneBreak
import com.github.kory33.util.sponge.exdata.kotlin.example.data.StoneBreakBuilder
import com.github.kory33.util.sponge.ezdata.kotlin.buildPartialRegistration
import org.spongepowered.api.Sponge
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import javax.inject.Inject

@Plugin(id = ExamplePlugin.ID, name = "Ez-data Example Plugin")
class ExamplePlugin {

    @Inject lateinit var container: PluginContainer

    @Listener
    fun onInit(event: GameInitializationEvent) {
        val dataManager = Sponge.getDataManager()

        buildPartialRegistration(StoneBreak::class.java, ImmutableStoneBreak::class.java, StoneBreakBuilder())
                .buildAndRegister(container)
    }

    companion object {
        const val ID = "ezdataexampleplugin"
    }

}