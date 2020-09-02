package com.github.noonmaru.sample.plugin

import org.bukkit.configuration.file.YamlConfiguration

class SampleConfig(config: YamlConfiguration) {
    val numberValue13: Int
    val stringValueHeptagram: String

    init {
        numberValue13 = config.getInt("number")
        stringValueHeptagram = requireNotNull(config.getString("string"))
    }
}