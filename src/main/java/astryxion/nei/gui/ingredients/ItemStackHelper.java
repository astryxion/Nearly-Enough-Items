package astryxion.nei.gui.ingredients;

import javax.annotation.Nonnull;
import java.util.Collection;

import net.minecraft.item.ItemStack;

import astryxion.nei.gui.Focus;
import astryxion.nei.util.StackUtil;

public class ItemStackHelper implements IIngredientHelper<ItemStack> {
	@Override
	public Collection<ItemStack> expandSubtypes(Collection<ItemStack> contained) {
		return StackUtil.getAllSubtypes(contained);
	}

	@Override
	public ItemStack getMatch(Iterable<ItemStack> contained, @Nonnull Focus toMatch) {
		return StackUtil.containsStack(contained, toMatch.getStack());
	}
}
