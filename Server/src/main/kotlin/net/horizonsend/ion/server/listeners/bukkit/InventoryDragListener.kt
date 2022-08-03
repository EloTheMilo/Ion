package net.horizonsend.ion.server.listeners.bukkit

import kotlin.math.max
import kotlin.math.min
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.managers.ScreenManager.isInScreen
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.DragType
import org.bukkit.event.inventory.InventoryDragEvent

@Suppress("Unused")
class InventoryDragListener(private val plugin: IonServer) : Listener {
	@EventHandler(priority = EventPriority.LOW)
	fun onInventoryDragEvent(event: InventoryDragEvent) {
		if (event.whoClicked.isInScreen) {
			event.isCancelled = true
			return
		}

		if (event.type == DragType.SINGLE) return

		val cursorIsCustom =
			event.oldCursor.type == Material.WARPED_FUNGUS_ON_A_STICK &&
				event.oldCursor.itemMeta!!.hasCustomModelData()

		if (!cursorIsCustom) return

		var count = event.oldCursor.amount
		val slotAmount = max(count.floorDiv(event.newItems.size), 1)

		for ((id, item) in event.newItems) {
			val setCount = min(count, slotAmount)
			count -= setCount
			event.view.setItem(id, item.asQuantity(setCount))
		}

		event.isCancelled = true

		Bukkit.getScheduler().runTask(plugin, Runnable {
			event.view.cursor?.amount = count
		})
	}
}