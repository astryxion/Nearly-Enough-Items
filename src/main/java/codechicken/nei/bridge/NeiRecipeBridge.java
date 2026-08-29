package codechicken.nei.bridge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;

import astryxion.nei.Internal;
import astryxion.nei.ItemRegistry;
import astryxion.nei.RecipeRegistry;
import astryxion.nei.discovery.DiscoveryReport;
import astryxion.nei.gui.Focus;
import astryxion.nei.gui.RecipesGui;
import codechicken.nei.ItemList;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.IUsageHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler.RecipeTransferRect;

/**
 * Converts registered NEI recipe/usage handlers into JEI categories and
 * indexed recipes.
 */
public final class NeiRecipeBridge {
	private static final List<ICraftingHandler> pendingCrafting = new ArrayList<ICraftingHandler>();
	private static final List<IUsageHandler> pendingUsage = new ArrayList<IUsageHandler>();
	private static final Set<String> indexedHandlers = new HashSet<String>();
	private static boolean adaptedHandlerRegistered;
	private static RecipesGui recipesGui;
	private static int adaptedRecipeCount;
	private static volatile boolean deferIndexing;

	public static void setDeferIndexing(boolean defer) {
		deferIndexing = defer;
	}

	private NeiRecipeBridge() {
	}

	public static void setRecipesGui(RecipesGui gui) {
		recipesGui = gui;
	}

	public static void registerCraftingHandler(ICraftingHandler handler) {
		if (handler == null) {
			return;
		}
		if (deferIndexing || Internal.getRecipeRegistry() == null) {
			synchronized (pendingCrafting) {
				pendingCrafting.add(handler);
			}
			return;
		}
		indexCraftingHandler(handler);
	}

	public static void registerUsageHandler(IUsageHandler handler) {
		if (handler == null) {
			return;
		}
		if (deferIndexing || Internal.getRecipeRegistry() == null) {
			synchronized (pendingUsage) {
				pendingUsage.add(handler);
			}
			return;
		}
		indexUsageHandler(handler);
	}

	public static void flushPending() {
		indexedHandlers.clear();
		adaptedHandlerRegistered = false;
		adaptedRecipeCount = 0;
		ensureAdaptedHandler();

		List<ICraftingHandler> crafting;
		synchronized (codechicken.nei.recipe.GuiCraftingRecipe.craftinghandlers) {
			crafting = new ArrayList<ICraftingHandler>(codechicken.nei.recipe.GuiCraftingRecipe.craftinghandlers);
		}
		synchronized (pendingCrafting) {
			for (int i = 0; i < pendingCrafting.size(); i++) {
				if (!crafting.contains(pendingCrafting.get(i))) {
					crafting.add(pendingCrafting.get(i));
				}
			}
			pendingCrafting.clear();
		}
		for (int i = 0; i < crafting.size(); i++) {
			try {
				indexCraftingHandler(crafting.get(i));
			} catch (Throwable t) {
				NeiCompatLog.warn("Failed to index crafting handler {}", crafting.get(i).getClass().getName(), t);
			}
		}

		List<IUsageHandler> usage;
		synchronized (codechicken.nei.recipe.GuiUsageRecipe.usagehandlers) {
			usage = new ArrayList<IUsageHandler>(codechicken.nei.recipe.GuiUsageRecipe.usagehandlers);
		}
		synchronized (pendingUsage) {
			for (int i = 0; i < pendingUsage.size(); i++) {
				if (!usage.contains(pendingUsage.get(i))) {
					usage.add(pendingUsage.get(i));
				}
			}
			pendingUsage.clear();
		}
		for (int i = 0; i < usage.size(); i++) {
			try {
				indexUsageHandler(usage.get(i));
			} catch (Throwable t) {
				NeiCompatLog.warn("Failed to index usage handler {}", usage.get(i).getClass().getName(), t);
			}
		}

		DiscoveryReport report = DiscoveryReport.getLast();
		report.setNeiCraftingHandlers(codechicken.nei.recipe.GuiCraftingRecipe.craftinghandlers.size());
		report.setNeiUsageHandlers(codechicken.nei.recipe.GuiUsageRecipe.usagehandlers.size());
		report.setNeiHandlerRecipes(adaptedRecipeCount);
	}

	public static boolean openCraftingRecipes(String outputId, Object... results) {
		if (recipesGui == null) {
			return false;
		}
		if ("item".equals(outputId) && results != null && results.length > 0 && results[0] instanceof ItemStack) {
			recipesGui.showRecipes(new Focus((ItemStack) results[0]));
			return recipesGui.isOpen();
		}
		return false;
	}

	public static boolean openUsageRecipes(String inputId, Object... ingredients) {
		if (recipesGui == null) {
			return false;
		}
		if ("item".equals(inputId) && ingredients != null && ingredients.length > 0 && ingredients[0] instanceof ItemStack) {
			recipesGui.showUses(new Focus((ItemStack) ingredients[0]));
			return recipesGui.isOpen();
		}
		return false;
	}

	private static void ensureAdaptedHandler() {
		if (adaptedHandlerRegistered) {
			return;
		}
		RecipeRegistry registry = Internal.getRecipeRegistry();
		if (registry == null) {
			return;
		}
		registry.addRecipeHandler(new NeiAdaptedRecipeHandler());
		adaptedHandlerRegistered = true;
	}

	private static void indexCraftingHandler(ICraftingHandler handler) {
		ensureAdaptedHandler();
		String id = handler.getClass().getName();
		if (!indexedHandlers.add(id)) {
			return;
		}

		NeiRecipeCategory category = new NeiRecipeCategory(handler);
		RecipeRegistry registry = Internal.getRecipeRegistry();
		registry.addRecipeCategory(category);

		List<IRecipeHandler> populated = collectCraftingRecipes(handler);
		int added = 0;
		for (int h = 0; h < populated.size(); h++) {
			IRecipeHandler instance = populated.get(h);
			int count = safeNumRecipes(instance);
			for (int recipe = 0; recipe < count; recipe++) {
				try {
					NeiAdaptedRecipe adapted = new NeiAdaptedRecipe(instance, recipe, category.getUid());
					registry.addRecipe(adapted, category.getUid());
					added++;
				} catch (Throwable t) {
					NeiCompatLog.warn("Failed to adapt crafting recipe {} of {}", Integer.valueOf(recipe), id, t);
				}
			}
		}
		adaptedRecipeCount += added;
		NeiCompatLog.info("Indexed NEI crafting handler {} -> {} recipes", id, Integer.valueOf(added));
	}

	private static void indexUsageHandler(IUsageHandler handler) {
		if (indexedHandlers.contains(handler.getClass().getName())) {
			return;
		}
		if (handler instanceof ICraftingHandler) {
			indexCraftingHandler((ICraftingHandler) handler);
			return;
		}
		ensureAdaptedHandler();
		String id = handler.getClass().getName();
		indexedHandlers.add(id);

		NeiRecipeCategory category = new NeiRecipeCategory(handler);
		RecipeRegistry registry = Internal.getRecipeRegistry();
		registry.addRecipeCategory(category);

		List<IRecipeHandler> populated = collectUsageRecipes(handler);
		int added = 0;
		for (int h = 0; h < populated.size(); h++) {
			IRecipeHandler instance = populated.get(h);
			int count = safeNumRecipes(instance);
			for (int recipe = 0; recipe < count; recipe++) {
				try {
					NeiAdaptedRecipe adapted = new NeiAdaptedRecipe(instance, recipe, category.getUid());
					registry.addRecipe(adapted, category.getUid());
					added++;
				} catch (Throwable t) {
					NeiCompatLog.warn("Failed to adapt usage recipe {} of {}", Integer.valueOf(recipe), id, t);
				}
			}
		}
		adaptedRecipeCount += added;
		NeiCompatLog.info("Indexed NEI usage handler {} -> {} recipes", id, Integer.valueOf(added));
	}

	private static List<IRecipeHandler> collectCraftingRecipes(ICraftingHandler handler) {
		List<IRecipeHandler> populated = new ArrayList<IRecipeHandler>();
		ICraftingHandler all = tryLoadAll(handler);
		if (all != null && safeNumRecipes(all) > 0) {
			populated.add(all);
			return populated;
		}

		List<ItemStack> items = itemSnapshot();
		Set<String> seen = new HashSet<String>();
		for (int i = 0; i < items.size(); i++) {
			ItemStack stack = items.get(i);
			ICraftingHandler instance;
			try {
				instance = handler.getRecipeHandler("item", stack);
			} catch (Throwable t) {
				NeiCompatLog.warn("getRecipeHandler failed for {} on {}", handler.getClass().getName(), stack, t);
				continue;
			}
			if (instance == null || safeNumRecipes(instance) == 0) {
				continue;
			}
			String key = instance.getClass().getName() + '#' + safeNumRecipes(instance) + '@' + stackKey(stack);
			if (seen.add(key)) {
				populated.add(instance);
			}
		}
		return populated;
	}

	private static List<IRecipeHandler> collectUsageRecipes(IUsageHandler handler) {
		List<IRecipeHandler> populated = new ArrayList<IRecipeHandler>();
		List<ItemStack> items = itemSnapshot();
		for (int i = 0; i < items.size(); i++) {
			ItemStack stack = items.get(i);
			IUsageHandler instance;
			try {
				instance = handler.getUsageHandler("item", stack);
			} catch (Throwable t) {
				NeiCompatLog.warn("getUsageHandler failed for {} on {}", handler.getClass().getName(), stack, t);
				continue;
			}
			if (instance != null && safeNumRecipes(instance) > 0) {
				populated.add(instance);
			}
		}
		return populated;
	}

	private static ICraftingHandler tryLoadAll(ICraftingHandler handler) {
		if (!(handler instanceof TemplateRecipeHandler)) {
			try {
				ICraftingHandler all = handler.getRecipeHandler("all");
				if (all != null && safeNumRecipes(all) > 0) {
					return all;
				}
			} catch (Throwable ignored) {
			}
			return null;
		}

		TemplateRecipeHandler template = (TemplateRecipeHandler) handler;
		TemplateRecipeHandler instance;
		try {
			instance = template.newInstance();
		} catch (Throwable t) {
			NeiCompatLog.warn("Failed to newInstance {}", handler.getClass().getName(), t);
			return null;
		}

		String overlay = instance.getOverlayIdentifier();
		if (overlay != null) {
			try {
				instance.loadCraftingRecipes(overlay);
			} catch (Throwable t) {
				NeiCompatLog.warn("loadCraftingRecipes({}) failed for {}", overlay, handler.getClass().getName(), t);
			}
			if (instance.numRecipes() > 0) {
				return instance;
			}
		}

		if (instance.transferRects != null) {
			for (int i = 0; i < instance.transferRects.size(); i++) {
				RecipeTransferRect rect = instance.transferRects.get(i);
				try {
					instance.arecipes.clear();
					instance.loadCraftingRecipes(rect.outputId, rect.results);
				} catch (Throwable t) {
					NeiCompatLog.warn("loadCraftingRecipes via transfer rect failed for {}", handler.getClass().getName(), t);
				}
				if (instance.numRecipes() > 0) {
					return instance;
				}
			}
		}
		return instance.numRecipes() > 0 ? instance : null;
	}

	private static int safeNumRecipes(IRecipeHandler handler) {
		try {
			return handler.numRecipes();
		} catch (Throwable t) {
			return 0;
		}
	}

	private static List<ItemStack> itemSnapshot() {
		if (ItemList.items != null && !ItemList.items.isEmpty()) {
			return ItemList.items;
		}
		ItemRegistry registry = Internal.getItemRegistry();
		if (registry != null) {
			return registry.getItemList();
		}
		return new ArrayList<ItemStack>();
	}

	private static String stackKey(ItemStack stack) {
		try {
			return astryxion.nei.util.StackUtil.getUniqueIdentifierForStack(stack);
		} catch (Throwable t) {
			return String.valueOf(stack);
		}
	}
}
