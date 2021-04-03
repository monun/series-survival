package com.github.monun.survival.plugin

import com.github.monun.kommand.kommand
import com.github.monun.survival.*
import com.github.monun.tap.effect.playFirework
import com.github.monun.tap.event.EntityEventManager
import com.github.monun.tap.fake.FakeEntityServer
import com.github.monun.tap.fake.invisible
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.File
import kotlin.random.Random.Default.nextInt

/**
 * @author Monun
 */
class SurvivalPlugin : JavaPlugin() {

    private lateinit var survival: Survival

    override fun onEnable() {
        val configFile = File(dataFolder, "config.yml")
        SurvivalConfig.load(configFile)
        Whitelist.load(File(dataFolder, "whitelist.txt"))

        setupRecipe()
        setupCommands()
        setupWorlds()

        val entityEventManager = EntityEventManager(this)
        val fakeEntityServerForZombie = FakeEntityServer.create(this)
        val fakeEntityServerForHuman = FakeEntityServer.create(this)

        survival = Survival(
            this,
            entityEventManager,
            fakeEntityServerForZombie,
            fakeEntityServerForHuman,
            File(dataFolder, "players")
        )
        survival.load()
        Bio.SuperZombie.initWarningStand(fakeEntityServerForHuman)

        server.apply {
            pluginManager.registerEvents(EventListener(survival), this@SurvivalPlugin)
            scheduler.runTaskTimer(
                this@SurvivalPlugin,
                TickTask(
                    logger,
                    configFile,
                    fakeEntityServerForZombie,
                    fakeEntityServerForHuman,
                    survival
                ), 0L, 1L
            )
        }
    }

    override fun onDisable() {
        if (::survival.isInitialized) {
            survival.unload()
        }
    }

    private fun setupCommands() {
        kommand {
            CommandSVL.register(this)
            register("thinking") {
                require {
                    this is Player && survival().bio is Bio.SuperZombie && gameMode != GameMode.SPECTATOR
                }
                executes {
                    val player = it.sender as Player

                    player.sendMessage(
                        text("살아남기 위해 고군분투했지만 슈퍼좀비가 되었다..\n하지만 좀비가 되어 Thinking을 해보니 인간은 사라져야해!").color(
                            TextColor.color(0x860707)
                        )
                    )
                    player.sendMessage(
                        text("인간을 말.살. 한다-").color(TextColor.color(0xFF0000))
                            .clickEvent(ClickEvent.runCommand("/evolve")).decorate(TextDecoration.BOLD)
                            .hoverEvent(text("하이퍼 좀비로 진화합니다\n하이퍼 좀비는 백신의 효과를 받을 수 없습니다.")))
                }
            }
            register("evolve") {
                require {
                    this is Player && survival().bio is Bio.SuperZombie && gameMode != GameMode.SPECTATOR
                }
                executes {
                    val sender = it.sender as Player
                    val loc = sender.location
                    loc.world.strikeLightningEffect(loc)

                    sender.survival().setBio(Bio.Type.HYPER_ZOMBIE)
                    Bukkit.getServer().sendMessage(
                        text("${sender.name}(이)가 하이퍼 좀비가 되었습니다!").color(TextColor.color(0xFF0000))
                            .decorate(TextDecoration.BOLD)
                    )
                    sender.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 30, 0, false, false))
                }
            }
            register("sacrifice") {
                require { this is Player && survival().bio is Bio.Human && gameMode != GameMode.SPECTATOR }
                executes {
                    val sender = it.sender as Player
                    val loc = sender.location.apply { y += 1.0 }
                    val world = loc.world
                    sender.gameMode = GameMode.SPECTATOR
                    FireworkEffect.builder().apply {
                        with(FireworkEffect.Type.STAR)
                        withColor(Color.fromRGB(nextInt(0xFFFFFF)))
                        withTrail()
                        withFlicker()
                    }.build().also { fireworkEffect ->
                        world.playFirework(loc, fireworkEffect)
                    }

                    world.spawn(loc, ArmorStand::class.java).apply {
                        customName = "${sender.name}의 흔적이다."
                        isCustomNameVisible = true
                        isMarker = true
                        invisible = true
                        isInvulnerable = true
                        setGravity(false)
                    }

                    if (nextInt(2) == 0) {
                        world.dropItem(loc, ItemStack(Material.NETHER_STAR))
                    } else {
                        world.spawn(loc.apply { y -= 0.25 }, ArmorStand::class.java).apply {
                            isCustomNameVisible = true
                            customName = "하지만 헛된 희생이었다."
                            isMarker = true
                            invisible = true
                            isInvulnerable = true
                            setGravity(false)
                        }
                    }
                }
            }
        }
    }

    private fun setupRecipe() {
        server.addRecipe(
            ShapelessRecipe(
                NamespacedKey.minecraft("greenjuice"),
                SurvivalItem.greenJuice
            ).apply {
                addIngredient(Material.ACACIA_SAPLING)
                addIngredient(Material.BIRCH_SAPLING)
                addIngredient(Material.DARK_OAK_SAPLING)
                addIngredient(Material.JUNGLE_SAPLING)
                addIngredient(Material.OAK_SAPLING)
                addIngredient(Material.SPRUCE_SAPLING)
            }
        )
        server.addRecipe(
            ShapedRecipe(
                NamespacedKey.minecraft("vaccine"),
                SurvivalItem.vaccine
            ).apply {
                shape(
                    " Z ",
                    "GEC",
                    " S "
                )
                setIngredient('Z', ItemStack(Material.ZOMBIE_HEAD))
                setIngredient('G', ItemStack(Material.GOLDEN_APPLE))
                setIngredient('E', ItemStack(Material.GLASS_BOTTLE))
                setIngredient('C', ItemStack(Material.GOLDEN_CARROT))
                setIngredient('S', ItemStack(SurvivalItem.greenJuice))
            }
        )
        server.addRecipe(
            ShapedRecipe(
                NamespacedKey.minecraft("hypervaccine"),
                SurvivalItem.hyperVaccine
            ).apply {
                shape(
                    "MG ",
                    "ASE",
                    " DC"
                )
                setIngredient('M', ItemStack(Material.MAGMA_BLOCK))
                setIngredient('G', ItemStack(Material.GOLD_BLOCK))
                setIngredient('A', ItemStack(Material.ANCIENT_DEBRIS))
                setIngredient('S', ItemStack(Material.NETHER_STAR))
                setIngredient('E', ItemStack(Material.EMERALD_BLOCK))
                setIngredient('D', ItemStack(Material.DIAMOND_BLOCK))
                setIngredient('C', ItemStack(Material.CRYING_OBSIDIAN))
            }
        )
    }

    private fun setupWorlds() {
        for (world in server.worlds) {
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
            if (world.name != "world_nether") // 네더일경우 멀리 갔을때 포탈 작용안함
                world.setGameRule(GameRule.REDUCED_DEBUG_INFO, true)
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true)
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false)
            world.setGameRule(GameRule.SPAWN_RADIUS, 2)
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
            world.setGameRule(GameRule.DO_FIRE_TICK, false)
        }

        //world, world_nether
        server.worlds.take(2).forEach { world ->
            world.worldBorder.apply {
                center = Location(world, 0.0, 0.0, 0.0)
                size = SurvivalConfig.worldSize
                damageAmount = 0.0
            }
        }
    }
}