package codechicken.nei.guihook;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

public interface IContainerObjectHandler {
	void load(GuiContainer gui);

	void guiTick(GuiContainer gui);

	boolean mouseClicked(GuiContainer gui, int mousex, int mousey, int button);

	ItemStack getStackUnderMouse(GuiContainer gui, int mousex, int mousey);
}
