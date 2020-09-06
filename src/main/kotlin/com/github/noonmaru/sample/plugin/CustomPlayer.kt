package com.github.noonmaru.sample.plugin

import com.github.noonmaru.tap.fake.FakeEntity
import com.github.noonmaru.tap.ref.UpstreamReference
import org.bukkit.Location
import org.bukkit.entity.Player

class CustomPlayer(
    player: Player
) {
    private val playerRef = UpstreamReference(player)

    val player
        get() = playerRef.get()

    val name: String = player.name
    val uniqueId = player.uniqueId

    lateinit var cursor: FakeEntity

    val cursorLocation: Location
        get() {
            return player.eyeLocation.apply {
                add(direction.multiply(8.0))
            }
        }
}