package codechicken.nei.recipe;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.DefaultOverlayRenderer;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerInputHandler;
import codechicken.nei.guihook.IContainerTooltipHandler;

/**
 * The standard NEI recipe-handler base class. Addons extend this and fill
 * {@link #arecipes}; the compatibility bridge then indexes those recipes into JEI.
 */
public abstract class TemplateRecipeHandler implements ICraftingHandler, IUsageHandler {

	public abstract class CachedRecipe {
		final long offset = System.currentTimeMillis();

		public abstract PositionedStack getResult();

		public List<PositionedStack> getIngredients() {
			ArrayList<PositionedStack> stacks = new ArrayList<PositionedStack>();
			PositionedStack stack = getIngredient();
			if (stack != null) {
				stacks.add(stack);
			}
			return stacks;
		}

		public PositionedStack getIngredient() {
			return null;
		}

		public List<PositionedStack> getOtherStacks() {
			ArrayList<PositionedStack> stacks = new ArrayList<PositionedStack>();
			try {
				PositionedStack stack = getOtherStack();
				if (stack != null) {
					stacks.add(stack);
				}
			} catch (ArithmeticException e) {
				NEIClientConfig.logger.error("Error in getOtherStacks: " + e);
			}
			return stacks;
		}

		public PositionedStack getOtherStack() {
			return null;
		}

		public void setIngredientPermutation(Collection<PositionedStack> ingredients, ItemStack ingredient) {
			for (PositionedStack stack : ingredients) {
				for (int i = 0; i < stack.items.length; i++) {
					if (NEIServerUtils.areStacksSameTypeCrafting(ingredient, stack.items[i])) {
						stack.item = stack.items[i];
						Items.feather.setDamage(stack.item, Items.feather.getDamage(ingredient));
						if (ingredient.hasTagCompound()) {
							stack.item.setTagCompound((NBTTagCompound) ingredient.getTagCompound().copy());
						}
						stack.items = new ItemStack[] { stack.item };
						stack.setPermutationToRender(0);
						break;
					}
				}
			}
		}

		public boolean contains(Collection<PositionedStack> ingredients, ItemStack ingredient) {
			for (PositionedStack stack : ingredients) {
				if (stack.contains(ingredient)) {
					return true;
				}
			}
			return false;
		}

		public boolean contains(Collection<PositionedStack> ingredients, Item ingredient) {
			for (PositionedStack stack : ingredients) {
				if (stack.contains(ingredient)) {
					return true;
				}
			}
			return false;
		}

		public List<PositionedStack> getCycledIngredients(int cycle, List<PositionedStack> ingredients) {
			if (ingredients == null) {
				return ingredients;
			}
			for (int itemIndex = 0; itemIndex < ingredients.size(); itemIndex++) {
				randomRenderPermutation(ingredients.get(itemIndex), cycle + itemIndex);
			}
			return ingredients;
		}

		public void randomRenderPermutation(List<PositionedStack> stacks, long cycle) {
			if (stacks == null) {
				return;
			}
			for (int i = 0; i < stacks.size(); i++) {
				randomRenderPermutation(stacks.get(i), cycle);
			}
		}

		public void randomRenderPermutation(PositionedStack stack, long cycle) {
			if (stack == null || stack.items == null || stack.items.length == 0) {
				return;
			}
			Random rand = new Random(cycle + offset);
			stack.setPermutationToRender(Math.abs(rand.nextInt()) % stack.items.length);
		}
	}

	public static class RecipeTransferRect {
		public RecipeTransferRect(Rectangle rectangle, String outputId, Object... results) {
			rect = rectangle;
			this.outputId = outputId;
			this.results = results;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof RecipeTransferRect && rect.equals(((RecipeTransferRect) obj).rect);
		}

		@Override
		public int hashCode() {
			return rect.hashCode();
		}

		public Rectangle getRect() {
			return rect;
		}

		public Rectangle rect;
		public String outputId;
		public Object[] results;
	}

	public static class RecipeTransferRectHandler implements IContainerInputHandler, IContainerTooltipHandler {
		private static final Map<Class<? extends GuiContainer>, HashSet<RecipeTransferRect>> guiMap = new HashMap<Class<? extends GuiContainer>, HashSet<RecipeTransferRect>>();

		public static void registerRectsToGuis(List<Class<? extends GuiContainer>> classes, List<RecipeTransferRect> rects) {
			if (classes == null) {
				return;
			}
			for (int i = 0; i < classes.size(); i++) {
				Class<? extends GuiContainer> clazz = classes.get(i);
				HashSet<RecipeTransferRect> set = guiMap.get(clazz);
				if (set == null) {
					set = new HashSet<RecipeTransferRect>();
					guiMap.put(clazz, set);
				}
				set.addAll(rects);
			}
		}

		public boolean canHandle(GuiContainer gui) {
			return guiMap.containsKey(gui.getClass());
		}

		@Override
		public boolean lastKeyTyped(GuiContainer gui, char keyChar, int keyCode) {
			return false;
		}

		@Override
		public boolean mouseClicked(GuiContainer gui, int mousex, int mousey, int button) {
			if (!canHandle(gui)) {
				return false;
			}
			if (button == 0) {
				return transferRect(gui, false);
			}
			if (button == 1) {
				return transferRect(gui, true);
			}
			return false;
		}

		private boolean transferRect(GuiContainer gui, boolean usage) {
			int[] offset = RecipeInfo.getGuiOffset(gui);
			return TemplateRecipeHandler.transferRect(gui, guiMap.get(gui.getClass()), offset[0], offset[1], usage);
		}

		@Override
		public void onKeyTyped(GuiContainer gui, char keyChar, int keyID) {
		}

		@Override
		public void onMouseClicked(GuiContainer gui, int mousex, int mousey, int button) {
		}

		@Override
		public void onMouseUp(GuiContainer gui, int mousex, int mousey, int button) {
		}

		@Override
		public boolean keyTyped(GuiContainer gui, char keyChar, int keyID) {
			return false;
		}

		@Override
		public boolean mouseScrolled(GuiContainer gui, int mousex, int mousey, int scrolled) {
			return false;
		}

		@Override
		public void onMouseScrolled(GuiContainer gui, int mousex, int mousey, int scrolled) {
		}

		@Override
		public List<String> handleTooltip(GuiContainer gui, int mousex, int mousey, List<String> currenttip) {
			return currenttip;
		}

		@Override
		public List<String> handleItemDisplayName(GuiContainer gui, ItemStack itemstack, List<String> currenttip) {
			return currenttip;
		}

		@Override
		public List<String> handleItemTooltip(GuiContainer gui, ItemStack itemstack, int mousex, int mousey, List<String> currenttip) {
			return currenttip;
		}

		@Override
		public void onMouseDragged(GuiContainer gui, int mousex, int mousey, int button, long heldTime) {
		}
	}

	static {
		GuiContainerManager.addInputHandler(new RecipeTransferRectHandler());
		GuiContainerManager.addTooltipHandler(new RecipeTransferRectHandler());
	}

	public int cycleticks = Math.abs((int) System.currentTimeMillis());
	public ArrayList<CachedRecipe> arecipes = new ArrayList<CachedRecipe>();
	public LinkedList<RecipeTransferRect> transferRects = new LinkedList<RecipeTransferRect>();

	public TemplateRecipeHandler() {
		try {
			loadTransferRects();
			RecipeTransferRectHandler.registerRectsToGuis(getRecipeTransferRectGuis(), transferRects);
		} catch (Exception e) {
			NEIClientConfig.logger.error("Failed to load transfer rects for " + getClass().getName(), e);
		}
	}

	public void loadTransferRects() {
	}

	public void loadCraftingRecipes(String outputId, Object... results) {
		if ("item".equals(outputId) && results.length > 0 && results[0] instanceof ItemStack) {
			loadCraftingRecipes((ItemStack) results[0]);
		}
	}

	public void loadCraftingRecipes(ItemStack result) {
	}

	public void loadUsageRecipes(String inputId, Object... ingredients) {
		if ("item".equals(inputId) && ingredients.length > 0 && ingredients[0] instanceof ItemStack) {
			loadUsageRecipes((ItemStack) ingredients[0]);
		}
	}

	public void loadUsageRecipes(ItemStack ingredient) {
	}

	public abstract String getGuiTexture();

	public String getOverlayIdentifier() {
		return null;
	}

	public void drawExtras(int recipe) {
	}

	public void drawProgressBar(int x, int y, int tx, int ty, int w, int h, int ticks, int direction) {
		if (ticks <= 0) {
			ticks = 1;
		}
		drawProgressBar(x, y, tx, ty, w, h, (cycleticks % ticks) / (float) ticks, direction);
	}

	public void drawProgressBar(int x, int y, int tx, int ty, int w, int h, float completion, int direction) {
		if (direction > 3) {
			completion = 1 - completion;
			direction %= 4;
		}
		int var = (int) (completion * (direction % 2 == 0 ? w : h));
		switch (direction) {
			case 0:
				GuiDraw.drawTexturedModalRect(x, y, tx, ty, var, h);
				break;
			case 1:
				GuiDraw.drawTexturedModalRect(x, y, tx, ty, w, var);
				break;
			case 2:
				GuiDraw.drawTexturedModalRect(x + w - var, y, tx + w - var, ty, var, h);
				break;
			case 3:
				GuiDraw.drawTexturedModalRect(x, y + h - var, tx, ty + h - var, w, var);
				break;
			default:
				break;
		}
	}

	public List<Class<? extends GuiContainer>> getRecipeTransferRectGuis() {
		Class<? extends GuiContainer> clazz = getGuiClass();
		if (clazz != null) {
			LinkedList<Class<? extends GuiContainer>> list = new LinkedList<Class<? extends GuiContainer>>();
			list.add(clazz);
			return list;
		}
		return null;
	}

	public Class<? extends GuiContainer> getGuiClass() {
		return null;
	}

	public TemplateRecipeHandler newInstance() {
		try {
			return getClass().getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			try {
				return getClass().newInstance();
			} catch (Exception e2) {
				throw new RuntimeException(e2);
			}
		}
	}

	@Override
	public ICraftingHandler getRecipeHandler(String outputId, Object... results) {
		TemplateRecipeHandler handler = newInstance();
		handler.loadCraftingRecipes(outputId, results);
		return handler;
	}

	@Override
	public IUsageHandler getUsageHandler(String inputId, Object... ingredients) {
		TemplateRecipeHandler handler = newInstance();
		handler.loadUsageRecipes(inputId, ingredients);
		return handler;
	}

	@Override
	public int numRecipes() {
		return arecipes.size();
	}

	@Override
	public void drawBackground(int recipe) {
		GL11.glColor4f(1, 1, 1, 1);
		GuiDraw.changeTexture(getGuiTexture());
		GuiDraw.drawTexturedModalRect(0, 0, 5, 11, 166, 65);
	}

	@Override
	public void drawForeground(int recipe) {
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glDisable(GL11.GL_LIGHTING);
		GuiDraw.changeTexture(getGuiTexture());
		drawExtras(recipe);
	}

	@Override
	public List<PositionedStack> getIngredientStacks(int recipe) {
		return arecipes.get(recipe).getIngredients();
	}

	@Override
	public PositionedStack getResultStack(int recipe) {
		try {
			return arecipes.get(recipe).getResult();
		} catch (ArrayIndexOutOfBoundsException ignored) {
			return null;
		}
	}

	@Override
	public List<PositionedStack> getOtherStacks(int recipe) {
		return arecipes.get(recipe).getOtherStacks();
	}

	@Override
	public void onUpdate() {
		cycleticks++;
	}

	@Override
	public boolean hasOverlay(GuiContainer gui, Container container, int recipe) {
		return RecipeInfo.hasDefaultOverlay(gui, getOverlayIdentifier())
				|| RecipeInfo.hasOverlayHandler(gui, getOverlayIdentifier());
	}

	@Override
	public IRecipeOverlayRenderer getOverlayRenderer(GuiContainer gui, int recipe) {
		IStackPositioner positioner = RecipeInfo.getStackPositioner(gui, getOverlayIdentifier());
		if (positioner == null) {
			return null;
		}
		return new DefaultOverlayRenderer(getIngredientStacks(recipe), positioner);
	}

	@Override
	public IOverlayHandler getOverlayHandler(GuiContainer gui, int recipe) {
		return RecipeInfo.getOverlayHandler(gui, getOverlayIdentifier());
	}

	@Override
	public int recipiesPerPage() {
		return 2;
	}

	@Override
	public List<String> handleTooltip(GuiRecipe gui, List<String> currenttip, int recipe) {
		return currenttip;
	}

	@Override
	public List<String> handleItemTooltip(GuiRecipe gui, ItemStack stack, List<String> currenttip, int recipe) {
		return currenttip;
	}

	@Override
	public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe) {
		return false;
	}

	@Override
	public boolean mouseClicked(GuiRecipe gui, int button, int recipe) {
		if (button == 0) {
			return transferRect(gui, recipe, false);
		}
		if (button == 1) {
			return transferRect(gui, recipe, true);
		}
		return false;
	}

	private boolean transferRect(GuiRecipe gui, int recipe, boolean usage) {
		Point offset = gui.getRecipePosition(recipe);
		return transferRect(gui, transferRects, offset.x, offset.y, usage);
	}

	private static boolean transferRect(GuiContainer gui, Collection<RecipeTransferRect> transferRects, int offsetx, int offsety, boolean usage) {
		if (transferRects == null) {
			return false;
		}
		Point pos = GuiDraw.getMousePosition();
		Point relMouse = new Point(pos.x - gui.guiLeft - offsetx, pos.y - gui.guiTop - offsety);
		for (RecipeTransferRect rect : transferRects) {
			if (rect.rect.contains(relMouse)) {
				boolean opened = usage
						? GuiUsageRecipe.openRecipeGui(rect.outputId, rect.results)
						: GuiCraftingRecipe.openRecipeGui(rect.outputId, rect.results);
				if (opened) {
					return true;
				}
			}
		}
		return false;
	}
}
