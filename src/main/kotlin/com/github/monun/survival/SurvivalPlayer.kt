package com.github.monun.survival

import com.github.monun.tap.ref.UpstreamReference
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

class SurvivalPlayer(
    survival: Survival,
    player: Player
) {
    val uniqueId = player.uniqueId

    val name = player.name

    private val survivalRef = UpstreamReference(survival)
    val survival
        get() = survivalRef.get()

    private val playerRef = UpstreamReference(player)
    val player
        get() = playerRef.get()

    val file
        get() = File(survival.playersFolder, "$uniqueId.yml")

    lateinit var bio: Bio
        private set

    var valid = true
        private set

    companion object {
        private const val KEY_BIO = "bio"
    }

    internal fun load() {
        val file = file

        if (file.exists()) {
            val yaml = YamlConfiguration.loadConfiguration(file)
            yaml.getConfigurationSection(KEY_BIO)?.let { section ->
                Bio.runCatching {
                    load(section, this@SurvivalPlayer)
                }.onSuccess { bio ->
                    this.bio = bio.apply {
                        applyAttribute()
                        onAttach()
                    }
                }.onFailure { exception ->
                    exception.printStackTrace()
                }
            }
        }
        if (::bio.isInitialized) return

        val bioType = when (name) {
            in SurvivalConfig.defaultHumans -> {
                Bio.Type.HUMAN
            }
            in SurvivalConfig.defaultSuperZombies -> {
                Bio.Type.SUPER_ZOMBIE
            }
            else -> {
                Bio.Type.ZOMBIE
            }
        }
        setBio(bioType)
    }

    fun save() {
        val file = file.also { it.parentFile.mkdirs() }
        val yaml = YamlConfiguration()
        yaml.createSection(KEY_BIO).let { bio.save(it) }
        yaml.save(file)
    }

    internal fun unload() {
        save()
        bio.onDetach()
        valid = false
        playerRef.clear()
    }

    fun checkState() {
        require(valid) { "Invalid ${this.javaClass.simpleName}@${System.identityHashCode(this).toString(0x10)}" }
    }

    fun setBio(type: Bio.Type): Bio {
        checkState()

        if (::bio.isInitialized) {
            bio.onDetach()
        }

        val newBio = type.creator().apply {
            initialize(this@SurvivalPlayer)
            applyAttribute()
            onAttach()
        }
        bio = newBio

        save()

        return newBio
    }

    internal fun update() {
        bio.onUpdate()
    }
}