package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.server.features.starship.Starship
import org.bukkit.entity.Player

class LegacyController(player: Player, starship: Starship) : PlayerController(player, starship, "Legacy")
