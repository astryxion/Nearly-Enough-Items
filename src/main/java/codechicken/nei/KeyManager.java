package codechicken.nei;

/**
 * Compatibility stub for API.addKeyBind.
 */
public class KeyManager {
	public static void registerKeyBinding(String ident, int defaultKey) {
		codechicken.nei.bridge.NeiCompatLog.unsupported("KeyManager.registerKeyBinding(" + ident + ")");
	}

	public static boolean isKeyDown(String ident) {
		return false;
	}
}
