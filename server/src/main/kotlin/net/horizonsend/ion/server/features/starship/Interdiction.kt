package net.horizonsend.ion.server.features.starship

import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.CHETHERITE
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.multiblock.type.gravitywell.GravityWellMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.features.starship.subsystem.misc.GravityWellSubsystem
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.World
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.sqrt

object Interdiction : IonServerComponent() {
	override fun onEnable() {
		listen<PlayerInteractEvent> { event ->
			val player = event.player
			val block = event.clickedBlock ?: return@listen
			if (!block.type.isWallSign) {
				return@listen
			}
			val sign = block.state as Sign
			val multiblock = Multiblocks[sign]
			if (multiblock !is GravityWellMultiblock) {
				return@listen
			}
			val starship = ActiveStarships.findByPassenger(player)
				?: return@listen player.userError("You're not riding the starship")
			if (!starship.contains(block.x, block.y, block.z)) {
				return@listen
			}
			if (StarshipCruising.isCruising(starship)) {
				return@listen player.userError("Cannot activate while cruising")
			}
			when (event.action) {
				Action.RIGHT_CLICK_BLOCK -> {
					toggleGravityWell(starship)
				}

				Action.LEFT_CLICK_BLOCK -> {
					pulseGravityWell(player, starship, sign)
				}

				else -> return@listen
			}
		}
	}

	fun toggleGravityWell(starship: ActiveStarship) {
		if (StarshipCruising.isCruising(starship)) {
			starship.setIsInterdicting(false)
			starship.userError("Cannot activate gravity well while cruising")
			return
		}

		if (!starship.world.ion.hasFlag(WorldFlag.SPACE_WORLD)) {
			starship.userError("You cannot use gravity wells within other gravity wells.")
			return
		}

		when (starship.isInterdicting) {
			true -> for (player in starship.world.getNearbyPlayers(
				starship.centerOfMass.toLocation(starship.world),
				starshipInterdictionRangeEquation(starship)
			)) {
				player.playSound(
					Sound.sound(
						Key.key("minecraft:entity.zombie_villager.converted"),
						Sound.Source.AMBIENT,
						5f,
						1.00f
					)
				)
			}

			false -> for (player in starship.world.getNearbyPlayers(
				starship.centerOfMass.toLocation(starship.world),
				starshipInterdictionRangeEquation(starship)
			)) {
				player.playSound(
					Sound.sound(
						Key.key("minecraft:entity.zombie_villager.converted"),
						Sound.Source.AMBIENT,
						5f,
						0.05f
					)
				)
			}
		}
		starship.setIsInterdicting(!starship.isInterdicting)
	}

	private fun pulseGravityWell(player: Player, starship: ActiveStarship, sign: Sign) {
		val world = sign.world

		if (!world.ion.hasFlag(WorldFlag.SPACE_WORLD)) {
			player.userError("You cannot use gravity wells within other gravity wells.")
			return
		}

		if (world.environment == World.Environment.NETHER) {
			player.userError("You can't use gravity wells in hyperspace.")
			return
		}

		if (!starship.isInterdicting) {
			player.success("Gravity well is disabled.")
			return
		}

		val input = GravityWellMultiblock.getInput(sign)

		if (LegacyItemUtils.getTotalItems(input, CHETHERITE.constructItemStack()) < 2) {
			player.userError(
				"Not enough hypermatter in the dropper. Two chetherite shards are required!"
			)
			return
		}

		for (cruisingShip in ActiveStarships.getInWorld(world)) {
			if (cruisingShip !is ActiveControlledStarship) {
				continue
			}

			if (cruisingShip == starship) {
				continue
			}

			if (!StarshipCruising.isCruising(cruisingShip)) {
				continue
			}

			val controlLoc = cruisingShip.playerPilot?.location ?: starship.centerOfMass.toLocation(starship.world)

			if (controlLoc.world != sign.world) continue
			if (controlLoc.distance(sign.location) > starshipInterdictionRangeEquation(starship)) {
				continue
			}

			cruisingShip.cruiseData.velocity.multiply(0.8)
			cruisingShip.onlinePassengers.forEach { passenger ->
				passenger.alert("Quantum fluctuations detected - velocity has been reduced by 20%.")
			}
		}

		input.removeItem(CHETHERITE.constructItemStack().asQuantity(2))
		starship.onlinePassengers.forEach { passenger ->
			passenger.alert("Gravity pulse has been invoked by ${player.name}.")
		}
	}

	fun findGravityWell(starship: ActiveStarship): GravityWellSubsystem? = starship.gravityWells.asSequence()
		.filter { it.isIntact() }
		.lastOrNull()

	fun starshipInterdictionRangeEquation(starship: Starship): Double {
		if (starship.type == StarshipType.SPEEDER ||
			starship.type == StarshipType.STARFIGHTER ||
			starship.type == StarshipType.INTERCEPTOR ||
			starship.type == StarshipType.SHUTTLE ||
			starship.type == StarshipType.PLATFORM) return 10.0
		return if (starship.type.typeCategory == TypeCategory.WAR_SHIP) 3000 / sqrt(12000.0) * sqrt(starship.initialBlockCount.toDouble())
		else (3000 / sqrt(12000.0) * sqrt(starship.initialBlockCount.toDouble())) / 2
	}
}
