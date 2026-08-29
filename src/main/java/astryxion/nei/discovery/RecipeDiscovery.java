package astryxion.nei.discovery;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import astryxion.nei.api.IGuiHelper;
import astryxion.nei.api.IItemRegistry;
import astryxion.nei.plugins.vanilla.brewing.BrewingRecipeMaker;
import astryxion.nei.plugins.vanilla.brewing.BrewingRecipeWrapper;
import astryxion.nei.plugins.vanilla.furnace.FuelRecipe;
import astryxion.nei.plugins.vanilla.furnace.FuelRecipeMaker;
import astryxion.nei.plugins.vanilla.furnace.SmeltingRecipe;
import astryxion.nei.plugins.vanilla.furnace.SmeltingRecipeMaker;
import astryxion.nei.util.Log;

/**
 * Universal recipe scan of vanilla/Forge registries. Known IRecipe classes
 * pass through unchanged; everything else is reflected into
 * {@link GenericCraftingRecipe} so modded GameRegistry.addRecipe calls appear
 * without per-mod plugins.
 */
public final class RecipeDiscovery {
	private RecipeDiscovery() {
	}

	@SuppressWarnings("unchecked")
	public static List<Object> getCraftingRecipes(DiscoveryReport report) {
		List<Object> discovered = new ArrayList<Object>();
		List<IRecipe> recipes;
		try {
			recipes = CraftingManager.getInstance().getRecipeList();
		} catch (Throwable t) {
			Log.error("Failed to read CraftingManager recipe list", t);
			return discovered;
		}
		if (recipes == null) {
			return discovered;
		}

		int generic = 0;
		int skipped = 0;
		for (int i = 0; i < recipes.size(); i++) {
			IRecipe recipe = recipes.get(i);
			if (recipe == null) {
				continue;
			}
			if (CraftingIngredientExtractor.isKnownVanillaOrForgeRecipe(recipe)) {
				discovered.add(recipe);
				continue;
			}
			GenericCraftingRecipe wrapped = CraftingIngredientExtractor.tryWrap(recipe);
			if (wrapped != null) {
				discovered.add(wrapped);
				generic++;
			} else {
				skipped++;
				report.addUnhandledCraftingClass(recipe.getClass());
			}
		}
		report.setCraftingRecipes(discovered.size());
		report.setGenericCraftingRecipes(generic);
		report.setSkippedCraftingRecipes(skipped);
		return discovered;
	}

	public static List<SmeltingRecipe> getSmeltingRecipes(DiscoveryReport report) {
		List<SmeltingRecipe> recipes = SmeltingRecipeMaker.getFurnaceRecipes();
		report.setSmeltingRecipes(recipes.size());
		return recipes;
	}

	public static List<FuelRecipe> getFuelRecipes(IItemRegistry itemRegistry, IGuiHelper guiHelper, DiscoveryReport report) {
		List<FuelRecipe> recipes = FuelRecipeMaker.getFuelRecipes(itemRegistry, guiHelper);
		report.setFuelRecipes(recipes.size());
		return recipes;
	}

	public static List<BrewingRecipeWrapper> getBrewingRecipes(IItemRegistry itemRegistry, DiscoveryReport report) {
		List<BrewingRecipeWrapper> recipes = BrewingRecipeMaker.getBrewingRecipes(itemRegistry);
		report.setBrewingRecipes(recipes.size());
		return recipes;
	}
}
