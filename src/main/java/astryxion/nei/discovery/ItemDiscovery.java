package astryxion.nei.discovery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import astryxion.nei.util.Log;
import astryxion.nei.util.StackUtil;
import codechicken.nei.api.ItemInfo;

/**
 * Universal item-list generation for 1.7.10, modeled on NEI's permutation
 * pipeline (overrides → getSubItems → damage search → variants) without
 * copying NEI internals.
 */
public final class ItemDiscovery {
	private static final int DAMAGE_SEARCH_RANGE = 16;
	private static final Set<String> DAMAGE_SEARCH_ERRORS = new HashSet<String>();

	private ItemDiscovery() {
	}

	public static Result discover() {
		Result result = new Result();

		for (Object blockObj : Block.blockRegistry) {
			if (!(blockObj instanceof Block)) {
				continue;
			}
			Block block = (Block) blockObj;
			Item item = Item.getItemFromBlock(block);
			if (item != null) {
				result.registeredBlocks++;
			}
		}

		for (Object itemObj : Item.itemRegistry) {
			if (!(itemObj instanceof Item)) {
				continue;
			}
			Item item = (Item) itemObj;
			result.registeredItems++;
			List<ItemStack> permutations = getPermutations(item);
			for (int i = 0; i < permutations.size(); i++) {
				ItemStack stack = permutations.get(i);
				if (stack == null || stack.getItem() == null) {
					continue;
				}
				if (ItemInfo.isHidden(stack)) {
					result.hiddenItemStacks++;
					continue;
				}
				result.stacks.add(stack);
			}
		}

		addEnchantedBooks(result);
		return result;
	}

	/**
	 * NEI permutation order: API overrides replace getSubItems; if those are
	 * empty, damage 0-15 is scanned for unique name/icon/tooltip combinations;
	 * API variants are always appended.
	 */
	public static List<ItemStack> getPermutations(Item item) {
		List<ItemStack> permutations = new ArrayList<ItemStack>();
		if (item == null) {
			return permutations;
		}

		List<ItemStack> overrides = ItemInfo.getItemOverrides(item);
		if (overrides != null && !overrides.isEmpty()) {
			permutations.addAll(overrides);
		} else {
			collectSubItems(item, permutations);
			if (permutations.isEmpty()) {
				damageSearch(item, permutations);
			}
			if (permutations.isEmpty()) {
				permutations.add(new ItemStack(item));
			}
		}

		List<ItemStack> variants = ItemInfo.getItemVariants(item);
		if (variants != null && !variants.isEmpty()) {
			permutations.addAll(variants);
		}

		return permutations;
	}

	private static void collectSubItems(Item item, List<ItemStack> out) {
		CreativeTabs[] tabs = item.getCreativeTabs();
		boolean collectedFromTab = false;
		if (tabs != null) {
			for (int i = 0; i < tabs.length; i++) {
				CreativeTabs tab = tabs[i];
				if (tab == null) {
					continue;
				}
				List<ItemStack> tabItems = new ArrayList<ItemStack>();
				try {
					item.getSubItems(item, tab, tabItems);
					collectedFromTab = true;
				} catch (Throwable t) {
					Log.error("Failed to get sub items for item {} on tab {}", item.getUnlocalizedName(), tab, t);
				}
				out.addAll(tabItems);
			}
		}

		if (!collectedFromTab || out.isEmpty()) {
			List<ItemStack> nullTabItems = new ArrayList<ItemStack>();
			try {
				item.getSubItems(item, null, nullTabItems);
			} catch (Throwable ignored) {
				// Some items NPE on a null creative tab; damage search covers them.
			}
			if (!nullTabItems.isEmpty()) {
				out.clear();
				out.addAll(nullTabItems);
			}
		}
	}

	/**
	 * NEI scans damage 0-15 when getSubItems produced nothing. Distinct
	 * display name + icon + tooltip combinations are kept.
	 */
	private static void damageSearch(Item item, List<ItemStack> permutations) {
		Set<String> seen = new LinkedHashSet<String>();
		EntityPlayer player = Minecraft.getMinecraft() != null ? Minecraft.getMinecraft().thePlayer : null;

		for (int damage = 0; damage < DAMAGE_SEARCH_RANGE; damage++) {
			try {
				ItemStack stack = new ItemStack(item, 1, damage);
				String key = permutationKey(stack, player);
				if (seen.add(key)) {
					permutations.add(stack);
				}
			} catch (Throwable t) {
				String id = String.valueOf(item) + ':' + damage;
				if (DAMAGE_SEARCH_ERRORS.add(id)) {
					Log.debug("Omitting {}:{} ({})", item, Integer.valueOf(damage), t.toString());
				}
			}
		}
	}

	private static String permutationKey(ItemStack stack, EntityPlayer player) {
		StringBuilder builder = new StringBuilder();
		try {
			builder.append(stack.getDisplayName());
		} catch (Throwable t) {
			builder.append('?');
		}
		builder.append('@');
		try {
			IIcon icon = stack.getItem().getIcon(stack, 0);
			builder.append(icon == null ? 0 : icon.hashCode());
		} catch (Throwable t) {
			builder.append(0);
		}
		builder.append('@');
		if (player != null) {
			try {
				List<String> tooltip = new ArrayList<String>();
				stack.getItem().addInformation(stack, player, tooltip, false);
				for (int i = 0; i < tooltip.size(); i++) {
					if (i > 0) {
						builder.append('\n');
					}
					builder.append(tooltip.get(i));
				}
			} catch (Throwable ignored) {
			}
		}
		return builder.toString();
	}

	private static void addEnchantedBooks(Result result) {
		Enchantment[] books = Enchantment.enchantmentsBookList;
		if (books == null) {
			return;
		}
		for (int i = 0; i < books.length; i++) {
			Enchantment enchantment = books[i];
			if (enchantment == null || enchantment.type == null) {
				continue;
			}
			try {
				EnchantmentData data = new EnchantmentData(enchantment, enchantment.getMaxLevel());
				ItemStack book = Items.enchanted_book.getEnchantedItemStack(data);
				if (book != null && book.getItem() != null && !ItemInfo.isHidden(book)) {
					result.stacks.add(book);
				}
			} catch (Throwable t) {
				Log.debug("Failed to add enchanted book for {}", enchantment, t);
			}
		}
	}

	public static class Result {
		public int registeredItems;
		public int registeredBlocks;
		public int hiddenItemStacks;
		public final List<ItemStack> stacks = new ArrayList<ItemStack>();

		public List<ItemStack> uniqueStacks() {
			List<ItemStack> unique = new ArrayList<ItemStack>();
			Set<String> seen = new HashSet<String>();
			for (int i = 0; i < stacks.size(); i++) {
				ItemStack stack = stacks.get(i);
				if (stack == null || stack.getItem() == null) {
					continue;
				}
				try {
					String key = StackUtil.getUniqueIdentifierForStack(stack);
					if (seen.add(key)) {
						unique.add(stack);
					}
				} catch (RuntimeException e) {
					Log.error("Couldn't create unique name for itemStack {}.", stack.getClass(), e);
				}
			}
			return unique;
		}
	}
}
