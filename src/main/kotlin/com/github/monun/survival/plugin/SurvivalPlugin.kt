package com.github.monun.survival.plugin

import com.github.monun.kommand.kommand
import com.github.monun.survival.SurvivalConfig
import com.github.monun.survival.Survival
import com.github.monun.survival.SurvivalItem
import com.github.monun.tap.event.EntityEventManager
import com.github.monun.tap.fake.FakeEntityServer
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * @author Monun
 */
class SurvivalPlugin : JavaPlugin() {

    private lateinit var survival: Survival

    override fun onEnable() {
        val configFile = File(dataFolder, "config.yml")
        SurvivalConfig.load(configFile)

        setupRecipe()
        setupCommands()
        setupWorlds()

        val entityEventManager = EntityEventManager(this)
        val fakeEntityServerForZombie = FakeEntityServer.create(this)
        val fakeEntityServerForHuman = FakeEntityServer.create(this)

        survival = Survival(
            entityEventManager,
            fakeEntityServerForZombie,
            fakeEntityServerForHuman,
            File(dataFolder, "players")
        )
        survival.load()

        server.apply {
            pluginManager.registerEvents(EventListener(survival), this@SurvivalPlugin)
            scheduler.runTaskTimer(
                this@SurvivalPlugin,
                TickTask(
                    logger,
                    configFile,
                    fakeEntityServerForZombie,
                    fakeEntityServerForHuman,
                    survival
                ), 0L, 1L
            )
        }
    }

    override fun onDisable() {
        if (::survival.isInitialized) {
            survival.unload()
        }
    }

    private fun setupCommands() {
        kommand {
            CommandSVL.register(this)
        }
    }

    private fun setupRecipe() {
        server.addRecipe(
            ShapedRecipe(
                NamespacedKey.minecraft("vaccine"),
                SurvivalItem.vaccine
            ).apply {
                shape(
                    "ABC",
                    "DEF",
                    "GHI"
                )
                setIngredient('A', Material.GLISTERING_MELON_SLICE)
                setIngredient('B', Material.GOLDEN_CARROT)
                setIngredient('C', Material.GOLDEN_APPLE)
                setIngredient('D', Material.SLIME_BALL)
                setIngredient('E', Material.GLASS_BOTTLE)
                setIngredient('F', Material.MAGMA_CREAM)
                setIngredient('G', Material.RABBIT_FOOT)
                setIngredient('H', Material.NAUTILUS_SHELL)
                setIngredient('I', Material.PHANTOM_MEMBRANE)
            }
        )
    }

    private fun setupWorlds() {
        for (world in server.worlds) {
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
            world.setGameRule(GameRule.REDUCED_DEBUG_INFO, true)
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true)
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false)
            world.setGameRule(GameRule.SPAWN_RADIUS, 5)
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
            world.setGameRule(GameRule.DO_FIRE_TICK, false)
        }

        //world, world_nether
        server.worlds.take(2).forEach { world ->
            world.worldBorder.apply {
                center = Location(world, 0.0, 0.0, 0.0)
                size = SurvivalConfig.worldSize
                damageAmount = 0.0
            }
        }
    }
}