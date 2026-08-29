package codechicken.nei;

/**
 * Compatibility stub. Subset tags are not shown in the JEI overlay.
 */
public class SubsetWidget {
	public static class SubsetTag {
		public final String name;

		public SubsetTag(String name) {
			this.name = name;
		}

		public SubsetTag(String name, codechicken.nei.api.ItemFilter filter) {
			this.name = name;
		}
	}

	public static void addTag(SubsetTag tag) {
	}

	public static void updateHiddenItems() {
	}
}
