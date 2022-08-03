package net.horizonsend.ion.server.listeners.bukkit

import kotlin.math.min
import net.horizonsend.ion.server.managers.ScreenManager.isInScreen
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent

@Suppress("Unused")
class InventoryClickListener : Listener {
	@EventHandler(priority = EventPriority.LOW)
	fun onInventoryClickEvent(event: InventoryClickEvent) {
		if (event.whoClicked.isInScreen) {
			event.isCancelled = true
			return
		}

		val slotIsCustom =
			event.currentItem?.type == Material.WARPED_FUNGUS_ON_A_STICK &&
			event.currentItem?.itemMeta!!.hasCustomModelData()

		val cursorIsCustom =
			event.cursor?.type == Material.WARPED_FUNGUS_ON_A_STICK &&
			event.cursor?.itemMeta!!.hasCustomModelData()

		fun update() {
			(event.whoClicked as? Player)?.updateInventory()
			event.isCancelled = true
		}

		// Custom items are warped fungus on a stick and potentially stacked, most but not all inventory operations work
		// fine, but some do not, so we need to specifically handle these ourselves.
		when (event.action) {
			InventoryAction.COLLECT_TO_CURSOR -> if (cursorIsCustom) {
				for (itemID in 0..event.view.countSlots()) {
					val item = event.view.getItem(itemID) ?: continue

					if (!item.hasItemMeta()) continue
					if (!item.itemMeta.hasCustomModelData()) continue
					if (item.type != Material.WARPED_FUNGUS_ON_A_STICK) continue
					if (item.itemMeta.customModelData != event.cursor!!.itemMeta.customModelData) continue

					val amountToTake = min(64 - event.cursor!!.amount, item.amount)

					if (amountToTake == 0) break

					event.cursor!!.amount += amountToTake
					item.amount -= amountToTake
				}
				update()
			}
			InventoryAction.CLONE_STACK -> if (slotIsCustom) {
				event.view.cursor = event.currentItem?.asQuantity(64)
				update()
			}
			InventoryAction.PLACE_ALL -> if (cursorIsCustom) {
				event.currentItem = event.cursor
				event.view.cursor = null
				update()
			}
			InventoryAction.PICKUP_SOME, InventoryAction.NOTHING -> if (slotIsCustom && cursorIsCustom){
				var total = event.currentItem!!.amount + event.cursor!!.amount
				val cursorAmount = min(total, 64)
				total -= cursorAmount
				event.currentItem!!.amount = cursorAmount
				if (total <= 0) event.view.cursor = null else event.cursor!!.amount = total
				update()
			}
			else -> {}
		}
	}
}