package com.github.noonmaru.sample.plugin

import org.bukkit.entity.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.reflect.Whitebox
import java.util.*

@RunWith(PowerMockRunner::class)
class CustomPlayerManagerTest {
    private val uniqueId = UUID.randomUUID()
    private val name = "Noonmaru"

    private lateinit var player: Player
    private lateinit var customPlayerManager: CustomPlayerManager

    @Before
    fun setup() {
        val player = Mockito.mock(Player::class.java)
        Mockito.`when`(player.uniqueId).thenReturn(uniqueId)
        Mockito.`when`(player.name).thenReturn(name)

        this.player = player
        this.customPlayerManager = CustomPlayerManager()
    }

    @Test
    fun test() {
        val privateMap = Whitebox.getInternalState<HashMap<UUID, CustomPlayer>>(customPlayerManager, "players")

        val customPlayer = customPlayerManager.registerPlayer(this.player)

        assertEquals(1, privateMap.count())
        assertEquals(customPlayerManager.getCustomPlayer(player.uniqueId), customPlayer)
        assertEquals(customPlayer, privateMap.values.first())

        val removeCustomPlayer = customPlayerManager.unregisterPlayer(player.uniqueId)

        assertEquals(customPlayer, removeCustomPlayer)
        assertTrue(privateMap.isEmpty())
    }
}