package com.github.monun.survival

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import kotlin.math.max

object Config {
    var bootsFallDamage = 4.0
    var compassDurationTime = 20000L
    var spectorDurationTick = 200
    var summonCooldownTick = 20 * 60 * 10
    var summonCount = 5
    var spectorCooldownTick = 20 * 60 * 1
    var compassCooldownTick = 20 * 60 * 30
    var worldSize = 4000.0
    var humanHealth = 20.0
    var zombieHealth = 40.0
    var superZombieHealth = 10.0
    var poisonDuration = 100
    var poisonAmplifier = 0
    var fatigueAmplifier = 0
    var defaultHumans: Set<String> = sortedSetOf(
        String.CASE_INSENSITIVE_ORDER,
        "heptagram",
        "Men1",
        "ehdgh141",
        "shalitz",
        "_BISE",
        "Ideon_97",
        "_nong_",
        "aringga",
        "youran6471",
        "j2_mong",
        "I_6ix",
        "ryu_96",
        "komq"
    )
    var defaultSuperZombies: Set<String> = sortedSetOf(
        String.CASE_INSENSITIVE_ORDER,
        "heptagram",
        "ehdgh141",
        "komq"
    )

    private const val WORLD_SIZE = "world-size"
    private const val HUMAN_HEALTH = "human-health"
    private const val ZOMBIE_HEALTH = "zombie-health"
    private const val SUPER_ZOMBIE_HEALTH = "super-zombie-health"
    private const val POISON_DURATION = "poison-duration"
    private const val POISON_AMPLIFIER = "poison-amplifier"
    private const val FATIGUE_AMPLIFIER = "fatigue-amplifier"
    private const val DEFAULT_HUMANS = "default-humans"
    private const val DEFAULT_SUPER_ZOMBIES = "default-super-zombies"

    fun load(file: File) {
        val config = YamlConfiguration()

        if (file.exists()) {
            config.load(file)
        } else {
            config[WORLD_SIZE] = 4000.0
            config[HUMAN_HEALTH] = 20.0
            config[ZOMBIE_HEALTH] = 40.0
            config[SUPER_ZOMBIE_HEALTH] = 10.0
            config[POISON_DURATION] = 100
            config[POISON_AMPLIFIER] = 0
            config[FATIGUE_AMPLIFIER] = 0
            config[DEFAULT_HUMANS] = arrayListOf(
                "heptagram",
                "Men1",
                "ehdgh141",
                "shalitz",
                "_BISE",
                "Ideon_97",
                "_nong_",
                "aringga",
                "youran6471",
                "j2_mong",
                "I_6ix",
                "ryu_96",
                "komq"
            )
            config[DEFAULT_SUPER_ZOMBIES] = arrayListOf(
                "heptagram",
                "ehdgh141",
                "komq"
            )

            config.save(file)
        }

        worldSize = max(256.0, config.getDouble(WORLD_SIZE))
        humanHealth = max(1.0, config.getDouble(HUMAN_HEALTH))
        zombieHealth = max(1.0, config.getDouble(ZOMBIE_HEALTH))
        superZombieHealth = max(1.0, config.getDouble(SUPER_ZOMBIE_HEALTH))
        poisonDuration = max(0, config.getInt(POISON_DURATION))
        poisonAmplifier = max(0, config.getInt(POISON_AMPLIFIER))
        defaultHumans = config.getStringList(DEFAULT_HUMANS).toSortedSet(String.CASE_INSENSITIVE_ORDER)
        defaultSuperZombies = config.getStringList(DEFAULT_SUPER_ZOMBIES).toSortedSet(String.CASE_INSENSITIVE_ORDER)
    }
}