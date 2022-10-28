package net.horizonsend.ion.server.space.configuration

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class SpaceObjectImplementation(
	override val name: String = "",
	@Setting("display_name")
	override val _displayName: String = name,

	override val positionX: Int = 0,
	override val positionZ: Int = 0,

	@Setting("server_level")
	override val _serverLevel: String = "",

	override val surfaceScale: Double = 0.1,
	@Setting("surface_materials")
	override val _surfaceMaterials: List<String> = listOf(),

	override val hasClouds: Boolean = false,
	override val cloudScale: Double = 0.1,
	@Setting("cloud_materials")
	override val _cloudMaterials: List<String> = listOf(),

	override val orbitingSpaceObjects: List<OrbitingSpaceObjectImplementation> = listOf()
) : AbstractSpaceObject()