package codechicken.nei;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

import codechicken.nei.bridge.NeiCompatLog;

public class ItemSorter implements Comparator<ItemStack> {
	public static ItemSorter instance = new ItemSorter();
	public Map<ItemStack, Integer> ordering = new HashMap<ItemStack, Integer>();

	public static void add(String name, Comparator<ItemStack> comparator) {
		NeiCompatLog.unsupported("ItemSorter.add(" + name + ")");
	}

	@Override
	public int compare(ItemStack o1, ItemStack o2) {
		Integer a = ordering.get(o1);
		Integer b = ordering.get(o2);
		int ia = a == null ? 0 : a.intValue();
		int ib = b == null ? 0 : b.intValue();
		return ia - ib;
	}
}
