package net.starlegacy.feature.multiblock.printer

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.util.isStainedTerracotta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object ArmorPrinterMultiblock : PrinterMultiblock() {
	override val signText = createSignText(
		line1 = "&6Armor",
		line2 = "&fPrinter",
		line3 = null,
		line4 = "&7!??_+!0"
	)

	override fun getOutput(product: Material): ItemStack = ItemStack(product, 1)

	override fun LegacyMultiblockShape.RequirementBuilder.printerMachineryBlock() = sponge()
	override fun LegacyMultiblockShape.RequirementBuilder.printerCoreBlock() = type(Material.ANVIL)
	override fun LegacyMultiblockShape.RequirementBuilder.printerProductBlock() =
		filteredTypes { it == Material.TERRACOTTA || it.isStainedTerracotta }
}