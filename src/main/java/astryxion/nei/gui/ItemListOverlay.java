package astryxion.nei.gui;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import astryxion.nei.gui.ingredients.ItemStackRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.client.config.GuiButtonExt;
import cpw.mods.fml.client.config.HoverChecker;

import org.lwjgl.input.Keyboard;

import astryxion.nei.Internal;
import astryxion.nei.ItemFilter;
import astryxion.nei.NearlyEnoughItems;
import astryxion.nei.api.gui.IDrawable;
import astryxion.nei.config.Config;
import astryxion.nei.config.Constants;
import astryxion.nei.config.NeiModConfigGui;
import astryxion.nei.gui.ingredients.GuiItemStackFast;
import astryxion.nei.gui.ingredients.GuiItemStackFastList;
import astryxion.nei.gui.ingredients.GuiItemStackGroup;
import astryxion.nei.input.GuiTextFieldFilter;
import astryxion.nei.input.IKeyable;
import astryxion.nei.input.IMouseHandler;
import astryxion.nei.input.IShowsRecipeFocuses;
import astryxion.nei.network.packets.PacketDeletePlayerItem;
import astryxion.nei.network.packets.PacketNEI;
import astryxion.nei.util.ItemStackElement;
import astryxion.nei.util.MathUtil;
import astryxion.nei.util.Translator;

public class ItemListOverlay implements IShowsRecipeFocuses, IMouseHandler, IKeyable {

	private static final int borderPadding = 4;
	private static final int searchHeight = 16;
	private static final int buttonPaddingX = 14;
	private static final int buttonPaddingY = 8;

	private static final int itemStackPadding = 1;
	private static final int itemStackWidth = GuiItemStackGroup.getWidth(itemStackPadding);
	private static final int itemStackHeight = GuiItemStackGroup.getHeight(itemStackPadding);
	private static int pageNum = 0;

	private final ItemFilter itemFilter;

	private int buttonHeight;
	private final GuiItemStackFastList guiItemStacks = new GuiItemStackFastList();
	private GuiButton nextButton;
	private GuiButton backButton;
	private GuiButton configButton;
	private IDrawable configButtonIcon;
	private HoverChecker configButtonHoverChecker;
	private GuiTextFieldFilter searchField;
	private int pageCount;

	private String pageNumDisplayString;
	private int pageNumDisplayX;
	private int pageNumDisplayY;

	private GuiItemStackFast hovered = null;

	// properties of the gui we're beside
	private int guiLeft;
	private int guiTop;
	private int guiXSize;
	private int guiYSize;
	private int screenWidth;
	private int screenHeight;

	private boolean open = false;
	private boolean enabled = true;

	public ItemListOverlay(ItemFilter itemFilter) {
		this.itemFilter = itemFilter;
	}

	public void initGui(@Nonnull GuiContainer guiContainer) {
		this.guiLeft = guiContainer.guiLeft;
		this.guiTop = guiContainer.guiTop;
		this.guiXSize = guiContainer.xSize;
		this.guiYSize = guiContainer.ySize;
		this.screenWidth = guiContainer.width;
		this.screenHeight = guiContainer.height;

		final int columns = getColumns();
		if (columns < 4) {
			close();
			return;
		}

		String next = ">";
		String back = "<";

		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		final int nextButtonWidth = buttonPaddingX + fontRenderer.getStringWidth(next);
		final int backButtonWidth = buttonPaddingX + fontRenderer.getStringWidth(back);
		buttonHeight = buttonPaddingY + fontRenderer.FONT_HEIGHT;

		final int rows = getRows();
		final int xSize = columns * itemStackWidth;
		final int xEmptySpace = screenWidth - guiLeft - guiXSize - xSize;

		final int leftEdge = guiLeft + guiXSize + (xEmptySpace / 2);
		final int rightEdge = leftEdge + xSize;

		final int yItemButtonSpace = getItemButtonYSpace();
		final int itemButtonsHeight = rows * itemStackHeight;

		final int buttonStartY = buttonHeight + (2 * borderPadding) + (yItemButtonSpace - itemButtonsHeight) / 2;
		createItemButtons(leftEdge, buttonStartY, columns, rows);

		nextButton = new GuiButtonExt(0, rightEdge - nextButtonWidth, borderPadding, nextButtonWidth, buttonHeight, next);
		backButton = new GuiButtonExt(1, leftEdge, borderPadding, backButtonWidth, buttonHeight, back);

		int configButtonSize = searchHeight + 4;
		int searchFieldY = screenHeight - searchHeight - borderPadding - 2;
		final int searchFieldX;
		final int searchFieldWidth;
		if (isSearchBarCentered()) {
			searchFieldX = guiLeft;
			searchFieldWidth = guiXSize - configButtonSize - 1;
		} else {
			searchFieldX = leftEdge;
			searchFieldWidth = rightEdge - leftEdge - configButtonSize - 1;
		}
		searchField = new GuiTextFieldFilter(0, fontRenderer, searchFieldX, searchFieldY, searchFieldWidth, searchHeight);

		int configButtonX = searchFieldX + searchFieldWidth + 1;
		int configButtonY = screenHeight - configButtonSize - borderPadding;
		configButton = new GuiButtonExt(2, configButtonX, configButtonY, configButtonSize, configButtonSize, null);
		ResourceLocation configButtonIconLocation = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "recipeBackground.png");
		configButtonIcon = Internal.getHelpers().getGuiHelper().createDrawable(configButtonIconLocation, 0, 166, 16, 16);
		configButtonHoverChecker = new HoverChecker(configButton, 0);
		setKeyboardFocus(false);
		searchField.setItemFilter(itemFilter);

		updateLayout();

		open();
	}

	public void updateGui(@Nonnull GuiContainer guiContainer) {
		if (this.guiLeft != guiContainer.guiLeft || this.guiTop != guiContainer.guiTop || this.guiXSize != guiContainer.xSize || this.guiYSize != guiContainer.ySize || this.screenWidth != guiContainer.width || this.screenHeight != guiContainer.height) {
			initGui(guiContainer);
		}
	}

	private void createItemButtons(final int xStart, final int yStart, final int columnCount, final int rowCount) {
		guiItemStacks.clear();

		for (int row = 0; row < rowCount; row++) {
			int y = yStart + (row * itemStackHeight);
			for (int column = 0; column < columnCount; column++) {
				int x = xStart + (column * itemStackWidth);
				guiItemStacks.add(new GuiItemStackFast(x, y, itemStackPadding));
			}
		}
	}

	private void updateLayout() {
		updatePageCount();
		if (pageNum >= getPageCount()) {
			pageNum = 0;
		}
		int i = pageNum * getCountPerPage();

		ImmutableList<ItemStackElement> itemList = itemFilter.getItemList();
		guiItemStacks.set(i, itemList);

		FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;

		pageNumDisplayString = (getPageNum() + 1) + "/" + getPageCount();
		int pageDisplayWidth = fontRendererObj.getStringWidth(pageNumDisplayString);
		pageNumDisplayX = ((backButton.xPosition + backButton.width) + nextButton.xPosition) / 2 - (pageDisplayWidth / 2);
		pageNumDisplayY = backButton.yPosition + Math.round((backButton.height - fontRendererObj.FONT_HEIGHT) / 2.0f);

		searchField.update();
	}

	private void nextPage() {
		if (pageNum == getPageCount() - 1) {
			setPageNum(0);
		} else {
			setPageNum(pageNum + 1);
		}
	}

	private void previousPage() {
		if (pageNum == 0) {
			setPageNum(getPageCount() - 1);
		} else {
			setPageNum(pageNum - 1);
		}
	}

	public void drawScreen(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		if (!isOpen()) {
			return;
		}

		GL11.glDisable(GL11.GL_LIGHTING);
		
		minecraft.fontRendererObj.drawString(pageNumDisplayString, pageNumDisplayX, pageNumDisplayY, Color.white.getRGB(), true);
		searchField.drawTextBox();

		nextButton.drawButton(minecraft, mouseX, mouseY);
		backButton.drawButton(minecraft, mouseX, mouseY);
		configButton.drawButton(minecraft, mouseX, mouseY);
		configButtonIcon.draw(minecraft, configButton.xPosition + 2, configButton.yPosition + 2);
		GL11.glDisable(GL11.GL_BLEND);

		boolean mouseOver = isMouseOver(mouseX, mouseY);

		if (mouseOver && shouldShowDeleteItemTooltip(minecraft)) {
			hovered = guiItemStacks.render(null, minecraft, false, mouseX, mouseY);

			String deleteItem = Translator.translateToLocal("nei.tooltip.delete.item");
			TooltipRenderer.drawHoveringText(minecraft, deleteItem, mouseX, mouseY);
		} else {
			hovered = guiItemStacks.render(hovered, minecraft, mouseOver, mouseX, mouseY);
		}

		if (configButtonHoverChecker.checkHover(mouseX, mouseY)) {
			String configString = Translator.translateToLocal("nei.tooltip.config");
			TooltipRenderer.drawHoveringText(minecraft, configString, mouseX, mouseY);
		}
	}

	private boolean shouldShowDeleteItemTooltip(Minecraft minecraft) {
		if (Config.isDeleteItemsInCheatModeActive()) {
			EntityPlayer player = minecraft.thePlayer;
			if (player.inventory.getItemStack() != null) {
				return true;
			}
		}
		return false;
	}

	public void drawHovered(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		if (hovered != null) {
			ItemStackRenderer.enableGuiItemRender();
			hovered.drawHovered(minecraft, mouseX, mouseY);
			ItemStackRenderer.disableGuiItemRender();

			hovered = null;
		}
	}

	public void handleTick() {
		if (searchField != null) {
			searchField.updateCursorCounter();
		}
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		if (!isOpen()) {
			return false;
		}
		if (mouseX >= guiLeft + guiXSize) {
			return true;
		}
		return isSearchBarCentered() && (
				(searchField != null && searchField.isMouseOver(mouseX, mouseY))
				|| (configButtonHoverChecker != null && configButtonHoverChecker.checkHover(mouseX, mouseY)));
	}

	@Override
	@Nullable
	public Focus getFocusUnderMouse(int mouseX, int mouseY) {
		if (!isMouseOver(mouseX, mouseY)) {
			return null;
		}

		Focus focus = guiItemStacks.getFocusUnderMouse(mouseX, mouseY);
		if (focus != null) {
			setKeyboardFocus(false);
		}
		return focus;
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!isMouseOver(mouseX, mouseY)) {
			setKeyboardFocus(false);
			return false;
		}

		if (Config.isDeleteItemsInCheatModeActive()) {
			Minecraft minecraft = Minecraft.getMinecraft();
			EntityPlayerSP player = minecraft.thePlayer;
			ItemStack itemStack = player.inventory.getItemStack();
			if (itemStack != null) {
				player.inventory.setItemStack(null);
				PacketNEI packet = new PacketDeletePlayerItem(itemStack);
				NearlyEnoughItems.getProxy().sendPacketToServer(packet);
				return true;
			}
		}

		boolean buttonClicked = handleMouseClickedButtons(mouseX, mouseY);
		if (buttonClicked) {
			setKeyboardFocus(false);
			return true;
		}

		return handleMouseClickedSearch(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
		if (!isMouseOver(mouseX, mouseY)) {
			return false;
		}
		if (scrollDelta < 0) {
			nextPage();
			return true;
		} else if (scrollDelta > 0) {
			previousPage();
			return true;
		}
		return false;
	}

	private boolean handleMouseClickedButtons(int mouseX, int mouseY) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (nextButton.mousePressed(minecraft, mouseX, mouseY)) {
			nextPage();
			return true;
		} else if (backButton.mousePressed(minecraft, mouseX, mouseY)) {
			previousPage();
			return true;
		} else if (configButton.mousePressed(minecraft, mouseX, mouseY)) {
			close();
			GuiScreen configScreen = new NeiModConfigGui(minecraft.currentScreen);
			minecraft.displayGuiScreen(configScreen);
			return true;
		}
		return false;
	}

	private boolean handleMouseClickedSearch(int mouseX, int mouseY, int mouseButton) {
		boolean searchClicked = searchField.isMouseOver(mouseX, mouseY);
		setKeyboardFocus(searchClicked);
		if (searchClicked && searchField.handleMouseClicked(mouseX, mouseY, mouseButton)) {
			updateLayout();
		}
		return searchClicked;
	}

	@Override
	public boolean hasKeyboardFocus() {
		return searchField != null && searchField.isFocused();
	}

	@Override
	public void setKeyboardFocus(boolean keyboardFocus) {
		if (searchField != null) {
			searchField.setFocused(keyboardFocus);
		}
	}

	@Override
	public boolean onKeyPressed(int keyCode) {
		return handleSearchKeyTyped(Keyboard.getEventCharacter(), keyCode);
	}

	public boolean handleSearchKeyTyped(char character, int keyCode) {
		if (!hasKeyboardFocus()) {
			return false;
		}
		if (keyCode == Keyboard.KEY_ESCAPE) {
			setKeyboardFocus(false);
			return true;
		}
		boolean success = searchField.textboxKeyTyped(character, keyCode);
		if (success) {
			updateLayout();
		}
		return true;
	}

	private int getItemButtonXSpace() {
		return screenWidth - (guiLeft + guiXSize + (2 * borderPadding));
	}

	private int getItemButtonYSpace() {
		if (isSearchBarCentered()) {
			return screenHeight - (buttonHeight + (3 * borderPadding));
		}
		return screenHeight - (buttonHeight + searchHeight + 2 + (4 * borderPadding));
	}

	private boolean isSearchBarCentered() {
		return Config.isCenterSearchBarEnabled() && guiTop + guiYSize + searchHeight < screenHeight;
	}

	private int getColumns() {
		return getItemButtonXSpace() / itemStackWidth;
	}

	private int getRows() {
		return getItemButtonYSpace() / itemStackHeight;
	}

	private int getCountPerPage() {
		return getColumns() * getRows();
	}

	private void updatePageCount() {
		int count = itemFilter.size();
		pageCount = MathUtil.divideCeil(count, getCountPerPage());
		if (pageCount == 0) {
			pageCount = 1;
		}
	}

	private int getPageCount() {
		return pageCount;
	}

	private int getPageNum() {
		return pageNum;
	}

	private void setPageNum(int pageNum) {
		if (ItemListOverlay.pageNum == pageNum) {
			return;
		}
		ItemListOverlay.pageNum = pageNum;
		updateLayout();
	}

	@Override
	public void open() {
		open = true;
		setKeyboardFocus(false);
	}

	@Override
	public void close() {
		open = false;
		setKeyboardFocus(false);
	}

	@Override
	public boolean isOpen() {
		return open && enabled;
	}

	public void toggleEnabled() {
		enabled = !enabled;
	}
}
