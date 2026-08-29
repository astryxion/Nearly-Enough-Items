package astryxion.nei.discovery;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

import astryxion.nei.util.Log;
import astryxion.nei.util.StackUtil;

/**
 * Turns 1.7.10 recipe-slot objects into ItemStacks. Vanilla/Forge use
 * ItemStack, ItemStack[], ore names, and lists. IC2 and similar mods put
 * {@code IRecipeInput}-style objects in the crafting grid instead.
 */
public final class IngredientExpander {
	private static final String[] STACK_LIST_METHODS = {
			"getInputs", "getInput", "getMatchingStacks", "getEquivalentStacks", "getStacks", "getItems"
	};
	private static final Set<String> loggedUnknown = new HashSet<String>();

	private IngredientExpander() {
	}

	public static List<ItemStack> expand(Object ingredient) {
		return expand(ingredient, 0);
	}

	private static List<ItemStack> expand(Object ingredient, int depth) {
		if (ingredient == null || depth > 8) {
			return Collections.emptyList();
		}

		if (ingredient instanceof ItemStack) {
			ItemStack stack = (ItemStack) ingredient;
			if (stack.getItem() == null) {
				return Collections.emptyList();
			}
			if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
				return copyAll(StackUtil.getSubtypes(stack));
			}
			return Collections.singletonList(stack.copy());
		}

		if (ingredient instanceof Item) {
			return copyAll(StackUtil.getSubtypes((Item) ingredient));
		}

		if (ingredient instanceof Block) {
			Item item = Item.getItemFromBlock((Block) ingredient);
			if (item == null) {
				return Collections.emptyList();
			}
			return copyAll(StackUtil.getSubtypes(item));
		}

		if (ingredient instanceof String) {
			return copyAll(OreDictionary.getOres((String) ingredient));
		}

		if (ingredient instanceof ItemStack[]) {
			return expandArray((ItemStack[]) ingredient, depth);
		}

		if (ingredient instanceof Object[]) {
			return expandArray((Object[]) ingredient, depth);
		}

		if (ingredient instanceof Iterable && !(ingredient instanceof ItemStack)) {
			List<ItemStack> combined = new ArrayList<ItemStack>();
			for (Object element : (Iterable<?>) ingredient) {
				if (element == ingredient) {
					continue;
				}
				combined.addAll(expand(element, depth + 1));
			}
			if (!combined.isEmpty()) {
				applyAmount(ingredient, combined);
				return combined;
			}
		}

		List<ItemStack> reflected = expandUnknown(ingredient, depth);
		if (reflected.isEmpty()) {
			logUnknown(ingredient);
		} else {
			applyAmount(ingredient, reflected);
		}
		return reflected;
	}

	private static List<ItemStack> expandArray(Object[] array, int depth) {
		List<ItemStack> combined = new ArrayList<ItemStack>();
		for (int i = 0; i < array.length; i++) {
			combined.addAll(expand(array[i], depth + 1));
		}
		return combined;
	}

	private static List<ItemStack> expandUnknown(Object ingredient, int depth) {
		for (int i = 0; i < STACK_LIST_METHODS.length; i++) {
			Object value = invokeNoArg(ingredient, STACK_LIST_METHODS[i]);
			if (value == null || value == ingredient) {
				continue;
			}
			List<ItemStack> expanded = expand(value, depth + 1);
			if (!expanded.isEmpty()) {
				return expanded;
			}
		}
		return Collections.emptyList();
	}

	private static void applyAmount(Object ingredient, List<ItemStack> stacks) {
		Integer amount = invokeInt(ingredient, "getAmount");
		if (amount == null || amount.intValue() <= 0) {
			return;
		}
		for (int i = 0; i < stacks.size(); i++) {
			ItemStack stack = stacks.get(i);
			if (stack != null) {
				stack.stackSize = amount.intValue();
			}
		}
	}

	private static Object invokeNoArg(Object target, String name) {
		Class<?> type = target.getClass();
		while (type != null && type != Object.class) {
			try {
				Method method = type.getDeclaredMethod(name);
				method.setAccessible(true);
				return method.invoke(target);
			} catch (NoSuchMethodException ignored) {
			} catch (Throwable t) {
				return null;
			}
			type = type.getSuperclass();
		}
		return null;
	}

	private static Integer invokeInt(Object target, String name) {
		Object value = invokeNoArg(target, name);
		if (value instanceof Integer) {
			return (Integer) value;
		}
		if (value instanceof Number) {
			return Integer.valueOf(((Number) value).intValue());
		}
		return null;
	}

	private static List<ItemStack> copyAll(List<ItemStack> stacks) {
		if (stacks == null || stacks.isEmpty()) {
			return Collections.emptyList();
		}
		List<ItemStack> copy = new ArrayList<ItemStack>(stacks.size());
		for (int i = 0; i < stacks.size(); i++) {
			ItemStack stack = stacks.get(i);
			if (stack != null && stack.getItem() != null) {
				copy.add(stack.copy());
			}
		}
		return copy;
	}

	private static void logUnknown(Object ingredient) {
		String name = ingredient.getClass().getName();
		if (loggedUnknown.add(name)) {
			Log.warning("[JEI-Discovery] Cannot expand crafting ingredient type {} ({})", name, ingredient);
		}
	}

	/**
	 * One crafting-grid slot: null if empty, a single ItemStack, or a list of
	 * alternatives (ore dict / IC2 IRecipeInput equivalents).
	 */
	public static Object normalizeSlot(Object ingredient) {
		if (ingredient == null) {
			return null;
		}
		if (ingredient instanceof ItemStack) {
			return ingredient;
		}
		List<ItemStack> stacks = expand(ingredient);
		if (stacks.isEmpty()) {
			return null;
		}
		if (stacks.size() == 1) {
			return stacks.get(0);
		}
		return stacks;
	}
}
