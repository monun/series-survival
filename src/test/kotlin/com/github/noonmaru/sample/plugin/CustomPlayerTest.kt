package com.github.noonmaru.sample.plugin

import org.bukkit.Location
import org.bukkit.entity.Player
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.powermock.modules.junit4.PowerMockRunner
import java.util.*

@RunWith(PowerMockRunner::class)
class CustomPlayerTest {
    private val uniqueId = UUID.randomUUID()
    private val name = "Noonmaru"
    private val location = Location(null, 0.0, 0.0, 0.0, 45.0F, 45.0F)

    private lateinit var customPlayer: CustomPlayer

    @Before
    fun setup() {
        val player = Mockito.mock(Player::class.java)
        Mockito.`when`(player.uniqueId).thenReturn(uniqueId)
        Mockito.`when`(player.name).thenReturn(name)
        Mockito.`when`(player.eyeLocation).thenReturn(location.clone())

        customPlayer = CustomPlayer(player)
    }

    @Test
    fun test() {
        val customPlayer = customPlayer

        assertEquals(uniqueId, customPlayer.uniqueId)
        assertEquals(name, customPlayer.name)
        assertEquals(location.clone().apply { add(direction.multiply(8.0)) }, customPlayer.cursorLocation)
    }
}