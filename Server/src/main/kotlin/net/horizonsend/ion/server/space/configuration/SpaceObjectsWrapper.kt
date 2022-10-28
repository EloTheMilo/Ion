package net.horizonsend.ion.server.space.configuration

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class SpaceObjectsWrapper(
	val spaceObjects: List<SpaceObjectImplementation> = listOf()
)