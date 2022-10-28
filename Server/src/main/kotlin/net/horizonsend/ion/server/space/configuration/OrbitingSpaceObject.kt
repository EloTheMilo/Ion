package net.horizonsend.ion.server.space.configuration

interface OrbitingSpaceObject : SpaceObject {
	val offsetX: Int
	val offsetZ: Int

	val orbitWidth: Int
	val orbitHeight: Int

	val orbitRotation: Int
}