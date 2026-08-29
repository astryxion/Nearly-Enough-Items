package codechicken.nei.recipe;

import codechicken.nei.NEIClientUtils;

public class BrewingRecipeHandler extends TemplateRecipeHandler {
	@Override
	public String getRecipeName() {
		return NEIClientUtils.translate("recipe.brewing");
	}

	@Override
	public String getGuiTexture() {
		return "textures/gui/container/brewing_stand.png";
	}

	@Override
	public String getOverlayIdentifier() {
		return "brewing";
	}
}
