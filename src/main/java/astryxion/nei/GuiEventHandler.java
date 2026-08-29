package astryxion.nei;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

import net.minecraftforge.client.event.MouseEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Method;

import cpw.mods.fml.relauncher.ReflectionHelper;

import astryxion.nei.gui.ItemListOverlay;
import astryxion.nei.gui.RecipesGui;
import astryxion.nei.input.InputHandler;
import astryxion.nei.util.Log;
import codechicken.nei.bridge.NeiRecipeBridge;

public class GuiEventHandler {

	@Nullable
	private ItemListOverlay itemListOverlay;
	@Nonnull
	private final RecipesGui recipesGui = new RecipesGui();
	@Nullable
	private InputHandler inputHandler;

	public GuiEventHandler() {
		NeiRecipeBridge.setRecipesGui(recipesGui);
	}

	private final boolean[] mouseButtonDown = new boolean[3];
	private int lastMouseWheel;
	private int ticksInWorld;

	private void ensureNeiStarted() {
		if (Internal.getRecipeRegistry() != null) {
			return;
		}
		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft == null || minecraft.thePlayer == null || minecraft.theWorld == null) {
			ticksInWorld = 0;
			return;
		}
		if (minecraft.thePlayer.sendQueue == null) {
			return;
		}
		ticksInWorld++;
		if (ticksInWorld < 10) {
			return;
		}
		NearlyEnoughItems.getProxy().startNEI();
	}

	public void setItemListOverlay(@Nullable ItemListOverlay itemListOverlay) {
		if (this.itemListOverlay != null) {
			this.itemListOverlay.close();
		}

		this.itemListOverlay = itemListOverlay;

		if (this.recipesGui.isOpen()) {
			this.recipesGui.close();
		}
		jeiCapturedMouse = false;
	}

	@SubscribeEvent
	public void onGuiInit(@Nonnull GuiScreenEvent.InitGuiEvent.Post event) {
		if (itemListOverlay == null) {
			return;
		}
		Minecraft minecraft = Minecraft.getMinecraft();
		GuiContainer guiContainer = asGuiContainer(minecraft.currentScreen);
		if (guiContainer == null) {
			return;
		}

		itemListOverlay.initGui(guiContainer);

		recipesGui.initGui(minecraft);
		inputHandler = new InputHandler(recipesGui, itemListOverlay, guiContainer);
	}
	
	@SubscribeEvent
	public void onGuiOpen(@Nonnull GuiOpenEvent event) {
		if (itemListOverlay == null) {
			return;
		}
		if (event.gui == null) {
			if (itemListOverlay.isOpen()) {
				itemListOverlay.close();
			}
			recipesGui.close();
			jeiCapturedMouse = false;
		} else if (!(event.gui instanceof GuiContainer)) {
			recipesGui.close();
			jeiCapturedMouse = false;
		}
	}

	@SubscribeEvent
	public void onDrawScreenEventPre(@Nonnull GuiScreenEvent.DrawScreenEvent.Pre event) {
		GuiContainer guiContainer = asGuiContainer(event.gui);
		if (guiContainer == null) {
			return;
		}

		if (recipesGui.isOpen()) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onDrawScreenEventPost(@Nonnull GuiScreenEvent.DrawScreenEvent.Post event) {
		if (itemListOverlay == null) {
			return;
		}
		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft.theWorld == null || minecraft.thePlayer == null) {
			return;
		}
		GuiContainer guiContainer = asGuiContainer(event.gui);
		if (guiContainer == null) {
			return;
		}

		if (recipesGui.isOpen()) {
			recipesGui.drawBackground();
		}

		itemListOverlay.updateGui(guiContainer);

		itemListOverlay.drawScreen(guiContainer.mc, event.mouseX, event.mouseY);
		recipesGui.draw(event.mouseX, event.mouseY);
		itemListOverlay.drawHovered(guiContainer.mc, event.mouseX, event.mouseY);

		ensureInputHandler(guiContainer);

		if (!recipesGui.isOpen()) {
			/**
			 * There is no way to render between the existing inventory tooltip and the dark background layer,
			 * so we have to re-render the inventory tooltip over the item list.
			 **/
			Slot slotUnderMouse = getSlotAtPosition(guiContainer, event.mouseX, event.mouseY);
			if (slotUnderMouse != null && slotUnderMouse.getHasStack()) {
				ItemStack itemStack = slotUnderMouse.getStack();
				guiContainer.renderToolTip(itemStack, event.mouseX, event.mouseY);
			}
		}
	}

	@SubscribeEvent
	public void onClientTick(@Nonnull TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			ensureNeiStarted();
		}
		if (itemListOverlay == null) {
			return;
		}

		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft.theWorld == null || minecraft.thePlayer == null) {
			return;
		}
		GuiContainer guiContainer = asGuiContainer(minecraft.currentScreen);
		if (guiContainer == null) {
			return;
		}

		if (event.phase == TickEvent.Phase.START) {
			ensureInputHandler(guiContainer);
			if (inputHandler != null) {
				inputHandler.handleGuiKeyboardEarly();
			}
			handleGuiMouseEarly(guiContainer);
			handleCtrlFFocus();
			return;
		}

		itemListOverlay.handleTick();
	}

	private boolean ctrlFWasDown;

	private void handleCtrlFFocus() {
		if (itemListOverlay == null) {
			return;
		}
		boolean ctrlFDown = GuiScreen.isCtrlKeyDown() && Keyboard.isKeyDown(Keyboard.KEY_F);
		if (ctrlFDown && !ctrlFWasDown) {
			itemListOverlay.setKeyboardFocus(true);
		}
		ctrlFWasDown = ctrlFDown;
	}

	@SubscribeEvent
	public void onGuiKeyboardEvent(InputEvent.KeyInputEvent event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (!(minecraft.currentScreen instanceof GuiContainer)) {
			return;
		}
		if (inputHandler != null) {
			inputHandler.handleKeyEvent();
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
	public void onForgeMouseEvent(MouseEvent event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		GuiScreen gui = minecraft.currentScreen;
		if (!(gui instanceof GuiContainer) || itemListOverlay == null) {
			return;
		}
		GuiContainer guiContainer = (GuiContainer) gui;
		ensureInputHandler(guiContainer);

		int[] mouse = getScaledMousePosition(minecraft, event.x, event.y);
		int mouseX = mouse[0];
		int mouseY = mouse[1];

		if (inputHandler != null) {
			if (inputHandler.handleForgeMouseEvent(event, mouseX, mouseY)) {
				event.setCanceled(true);
			} else if (inputHandler.shouldBlockInventoryMouseInput(mouseX, mouseY)) {
				event.setCanceled(true);
			}
		}

		syncMouseButtonState();
		if (event.dwheel != 0) {
			lastMouseWheel = event.dwheel;
		}
	}

	private void syncMouseButtonState() {
		for (int i = 0; i < mouseButtonDown.length; i++) {
			mouseButtonDown[i] = Mouse.isButtonDown(i);
		}
	}

	private void ensureInputHandler(@Nonnull GuiContainer guiContainer) {
		if (inputHandler == null && itemListOverlay != null) {
			itemListOverlay.initGui(guiContainer);
			recipesGui.initGui(Minecraft.getMinecraft());
			inputHandler = new InputHandler(recipesGui, itemListOverlay, guiContainer);
		}
	}

	private boolean jeiCapturedMouse;

	/**
	 * 1.7.10 has no GuiScreenEvent.MouseInputEvent. Drain the mouse queue before
	 * GuiScreen.handleInput so clicks on the JEI overlay are not treated as
	 * "clicked outside the inventory" (which throws the cursor item into the world).
	 */
	private void handleGuiMouseEarly(GuiScreen gui) {
		if (inputHandler == null || !Mouse.isCreated()) {
			return;
		}

		Minecraft mc = Minecraft.getMinecraft();
		while (Mouse.next()) {
			int mouseX = Mouse.getEventX() * gui.width / mc.displayWidth;
			int mouseY = gui.height - Mouse.getEventY() * gui.height / mc.displayHeight - 1;
			int button = Mouse.getEventButton();
			boolean pressed = Mouse.getEventButtonState();
			int wheel = Mouse.getEventDWheel();

			if (button >= 0 && pressed) {
				if (inputHandler.captureMousePress(button, mouseX, mouseY)) {
					jeiCapturedMouse = true;
					continue;
				}
				jeiCapturedMouse = false;
			} else if (button >= 0 && !pressed) {
				inputHandler.onMouseReleased(button);
				if (jeiCapturedMouse) {
					jeiCapturedMouse = false;
					continue;
				}
			} else if (wheel != 0) {
				if (inputHandler.onMouseScrolled(wheel, mouseX, mouseY)) {
					continue;
				}
			} else if (jeiCapturedMouse) {
				continue;
			}

			gui.handleMouseInput();
		}
	}

	private static int[] getScaledMousePosition(Minecraft minecraft, int eventX, int eventY) {
		ScaledResolution scaledresolution = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
		int scaledWidth = scaledresolution.getScaledWidth();
		int scaledHeight = scaledresolution.getScaledHeight();
		int mouseX = eventX * scaledWidth / minecraft.displayWidth;
		int mouseY = scaledHeight - eventY * scaledHeight / minecraft.displayHeight - 1;
		return new int[] {mouseX, mouseY};
	}

	@Nullable
	private static Slot getSlotAtPosition(GuiContainer guiContainer, int mouseX, int mouseY) {
		try {
			Method method = ReflectionHelper.findMethod(GuiContainer.class, guiContainer, new String[]{"getSlotAtPosition", "func_146975_c"}, int.class, int.class);
			return (Slot) method.invoke(guiContainer, mouseX, mouseY);
		} catch (Exception e) {
			Log.error("Failed to get slot at position", e);
			return null;
		}
	}

	@Nullable
	private GuiContainer asGuiContainer(GuiScreen guiScreen) {
		if (!(guiScreen instanceof GuiContainer)) {
			return null;
		}
		return (GuiContainer) guiScreen;
	}
}
