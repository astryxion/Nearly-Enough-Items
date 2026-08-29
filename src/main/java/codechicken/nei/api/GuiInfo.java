package codechicken.nei.api;

import java.util.HashSet;
import java.util.LinkedList;

import net.minecraft.client.gui.inventory.GuiContainer;

public class GuiInfo {
	public static final LinkedList<INEIGuiHandler> guiHandlers = new LinkedList<INEIGuiHandler>();
	public static final HashSet<Class<? extends GuiContainer>> customSlotGuis = new HashSet<Class<? extends GuiContainer>>();

	public static void load() {
	}

	public static void clearGuiHandlers() {
		guiHandlers.clear();
	}

	public static boolean hasCustomSlots(GuiContainer gui) {
		return gui != null && customSlotGuis.contains(gui.getClass());
	}
}
