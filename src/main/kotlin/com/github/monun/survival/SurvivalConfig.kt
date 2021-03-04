package com.github.monun.survival

import com.github.monun.tap.config.Config
import com.github.monun.tap.config.computeConfig
import java.io.File

object SurvivalConfig {
    var bootsFallSlow = 4
    @Config
    var bootsFallDamage = 6.0
    @Config
    var compassDurationTime = 20000L
    @Config
    var spectorDurationTick = 200
    @Config
    var summonCooldownTick = 20 * 60 * 10
    @Config
    var summonCount = 5
    @Config
    var spectorCooldownTick = 20 * 60 * 1
    @Config
    var compassCooldownTick = 20 * 60 * 30
    @Config
    var worldSize = 4000.0
    @Config
    var humanHealth = 20.0
    @Config
    var zombieHealth = 40.0
    @Config
    var superZombieHealth = 10.0
    @Config
    var poisonDuration = 100
    @Config
    var poisonAmplifier = 0
    @Config
    var fatigueAmplifier = 0
    @Config
    var zombieDamage = 0.5
    @Config
    var defaultHumanList = arrayListOf(
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
    @Config
    var defaultSuperZombieList = arrayListOf(
        "heptagram",
        "ehdgh141",
        "komq"
    )

    lateinit var defaultHumans: Set<String>
    lateinit var defaultSuperZombies: Set<String>

    fun load(configFile: File) {
        computeConfig(configFile)

        defaultHumans = defaultHumanList.toSortedSet(String.CASE_INSENSITIVE_ORDER)
        defaultSuperZombies = defaultSuperZombieList.toSortedSet(String.CASE_INSENSITIVE_ORDER)
    }
}