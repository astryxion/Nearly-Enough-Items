package astryxion.nei.util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import astryxion.nei.api.recipe.IRecipeCategory;

public class RecipeCategoryComparator implements Comparator<IRecipeCategory> {
	@Nonnull
	private final List<IRecipeCategory> recipeCategories;

	public RecipeCategoryComparator(@Nonnull List<IRecipeCategory> recipeCategories) {
		this.recipeCategories = new ArrayList<IRecipeCategory>(recipeCategories);
	}

	public void addCategory(@Nonnull IRecipeCategory recipeCategory) {
		if (!recipeCategories.contains(recipeCategory)) {
			recipeCategories.add(recipeCategory);
		}
	}

	@Override
	public int compare(IRecipeCategory recipeCategory1, IRecipeCategory recipeCategory2) {
		int index1 = recipeCategories.indexOf(recipeCategory1);
		int index2 = recipeCategories.indexOf(recipeCategory2);
		if (index1 < 0) {
			index1 = Integer.MAX_VALUE;
		}
		if (index2 < 0) {
			index2 = Integer.MAX_VALUE;
		}
		return Integer.valueOf(index1).compareTo(Integer.valueOf(index2));
	}
}
