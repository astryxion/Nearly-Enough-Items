package codechicken.nei.api;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class TaggedInventoryArea {
	public String tag;
	public List<Integer> slots;
	public IInventory inventory;

	public TaggedInventoryArea(String name, List<Integer> slots, IInventory inventory) {
		this.tag = name;
		this.slots = slots;
		this.inventory = inventory;
	}

	public boolean isContainer() {
		return inventory instanceof net.minecraft.inventory.Container;
	}

	public ItemStack[] getStackArray(EntityPlayer player) {
		ItemStack[] stacks = new ItemStack[slots.size()];
		for (int i = 0; i < slots.size(); i++) {
			stacks[i] = inventory.getStackInSlot(slots.get(i).intValue());
		}
		return stacks;
	}
}
