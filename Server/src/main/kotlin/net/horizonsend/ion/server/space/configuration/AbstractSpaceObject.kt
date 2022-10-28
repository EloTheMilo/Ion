package net.horizonsend.ion.server.space.configuration

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Block
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld

abstract class AbstractSpaceObject : SpaceObject {
	abstract val _displayName: String

	abstract val _serverLevel: String

	abstract val _surfaceMaterials: List<String>

	abstract val _cloudMaterials: List<String>

	override val displayName: Component = MiniMessage.miniMessage().deserialize(_displayName)

	override val serverLevel: ServerLevel get() = (Bukkit.getWorld(_serverLevel) as CraftWorld).handle

	override val surfaceMaterials: List<Block> = _surfaceMaterials.map {
		Registry.BLOCK.get(ResourceLocation.tryParse(it))
	}

	override val cloudMaterials: List<Block> = _cloudMaterials.map {
		Registry.BLOCK.get(ResourceLocation.tryParse(it))
	}
}