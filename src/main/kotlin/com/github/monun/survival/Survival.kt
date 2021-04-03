package com.github.monun.survival

import com.github.monun.survival.plugin.SurvivalPlugin
import com.github.monun.tap.event.EntityEventManager
import com.github.monun.tap.fake.FakeEntityServer
import com.github.monun.tap.ref.UpstreamReference
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.EntityEffect
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.io.File
import java.util.*
import kotlin.random.Random.Default.nextFloat

class Survival(
    internal val plugin: SurvivalPlugin,
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
        fakeEntityServerForZombie.shutdown()
        fakeEntityServerForHuman.shutdown()
        entityEventManager.unregisterAll()
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

    fun playHyperVaccine(loc: Location) {
        val task = HyperVaccine(loc)

        task.task = plugin.server.scheduler.runTaskTimer(plugin, task, 60L, 1L)
    }
}

fun Player.survival(): SurvivalPlayer {
    return Survival.instance.getSurvivalPlayer(this)
}

class HyperVaccine(private val center: Location) : Runnable {
    lateinit var task: BukkitTask

    private var ticks = 0

    override fun run() {
        if (ticks++ < 200) {
            val world = center.world
            for (i in 0 until 5) {

                world.spawn(center, Firework::class.java).apply {
                    center.yaw = nextFloat() * 360.0F
                    center.pitch = -45F + nextFloat() * -45.0F
                    fireworkMeta = fireworkMeta.apply {
                        power = 10
                    }
                    velocity = center.direction.multiply(1.5)
                }
            }
            return
        }

        if (ticks % 4 == 0) {
            val zombie = Bukkit.getOnlinePlayers().asSequence().map { it.survival() }
                .find { it.bio is Bio.Zombie && it.bio !is Bio.HyperZombie }

            if (zombie != null) {
                zombie.player.playEffect(EntityEffect.TOTEM_RESURRECT)
                zombie.setBio(Bio.Type.HUMAN)
                Bukkit.getServer().sendMessage(text("${zombie.name}(이)가 좀비 바이러스로부터 해방되었습니다!"))
                return
            }

            Bukkit.getOnlinePlayers().asSequence().map { it.survival() }
                .find { it.player.gameMode != GameMode.SPECTATOR && it.bio is Bio.HyperZombie }
                ?.let { hyperZombie ->
                    hyperZombie.player.playEffect(EntityEffect.TOTEM_RESURRECT)
                    hyperZombie.player.gameMode = GameMode.SPECTATOR
                    Bukkit.getServer()
                        .sendMessage(text("${hyperZombie.name}(이)가 소멸되었습니다!").color(TextColor.color(0xFF0000)))
                    return
                }

            task.cancel()
        }
    }
}