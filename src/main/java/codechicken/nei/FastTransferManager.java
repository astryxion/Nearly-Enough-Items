package codechicken.nei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;

/**
 * Slot-click helper used by overlay transfer. 1.7.10 windowClick modes:
 * 0 pickup, 1 shift-click (quick move).
 */
public class FastTransferManager {
	public static void clickSlot(GuiContainer window, int slotIndex) {
		clickSlot(window, slotIndex, 0, 0);
	}

	public static void clickSlot(GuiContainer window, int slotIndex, int button, int modifier) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc == null || mc.thePlayer == null || mc.playerController == null || window == null || window.inventorySlots == null) {
			return;
		}
		mc.playerController.windowClick(window.inventorySlots.windowId, slotIndex, button, modifier, mc.thePlayer);
	}
}
