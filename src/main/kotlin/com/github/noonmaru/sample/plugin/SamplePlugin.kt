package com.github.noonmaru.sample.plugin

import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Noonmaru
 */
open class SamplePlugin : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()

        logger.info("Hello Kotlin Plugin!")
    }
}