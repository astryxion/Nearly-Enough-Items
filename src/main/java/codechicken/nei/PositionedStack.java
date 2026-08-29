package codechicken.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

/**
 * An {@link ItemStack} with a GUI position. The object accepted by the
 * constructor may be an ItemStack, ItemStack[], List of ItemStacks, or ore
 * name / ore list as NEI addons expect.
 */
public class PositionedStack implements Cloneable {
	public int relx;
	public int rely;
	public int width = 16;
	public int height = 16;
	public ItemStack[] items;
	public ItemStack item;

	protected boolean permutated = false;

	public PositionedStack(Object object, int x, int y, boolean genPerms) {
		items = NEIServerUtils.extractRecipeItems(object);
		relx = x;
		rely = y;
		if (genPerms) {
			generatePermutations();
		} else {
			setPermutationToRender(0);
		}
	}

	public PositionedStack(Object object, int x, int y) {
		this(object, x, y, true);
	}

	public void generatePermutations() {
		if (permutated) {
			return;
		}

		List<ItemStack> stacks = new ArrayList<ItemStack>();
		for (int i = 0; i < items.length; i++) {
			ItemStack candidate = items[i];
			if (candidate == null || candidate.getItem() == null) {
				continue;
			}
			if (candidate.getMetadata() == OreDictionary.WILDCARD_VALUE || candidate.getMetadata() == Short.MAX_VALUE) {
				List<ItemStack> permutations = ItemList.itemMap.get(candidate.getItem());
				if (permutations != null && !permutations.isEmpty()) {
					for (int p = 0; p < permutations.size(); p++) {
						ItemStack toAdd = permutations.get(p).copy();
						toAdd.stackSize = candidate.stackSize;
						stacks.add(toAdd);
					}
				} else {
					List<ItemStack> subtypes = astryxion.nei.util.StackUtil.getSubtypes(candidate.getItem());
					if (subtypes.isEmpty()) {
						ItemStack base = new ItemStack(candidate.getItem(), candidate.stackSize);
						base.stackTagCompound = candidate.stackTagCompound;
						stacks.add(base);
					} else {
						for (int s = 0; s < subtypes.size(); s++) {
							ItemStack toAdd = subtypes.get(s).copy();
							toAdd.stackSize = candidate.stackSize;
							stacks.add(toAdd);
						}
					}
				}
				continue;
			}
			stacks.add(candidate.copy());
		}

		if (stacks.isEmpty()) {
			items = new ItemStack[] { new ItemStack(Blocks.fire) };
		} else {
			items = stacks.toArray(new ItemStack[stacks.size()]);
		}
		permutated = true;
		setPermutationToRender(0);
	}

	public void setMaxSize(int i) {
		for (int n = 0; n < items.length; n++) {
			if (items[n] != null && items[n].stackSize > i) {
				items[n].stackSize = i;
			}
		}
	}

	public PositionedStack copy() {
		try {
			PositionedStack pStack = (PositionedStack) super.clone();
			pStack.items = new ItemStack[items.length];
			for (int i = 0; i < items.length; i++) {
				pStack.items[i] = items[i] == null ? null : items[i].copy();
			}
			pStack.item = item == null ? null : item.copy();
			return pStack;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public void setPermutationToRender(int index) {
		if (items.length == 0) {
			item = new ItemStack(Blocks.fire);
			return;
		}
		if (index < 0 || index >= items.length) {
			index = 0;
		}
		this.item = items[index] == null ? new ItemStack(Blocks.fire) : items[index].copy();
		if (this.item.getItem() == null) {
			this.item = new ItemStack(Blocks.fire);
		} else if (this.item.getMetadata() == OreDictionary.WILDCARD_VALUE && this.item.getItem().isDamageable()) {
			this.item.setMetadata(0);
		}
	}

	public boolean contains(ItemStack ingredient) {
		for (int i = 0; i < items.length; i++) {
			if (NEIServerUtils.areStacksSameTypeCrafting(items[i], ingredient)) {
				return true;
			}
		}
		return false;
	}

	public boolean contains(Item ingred) {
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null && items[i].getItem() == ingred) {
				return true;
			}
		}
		return false;
	}

	public List<ItemStack> getItemList() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				list.add(items[i]);
			}
		}
		return list;
	}
}
