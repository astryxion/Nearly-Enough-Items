package codechicken.nei.api;

/**
 * An NEI configuration entry point should implement this class and have a name matching {@code NEI*Config}.
 * {@link #loadConfig()} is called when the compatibility layer is loaded.
 */
public interface IConfigureNEI {
	void loadConfig();

	String getName();

	String getVersion();
}
