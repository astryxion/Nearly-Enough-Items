package codechicken.nei.recipe;

public interface IUsageHandler extends IRecipeHandler {
	IUsageHandler getUsageHandler(String inputId, Object... ingredients);
}
