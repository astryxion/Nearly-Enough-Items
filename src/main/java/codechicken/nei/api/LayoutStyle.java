package codechicken.nei.api;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import codechicken.nei.VisiblityData;

public abstract class LayoutStyle {
	public abstract String getName();

	public abstract void init();

	public abstract void reset();

	public abstract void layout(GuiContainer gui, VisiblityData visibility);

	public abstract String getButtonName();

	public void drawButton(int buttonID, int x, int y, int w, int h, int mousex, int mousey, ItemStack stack) {
	}

	public void drawForeground(GuiContainer gui) {
	}

	public void drawBackground(GuiContainer gui) {
	}
}
