package codechicken.nei.guihook;

import net.minecraft.client.gui.inventory.GuiContainer;

public interface IContainerDrawHandler {
	void onPreDraw(GuiContainer gui);

	void renderObjects(GuiContainer gui, int mousex, int mousey);

	void postRenderObjects(GuiContainer gui, int mousex, int mousey);

	void renderSlotUnderlay(GuiContainer gui, net.minecraft.inventory.Slot slot);

	void renderSlotOverlay(GuiContainer gui, net.minecraft.inventory.Slot slot);
}
