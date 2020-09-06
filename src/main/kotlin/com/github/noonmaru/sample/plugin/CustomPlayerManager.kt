package com.github.noonmaru.sample.plugin

import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap

class CustomPlayerManager {
    private val players = HashMap<UUID, CustomPlayer>()

    val customPlayers
        get() = players.values

    fun registerPlayer(player: Player): CustomPlayer {
        return players.computeIfAbsent(player.uniqueId) { CustomPlayer(player) }
    }

    fun unregisterPlayer(uniqueId: UUID): CustomPlayer? {
        return players.remove(uniqueId)
    }

    fun getCustomPlayer(uniqueId: UUID): CustomPlayer? {
        return players[uniqueId]
    }
}