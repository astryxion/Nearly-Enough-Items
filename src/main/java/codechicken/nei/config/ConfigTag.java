package codechicken.nei.config;

/**
 * Minimal config tag so dump options can store a mode integer.
 */
public class ConfigTag {
	private int intValue;

	public int getIntValue() {
		return intValue;
	}

	public int getIntValue(int defaultValue) {
		return intValue;
	}

	public void setIntValue(int value) {
		this.intValue = value;
	}
}
