package astryxion.nei.plugins.vanilla;

import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerWorkbench;

import astryxion.nei.transfer.PlayerRecipeTransferHandler;

import astryxion.nei.api.IGuiHelper;
import astryxion.nei.api.IItemRegistry;
import astryxion.nei.api.INeiHelpers;
import astryxion.nei.api.IModPlugin;
import astryxion.nei.api.IModRegistry;
import astryxion.nei.api.IRecipeRegistry;
import astryxion.nei.api.NEIPlugin;
import astryxion.nei.api.recipe.VanillaRecipeCategoryUid;
import astryxion.nei.api.recipe.transfer.IRecipeTransferRegistry;
import astryxion.nei.plugins.vanilla.brewing.BrewingRecipeCategory;
import astryxion.nei.plugins.vanilla.brewing.BrewingRecipeHandler;
import astryxion.nei.plugins.vanilla.brewing.BrewingRecipeMaker;
import astryxion.nei.plugins.vanilla.crafting.CraftingRecipeCategory;
import astryxion.nei.plugins.vanilla.crafting.CraftingRecipeMaker;
import astryxion.nei.plugins.vanilla.crafting.ShapedOreRecipeHandler;
import astryxion.nei.plugins.vanilla.crafting.ShapedRecipesHandler;
import astryxion.nei.plugins.vanilla.crafting.ShapelessOreRecipeHandler;
import astryxion.nei.plugins.vanilla.crafting.ShapelessRecipesHandler;
import astryxion.nei.plugins.vanilla.furnace.FuelRecipeHandler;
import astryxion.nei.plugins.vanilla.furnace.FuelRecipeMaker;
import astryxion.nei.plugins.vanilla.furnace.FurnaceFuelCategory;
import astryxion.nei.plugins.vanilla.furnace.FurnaceSmeltingCategory;
import astryxion.nei.plugins.vanilla.furnace.SmeltingRecipeHandler;
import astryxion.nei.plugins.vanilla.furnace.SmeltingRecipeMaker;

@NEIPlugin
public class VanillaPlugin implements IModPlugin {
	private IItemRegistry itemRegistry;
	private INeiHelpers neiHelpers;

	@Override
	public void onNeiHelpersAvailable(INeiHelpers neiHelpers) {
		this.neiHelpers = neiHelpers;
	}

	@Override
	public void onItemRegistryAvailable(IItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
	}

	@Override
	public void register(IModRegistry registry) {
		IGuiHelper guiHelper = neiHelpers.getGuiHelper();
		registry.addRecipeCategories(
				new CraftingRecipeCategory(guiHelper),
				new FurnaceFuelCategory(guiHelper),
				new FurnaceSmeltingCategory(guiHelper),
				new BrewingRecipeCategory(guiHelper)
		);

		registry.addRecipeHandlers(
				new ShapedOreRecipeHandler(),
				new ShapedRecipesHandler(),
				new ShapelessOreRecipeHandler(),
				new ShapelessRecipesHandler(),
				new FuelRecipeHandler(),
				new SmeltingRecipeHandler(),
				new BrewingRecipeHandler()
		);

		IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();

		recipeTransferRegistry.addRecipeTransferHandler(ContainerWorkbench.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
		recipeTransferRegistry.addRecipeTransferHandler(new PlayerRecipeTransferHandler(neiHelpers.recipeTransferHandlerHelper()));
		recipeTransferRegistry.addRecipeTransferHandler(ContainerFurnace.class, VanillaRecipeCategoryUid.SMELTING, 0, 1, 1, 36);
		recipeTransferRegistry.addRecipeTransferHandler(ContainerFurnace.class, VanillaRecipeCategoryUid.FUEL, 1, 1, 1, 36);
		recipeTransferRegistry.addRecipeTransferHandler(ContainerBrewingStand.class, VanillaRecipeCategoryUid.BREWING, 0, 4, 4, 36);

		registry.addRecipes(CraftingRecipeMaker.getCraftingRecipes());
		registry.addRecipes(SmeltingRecipeMaker.getFurnaceRecipes());
		registry.addRecipes(FuelRecipeMaker.getFuelRecipes(itemRegistry, guiHelper));
		registry.addRecipes(BrewingRecipeMaker.getBrewingRecipes(itemRegistry));
	}

	@Override
	public void onRecipeRegistryAvailable(IRecipeRegistry recipeRegistry) {

	}
}
