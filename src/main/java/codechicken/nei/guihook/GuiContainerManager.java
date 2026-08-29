package codechicken.nei.guihook;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

/**
 * Compatibility subset of NEI's GUI hook manager. Input/tooltip handler lists
 * exist so addons can register; drawing helpers are implemented.
 */
public class GuiContainerManager {
	public static final LinkedList<IContainerInputHandler> inputHandlers = new LinkedList<IContainerInputHandler>();
	public static final LinkedList<IContainerTooltipHandler> tooltipHandlers = new LinkedList<IContainerTooltipHandler>();
	public static final LinkedList<IContainerDrawHandler> drawHandlers = new LinkedList<IContainerDrawHandler>();
	public static final LinkedList<IContainerObjectHandler> objectHandlers = new LinkedList<IContainerObjectHandler>();

	public GuiContainer window;

	public GuiContainerManager(GuiContainer screen) {
		window = screen;
	}

	public static void addInputHandler(IContainerInputHandler handler) {
		inputHandlers.add(handler);
	}

	public static void addTooltipHandler(IContainerTooltipHandler handler) {
		tooltipHandlers.add(handler);
	}

	public static void addDrawHandler(IContainerDrawHandler handler) {
		drawHandlers.add(handler);
	}

	public static void addObjectHandler(IContainerObjectHandler handler) {
		objectHandlers.add(handler);
	}

	public static boolean shouldShowTooltip(GuiContainer gui) {
		return gui != null;
	}

	public static ItemStack getStackMouseOver(GuiContainer gui) {
		if (gui == null) {
			return null;
		}
		Slot slot = gui.getSlotAtPosition(getMouseX(gui), getMouseY(gui));
		return slot == null ? null : slot.getStack();
	}

	public static int getMouseX(GuiContainer gui) {
		return codechicken.nei.NEIClientUtils.getMousePosition().x;
	}

	public static int getMouseY(GuiContainer gui) {
		return codechicken.nei.NEIClientUtils.getMousePosition().y;
	}

	public static void drawItem(int i, int j, ItemStack itemstack) {
		drawItem(i, j, itemstack, false);
	}

	public static void drawItem(int i, int j, ItemStack itemstack, boolean small) {
		if (itemstack == null || itemstack.getItem() == null) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		RenderItem renderItem = RenderItem.getInstance();
		FontRenderer font = itemstack.getItem().getFontRenderer(itemstack);
		if (font == null) {
			font = mc.fontRendererObj;
		}
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		renderItem.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), itemstack, i, j);
		renderItem.renderItemOverlayIntoGUI(font, mc.getTextureManager(), itemstack, i, j);
		GL11.glDisable(GL11.GL_LIGHTING);
	}

	public static String itemDisplayNameShort(ItemStack itemstack) {
		if (itemstack == null) {
			return "";
		}
		List<String> tooltip = itemstack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
		return tooltip.isEmpty() ? itemstack.getDisplayName() : tooltip.get(0);
	}

	public static List<String> itemDisplayNameMultiline(ItemStack itemstack, GuiContainerManager gui, boolean includeHandlers) {
		if (itemstack == null) {
			return new ArrayList<String>();
		}
		return itemstack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
	}

	public static void enableItemLighting() {
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	public static void disableItemLighting() {
		GL11.glDisable(GL11.GL_LIGHTING);
	}
}
