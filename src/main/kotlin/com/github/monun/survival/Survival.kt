package com.github.monun.survival

import com.github.monun.tap.event.EntityEventManager
import com.github.monun.tap.fake.FakeEntityServer
import com.github.monun.tap.ref.UpstreamReference
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File
import java.util.*

class Survival(
    internal val entityEventManager: EntityEventManager,
    internal val fakeEntityServerForZombie: FakeEntityServer,
    internal val fakeEntityServerForHuman: FakeEntityServer,
    internal val playersFolder: File
) {
    companion object {
        private lateinit var instanceRef: UpstreamReference<Survival>
        val instance
            get() = instanceRef.get()
    }

    private val playerByPlayers = IdentityHashMap<Player, SurvivalPlayer>(Bukkit.getMaxPlayers())
    val players = Collections.unmodifiableCollection(playerByPlayers.values)

    init {
        instanceRef = UpstreamReference(this)
    }

    internal fun load() {
        Bukkit.getOnlinePlayers().forEach(this::loadPlayer)
    }

    internal fun unload() {
        Bukkit.getOnlinePlayers().forEach(this::unloadPlayer)
        instanceRef.clear()
    }

    internal fun loadPlayer(player: Player) {
        playerByPlayers.computeIfAbsent(player) {
            SurvivalPlayer(this, it).apply { load() }
        }
    }

    internal fun unloadPlayer(player: Player) {
        playerByPlayers.remove(player)?.unload()
    }

    fun getSurvivalPlayer(player: Player): SurvivalPlayer {
        return requireNotNull(playerByPlayers[player]) { " Unknown player ${player.name}" }
    }
}

fun Player.survival(): SurvivalPlayer {
    return Survival.instance.getSurvivalPlayer(this)
}