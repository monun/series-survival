package com.github.noonmaru.sample.plugin

import com.github.noonmaru.tap.fake.FakeEntityServer
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class CustomCursor(
    private val customPlayerManager: CustomPlayerManager,
    private val fakeEntityServer: FakeEntityServer
) : Listener, Runnable {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        fakeEntityServer.addPlayer(player)
        customPlayerManager.registerPlayer(player).apply {
            cursor = fakeEntityServer.spawnEntity(cursorLocation, ArmorStand::class.java)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        customPlayerManager.unregisterPlayer(event.player.uniqueId)?.cursor?.remove()
    }

    override fun run() {
        for (customPlayer in customPlayerManager.customPlayers) {
            customPlayer.cursor.moveTo(customPlayer.cursorLocation)
        }

        fakeEntityServer.update()
    }
}