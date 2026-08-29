package codechicken.nei.api;

public interface IRecipeFilter {
	public static interface IRecipeFilterProvider {
		IRecipeFilter getFilter();
	}

	boolean matches(Object recipe);
}
