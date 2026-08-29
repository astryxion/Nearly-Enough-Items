package codechicken.nei;

import java.awt.Point;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import astryxion.nei.util.MouseHelper;

public class NEIClientUtils {
	public static Minecraft mc() {
		return Minecraft.getMinecraft();
	}

	public static String translate(String key, Object... params) {
		return StatCollector.translateToLocalFormatted("nei." + key, params);
	}

	public static boolean altKey() {
		return org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_LMENU)
				|| org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_RMENU);
	}

	public static boolean shiftKey() {
		return org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_LSHIFT)
				|| org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_RSHIFT);
	}

	public static boolean controlKey() {
		return org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_LCONTROL)
				|| org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_RCONTROL);
	}

	public static Point getMousePosition() {
		MouseHelper helper = new MouseHelper();
		return new Point(helper.getX(), helper.getY());
	}

	public static boolean canItemFitInInventory(net.minecraft.entity.player.EntityPlayer player, ItemStack itemstack) {
		return true;
	}

	public static void cheatItem(ItemStack stack, int button, int mode) {
	}

	public static void deleteHeldItem() {
	}

	public static void setSlotContents(int slot, ItemStack item, boolean containerInv) {
	}

	public static void printChatMessage(net.minecraft.util.IChatComponent message) {
		Minecraft mc = mc();
		if (mc != null && mc.thePlayer != null && message != null) {
			mc.thePlayer.addChatMessage(message);
		}
	}

	public static void playClickSound() {
		Minecraft mc = mc();
		if (mc != null && mc.thePlayer != null) {
			mc.thePlayer.playSound("random.click", 1.0F, 1.0F);
		}
	}
}
