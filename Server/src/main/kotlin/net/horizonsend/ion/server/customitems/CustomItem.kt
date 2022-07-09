package net.horizonsend.ion.server.customitems

import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

abstract class CustomItem {
	abstract val customModelData: Int

	open fun onPrimaryInteract(source: LivingEntity, item: ItemStack) {}
	open fun onSecondaryInteract(entity: LivingEntity, item: ItemStack) {}
}