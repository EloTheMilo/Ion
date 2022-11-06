package net.horizonsend.ion.server.customitems.blasters

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.managers.ProjectileManager
import net.horizonsend.ion.server.projectiles.Projectile
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class AmmoRequiringSingleShotBlaster : SingleShotBlaster() {
	abstract val requiredAmmo: ItemStack

	override fun onPrimaryInteract(source: LivingEntity, item: ItemStack) {
		if (!(source as? Player)?.inventory!!.contains(this.requiredAmmo)) return
		(source as? Player)?.inventory?.remove(this.requiredAmmo)
		item.editMeta {
			it.lore()?.clear()
			it.lore(customItemlist.itemStack.lore())
		}
		item.itemMeta.persistentDataContainer.remove(NamespacedKey(IonServer.Ion, "ammo"))
		item.itemMeta.persistentDataContainer[NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER] = singleShotWeaponBalancing.magazineSize
		(source as? Player)?.setCooldown(item.type, this.singleShotWeaponBalancing.reload)
	}
	override fun onSecondaryInteract(entity: LivingEntity, item: ItemStack) {
		if ((entity as? Player)!!.hasCooldown(item.type)) return
		if(reload(item, entity)) return
		entity.location.world.playSound(entity.location,"laser", 1f, singleShotWeaponBalancing.pitch)
		ProjectileManager.addProjectile(
			Projectile(
				entity.eyeLocation,
				if (getParticleType(entity) == Particle.REDSTONE){
					Particle.DustOptions(getParticleColour(entity), singleShotWeaponBalancing.shotSize)} else null,
				singleShotWeaponBalancing.iterationsPerTick,
				singleShotWeaponBalancing.distancePerIteration,
				getParticleType(entity),
				entity,
				singleShotWeaponBalancing.damage,
				singleShotWeaponBalancing.shouldPassThroughEntities,
				singleShotWeaponBalancing.shotSize.toDouble()
			)
		)
	}
	fun reload(item: ItemStack, source: LivingEntity): Boolean {
		if ((source as? Player)!!.hasCooldown(item.type)) return true
		val pdc = item.itemMeta.persistentDataContainer.get(
			NamespacedKey(IonServer.Ion, "ammo"),
			PersistentDataType.INTEGER
		)
		if (pdc != 0 && pdc != null) {
			item.editMeta {
				it.lore()?.clear()
				it.lore(mutableListOf(MiniMessage.miniMessage().deserialize("<bold><gray>Ammo:${pdc.minus(1)}/${singleShotWeaponBalancing.magazineSize}")))
			}
			(source as? Player)?.setCooldown(item.type, this.singleShotWeaponBalancing.timeBetweenShots)
			item.itemMeta.persistentDataContainer.remove(NamespacedKey(IonServer.Ion, "ammo"))
			item.itemMeta.persistentDataContainer.set(NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER, pdc.minus(1))


			return false
		}
		if (pdc == 0) {
			(source as? Player)?.setCooldown(item.type, this.singleShotWeaponBalancing.reload)
			if (!(source as? Player)?.inventory!!.contains(this.requiredAmmo)) return true
			(source as? Player)?.inventory?.remove(this.requiredAmmo)
			item.editMeta {
				it.lore()?.clear()
				it.lore(customItemlist.itemStack.lore())
			}
			item.itemMeta.persistentDataContainer.remove(NamespacedKey(IonServer.Ion, "ammo"))
			item.itemMeta.persistentDataContainer[NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER] = singleShotWeaponBalancing.magazineSize
			return true
		}
		return true
	}
}