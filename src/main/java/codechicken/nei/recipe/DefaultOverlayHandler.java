package codechicken.nei.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import codechicken.lib.inventory.InventoryUtils;
import codechicken.nei.FastTransferManager;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;

/**
 * Moves recipe ingredients from the player inventory into a crafting grid.
 * Tinkers Crafting Station and NEI Addons table handlers subclass this.
 */
public class DefaultOverlayHandler implements IOverlayHandler {

	public static class DistributedIngred {
		public DistributedIngred(ItemStack item) {
			stack = InventoryUtils.copyStack(item, 1);
		}

		public ItemStack stack;
		public int invAmount;
		public int distributed;
		public int numSlots;
		public int recipeAmount;
	}

	public static class IngredientDistribution {
		public IngredientDistribution(DistributedIngred distrib, ItemStack permutation) {
			this.distrib = distrib;
			this.permutation = permutation;
		}

		public DistributedIngred distrib;
		public ItemStack permutation;
		public Slot[] slots;
	}

	public int offsetx;
	public int offsety;

	public DefaultOverlayHandler() {
		this(5, 11);
	}

	public DefaultOverlayHandler(int x, int y) {
		this.offsetx = x;
		this.offsety = y;
	}

	@Override
	public void overlayRecipe(GuiContainer gui, IRecipeHandler recipe, int recipeIndex, boolean shift) {
		if (gui == null || recipe == null) {
			return;
		}
		List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
		if (ingredients == null || ingredients.isEmpty()) {
			return;
		}
		List<DistributedIngred> ingredStacks = getPermutationIngredients(ingredients);
		if (!clearIngredients(gui, ingredients)) {
			return;
		}
		findInventoryQuantities(gui, ingredStacks);
		List<IngredientDistribution> assignedIngredients = assignIngredients(ingredients, ingredStacks);
		if (assignedIngredients == null) {
			return;
		}
		assignIngredSlots(gui, ingredients, assignedIngredients);
		int quantity = calculateRecipeQuantity(assignedIngredients);
		if (quantity != 0) {
			moveIngredients(gui, assignedIngredients, quantity);
		}
	}

	@SuppressWarnings("unchecked")
	protected boolean clearIngredients(GuiContainer gui, List<PositionedStack> ingreds) {
		for (PositionedStack pstack : ingreds) {
			for (Slot slot : (List<Slot>) gui.inventorySlots.inventorySlots) {
				if (slot.xDisplayPosition == pstack.relx + offsetx && slot.yDisplayPosition == pstack.rely + offsety) {
					if (!slot.getHasStack()) {
						continue;
					}
					FastTransferManager.clickSlot(gui, slot.slotNumber, 0, 1);
					if (slot.getHasStack()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	protected void moveIngredients(GuiContainer gui, List<IngredientDistribution> assignedIngredients, int quantity) {
		for (IngredientDistribution distrib : assignedIngredients) {
			if (distrib.slots == null || distrib.slots.length == 0 || distrib.permutation == null) {
				continue;
			}
			ItemStack pstack = distrib.permutation;
			int transferCap = quantity * Math.max(pstack.stackSize, 1);
			int transferred = 0;
			int destSlotIndex = 0;
			Slot dest = distrib.slots[0];
			int slotTransferred = 0;
			int slotTransferCap = pstack.getMaxStackSize();

			for (Slot slot : (List<Slot>) gui.inventorySlots.inventorySlots) {
				if (!slot.getHasStack() || !canMoveFrom(slot, gui)) {
					continue;
				}
				ItemStack stack = slot.getStack();
				if (!canStack(stack, pstack)) {
					continue;
				}
				FastTransferManager.clickSlot(gui, slot.slotNumber);
				int amount = Math.min(transferCap - transferred, stack.stackSize);
				for (int c = 0; c < amount; c++) {
					FastTransferManager.clickSlot(gui, dest.slotNumber, 1, 0);
					transferred++;
					slotTransferred++;
					if (slotTransferred >= slotTransferCap) {
						destSlotIndex++;
						if (destSlotIndex == distrib.slots.length) {
							dest = null;
							break;
						}
						dest = distrib.slots[destSlotIndex];
						slotTransferred = 0;
					}
				}
				FastTransferManager.clickSlot(gui, slot.slotNumber);
				if (transferred >= transferCap || dest == null) {
					break;
				}
			}
		}
	}

	protected int calculateRecipeQuantity(List<IngredientDistribution> assignedIngredients) {
		int quantity = Integer.MAX_VALUE;
		for (IngredientDistribution distrib : assignedIngredients) {
			DistributedIngred istack = distrib.distrib;
			if (istack.numSlots == 0) {
				return 0;
			}
			if (istack.distributed == 0) {
				continue;
			}
			int allSlots = istack.invAmount;
			if (istack.numSlots > 0 && allSlots / istack.numSlots > istack.stack.getMaxStackSize()) {
				allSlots = istack.numSlots * istack.stack.getMaxStackSize();
			}
			quantity = Math.min(quantity, allSlots / istack.distributed);
		}
		return quantity == Integer.MAX_VALUE ? 0 : quantity;
	}

	protected Slot[][] assignIngredSlots(GuiContainer gui, List<PositionedStack> ingredients, List<IngredientDistribution> assignedIngredients) {
		Slot[][] recipeSlots = mapIngredSlots(gui, ingredients);
		HashMap<Slot, Integer> distribution = new HashMap<Slot, Integer>();
		for (int i = 0; i < recipeSlots.length; i++) {
			for (int j = 0; j < recipeSlots[i].length; j++) {
				Slot slot = recipeSlots[i][j];
				if (!distribution.containsKey(slot)) {
					distribution.put(slot, -1);
				}
			}
		}

		HashSet<Slot> availableSlots = new HashSet<Slot>(distribution.keySet());
		HashSet<Integer> remainingIngreds = new HashSet<Integer>();
		ArrayList<LinkedList<Slot>> assignedSlots = new ArrayList<LinkedList<Slot>>();
		for (int i = 0; i < ingredients.size(); i++) {
			remainingIngreds.add(i);
			assignedSlots.add(new LinkedList<Slot>());
		}

		while (!availableSlots.isEmpty() && !remainingIngreds.isEmpty()) {
			for (Iterator<Integer> iterator = remainingIngreds.iterator(); iterator.hasNext();) {
				int i = iterator.next();
				boolean assigned = false;
				DistributedIngred istack = assignedIngredients.get(i).distrib;
				for (int s = 0; s < recipeSlots[i].length; s++) {
					Slot slot = recipeSlots[i][s];
					if (availableSlots.contains(slot)) {
						availableSlots.remove(slot);
						if (slot.getHasStack()) {
							continue;
						}
						istack.numSlots++;
						assignedSlots.get(i).add(slot);
						assigned = true;
						break;
					}
				}
				if (!assigned || istack.numSlots * istack.stack.getMaxStackSize() >= istack.invAmount) {
					iterator.remove();
				}
			}
		}

		for (int i = 0; i < ingredients.size(); i++) {
			assignedIngredients.get(i).slots = assignedSlots.get(i).toArray(new Slot[assignedSlots.get(i).size()]);
		}
		return recipeSlots;
	}

	protected List<IngredientDistribution> assignIngredients(List<PositionedStack> ingredients, List<DistributedIngred> ingredStacks) {
		ArrayList<IngredientDistribution> assignedIngredients = new ArrayList<IngredientDistribution>();
		for (PositionedStack posstack : ingredients) {
			DistributedIngred biggestIngred = null;
			ItemStack permutation = null;
			int biggestSize = 0;
			for (int p = 0; p < posstack.items.length; p++) {
				ItemStack pstack = posstack.items[p];
				if (pstack == null || pstack.stackSize == 0) {
					continue;
				}
				for (int j = 0; j < ingredStacks.size(); j++) {
					DistributedIngred istack = ingredStacks.get(j);
					if (!canStack(pstack, istack.stack) || istack.invAmount - istack.distributed < pstack.stackSize || istack.recipeAmount == 0) {
						continue;
					}
					int relsize = (istack.invAmount - istack.invAmount / istack.recipeAmount * istack.distributed) / pstack.stackSize;
					if (relsize > biggestSize) {
						biggestSize = relsize;
						biggestIngred = istack;
						permutation = pstack;
						break;
					}
				}
			}
			if (biggestIngred == null) {
				return null;
			}
			biggestIngred.distributed += permutation.stackSize;
			assignedIngredients.add(new IngredientDistribution(biggestIngred, permutation));
		}
		return assignedIngredients;
	}

	@SuppressWarnings("unchecked")
	protected void findInventoryQuantities(GuiContainer gui, List<DistributedIngred> ingredStacks) {
		for (Slot slot : (List<Slot>) gui.inventorySlots.inventorySlots) {
			if (slot.getHasStack() && canMoveFrom(slot, gui)) {
				ItemStack pstack = slot.getStack();
				DistributedIngred istack = findIngred(ingredStacks, pstack);
				if (istack != null) {
					istack.invAmount += pstack.stackSize;
				}
			}
		}
	}

	protected List<DistributedIngred> getPermutationIngredients(List<PositionedStack> ingredients) {
		ArrayList<DistributedIngred> ingredStacks = new ArrayList<DistributedIngred>();
		for (PositionedStack posstack : ingredients) {
			if (posstack == null || posstack.items == null) {
				continue;
			}
			for (int i = 0; i < posstack.items.length; i++) {
				ItemStack pstack = posstack.items[i];
				if (pstack == null) {
					continue;
				}
				DistributedIngred istack = findIngred(ingredStacks, pstack);
				if (istack == null) {
					istack = new DistributedIngred(pstack);
					ingredStacks.add(istack);
				}
				istack.recipeAmount += pstack.stackSize;
			}
		}
		return ingredStacks;
	}

	public boolean canMoveFrom(Slot slot, GuiContainer gui) {
		return slot.inventory instanceof InventoryPlayer;
	}

	@SuppressWarnings("unchecked")
	public Slot[][] mapIngredSlots(GuiContainer gui, List<PositionedStack> ingredients) {
		Slot[][] recipeSlotList = new Slot[ingredients.size()][];
		for (int i = 0; i < ingredients.size(); i++) {
			LinkedList<Slot> recipeSlots = new LinkedList<Slot>();
			PositionedStack pstack = ingredients.get(i);
			for (Slot slot : (List<Slot>) gui.inventorySlots.inventorySlots) {
				if (slot.xDisplayPosition == pstack.relx + offsetx && slot.yDisplayPosition == pstack.rely + offsety) {
					recipeSlots.add(slot);
					break;
				}
			}
			recipeSlotList[i] = recipeSlots.toArray(new Slot[recipeSlots.size()]);
		}
		return recipeSlotList;
	}

	public DistributedIngred findIngred(List<DistributedIngred> ingredStacks, ItemStack pstack) {
		for (int i = 0; i < ingredStacks.size(); i++) {
			DistributedIngred istack = ingredStacks.get(i);
			if (canStack(pstack, istack.stack)) {
				return istack;
			}
		}
		return null;
	}

	protected boolean canStack(ItemStack dst, ItemStack src) {
		if (dst == null || src == null) {
			return true;
		}
		return NEIServerUtils.areStacksSameTypeCrafting(dst, src);
	}
}
