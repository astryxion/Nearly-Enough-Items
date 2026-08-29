package codechicken.nei.recipe;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import codechicken.lib.gui.GuiDraw;

/**
 * Compatibility base class for NEI recipe GUIs. Opening recipes is routed to
 * JEI; this class exists because addons reference it.
 */
public abstract class GuiRecipe extends GuiContainer {
	public GuiScreen firstGui;
	public GuiContainer prevGui;
	public ArrayList<? extends IRecipeHandler> currenthandlers = new ArrayList<IRecipeHandler>();
	public int page;
	public int recipetype;

	public GuiRecipe(GuiScreen prevgui) {
		super(new Container() {
			@Override
			public boolean canInteractWith(EntityPlayer player) {
				return false;
			}
		});
		this.firstGui = prevgui;
		if (prevgui instanceof GuiContainer) {
			this.prevGui = (GuiContainer) prevgui;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
	}

	public Point getRecipePosition(int recipe) {
		return new Point(5, 16 + yOffset(recipe));
	}

	protected int yOffset(int recipe) {
		return recipe * 166 / Math.max(1, recipiesPerPage());
	}

	public int recipiesPerPage() {
		return 2;
	}

	public List getHandlerIngredients(IRecipeHandler handler, int recipe) {
		return handler.getIngredientStacks(recipe);
	}

	public static Point getMousePosition() {
		return GuiDraw.getMousePosition();
	}
}
