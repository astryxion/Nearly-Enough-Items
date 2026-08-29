package codechicken.nei.bridge;

import astryxion.nei.util.Log;

/**
 * Dedicated log for NEI API usage so we can see which compatibility methods
 * loaded addons actually call.
 */
public final class NeiCompatLog {
	private NeiCompatLog() {
	}

	public static void api(String method, Object... detail) {
		if (isHighFrequency(method)) {
			return;
		}
		if (detail == null || detail.length == 0) {
			Log.info("[NEI-Compat] {}", method);
			return;
		}
		StringBuilder builder = new StringBuilder(method);
		for (int i = 0; i < detail.length; i++) {
			builder.append(' ').append(String.valueOf(detail[i]));
		}
		Log.info("[NEI-Compat] {}", builder.toString());
	}

	private static boolean isHighFrequency(String method) {
		return "hideItem".equals(method)
				|| "hideItem(rule)".equals(method)
				|| "addItemListEntry".equals(method)
				|| "addItemVariant".equals(method)
				|| "setOverrideName".equals(method);
	}

	public static void info(String message, Object... params) {
		Log.info("[NEI-Compat] " + message, params);
	}

	public static void warn(String message, Object... params) {
		Log.warning("[NEI-Compat] " + message, params);
	}

	public static void error(String message, Object... params) {
		Log.error("[NEI-Compat] " + message, params);
	}

	public static void unsupported(String method) {
		Log.warning("[NEI-Compat] Unsupported NEI API call (recorded, no-op): {}", method);
	}
}
