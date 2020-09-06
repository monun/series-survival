package com.github.noonmaru.sample.plugin

import com.github.noonmaru.tap.fake.FakeEntityServer
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Noonmaru
 */
open class SamplePlugin : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()

        //sample code
        val customPlayerManager = CustomPlayerManager()
        val fakeEntityServer = FakeEntityServer.create(this)
        val cursor = CustomCursor(customPlayerManager, fakeEntityServer)

        server.apply {
            pluginManager.registerEvents(cursor, this@SamplePlugin)
            scheduler.runTaskTimer(this@SamplePlugin, cursor, 0L, 1L)
        }
    }
}