package codechicken.nei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import codechicken.nei.api.ItemFilter;
import codechicken.nei.api.ItemFilter.ItemFilterProvider;
import codechicken.nei.api.ItemInfo;

/**
 * Compatibility facade for NEI's global item list. Contents are populated from
 * the JEI item registry after discovery.
 */
public class ItemList {
	public static volatile List<ItemStack> items = new ArrayList<ItemStack>();
	public static volatile ListMultimap<Item, ItemStack> itemMap = ArrayListMultimap.create();
	public static final List<ItemFilterProvider> itemFilterers = new LinkedList<ItemFilterProvider>();
	public static final List<ItemsLoadedCallback> loadCallbacks = new LinkedList<ItemsLoadedCallback>();
	public static boolean loadFinished;

	public static final RestartableTask loadItems = new RestartableTask("NEI Item Loading") {
		@Override
		public void execute() {
			try {
				astryxion.nei.ItemRegistry registry = astryxion.nei.Internal.getItemRegistry();
				if (registry == null) {
					registry = new astryxion.nei.ItemRegistry();
					astryxion.nei.Internal.setItemRegistry(registry);
				}
				populateFrom(registry.getItemList());
				LayoutManager.itemsLoaded = true;
			} catch (Throwable t) {
				codechicken.nei.bridge.NeiCompatLog.warn("ItemList.loadItems failed: {}", t);
			}
		}
	};

	@Deprecated
	public static void loadItems() {
		loadItems.restart();
	}

	public static class EverythingItemFilter implements ItemFilter {
		@Override
		public boolean matches(ItemStack item) {
			return true;
		}
	}

	public static class NothingItemFilter implements ItemFilter {
		@Override
		public boolean matches(ItemStack item) {
			return false;
		}
	}

	public static class NegatedItemFilter implements ItemFilter {
		public ItemFilter filter;

		public NegatedItemFilter(ItemFilter filter) {
			this.filter = filter;
		}

		@Override
		public boolean matches(ItemStack item) {
			return this.filter == null || !this.filter.matches(item);
		}
	}

	public static class PatternItemFilter implements ItemFilter {
		public Pattern pattern;

		public PatternItemFilter(Pattern pattern) {
			this.pattern = pattern;
		}

		@Override
		public boolean matches(ItemStack item) {
			if (item == null || pattern == null) {
				return false;
			}
			try {
				String displayName = EnumChatFormatting.getTextWithoutFormattingCodes(item.getDisplayName());
				if (displayName != null && !displayName.isEmpty() && pattern.matcher(displayName).find()) {
					return true;
				}
				List<String> aliases = ItemInfo.getAliases(item);
				for (int i = 0; i < aliases.size(); i++) {
					if (pattern.matcher(aliases.get(i)).find()) {
						return true;
					}
				}
			} catch (Throwable ignored) {
			}
			return false;
		}
	}

	public static class AllMultiItemFilter implements ItemFilter {
		public List<ItemFilter> filters;

		public AllMultiItemFilter(List<ItemFilter> filters) {
			this.filters = filters;
		}

		public AllMultiItemFilter(ItemFilter... filters) {
			this(new LinkedList<ItemFilter>(Arrays.asList(filters)));
		}

		public AllMultiItemFilter() {
			this(new LinkedList<ItemFilter>());
		}

		@Override
		public boolean matches(ItemStack item) {
			for (int i = 0; i < filters.size(); i++) {
				ItemFilter filter = filters.get(i);
				try {
					if (filter != null && !filter.matches(item)) {
						return false;
					}
				} catch (Exception ignored) {
				}
			}
			return true;
		}
	}

	public static class AnyMultiItemFilter implements ItemFilter {
		public List<ItemFilter> filters;

		public AnyMultiItemFilter(List<ItemFilter> filters) {
			this.filters = filters;
		}

		public AnyMultiItemFilter() {
			this(new LinkedList<ItemFilter>());
		}

		@Override
		public boolean matches(ItemStack item) {
			for (int i = 0; i < filters.size(); i++) {
				ItemFilter filter = filters.get(i);
				try {
					if (filter != null && filter.matches(item)) {
						return true;
					}
				} catch (Exception ignored) {
				}
			}
			return false;
		}
	}

	public interface ItemsLoadedCallback {
		void itemsLoaded();
	}

	public static boolean itemMatchesAll(ItemStack item, List<ItemFilter> filters) {
		for (int i = 0; i < filters.size(); i++) {
			ItemFilter filter = filters.get(i);
			try {
				if (filter != null && !filter.matches(item)) {
					return false;
				}
			} catch (Exception ignored) {
			}
		}
		return true;
	}

	@Deprecated
	public static boolean itemMatches(ItemStack item) {
		return getItemListFilter().matches(item);
	}

	public static ItemFilter getItemListFilter() {
		return new AllMultiItemFilter(getItemFilters());
	}

	public static List<ItemFilter> getItemFilters() {
		LinkedList<ItemFilter> filters = new LinkedList<ItemFilter>();
		synchronized (itemFilterers) {
			for (int i = 0; i < itemFilterers.size(); i++) {
				filters.add(itemFilterers.get(i).getFilter());
			}
		}
		return filters;
	}

	public static void populateFrom(List<ItemStack> discovered) {
		List<ItemStack> copy = new ArrayList<ItemStack>(discovered);
		ListMultimap<Item, ItemStack> map = ArrayListMultimap.create();
		for (int i = 0; i < copy.size(); i++) {
			ItemStack stack = copy.get(i);
			if (stack != null && stack.getItem() != null) {
				map.put(stack.getItem(), stack);
			}
		}
		items = copy;
		itemMap = map;
		loadFinished = true;
		for (int i = 0; i < loadCallbacks.size(); i++) {
			try {
				loadCallbacks.get(i).itemsLoaded();
			} catch (Throwable ignored) {
			}
		}
	}
}
