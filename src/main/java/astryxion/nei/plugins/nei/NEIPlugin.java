package astryxion.nei.plugins.nei;

import java.util.Arrays;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import astryxion.nei.api.IGuiHelper;
import astryxion.nei.api.IItemRegistry;
import astryxion.nei.api.INeiHelpers;
import astryxion.nei.api.IModPlugin;
import astryxion.nei.api.IModRegistry;
import astryxion.nei.api.IRecipeRegistry;
import astryxion.nei.config.Config;
import astryxion.nei.plugins.nei.debug.DebugRecipe;
import astryxion.nei.plugins.nei.debug.DebugRecipeCategory;
import astryxion.nei.plugins.nei.debug.DebugRecipeHandler;
import astryxion.nei.plugins.nei.description.ItemDescriptionRecipeCategory;
import astryxion.nei.plugins.nei.description.ItemDescriptionRecipeHandler;

@astryxion.nei.api.NEIPlugin
public class NEIPlugin implements IModPlugin {
	private INeiHelpers neiHelpers;

	@Override
	public void onNeiHelpersAvailable(INeiHelpers neiHelpers) {
		this.neiHelpers = neiHelpers;
	}

	@Override
	public void onItemRegistryAvailable(IItemRegistry itemRegistry) {

	}

	@Override
	public void register(IModRegistry registry) {
		IGuiHelper guiHelper = neiHelpers.getGuiHelper();

		registry.addRecipeCategories(
				new ItemDescriptionRecipeCategory(guiHelper)
		);

		registry.addRecipeHandlers(
				new ItemDescriptionRecipeHandler()
		);

		if (Config.isDebugModeEnabled()) {
			registry.addDescription(Arrays.asList(
					new ItemStack(Items.wooden_door)
					),
					"description.nei.wooden.door.1", // actually 2 lines
					"description.nei.wooden.door.2",
					"description.nei.wooden.door.3"
			);

			registry.addRecipeCategories(new DebugRecipeCategory(guiHelper));
			registry.addRecipeHandlers(new DebugRecipeHandler());
			registry.addRecipes(Arrays.asList(
					new DebugRecipe(),
					new DebugRecipe()
			));
		}
	}

	@Override
	public void onRecipeRegistryAvailable(IRecipeRegistry recipeRegistry) {

	}
}
