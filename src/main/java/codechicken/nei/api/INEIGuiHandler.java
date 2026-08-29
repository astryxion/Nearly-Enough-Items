package codechicken.nei.api;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import codechicken.nei.VisiblityData;

/**
 * If this is implemented on a gui, it will be automatically registered.
 */
public interface INEIGuiHandler {
	VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility);

	Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item);

	List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui);

	boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button);

	boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h);
}
