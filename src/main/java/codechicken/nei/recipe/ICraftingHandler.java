package codechicken.nei.recipe;

public interface ICraftingHandler extends IRecipeHandler {
	ICraftingHandler getRecipeHandler(String outputId, Object... results);
}
