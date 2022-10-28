package net.horizonsend.ion.server.space.configuration

import net.kyori.adventure.text.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Block

interface SpaceObject {
	val name: String
	val displayName: Component

	val positionX: Int
	val positionZ: Int

	val serverLevel: ServerLevel?

	val surfaceScale: Double
	val surfaceMaterials: List<Block>

	val hasClouds: Boolean
	val cloudScale: Double
	val cloudMaterials: List<Block>

	val orbitingSpaceObjects: List<OrbitingSpaceObject>
}