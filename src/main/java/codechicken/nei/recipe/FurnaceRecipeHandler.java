package codechicken.nei.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;

public class FurnaceRecipeHandler extends TemplateRecipeHandler {
	public static ArrayList<FuelPair> afuels = new ArrayList<FuelPair>();

	public static class FuelPair {
		public PositionedStack stack;
		public int burnTime;

		public FuelPair(ItemStack ingred, int burnTime) {
			this.stack = new PositionedStack(ingred, 51, 42, false);
			this.burnTime = burnTime;
		}
	}

	public class SmeltingPair extends CachedRecipe {
		PositionedStack ingred;
		PositionedStack result;

		public SmeltingPair(ItemStack ingred, ItemStack result) {
			this.ingred = new PositionedStack(ingred, 51, 6);
			this.result = new PositionedStack(result, 111, 24);
		}

		@Override
		public List<PositionedStack> getIngredients() {
			return getCycledIngredients(cycleticks / 48, java.util.Arrays.asList(new PositionedStack[] { ingred }));
		}

		@Override
		public PositionedStack getResult() {
			return result;
		}

		@Override
		public PositionedStack getOtherStack() {
			if (afuels == null || afuels.isEmpty()) {
				return null;
			}
			return afuels.get((cycleticks / 48) % afuels.size()).stack;
		}
	}

	@Override
	public String getRecipeName() {
		return NEIClientUtils.translate("recipe.furnace");
	}

	@Override
	public String getGuiTexture() {
		return "textures/gui/container/furnace.png";
	}

	@Override
	public String getOverlayIdentifier() {
		return "smelting";
	}
}
