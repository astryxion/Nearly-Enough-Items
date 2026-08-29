package astryxion.nei;

import javax.annotation.Nonnull;
import java.util.Map;

import net.minecraft.item.Item;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLModIdMappingEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

import astryxion.nei.config.Config;
import astryxion.nei.config.Constants;
import astryxion.nei.debug.DebugItem;
import astryxion.nei.network.PacketHandler;

@Mod(modid = Constants.MOD_ID,
		name = Constants.NAME,
		version = Constants.VERSION,
		guiFactory = "astryxion.nei.config.NeiModGuiFactory",
		dependencies = "required-after:Forge@[10.13.0.0,);")
public class NearlyEnoughItems {

	@SidedProxy(clientSide = "astryxion.nei.ProxyCommonClient", serverSide = "astryxion.nei.ProxyCommon")
	private static ProxyCommon proxy;
	private static PacketHandler packetHandler;

	public static PacketHandler getPacketHandler() {
		return packetHandler;
	}

	public static ProxyCommon getProxy() {
		return proxy;
	}

	@NetworkCheckHandler
	public boolean checkModLists(Map<String, String> modList, Side side) {
		boolean neiOnServer = modList.containsKey(Constants.MOD_ID);
		if (side == Side.SERVER) {
			Config.setNeiOnServer(neiOnServer);
		} else if (side == Side.CLIENT && neiOnServer) {
			// Integrated server / LAN: recipe transfer needs this on the client too
			Config.setNeiOnServer(true);
		}

		return true;
	}

	@Mod.EventHandler
	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		packetHandler = new PacketHandler();
		Internal.setHelpers(new NeiHelpers());
		proxy.preInit(event);

		if (Config.isDebugModeEnabled()) {
			String name = "neiDebug";
			Item debugItem = new DebugItem(name);
			debugItem.setUnlocalizedName(name);
			GameRegistry.registerItem(debugItem, name);
		}
	}

	@Mod.EventHandler
	public void init(@Nonnull FMLInitializationEvent event) {
		proxy.init(event);
	}

	@Mod.EventHandler
	public void startNEI(@Nonnull FMLModIdMappingEvent event) {
		// Do not start JEI here. Remap fires during world load on the integrated
		// server; scheduling onto the client thread blocks FML handshake.
	}
}
