package codechicken.nei;

public class VisiblityData {
	public boolean showUtilityButtons = true;
	public boolean showItemPanel = true;
	public boolean showBookmarkPanel = true;
	public boolean showSubsetDropdown = true;
	public boolean showItemSection = true;
	public boolean showSearchSection = true;
	public boolean showWidgets = true;
	public boolean showNEI = true;
	public boolean enableDeleteMode = true;
	public boolean showStateButtons = false;

	public void translateDependancies() {
		if (!showNEI) {
			showWidgets = false;
		}
		if (!showWidgets) {
			showItemSection = false;
			showUtilityButtons = false;
		}
		if (!showItemSection) {
			showBookmarkPanel = false;
			showSubsetDropdown = false;
			showSearchSection = false;
			showItemPanel = false;
		}
	}
}
