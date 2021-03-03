package com.github.monun.survival

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent
import com.github.monun.tap.effect.playFirework
import com.github.monun.tap.event.EntityProvider
import com.github.monun.tap.event.RegisteredEntityListener
import com.github.monun.tap.event.TargetEntity
import com.github.monun.tap.fake.FakeEntity
import com.github.monun.tap.ref.UpstreamReference
import com.google.common.collect.ImmutableSortedMap
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.title.Title
import net.md_5.bungee.api.ChatColor
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team
import org.bukkit.util.EulerAngle
import java.time.Duration
import java.util.*
import kotlin.random.Random.Default.nextInt

abstract class Bio(
    val type: Type
) {
    enum class Type(
        val key: String,
        val displayName: String,
        val creator: () -> Bio
    ) {
        HUMAN("human", "생존자", ::Human),
        ZOMBIE("zombie", "좀비", ::Zombie),
        SUPER_ZOMBIE("super_zombie", "슈퍼 좀비", ::SuperZombie);

        override fun toString(): String {
            return displayName
        }

        companion object {
            private val BY_KEY: Map<String, Type>

            init {
                BY_KEY =
                    values().associateByTo(TreeMap<String, Type>(String.CASE_INSENSITIVE_ORDER)) { type -> type.key }
                        .let { map ->
                            ImmutableSortedMap.copyOf(map)
                        }
            }

            fun byKey(key: String): Type? {
                return BY_KEY[key]
            }
        }
    }

    companion object {
        private const val KEY_TYPE = "type"

        fun load(config: ConfigurationSection, player: SurvivalPlayer): Bio {
            val key = requireNotNull(config.getString(KEY_TYPE)) { " Unknown Bio key $KEY_TYPE" }
            val type = requireNotNull(Type.byKey(key)) { "Unknown type key $key" }

            return type.creator().apply {
                initialize(player)
                onLoad(config)
            }
        }
    }

    private lateinit var survivorRef: UpstreamReference<SurvivalPlayer>
    val survivor: SurvivalPlayer
        get() = survivorRef.get()
    val player: Player
        get() = survivor.player

    abstract val team: Team

    internal fun initialize(player: SurvivalPlayer) {
        survivorRef = UpstreamReference(player)
        team.addEntry(player.name)

        runCatching { onInitialize() }
    }

    protected open fun onInitialize() {}

    internal open fun onAttach() {}

    internal open fun onDetach() {}

    internal open fun onUpdate() {}

    internal open fun applyAttribute() {}

    protected open fun onLoad(config: ConfigurationSection) {}

    fun save(config: ConfigurationSection) {
        config[KEY_TYPE] = type.key
        onSave(config)
    }

    protected open fun onSave(config: ConfigurationSection) {}

    class Human : Bio(Type.HUMAN), Listener {
        private var zombieFlag: FakeEntity? = null

        private val flagLocation: Location
            get() = player.eyeLocation

        private var registereListener: RegisteredEntityListener? = null

        override val team: Team
            get() {
                val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
                return scoreboard.getTeam("human") ?: scoreboard.registerNewTeam("human").apply {
                    displayName(Component.text("HUMAN"))
                    color(NamedTextColor.WHITE)
                    setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS)
                    setCanSeeFriendlyInvisibles(false)
                    setAllowFriendlyFire(true)
                }
            }

        override fun onAttach() {
            val player = player
            val survival = survivor.survival
            val forZombie = survival.fakeEntityServerForZombie

            zombieFlag = forZombie.spawnEntity(flagLocation, ArmorStand::class.java).apply {
                updateMetadata<ArmorStand> {
                    isMarker = true
                    isInvisible = true
                    isGlowing = true
                    headPose = EulerAngle.ZERO

                }
                updateEquipment {
                    helmet = ItemStack(Material.ZOMBIE_HEAD)
                }
            }

            survival.fakeEntityServerForHuman.addPlayer(player)
            registereListener = survival.entityEventManager.registerEvents(player, this)
        }

        override fun onDetach() {
            zombieFlag?.run {
                remove()
                zombieFlag = null
            }

            survivor.survival.fakeEntityServerForHuman.removePlayer(player)
            registereListener?.unregister()
        }

        override fun onUpdate() {
            zombieFlag?.moveTo(flagLocation)
        }

        override fun applyAttribute() {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = SurvivalConfig.humanHealth
        }

        @EventHandler
        fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
            val player = player
            val item = player.inventory.itemInMainHand

            if (item.type == Material.TOTEM_OF_UNDYING) {
                val clicked = event.rightClicked

                if (clicked is Player) {
                    val survivor = clicked.survival()

                    if (survivor.bio is Zombie) {
                        item.amount -= 1
                        survivor.setBio(Type.HUMAN)
                        clicked.playEffect(EntityEffect.TOTEM_RESURRECT)
                        player.sendMessage(
                            Component.text().color(TextColor.color(0x00FFFF)).content("${clicked.name}님을 치료했습니다!")
                        )
                        clicked.sendMessage(
                            Component.text().color(TextColor.color(0x00FFFF)).content("좀비 바이러스가 치료되었습니다!")
                        )
                        val title = Title.title(
                            Component.text().color(TextColor.color(0x00FFFF)).content(clicked.name).build(),
                            Component.text().color(TextColor.color(0x00FFFF)).content("좀비 바이러스로부터 해방되었습니다!").build(),
                            Title.Times.of(Duration.ofMillis(500), Duration.ofSeconds(5), Duration.ofMillis(500))
                        )
                        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                            onlinePlayer.showTitle(title)
                        }
                    }
                }
            }
        }

        @EventHandler
        fun onPlayerDeath(event: PlayerDeathEvent) {
            val name = player.name
            val newBioType = if (name in SurvivalConfig.defaultSuperZombies) Type.SUPER_ZOMBIE else Type.ZOMBIE

            survivor.setBio(newBioType)

            val message = Component.text("${ChatColor.RED}${player.name}님이 ${newBioType.displayName}가 되었습니다!")
            val title = Title.title(
                Component.text("${ChatColor.RED}생존자 사망"),
                message,
                Title.Times.of(Duration.ofMillis(500L), Duration.ofSeconds(5L), Duration.ofSeconds(1))
            )

            Bukkit.getOnlinePlayers().forEach {
                it.sendMessage(message)
                it.showTitle(title)
            }
        }
    }

    open class Zombie(type: Type = Type.ZOMBIE) : Bio(type), Listener {
        internal var ticks = 0
        private var registeredListener: RegisteredEntityListener? = null
        private val wandHandlers = ArrayList<Pair<ItemStack, (PlayerInteractEvent) -> Unit>>()

        override val team: Team
            get() {
                val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
                return scoreboard.getTeam("zombie") ?: scoreboard.registerNewTeam("zombie").apply {
                    displayName(Component.text("ZOMBIE"))
                    color(NamedTextColor.RED)
                    setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS)
                    setCanSeeFriendlyInvisibles(true)
                    setAllowFriendlyFire(false)
                }
            }

        protected fun registerWandHandler(itemStack: ItemStack, handler: (event: PlayerInteractEvent) -> Unit) {
            wandHandlers.add(itemStack to handler)
        }

        override fun onInitialize() {
            registerWandHandler(SurvivalItem.wandNavigate) { event ->
                val randomSuperZombie =
                    survivor.survival.players.asSequence().filter { it.bio is SuperZombie && it != survivor }.toList()
                        .randomOrNull()

                if (randomSuperZombie != null) {
                    event.item!!.run {
                        amount -= 1
                        player.setCooldown(type, SurvivalConfig.compassCooldownTick)
                    }
                    val p = randomSuperZombie.player
                    p.sendMessage(
                        Component.text("위치 추적기가 도착했습니다.")
                    )
                    p.world.dropItemNaturally(p.eyeLocation, ItemStack(Material.COMPASS).apply {
                        itemMeta = (itemMeta as CompassMeta).apply {
                            displayName(Component.text(survivor.name))
                            lore(listOf(Component.text(System.currentTimeMillis())))
                            isLodestoneTracked = false
                            lodestone = player.location
                        }
                    })
                }
            }
        }

        override fun onAttach() {
            val player = player
            survivor.survival.let { survival ->
                survival.fakeEntityServerForZombie.addPlayer(player)
                registeredListener = survival.entityEventManager.registerEvents(player, this)
            }
        }

        override fun onDetach() {
            registeredListener?.run {
                unregister()
                registeredListener = null
            }

            val player = player

            for (type in PotionEffectType.values()) {
                player.removePotionEffect(type)
            }

            for (wandHandler in wandHandlers) {
                player.setCooldown(wandHandler.first.type, 0)
            }

            survivor.survival.fakeEntityServerForZombie.removePlayer(player)


        }

        override fun onLoad(config: ConfigurationSection) {
            config.getConfigurationSection("cooldown")?.let { section ->
                for (wandHandler in wandHandlers) {
                    val type = wandHandler.first.type
                    player.setCooldown(type, section.getInt(type.name))
                }
            }
        }

        override fun onSave(config: ConfigurationSection) {
            config.createSection("cooldown").let { section ->
                for (wandHandler in wandHandlers) {
                    val type = wandHandler.first.type
                    section[type.name] = player.getCooldown(type)
                }
            }
        }

        override fun onUpdate() {
            ticks++

            if (ticks % 20 == 0) {
                player.apply {
                    addPotionEffect(
                        PotionEffect(
                            PotionEffectType.POISON,
                            100,
                            0,
                            false,
                            false,
                            false
                        )
                    )
                    addPotionEffect(
                        PotionEffect(
                            PotionEffectType.SLOW_DIGGING,
                            100,
                            SurvivalConfig.fatigueAmplifier,
                            false,
                            false,
                            false
                        )
                    )
                }
            }
        }

        override fun applyAttribute() {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = SurvivalConfig.zombieHealth
        }

        @EventHandler(ignoreCancelled = true)
        fun onEntityDamage(event: EntityDamageEvent) {
            if (event.cause == EntityDamageEvent.DamageCause.POISON) {
                event.isCancelled = true
            }
        }

        @EventHandler(ignoreCancelled = true)
        @TargetEntity(EntityProvider.EntityDamageByEntity.Damager::class)
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (SurvivalConfig.poisonDuration <= 0) return
            val victim = event.entity; if (victim !is LivingEntity) return

            if (victim is Player) {
                val survivor = victim.survival()

                if (survivor.bio is Zombie) {
                    event.isCancelled = true
                    return
                }
            }

            event.damage /= SurvivalConfig.zombieDamage

            victim.addPotionEffect(
                PotionEffect(
                    PotionEffectType.POISON,
                    SurvivalConfig.poisonDuration,
                    SurvivalConfig.poisonAmplifier,
                    true,
                    true,
                    true
                )
            )
        }

        @EventHandler(ignoreCancelled = true)
        fun onInventoryOpen(event: InventoryOpenEvent) {
            if (event.inventory.type == InventoryType.WORKBENCH) {
                event.isCancelled = true
            }
        }

        @EventHandler
        fun onFoodLevelChange(event: FoodLevelChangeEvent) {
            event.foodLevel = 20
        }

        @EventHandler(ignoreCancelled = true)
        @TargetEntity(TargetProvider::class)
        fun onEntityTarget(event: EntityTargetLivingEntityEvent) {
            val entity = event.entity; if (entity is Animals) return

            if (entity is LivingEntity) {
                if (entity.killer != player) {
                    event.isCancelled = true
                }
            }
        }

        @EventHandler(ignoreCancelled = true)
        fun onRegainHealth(event: EntityRegainHealthEvent) {
            if (player.killer != null) {
                event.isCancelled = true
            }
        }

        @EventHandler
        fun onPlayerInteract(event: PlayerInteractEvent) {
            val item = event.item ?: return
            val action = event.action

            if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && player.getCooldown(item.type) <= 0) {
                wandHandlers.find { it.first.isSimilar(item) }?.let { found ->
                    found.second(event)
                }
            }
        }

        @EventHandler(ignoreCancelled = true)
        @TargetEntity(TamerProvider::class)
        fun onEntityTame(event: EntityTameEvent) {
            event.isCancelled = true
        }

        @EventHandler(ignoreCancelled = true)
        fun onPickupItem(event: PlayerAttemptPickupItemEvent) {
            if (event.item.itemStack.type == Material.TOTEM_OF_UNDYING) {
                event.isCancelled = true
            }
        }

        fun resetCooldown() {
            val player = player

            for (wandHandler in wandHandlers) {
                player.setCooldown(wandHandler.first.type, 0)
            }
        }
    }

    class SuperZombie : Zombie(Type.SUPER_ZOMBIE) {
        private var summonYaw = 0F
        private var summonTicks = 0
        private var summons = arrayListOf<SurvivalPlayer>()
        private var spectorTicks = 0
        private var spectorLocation: Location? = null

        override fun onInitialize() {
            super.onInitialize()

            registerWandHandler(SurvivalItem.wandSpector) { event ->
                survivor.survival.players.filter { it != survivor && it.bio is Human }.randomOrNull()?.let { target ->
                    val player = player
                    spectorTicks = SurvivalConfig.spectorDurationTick
                    spectorLocation = player.location
                    player.gameMode = GameMode.SPECTATOR
                    player.teleport(target.player)
                    player.spectatorTarget = target.player

                    event.item!!.apply {
                        amount -= 1
                        player.setCooldown(type, SurvivalConfig.spectorCooldownTick)
                    }
                }
            }

            registerWandHandler(SurvivalItem.wandSummon) { event ->
                if (player.world.name.endsWith("nether")) return@registerWandHandler

                survivor.survival.players.asSequence()
                    .filter { it.bio is Zombie && it != survivor && it.player.gameMode != GameMode.SPECTATOR }
                    .sortedBy { other ->
                        val a = player
                        val b = other.player
                        if (a.world != b.world) Double.POSITIVE_INFINITY
                        else a.location.distanceSquared(b.location)
                    }.take(SurvivalConfig.summonCount).toList().takeIf { it.isNotEmpty() }?.let { list ->
                    summonTicks = 0
                    summons.addAll(list)
                    summonYaw = 360.0F / summons.count()

                    val title = Title.title(
                        Component.text("${ChatColor.RED}GRRRR.."),
                        Component.text("${ChatColor.RESET}${player.name}(이)가 당신을 소환하려합니다!"),
                        Title.Times.of(Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofSeconds(1))
                    )

                    for (survivalPlayer in list) {
                        survivalPlayer.player.showTitle(title)
                    }

                    event.item!!.apply {
                        amount -= 1
                        val player = player
                        player.setCooldown(type, SurvivalConfig.summonCooldownTick)
                        player.world.strikeLightningEffect(player.location)
                    }
                }
            }
        }

        override fun onDetach() {
            super.onDetach()

            summonTicks = 0
            summons.clear()

            spectorLocation?.let { loc ->
                spectorLocation = null
                player.teleport(loc)
                player.gameMode = GameMode.SURVIVAL
            }
        }

        override fun onUpdate() {
            super.onUpdate()

            val player = this.player

            if (ticks % 20 == 0) {
                player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 100, 0, false, false, false))
            }

            if (summons.isNotEmpty()) {
                summonTicks++

                if (summonTicks > 60) {
                    if (summonTicks % 4 == 0) {
                        val summon = summons.removeLast()

                        if (summon.valid) {
                            val loc = player.location.run {
                                yaw = summons.count() * summonYaw
                                add(direction.multiply(16.0))
                                world.getHighestBlockAt(this).location.add(0.5, 1.0, 0.5)
                            }

                            summon.player.teleport(loc)
                            loc.world.playFirework(
                                loc.add(0.0, 1.0, 0.0),
                                FireworkEffect.builder().withColor(Color.fromRGB(nextInt(0xFFFFFF)))
                                    .with(FireworkEffect.Type.BALL_LARGE).build()
                            )
                        }
                    }
                }
            }

            spectorLocation?.let { loc ->
                if (--spectorTicks <= 0) {
                    spectorLocation = null
                    player.teleport(loc)
                    player.gameMode = GameMode.SURVIVAL
                }
            }

            val inv = player.inventory

            for (i in 0 until 36) {
                val item = inv.getItem(i)

                if (item != null && item.type == Material.COMPASS && item.hasItemMeta()) {
                    val meta = item.itemMeta as CompassMeta
                    val displayName = meta.displayName() ?: continue
                    val lore = meta.lore()?.firstOrNull() ?: continue
                    val removeTime = (lore as TextComponent).content().toLong() + SurvivalConfig.compassDurationTime

                    if (removeTime < System.currentTimeMillis()) {
                        item.amount = 0
                    }

                    val targetName = (displayName as TextComponent).content()
                    val target = Bukkit.getPlayerExact(targetName) ?: continue
                    meta.lodestone = target.location
                    meta.isLodestoneTracked = false
                    item.itemMeta = meta
                }
            }
        }

        override fun applyAttribute() {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = SurvivalConfig.superZombieHealth
        }

        @EventHandler(ignoreCancelled = true)
        fun onPlayerStopSpectatingEntity(event: PlayerStopSpectatingEntityEvent) {
            if (this.spectorLocation != null) {
                event.isCancelled = true
            }
        }

        @EventHandler(ignoreCancelled = true)
        fun onPlayerTeleport(event: PlayerTeleportEvent) {
            if (spectorLocation != null && event.cause == PlayerTeleportEvent.TeleportCause.SPECTATE) {
                event.isCancelled = true
            }
        }
    }
}

class TargetProvider : EntityProvider<EntityTargetLivingEntityEvent> {
    override fun getFrom(event: EntityTargetLivingEntityEvent): Entity? {
        return event.target
    }
}

class TamerProvider: EntityProvider<EntityTameEvent> {
    override fun getFrom(event: EntityTameEvent): Entity {
        return event.owner.takeIf { it is Entity } as Entity
    }
}