package codechicken.nei;

public class SearchTokenParser {
	public interface ISearchParserProvider {
		char getPrefix();
	}

	public void addProvider(ISearchParserProvider provider) {
		codechicken.nei.bridge.NeiCompatLog.unsupported("SearchTokenParser.addProvider");
	}
}
