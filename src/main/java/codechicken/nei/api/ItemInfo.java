package codechicken.nei.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.google.common.collect.ArrayListMultimap;

import codechicken.nei.ItemList;
import codechicken.nei.ItemStackSet;
import codechicken.nei.bridge.NeiCompatLog;

/**
 * Storage for item-panel overrides, hidden items, and related NEI API state.
 * Addons read these fields; the JEI item discovery layer honors them.
 */
public class ItemInfo {
	public enum Layout {
		HEADER,
		BODY,
		FOOTER;

		public static final Layout[] VALUES = values();
	}

	public static final ArrayListMultimap<Layout, IHighlightHandler> highlightHandlers = ArrayListMultimap.create();
	public static final Map<ItemStack, String> nameOverrides = new HashMap<ItemStack, String>();
	public static final Map<ItemStack, List<String>> itemAliases = new HashMap<ItemStack, List<String>>();
	public static final ItemStackSet hiddenItems = new ItemStackSet();
	public static final ItemList.AnyMultiItemFilter hiddenItemsRules = new ItemList.AnyMultiItemFilter();
	public static final ItemStackSet finiteItems = new ItemStackSet();
	public static final ArrayListMultimap<Item, ItemStack> itemOverrides = ArrayListMultimap.create();
	public static final ArrayListMultimap<Item, ItemStack> itemVariants = ArrayListMultimap.create();
	public static final LinkedList<IInfiniteItemHandler> infiniteHandlers = new LinkedList<IInfiniteItemHandler>();
	public static final ArrayListMultimap<Block, IHighlightHandler> highlightIdentifiers = ArrayListMultimap.create();
	public static final HashSet<Class<? extends Slot>> fastTransferExemptions = new HashSet<Class<? extends Slot>>();
	public static final HashMap<Item, String> itemOwners = new HashMap<Item, String>();

	public static boolean isHidden(ItemStack stack) {
		if (stack == null) {
			return false;
		}
		try {
			if (hiddenItems.contains(stack)) {
				return true;
			}
			if (hiddenItemsRules != null && hiddenItemsRules.matches(stack)) {
				return true;
			}
		} catch (Throwable t) {
			NeiCompatLog.warn("ItemInfo.isHidden failed for {}", stack, t);
		}
		return false;
	}

	@Deprecated
	public static boolean isHidden(Item item) {
		return hiddenItems.containsAll(item);
	}

	public static String getNameOverride(ItemStack stack) {
		return nameOverrides.get(stack);
	}

	public static List<String> getAliases(ItemStack stack) {
		List<String> aliases = itemAliases.get(stack);
		return aliases != null ? aliases : Collections.<String>emptyList();
	}

	public static boolean canBeInfinite(ItemStack stack) {
		return !finiteItems.contains(stack);
	}

	@Deprecated
	public static List<ItemStack> getItemOverrides(Item item) {
		if (item == null) {
			return Collections.emptyList();
		}
		return itemOverrides.get(item);
	}

	public static List<ItemStack> getItemVariants(Item item) {
		if (item == null) {
			return Collections.emptyList();
		}
		return itemVariants.get(item);
	}

	public static void registerHighlightHandler(IHighlightHandler handler, Layout... layout) {
		if (handler == null || layout == null) {
			return;
		}
		for (int i = 0; i < layout.length; i++) {
			highlightHandlers.put(layout[i], handler);
		}
	}

	public static void preInit() {
	}

	public static void load() {
	}
}
