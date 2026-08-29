package codechicken.nei;

import cpw.mods.fml.common.Mod;

import astryxion.nei.config.Constants;

/**
 * Dummy mod container so 1.7.10 addons depending on {@code NotEnoughItems}
 * still load against this JEI backport.
 */
@Mod(modid = "NotEnoughItems",
		name = "Not Enough Items",
		version = Constants.VERSION,
		dependencies = "required-after:NEI")
public class NotEnoughItems {
}
