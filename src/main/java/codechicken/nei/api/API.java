package codechicken.nei.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import codechicken.nei.BookmarkContainerInfo;
import codechicken.nei.ItemList;
import codechicken.nei.ItemSorter;
import codechicken.nei.ItemStackSet;
import codechicken.nei.KeyManager;
import codechicken.nei.LayoutManager;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.OffsetPositioner;
import codechicken.nei.SearchField;
import codechicken.nei.SearchTokenParser.ISearchParserProvider;
import codechicken.nei.SubsetWidget;
import codechicken.nei.SubsetWidget.SubsetTag;
import codechicken.nei.api.IRecipeFilter.IRecipeFilterProvider;
import codechicken.nei.api.ItemFilter.ItemFilterProvider;
import codechicken.nei.bridge.NeiCompatLog;
import codechicken.nei.config.Option;
import codechicken.nei.recipe.CatalystInfo;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.IUsageHandler;
import codechicken.nei.recipe.RecipeCatalysts;
import codechicken.nei.recipe.RecipeInfo;
import codechicken.nei.recipe.StackInfo;
import codechicken.nei.util.ItemStackFilterParser;

import astryxion.nei.Internal;
import astryxion.nei.api.IItemBlacklist;

/**
 * Main NEI API. Binary-compatible with 1.7.10 addons. Registration calls are
 * bridged into the JEI backend.
 */
public class API {

	public static void registerRecipeHandler(ICraftingHandler handler) {
		GuiCraftingRecipe.registerRecipeHandler(handler);
	}

	public static void registerUsageHandler(IUsageHandler handler) {
		GuiUsageRecipe.registerUsageHandler(handler);
	}

	public static void registerGuiOverlay(Class<? extends GuiContainer> classz, String ident) {
		registerGuiOverlay(classz, ident, 5, 11);
	}

	public static void registerGuiOverlay(Class<? extends GuiContainer> classz, String ident, int x, int y) {
		registerGuiOverlay(classz, ident, new OffsetPositioner(x, y));
	}

	public static void registerGuiOverlay(Class<? extends GuiContainer> classz, String ident, IStackPositioner positioner) {
		NeiCompatLog.api("registerGuiOverlay", classz == null ? "null" : classz.getName(), ident);
		RecipeInfo.registerGuiOverlay(classz, ident, positioner);
	}

	public static void registerGuiOverlayHandler(Class<? extends GuiContainer> classz, IOverlayHandler handler, String ident) {
		NeiCompatLog.api("registerGuiOverlayHandler", classz == null ? "null" : classz.getName(), ident);
		RecipeInfo.registerOverlayHandler(classz, handler, ident);
	}

	public static boolean hasGuiOverlayHandler(Class<? extends GuiContainer> classz, String ident) {
		return RecipeInfo.hasOverlayHandler(classz, ident);
	}

	public static void setGuiOffset(Class<? extends GuiContainer> classz, int x, int y) {
		RecipeInfo.setGuiOffset(classz, x, y);
	}

	public static void registerNEIGuiHandler(INEIGuiHandler handler) {
		NeiCompatLog.api("registerNEIGuiHandler", handler == null ? "null" : handler.getClass().getName());
		GuiInfo.guiHandlers.add(handler);
	}

	public static void hideItem(ItemStack item) {
		NeiCompatLog.api("hideItem", item);
		if (item == null) {
			return;
		}
		if (!ItemInfo.hiddenItems.contains(item)) {
			ItemInfo.hiddenItems.add(item);
		}
		IItemBlacklist blacklist = Internal.getHelpers() == null ? null : Internal.getHelpers().getItemBlacklist();
		if (blacklist != null) {
			blacklist.addItemToBlacklist(item);
		}
		LayoutManager.markItemsDirty();
	}

	public static void hideItem(String rule) {
		NeiCompatLog.api("hideItem(rule)", rule);
		ItemFilter filter = ItemStackFilterParser.parse(rule);
		if (filter != null) {
			ItemInfo.hiddenItemsRules.filters.add(filter);
			LayoutManager.markItemsDirty();
		}
	}

	public static void setOverrideName(ItemStack item, String name) {
		NeiCompatLog.api("setOverrideName", item, name);
		if (name == null) {
			ItemInfo.nameOverrides.remove(item);
		} else {
			ItemInfo.nameOverrides.put(item, name);
		}
		LayoutManager.markItemsDirty();
	}

	public static void setAliases(ItemStack item, String... aliases) {
		if (aliases == null || aliases.length == 0) {
			ItemInfo.itemAliases.remove(item);
		} else {
			ItemInfo.itemAliases.put(item, Arrays.asList(aliases));
		}
	}

	public static void addItemListEntry(ItemStack item) {
		NeiCompatLog.api("addItemListEntry", item);
		if (item == null || item.getItem() == null) {
			return;
		}
		if (!ItemInfo.itemOverrides.containsEntry(item.getItem(), item)) {
			ItemInfo.itemOverrides.put(item.getItem(), item);
			LayoutManager.markItemsDirty();
		}
	}

	public static void setItemListEntries(Item item, Iterable<ItemStack> items) {
		NeiCompatLog.api("setItemListEntries", item);
		if (items == null) {
			items = Collections.emptyList();
		}
		ItemInfo.itemOverrides.replaceValues(item, items);
		LayoutManager.markItemsDirty();
	}

	public static void addKeyBind(String ident, int defaultKey) {
		KeyManager.registerKeyBinding(ident, defaultKey);
	}

	@Deprecated
	public static void addHashBind(String ident, int defaultKey) {
		addKeyBind(ident, defaultKey);
	}

	public static void addOption(Option option) {
		NEIClientConfig.getOptionList().addOption(option);
	}

	public static void addLayoutStyle(int styleID, LayoutStyle style) {
		LayoutManager.layoutStyles.put(Integer.valueOf(styleID), style);
	}

	public static void addInfiniteItemHandler(IInfiniteItemHandler handler) {
		ItemInfo.infiniteHandlers.addFirst(handler);
	}

	public static void registerHighlightIdentifier(Block block, IHighlightHandler handler) {
		ItemInfo.highlightIdentifiers.put(block, handler);
	}

	public static void addFastTransferExemptSlot(Class<? extends Slot> slotClass) {
		ItemInfo.fastTransferExemptions.add(slotClass);
	}

	public static void registerHighlightHandler(IHighlightHandler handler, ItemInfo.Layout... layout) {
		ItemInfo.registerHighlightHandler(handler, layout);
	}

	public static void registerModeHandler(INEIModeHandler handler) {
		NEIInfo.modeHandlers.add(handler);
	}

	public static void addRecipeFilter(IRecipeFilterProvider filterProvider) {
		NeiCompatLog.unsupported("addRecipeFilter");
	}

	public static void addItemFilter(ItemFilterProvider filterProvider) {
		NeiCompatLog.api("addItemFilter", filterProvider == null ? "null" : filterProvider.getClass().getName());
		synchronized (ItemList.itemFilterers) {
			ItemList.itemFilterers.add(filterProvider);
		}
	}

	public static void addSubset(String name, ItemFilter filter) {
		addSubset(new SubsetTag(name, filter));
	}

	public static void addSubset(String name, Iterable<ItemStack> items) {
		ItemStackSet filter = new ItemStackSet();
		if (items != null) {
			for (ItemStack item : items) {
				filter.add(item);
			}
		}
		addSubset(new SubsetTag(name, filter));
	}

	public static void addSubset(SubsetTag tag) {
		NeiCompatLog.api("addSubset", tag == null ? "null" : tag.name);
		SubsetWidget.addTag(tag);
	}

	@Deprecated
	public static void addSearchProvider(SearchField.ISearchProvider provider) {
	}

	public static void addSearchProvider(ISearchParserProvider provider) {
		SearchField.searchParser.addProvider(provider);
	}

	public static void addSortOption(String name, Comparator<ItemStack> comparator) {
		ItemSorter.add(name, comparator);
	}

	public static void addItemVariant(Item item, ItemStack variant) {
		NeiCompatLog.api("addItemVariant", item, variant);
		if (item == null || variant == null) {
			return;
		}
		if (!ItemInfo.itemVariants.containsEntry(item, variant)) {
			ItemInfo.itemVariants.put(item, variant);
			LayoutManager.markItemsDirty();
		}
	}

	public static void registerStackStringifyHandler(IStackStringifyHandler handler) {
		StackInfo.stackStringifyHandlers.add(handler);
	}

	public static void addRecipeCatalyst(ItemStack stack, IRecipeHandler handler, int priority) {
		addRecipeCatalyst(stack, RecipeCatalysts.getRecipeID(handler), priority);
	}

	public static void addRecipeCatalyst(ItemStack stack, IRecipeHandler handler) {
		addRecipeCatalyst(stack, handler, 0);
	}

	public static void addRecipeCatalyst(ItemStack stack, String handlerID, int priority) {
		NeiCompatLog.api("addRecipeCatalyst", handlerID);
		RecipeCatalysts.addRecipeCatalyst(handlerID, new CatalystInfo(stack, priority));
	}

	public static void addRecipeCatalyst(ItemStack stack, String handlerID) {
		addRecipeCatalyst(stack, handlerID, 0);
	}

	public static void removeRecipeCatalyst(ItemStack stack, IRecipeHandler handler) {
		removeRecipeCatalyst(stack, RecipeCatalysts.getRecipeID(handler));
	}

	public static void removeRecipeCatalyst(ItemStack stack, String handlerID) {
		RecipeCatalysts.removeRecipeCatalyst(handlerID, stack);
	}

	public static void registerBookmarkContainerHandler(Class<? extends GuiContainer> gui, IBookmarkContainerHandler handler) {
		BookmarkContainerInfo.registerBookmarkContainerHandler(gui, handler);
	}

	@Deprecated
	public static void addRecipeCatalyst(List<ItemStack> stacks, IRecipeHandler handler) {
		if (stacks == null) {
			return;
		}
		for (int i = 0; i < stacks.size(); i++) {
			addRecipeCatalyst(stacks.get(i), handler);
		}
	}

	@Deprecated
	public static void addRecipeCatalyst(List<ItemStack> stacks, String handlerID) {
		if (stacks == null) {
			return;
		}
		for (int i = 0; i < stacks.size(); i++) {
			addRecipeCatalyst(stacks.get(i), handlerID);
		}
	}

	@Deprecated
	public static void addRecipeCatalyst(ItemStack stack, Class<? extends IRecipeHandler> handler) {
	}

	@Deprecated
	public static void addRecipeCatalyst(List<ItemStack> stacks, Class<? extends IRecipeHandler> handler) {
	}
}
