package codechicken.nei.bridge;

import astryxion.nei.Internal;
import astryxion.nei.ItemRegistry;
import astryxion.nei.NearlyEnoughItems;
import codechicken.nei.ItemList;
import codechicken.nei.LayoutManager;

/**
 * Applies NEI item-list mutations (hide, variants, overrides) back into JEI.
 */
public final class NeiItemListSync {
	private NeiItemListSync() {
	}

	public static void markDirty() {
		ItemRegistry itemRegistry = Internal.getItemRegistry();
		if (itemRegistry != null) {
			itemRegistry.rebuild();
			ItemList.populateFrom(itemRegistry.getItemList());
			LayoutManager.itemsLoaded = true;
		}
		if (NearlyEnoughItems.getProxy() != null) {
			NearlyEnoughItems.getProxy().resetItemFilter();
		}
	}
}
