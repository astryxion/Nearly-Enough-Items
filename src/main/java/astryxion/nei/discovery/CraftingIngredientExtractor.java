package astryxion.nei.discovery;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;

import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import astryxion.nei.util.Log;

/**
 * Reflects unknown {@link IRecipe} implementations into a shaped/shapeless
 * ingredient list. Covers the field/method patterns used by typical 1.7.10
 * custom crafting recipes without per-mod handlers.
 */
public final class CraftingIngredientExtractor {
	private static final String[] INPUT_METHOD_NAMES = {
			"getInput", "getInputs", "getIngredients", "getRecipeInput", "getRecipeInputs"
	};
	private static final String[] INPUT_FIELD_NAMES = {
			"recipeItems", "input", "inputs", "ingredients", "inputItems"
	};
	private static final String[] WIDTH_NAMES = { "recipeWidth", "inputWidth", "getInputWidth", "getRecipeWidth", "width", "widthCraft" };
	private static final String[] HEIGHT_NAMES = { "recipeHeight", "inputHeight", "getInputHeight", "getRecipeHeight", "height", "heightCraft" };

	private CraftingIngredientExtractor() {
	}

	public static boolean isKnownVanillaOrForgeRecipe(IRecipe recipe) {
		return recipe instanceof ShapedRecipes
				|| recipe instanceof ShapelessRecipes
				|| recipe instanceof ShapedOreRecipe
				|| recipe instanceof ShapelessOreRecipe;
	}

	public static GenericCraftingRecipe tryWrap(IRecipe recipe) {
		if (recipe == null) {
			return null;
		}
		ItemStack output = null;
		try {
			output = recipe.getRecipeOutput();
		} catch (Throwable t) {
			Log.debug("Failed to read output from {}", recipe.getClass().getName(), t);
		}
		if (output == null || output.getItem() == null) {
			return null;
		}

		Object rawInputs = findInputs(recipe);
		if (rawInputs == null) {
			return null;
		}

		List<Object> inputs = toIngredientList(rawInputs);
		if (inputs == null) {
			return null;
		}
		List<Object> normalized = new ArrayList<Object>(inputs.size());
		for (int i = 0; i < inputs.size(); i++) {
			normalized.add(IngredientExpander.normalizeSlot(inputs.get(i)));
		}
		if (countPresentIngredients(normalized) == 0) {
			return null;
		}

		Integer width = findInt(recipe, WIDTH_NAMES);
		Integer height = findInt(recipe, HEIGHT_NAMES);
		boolean shaped = width != null && height != null && width.intValue() > 0 && height.intValue() > 0;
		if (shaped && width.intValue() * height.intValue() != normalized.size()) {
			shaped = false;
		}

		if (shaped) {
			return new GenericCraftingRecipe(recipe, normalized, output, width.intValue(), height.intValue());
		}
		List<Object> compacted = new ArrayList<Object>();
		for (int i = 0; i < normalized.size(); i++) {
			Object ingredient = normalized.get(i);
			if (ingredient != null) {
				compacted.add(ingredient);
			}
		}
		return new GenericCraftingRecipe(recipe, compacted, output, 0, 0);
	}

	private static Object findInputs(IRecipe recipe) {
		for (int i = 0; i < INPUT_METHOD_NAMES.length; i++) {
			Object value = invokeNoArg(recipe, INPUT_METHOD_NAMES[i]);
			if (isIngredientHolder(value)) {
				return value;
			}
		}
		Class<?> type = recipe.getClass();
		while (type != null && type != Object.class) {
			for (int i = 0; i < INPUT_FIELD_NAMES.length; i++) {
				Object value = readField(type, recipe, INPUT_FIELD_NAMES[i]);
				if (isIngredientHolder(value)) {
					return value;
				}
			}
			type = type.getSuperclass();
		}
		return null;
	}

	private static Integer findInt(IRecipe recipe, String[] names) {
		for (int i = 0; i < names.length; i++) {
			Object value = invokeNoArg(recipe, names[i]);
			if (value instanceof Integer) {
				return (Integer) value;
			}
			if (value instanceof Number) {
				return Integer.valueOf(((Number) value).intValue());
			}
		}
		Class<?> type = recipe.getClass();
		while (type != null && type != Object.class) {
			for (int i = 0; i < names.length; i++) {
				Object value = readField(type, recipe, names[i]);
				if (value instanceof Integer) {
					return (Integer) value;
				}
				if (value instanceof Number) {
					return Integer.valueOf(((Number) value).intValue());
				}
			}
			type = type.getSuperclass();
		}
		return null;
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

	private static Object readField(Class<?> type, Object target, String name) {
		try {
			Field field = type.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(target);
		} catch (NoSuchFieldException ignored) {
			return null;
		} catch (Throwable t) {
			return null;
		}
	}

	private static boolean isIngredientHolder(Object value) {
		return value instanceof Object[] || value instanceof List;
	}

	private static List<Object> toIngredientList(Object rawInputs) {
		if (rawInputs instanceof Object[]) {
			return new ArrayList<Object>(Arrays.asList((Object[]) rawInputs));
		}
		if (rawInputs instanceof List) {
			return new ArrayList<Object>((List<?>) rawInputs);
		}
		return null;
	}

	private static int countPresentIngredients(List<Object> inputs) {
		int count = 0;
		for (int i = 0; i < inputs.size(); i++) {
			Object ingredient = inputs.get(i);
			if (ingredient == null) {
				continue;
			}
			if (ingredient instanceof List && ((List<?>) ingredient).isEmpty()) {
				continue;
			}
			count++;
		}
		return count;
	}
}
