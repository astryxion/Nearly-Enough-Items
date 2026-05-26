package astryxion.nei.gui.ingredients;

import javax.annotation.Nonnull;
import java.util.Collection;

import astryxion.nei.gui.Focus;

public interface IIngredientHelper<T> {
	Collection<T> expandSubtypes(Collection<T> contained);

	T getMatch(Iterable<T> contained, @Nonnull Focus toMatch);
}
