package astryxion.nei;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

import cpw.mods.fml.common.registry.GameRegistry;

import astryxion.nei.api.IItemRegistry;
import astryxion.nei.discovery.DiscoveryReport;
import astryxion.nei.discovery.ItemDiscovery;
import astryxion.nei.util.Log;
import astryxion.nei.util.ModList;
import astryxion.nei.util.StackUtil;

public class ItemRegistry implements IItemRegistry {

	@Nonnull
	private final Set<String> itemNameSet = new HashSet<String>();
	@Nonnull
	private ImmutableList<ItemStack> itemList;
	@Nonnull
	private ImmutableListMultimap<String, ItemStack> itemsByModId;
	@Nonnull
	private ImmutableList<ItemStack> potionIngredients;
	@Nonnull
	private ImmutableList<ItemStack> fuels;
	@Nonnull
	private final ModList modList;

	public ItemRegistry() {
		this.modList = new ModList();
		rebuild();
	}

	/**
	 * Rebuilds the item panel list from Minecraft/Forge registries plus any
	 * NEI API overrides/variants registered so far.
	 */
	public void rebuild() {
		itemNameSet.clear();

		ItemDiscovery.Result discovered = ItemDiscovery.discover();
		List<ItemStack> uniqueStacks = discovered.uniqueStacks();
		for (int i = 0; i < uniqueStacks.size(); i++) {
			itemNameSet.add(StackUtil.getUniqueIdentifierForStack(uniqueStacks.get(i)));
		}

		ImmutableList.Builder<ItemStack> fuelsBuilder = ImmutableList.builder();
		for (int i = 0; i < uniqueStacks.size(); i++) {
			ItemStack stack = uniqueStacks.get(i);
			try {
				if (TileEntityFurnace.isItemFuel(stack)) {
					fuelsBuilder.add(stack);
				}
			} catch (Throwable t) {
				Log.debug("Failed to check fuel status for {}", stack, t);
			}
		}

		this.itemList = ImmutableList.copyOf(uniqueStacks);
		this.fuels = fuelsBuilder.build();

		ImmutableListMultimap.Builder<String, ItemStack> itemsByModIdBuilder = ImmutableListMultimap.builder();
		for (int i = 0; i < uniqueStacks.size(); i++) {
			ItemStack itemStack = uniqueStacks.get(i);
			Item item = itemStack.getItem();
			if (item != null) {
				try {
					GameRegistry.UniqueIdentifier uniqueIdentifier = GameRegistry.findUniqueIdentifierFor(item);
					if (uniqueIdentifier != null && uniqueIdentifier.modId != null) {
						itemsByModIdBuilder.put(uniqueIdentifier.modId.toLowerCase(Locale.ENGLISH), itemStack);
					}
				} catch (Throwable t) {
					Log.debug("Failed to resolve mod id for item {}", item, t);
				}
			}
		}
		this.itemsByModId = itemsByModIdBuilder.build();

		ImmutableList.Builder<ItemStack> potionIngredientBuilder = ImmutableList.builder();
		for (int i = 0; i < this.itemList.size(); i++) {
			ItemStack itemStack = this.itemList.get(i);
			try {
				if (itemStack.getItem() != null && itemStack.getItem().isPotionIngredient(itemStack)) {
					potionIngredientBuilder.add(itemStack);
				}
			} catch (Throwable t) {
				Log.debug("Failed to check potion ingredient for {}", itemStack, t);
			}
		}
		this.potionIngredients = potionIngredientBuilder.build();

		DiscoveryReport report = DiscoveryReport.getLast();
		report.setRegisteredItems(discovered.registeredItems);
		report.setRegisteredBlocks(discovered.registeredBlocks);
		report.setItemStackVariants(this.itemList.size());
		report.setHiddenItemStacks(discovered.hiddenItemStacks);
	}

	public boolean addItemStack(@Nonnull ItemStack stack) {
		if (stack == null || stack.getItem() == null) {
			return false;
		}
		try {
			String itemKey = StackUtil.getUniqueIdentifierForStack(stack);
			if (!itemNameSet.add(itemKey)) {
				return false;
			}

			java.util.ArrayList<ItemStack> mutable = new java.util.ArrayList<ItemStack>(itemList);
			mutable.add(stack);
			this.itemList = ImmutableList.copyOf(mutable);

			try {
				if (TileEntityFurnace.isItemFuel(stack)) {
					java.util.ArrayList<ItemStack> fuelMutable = new java.util.ArrayList<ItemStack>(fuels);
					fuelMutable.add(stack);
					this.fuels = ImmutableList.copyOf(fuelMutable);
				}
			} catch (Throwable ignored) {
			}

			try {
				if (stack.getItem().isPotionIngredient(stack)) {
					java.util.ArrayList<ItemStack> potionMutable = new java.util.ArrayList<ItemStack>(potionIngredients);
					potionMutable.add(stack);
					this.potionIngredients = ImmutableList.copyOf(potionMutable);
				}
			} catch (Throwable ignored) {
			}

			try {
				GameRegistry.UniqueIdentifier uniqueIdentifier = GameRegistry.findUniqueIdentifierFor(stack.getItem());
				if (uniqueIdentifier != null && uniqueIdentifier.modId != null) {
					ImmutableListMultimap.Builder<String, ItemStack> builder = ImmutableListMultimap.builder();
					builder.putAll(itemsByModId);
					builder.put(uniqueIdentifier.modId.toLowerCase(Locale.ENGLISH), stack);
					this.itemsByModId = builder.build();
				}
			} catch (Throwable ignored) {
			}

			DiscoveryReport.getLast().setItemStackVariants(this.itemList.size());
			return true;
		} catch (RuntimeException e) {
			Log.error("Couldn't add itemStack {}.", stack.getClass(), e);
			return false;
		}
	}

	@Override
	@Nonnull
	public ImmutableList<ItemStack> getItemList() {
		return itemList;
	}

	@Override
	@Nonnull
	public ImmutableList<ItemStack> getFuels() {
		return fuels;
	}

	@Override
	@Nonnull
	public ImmutableList<ItemStack> getPotionIngredients() {
		return potionIngredients;
	}

	@Nonnull
	@Override
	public String getModNameForItem(@Nullable Item item) {
		if (item == null) {
			Log.error("Null item", new NullPointerException());
			return "";
		}
		return modList.getModNameForItem(item);
	}

	@Nonnull
	@Override
	public ImmutableList<ItemStack> getItemListForModId(@Nullable String modId) {
		if (modId == null) {
			Log.error("Null modId", new NullPointerException());
			return ImmutableList.of();
		}
		String lowerCaseModId = modId.toLowerCase(Locale.ENGLISH);
		return itemsByModId.get(lowerCaseModId);
	}
}
