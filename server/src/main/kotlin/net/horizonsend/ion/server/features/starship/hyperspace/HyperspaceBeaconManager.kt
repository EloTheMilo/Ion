package net.horizonsend.ion.server.features.starship.hyperspace

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.event.StarshipExitHyperspaceEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.features.starship.event.movement.StarshipTranslateEvent
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.event.EventHandler
import org.joml.Vector2i.distance
import java.util.UUID

object HyperspaceBeaconManager : SLEventListener() {
	// Your problem if it throws null pointers
	val beaconWorlds get() = ConfigurationFiles.serverConfiguration().beacons.groupBy { it.spaceLocation.bukkitWorld() }

	// Make it yell at you once every couple seconds not every time your ship moves
	private val activeRequests: MutableMap<UUID, Long> = mutableMapOf()

	private fun clearExpired() {
		activeRequests.filterValues {
			it + 1000 * 30 < System.currentTimeMillis()
		}.keys.forEach {
			activeRequests.remove(it)
		}
	}

	@EventHandler
	fun onStarshipUnpilot(event: StarshipUnpilotEvent) {
		val player = (event.starship.controller as? PlayerController)?.player ?: return
		activeRequests.remove(player.uniqueId)
	}

	@EventHandler
	fun onStarshipMove(event: StarshipTranslateEvent) {
		clearExpired()
		detectNearbyBeacons(event.starship, event.x, event.z)
	}

	@EventHandler
	fun onStarshipExitHyperspace(event: StarshipExitHyperspaceEvent) {
		if (event.starship is ActiveControlledStarship) {
			detectNearbyBeacons(event.starship, 0, 0)
		}
	}

	fun detectNearbyBeacons(starship: ActiveControlledStarship, x: Int, z: Int) {
		val pilot = starship.playerPilot ?: return
		if (starship.hyperdrives.isEmpty()) return

		val worldBeacons = beaconWorlds[starship.world] ?: return

		if (
			worldBeacons.any { beacon ->
				val distance = distance(
					beacon.spaceLocation.x,
					beacon.spaceLocation.z,
					(x + starship.centerOfMass.x),
					(z + starship.centerOfMass.z)
				)

				if (distance <= beacon.radius) {
					starship.beacon = beacon
					true
				} else {
					starship.beacon = null
					false
				}
			}
		) {
			if (activeRequests.containsKey(pilot.uniqueId)) return
			val beacon = starship.beacon

			if (beacon?.prompt != null) pilot.sendRichMessage(beacon.prompt)
			pilot.sendRichMessage(
				"<aqua>Detected signal from hyperspace beacon<yellow> ${beacon!!.name}<aqua>" + // not null if true
						", destination<yellow> " +
						"${beacon.destinationName ?: "${beacon.destination.world}: ${beacon.destination.x}, ${beacon.destination.z}"}<aqua>. " +
						"<gold><italic><hover:show_text:'<gray>/usebeacon'><click:run_command:/usebeacon>Engage hyperdrive?</click>"
			)
			activeRequests[pilot.uniqueId] = System.currentTimeMillis()
		} else {
			if (activeRequests.containsKey(pilot.uniqueId)) {
				if (!activeRequests.containsKey(pilot.uniqueId)) return // returned already if null

				pilot.information("Exited beacon communication radius.")
				activeRequests.remove(pilot.uniqueId)
				return
			}
			return
		}
	}
}
