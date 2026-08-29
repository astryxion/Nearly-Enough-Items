package codechicken.nei;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Compatibility stub. Server cheating/give-item features stay on the JEI side.
 */
public class NEIServerConfig {
	public static final Logger logger = LogManager.getLogger("NotEnoughItems");

	public static boolean isActionPermitted(String name) {
		return false;
	}
}
