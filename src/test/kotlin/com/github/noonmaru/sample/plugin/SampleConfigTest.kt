package com.github.noonmaru.sample.plugin

import com.github.noonmaru.sample.BukkitInitialization
import org.bukkit.configuration.file.YamlConfiguration
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.modules.junit4.PowerMockRunner
import java.io.File

@RunWith(PowerMockRunner::class)
@PowerMockIgnore(
    "org.apache.log4j.*",
    "org.apache.logging.*",
    "org.bukkit.craftbukkit.libs.jline.*",
    "com.sun.org.apache.xerces.internal.jaxp.*",
)
class SampleConfigTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun before() {
            BukkitInitialization.initialize()
        }
    }

    private lateinit var config: SampleConfig

    @Before
    fun setup() {
        val configFile = File(javaClass.getResource("/config.yml").file)
        val config = YamlConfiguration.loadConfiguration(configFile)

        this.config = SampleConfig(config)
    }

    @Test
    fun test() {
        assertEquals(13, config.numberValue13)
        assertEquals("Heptagram", config.stringValueHeptagram)
    }
}