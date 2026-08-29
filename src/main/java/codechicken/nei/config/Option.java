package codechicken.nei.config;

import net.minecraft.util.StatCollector;

/**
 * Compatibility stub for API.addOption and NEI dumpers.
 */
public class Option {
	public String name;
	private final ConfigTag tag = new ConfigTag();

	public Option(String name) {
		this.name = name;
	}

	public String translateN(String n, Object... params) {
		return StatCollector.translateToLocalFormatted("nei." + n, params);
	}

	public ConfigTag renderTag() {
		return tag;
	}

	public ConfigTag getTag() {
		return tag;
	}

	public OptionSlot getSlot() {
		return new OptionSlot();
	}

	public static class OptionSlot {
		public int slotWidth() {
			return 200;
		}
	}
}
