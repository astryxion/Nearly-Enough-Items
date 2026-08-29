package codechicken.nei.recipe;

import codechicken.nei.NEIClientUtils;

public class FuelRecipeHandler extends FurnaceRecipeHandler {
	@Override
	public String getRecipeName() {
		return NEIClientUtils.translate("recipe.fuel");
	}

	@Override
	public String getOverlayIdentifier() {
		return "fuel";
	}
}
