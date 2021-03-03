package com.github.monun.survival.plugin

import com.github.monun.kommand.KommandContext
import com.github.monun.kommand.KommandDispatcherBuilder
import com.github.monun.kommand.argument.KommandArgument
import com.github.monun.kommand.argument.player
import com.github.monun.kommand.argument.suggestions
import com.github.monun.kommand.sendFeedback
import com.github.monun.survival.Bio
import com.github.monun.survival.SurvivalItem
import com.github.monun.survival.survival
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie

object CommandSVL {
    internal fun register(builder: KommandDispatcherBuilder) {
        builder.register("svl") {
            then("attach") {
                then("player" to player(), "bio" to BioArgument) {
                    executes { context ->
                        attach(context.sender, context.parseArgument("player"), context.parseArgument("bio"))
                    }
                }
            }
            then("vaccine") {
                require { this is Player }
                executes { context ->
                    vaccine(context.sender, context.sender as Player)
                }
                then("player" to player()) {
                    executes { context ->
                        vaccine(context.sender, context.parseArgument("player"))
                    }
                }
            }
            then("resetcooldown") {
                require { this is Player }
                executes { context ->
                    resetCooldown(context.sender, context.sender as Player)
                }
            }
        }
    }

    private fun attach(sender: CommandSender, player: Player, bio: Bio.Type) {
        player.survival().setBio(bio)
        sender.sendFeedback("${player.name} = ${bio.displayName}")
    }

    private fun vaccine(sender: CommandSender, player: Player) {
        player.inventory.addItem(SurvivalItem.vaccine.clone())
        sender.sendFeedback("백신을 지급했습니다.")
    }

    private fun resetCooldown(sender: CommandSender, player: Player) {
        val survival = player.survival()
        val bio = survival.bio

        if (bio is Bio.Zombie) {
            bio.resetCooldown()
        }
    }
}

object BioArgument : KommandArgument<Bio.Type> {
    override fun parse(context: KommandContext, param: String): Bio.Type? {
        return Bio.Type.byKey(param)
    }

    override fun listSuggestion(context: KommandContext, target: String): Collection<String> {
        return Bio.Type.values().asList().suggestions(target) { it.key }
    }
}