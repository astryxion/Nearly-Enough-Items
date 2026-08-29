package codechicken.nei;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

import astryxion.nei.util.StackUtil;
import codechicken.nei.api.ItemFilter;

/**
 * Minimal ItemStackSet used by the NEI API (hide lists, subset filters).
 * Wildcard damage matches every metadata of that item.
 */
public class ItemStackSet implements ItemFilter {
	private final Set<String> exact = new HashSet<String>();
	private final Set<String> wildcards = new HashSet<String>();

	public ItemStackSet add(ItemStack item) {
		if (item == null || item.getItem() == null) {
			return this;
		}
		if (item.getMetadata() == OreDictionary.WILDCARD_VALUE) {
			wildcards.add(wildcardKey(item));
		} else {
			exact.add(StackUtil.getUniqueIdentifierForStack(item));
		}
		return this;
	}

	public boolean contains(ItemStack item) {
		if (item == null || item.getItem() == null) {
			return false;
		}
		if (wildcards.contains(wildcardKey(item))) {
			return true;
		}
		try {
			return exact.contains(StackUtil.getUniqueIdentifierForStack(item));
		} catch (RuntimeException e) {
			return false;
		}
	}

	public boolean containsAll(Item item) {
		return item != null && wildcards.contains(itemKey(item));
	}

	@Override
	public boolean matches(ItemStack item) {
		return contains(item);
	}

	private static String wildcardKey(ItemStack item) {
		return itemKey(item.getItem());
	}

	private static String itemKey(Item item) {
		try {
			return StackUtil.getUniqueIdentifierForStack(new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE), true);
		} catch (RuntimeException e) {
			return String.valueOf(item);
		}
	}
}
