package codechicken.nei;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NEIClientConfig {
	public static final Logger logger = LogManager.getLogger("NotEnoughItems");
	public static final Set<String> serialHandlers = new HashSet<String>();
	public static boolean loaded;

	public static boolean isEnabled() {
		return true;
	}

	public static boolean isLoaded() {
		return loaded;
	}

	public static boolean canCheatItem(net.minecraft.item.ItemStack stack) {
		return false;
	}

	public static boolean canPerformAction(String name) {
		return false;
	}

	public static void setEnabled(boolean enabled) {
	}

	public static int getItemLoadingTimeout() {
		return 500;
	}

	public static final OptionList optionList = new OptionList();

	public static OptionList getOptionList() {
		return optionList;
	}

	public static class OptionList {
		public void addOption(codechicken.nei.config.Option option) {
			codechicken.nei.bridge.NeiCompatLog.unsupported("OptionList.addOption");
		}
	}
}
