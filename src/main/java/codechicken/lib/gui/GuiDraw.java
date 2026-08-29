package codechicken.lib.gui;

import java.awt.Point;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.config.GuiUtils;

import codechicken.nei.NEIClientUtils;

/**
 * CodeChickenLib GuiDraw surface used by TemplateRecipeHandler and NEI addons.
 */
public class GuiDraw {
	public static class GuiHook extends Gui {
		public float getZLevel() {
			return zLevel;
		}

		public void setZLevel(float z) {
			zLevel = z;
		}

		public void incZLevel(float z) {
			zLevel += z;
		}

		public void drawGradientRect(int left, int top, int right, int bottom, int colour1, int colour2) {
			super.drawGradientRect(left, top, right, bottom, colour1, colour2);
		}
	}

	public static final GuiHook gui = new GuiHook();
	public static FontRenderer fontRenderer;
	public static RenderItem renderItem = RenderItem.getInstance();

	public static void bindFont() {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc != null) {
			fontRenderer = mc.fontRendererObj;
		}
	}

	public static void changeTexture(String texture) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(texture));
	}

	public static void changeTexture(ResourceLocation texture) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
	}

	public static void drawTexturedModalRect(int x, int y, int u, int v, int w, int h) {
		GuiUtils.drawTexturedModalRect(x, y, u, v, w, h, gui.getZLevel());
	}

	public static void drawRect(int x, int y, int w, int h, int colour) {
		Gui.drawRect(x, y, x + w, y + h, colour);
	}

	public static void drawGradientRect(int x, int y, int w, int h, int colour1, int colour2) {
		gui.drawGradientRect(x, y, x + w, y + h, colour1, colour2);
	}

	public static Point getMousePosition() {
		return NEIClientUtils.getMousePosition();
	}

	public static int getStringWidth(String text) {
		bindFont();
		if (fontRenderer == null || text == null) {
			return 0;
		}
		return fontRenderer.getStringWidth(text);
	}

	public static void drawString(String text, int x, int y, int colour) {
		drawString(text, x, y, colour, true);
	}

	public static void drawString(String text, int x, int y, int colour, boolean shadow) {
		bindFont();
		if (fontRenderer == null || text == null) {
			return;
		}
		fontRenderer.drawString(text, x, y, colour, shadow);
	}

	public static void drawStringC(String text, int x, int y, int colour) {
		int w = getStringWidth(text);
		drawString(text, x - w / 2, y, colour, false);
	}

	public static void drawStringC(String text, int x, int y, int w, int h, int colour) {
		drawString(text, x + (w - getStringWidth(text)) / 2, y + (h - 8) / 2, colour, false);
	}

	public static void setColour(int colour) {
		float a = (colour >> 24 & 255) / 255.0F;
		float r = (colour >> 16 & 255) / 255.0F;
		float g = (colour >> 8 & 255) / 255.0F;
		float b = (colour & 255) / 255.0F;
		if (a == 0) {
			a = 1.0F;
		}
		GL11.glColor4f(r, g, b, a);
	}
}
