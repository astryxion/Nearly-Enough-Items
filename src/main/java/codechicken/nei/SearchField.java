package codechicken.nei;

/**
 * Compatibility stub for SearchField providers.
 */
public class SearchField {
	public interface ISearchProvider {
		boolean canProvide();
	}

	public static final SearchTokenParser searchParser = new SearchTokenParser();
}
