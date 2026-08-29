package codechicken.nei.guihook;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

public interface IContainerTooltipHandler {
	List<String> handleTooltip(GuiContainer gui, int mousex, int mousey, List<String> currenttip);

	List<String> handleItemDisplayName(GuiContainer gui, ItemStack itemstack, List<String> currenttip);

	List<String> handleItemTooltip(GuiContainer gui, ItemStack itemstack, int mousex, int mousey, List<String> currenttip);
}
