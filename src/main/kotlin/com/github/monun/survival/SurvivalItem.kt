package com.github.monun.survival

import net.kyori.adventure.text.Component
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object SurvivalItem {
    val vaccine = ItemStack(Material.TOTEM_OF_UNDYING).apply {
        itemMeta = itemMeta.apply {
            displayName(Component.text("${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}백신"))
            lore(
                listOf(
                    Component.text("${ChatColor.GRAY}좀비를 우클릭하여 바이러스로부터 해방시키세요!")
                )
            )
        }
    }

    val wandNavigate = ItemStack(Material.GOLD_INGOT)

    val wandSpector = ItemStack(Material.DIAMOND)

    val wandSummon = ItemStack(Material.EMERALD)
}