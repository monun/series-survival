package com.github.noonmaru.sample.plugin

import org.bukkit.entity.Player

class CustomPlayer(player: Player) {
    val name: String = player.name
    val uniqueId = player.uniqueId
}