package codechicken.nei;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.gui.inventory.GuiContainer;

import codechicken.nei.api.LayoutStyle;
import codechicken.nei.bridge.NeiItemListSync;

/**
 * Compatibility stub for addons that call {@code LayoutManager.markItemsDirty()}.
 */
public class LayoutManager {
	public static boolean itemsLoaded;
	public static volatile boolean suppressItemListSync;
	public static final Map<Integer, LayoutStyle> layoutStyles = new HashMap<Integer, LayoutStyle>();

	public static void markItemsDirty() {
		if (suppressItemListSync) {
			return;
		}
		NeiItemListSync.markDirty();
	}

	public static GuiContainer getGuiContainer() {
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
		if (mc != null && mc.currentScreen instanceof GuiContainer) {
			return (GuiContainer) mc.currentScreen;
		}
		return null;
	}
}
