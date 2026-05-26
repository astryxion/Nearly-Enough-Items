package astryxion.nei.config;

import javax.annotation.Nonnull;

import net.minecraft.client.settings.KeyBinding;

import cpw.mods.fml.client.registry.ClientRegistry;

import org.lwjgl.input.Keyboard;

public class KeyBindings {
	private static final String categoryName = Constants.MOD_ID + " (" + Constants.NAME + ')';

	@Nonnull
	public static final KeyBinding toggleOverlay = new KeyBinding("key.nei.toggleOverlay", Keyboard.KEY_O, categoryName);
	@Nonnull
	public static final KeyBinding showRecipe = new KeyBinding("key.nei.showRecipe", Keyboard.KEY_R, categoryName);
	@Nonnull
	public static final KeyBinding showUses = new KeyBinding("key.nei.showUses", Keyboard.KEY_U, categoryName);

	public static void init() {
		ClientRegistry.registerKeyBinding(toggleOverlay);
		ClientRegistry.registerKeyBinding(showRecipe);
		ClientRegistry.registerKeyBinding(showUses);
	}
}
