package astryxion.nei.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import astryxion.nei.util.Log;

/**
 * Snapshot of what the universal discovery layer found. Logged at startup so
 * item/recipe coverage can be compared against NEI in the same pack.
 */
public class DiscoveryReport {
	private static DiscoveryReport lastReport = new DiscoveryReport();

	private int registeredItems;
	private int registeredBlocks;
	private int itemStackVariants;
	private int hiddenItemStacks;
	private int craftingRecipes;
	private int genericCraftingRecipes;
	private int skippedCraftingRecipes;
	private int smeltingRecipes;
	private int fuelRecipes;
	private int brewingRecipes;
	private int neiHandlerRecipes;
	private int neiCraftingHandlers;
	private int neiUsageHandlers;
	private final Map<String, Integer> unhandledCraftingClasses = new LinkedHashMap<String, Integer>();
	private final List<String> notes = new ArrayList<String>();

	public static DiscoveryReport getLast() {
		return lastReport;
	}

	public static void setLast(DiscoveryReport report) {
		lastReport = report;
	}

	public void setRegisteredItems(int registeredItems) {
		this.registeredItems = registeredItems;
	}

	public void setRegisteredBlocks(int registeredBlocks) {
		this.registeredBlocks = registeredBlocks;
	}

	public void setItemStackVariants(int itemStackVariants) {
		this.itemStackVariants = itemStackVariants;
	}

	public void setHiddenItemStacks(int hiddenItemStacks) {
		this.hiddenItemStacks = hiddenItemStacks;
	}

	public void setCraftingRecipes(int craftingRecipes) {
		this.craftingRecipes = craftingRecipes;
	}

	public void setGenericCraftingRecipes(int genericCraftingRecipes) {
		this.genericCraftingRecipes = genericCraftingRecipes;
	}

	public void setSkippedCraftingRecipes(int skippedCraftingRecipes) {
		this.skippedCraftingRecipes = skippedCraftingRecipes;
	}

	public void setSmeltingRecipes(int smeltingRecipes) {
		this.smeltingRecipes = smeltingRecipes;
	}

	public void setFuelRecipes(int fuelRecipes) {
		this.fuelRecipes = fuelRecipes;
	}

	public void setBrewingRecipes(int brewingRecipes) {
		this.brewingRecipes = brewingRecipes;
	}

	public void setNeiHandlerRecipes(int neiHandlerRecipes) {
		this.neiHandlerRecipes = neiHandlerRecipes;
	}

	public void setNeiCraftingHandlers(int neiCraftingHandlers) {
		this.neiCraftingHandlers = neiCraftingHandlers;
	}

	public void setNeiUsageHandlers(int neiUsageHandlers) {
		this.neiUsageHandlers = neiUsageHandlers;
	}

	public void addUnhandledCraftingClass(Class<?> recipeClass) {
		String name = recipeClass.getName();
		Integer count = unhandledCraftingClasses.get(name);
		unhandledCraftingClasses.put(name, count == null ? 1 : count.intValue() + 1);
	}

	public void addNote(String note) {
		notes.add(note);
	}

	public int getRegisteredItems() {
		return registeredItems;
	}

	public int getRegisteredBlocks() {
		return registeredBlocks;
	}

	public int getItemStackVariants() {
		return itemStackVariants;
	}

	public int getCraftingRecipes() {
		return craftingRecipes;
	}

	public int getSmeltingRecipes() {
		return smeltingRecipes;
	}

	public int getFuelRecipes() {
		return fuelRecipes;
	}

	public int getBrewingRecipes() {
		return brewingRecipes;
	}

	public int getNeiHandlerRecipes() {
		return neiHandlerRecipes;
	}

	public Map<String, Integer> getUnhandledCraftingClasses() {
		return Collections.unmodifiableMap(unhandledCraftingClasses);
	}

	public void logToConsole() {
		Log.info("[JEI-Discovery] Registered Items: {}", Integer.valueOf(registeredItems));
		Log.info("[JEI-Discovery] Registered Blocks (with items): {}", Integer.valueOf(registeredBlocks));
		Log.info("[JEI-Discovery] ItemStack variants in panel: {}", Integer.valueOf(itemStackVariants));
		Log.info("[JEI-Discovery] Hidden ItemStacks: {}", Integer.valueOf(hiddenItemStacks));
		Log.info("[JEI-Discovery] Crafting recipes: {} (generic wrappers: {}, skipped: {})",
				Integer.valueOf(craftingRecipes),
				Integer.valueOf(genericCraftingRecipes),
				Integer.valueOf(skippedCraftingRecipes));
		Log.info("[JEI-Discovery] Smelting recipes: {}", Integer.valueOf(smeltingRecipes));
		Log.info("[JEI-Discovery] Fuel recipes: {}", Integer.valueOf(fuelRecipes));
		Log.info("[JEI-Discovery] Brewing recipes: {}", Integer.valueOf(brewingRecipes));
		Log.info("[JEI-Discovery] NEI handlers: {} crafting, {} usage, {} adapted recipes",
				Integer.valueOf(neiCraftingHandlers),
				Integer.valueOf(neiUsageHandlers),
				Integer.valueOf(neiHandlerRecipes));
		if (!unhandledCraftingClasses.isEmpty()) {
			Log.warning("[JEI-Discovery] Unhandled IRecipe classes (no extractable inputs):");
			for (Map.Entry<String, Integer> entry : unhandledCraftingClasses.entrySet()) {
				Log.warning("[JEI-Discovery]   {} x{}", entry.getKey(), entry.getValue());
			}
		}
		for (int i = 0; i < notes.size(); i++) {
			Log.info("[JEI-Discovery] {}", notes.get(i));
		}
	}
}
