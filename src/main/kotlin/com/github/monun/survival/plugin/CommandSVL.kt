package com.github.monun.survival.plugin

import com.github.monun.kommand.KommandDispatcherBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta

object CommandSVL {
    internal fun register(builder: KommandDispatcherBuilder) {
        builder.register("svl") {
            executes {
                val p = it.sender as Player
                p.inventory.addItem(
                    ItemStack(Material.COMPASS).apply {
                        itemMeta = (itemMeta as CompassMeta).apply {
                            lodestone = p.location
                            isLodestoneTracked = false
                        }
                    }
                )
            }
        }
    }
}