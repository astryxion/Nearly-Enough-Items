package astryxion.nei;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;

import astryxion.nei.api.IModPlugin;
import astryxion.nei.config.Config;
import astryxion.nei.config.Constants;
import astryxion.nei.config.KeyBindings;
import astryxion.nei.gui.ItemListOverlay;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import astryxion.nei.network.packets.PacketNEI;
import astryxion.nei.util.AnnotatedInstanceUtil;
import astryxion.nei.util.Log;
import astryxion.nei.util.ModRegistry;

public class ProxyCommonClient extends ProxyCommon {
	private static boolean started = false;
	@Nullable
	private ItemFilter itemFilter;
	private GuiEventHandler guiEventHandler;
	private List<IModPlugin> plugins;

	private static void initVersionChecker() {
		final NBTTagCompound compound = new NBTTagCompound();
		compound.setString("curseProjectName", "just-enough-items-jei");
		compound.setString("curseFilenameParser", "nei-" + MinecraftForge.MC_VERSION + "-[].jar");
		FMLInterModComms.sendRuntimeMessage(Constants.MOD_ID, "VersionChecker", "addCurseCheck", compound);
	}
	
	@Override
	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		Config.preInit(event);
		initVersionChecker();

		ASMDataTable asmDataTable = event.getAsmData();
		this.plugins = AnnotatedInstanceUtil.getModPlugins(asmDataTable);

		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				plugin.onNeiHelpersAvailable(Internal.getHelpers());
			} catch (AbstractMethodError ignored) {
				// older plugins don't have this method
			} catch (Exception e) {
				Log.error("Mod plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}
	}

	@Override
	public void init(@Nonnull FMLInitializationEvent event) {
		KeyBindings.init();
		FMLCommonHandler.instance().bus().register(this);

		guiEventHandler = new GuiEventHandler();
		MinecraftForge.EVENT_BUS.register(guiEventHandler);
		FMLCommonHandler.instance().bus().register(guiEventHandler);
	}

	@Override
	public void scheduleStartNEI() {
		final Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft == null) {
			return;
		}
		minecraft.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				NearlyEnoughItems.getProxy().startNEI();
			}
		});
	}

	@Override
	public void startNEI() {
		if (started && Internal.getRecipeRegistry() != null) {
			return;
		}

		started = true;
		ItemRegistry itemRegistry = new ItemRegistry();
		Internal.setItemRegistry(itemRegistry);

		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				plugin.onItemRegistryAvailable(itemRegistry);
			} catch (AbstractMethodError ignored) {
				// older plugins don't have this method
			} catch (Exception e) {
				Log.error("Mod plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}

		ModRegistry modRegistry = new ModRegistry();

		iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				plugin.register(modRegistry);
				Log.info("Registered plugin: {}", plugin.getClass().getName());
			} catch (Exception e) {
				Log.error("Failed to register mod plugin: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}

		RecipeRegistry recipeRegistry = modRegistry.createRecipeRegistry();
		Internal.setRecipeRegistry(recipeRegistry);

		iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				plugin.onRecipeRegistryAvailable(recipeRegistry);
			} catch (AbstractMethodError ignored) {
				// older plugins don't have this method
			} catch (Exception e) {
				Log.error("Mod plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}

		itemFilter = new ItemFilter(itemRegistry);
		ItemListOverlay itemListOverlay = new ItemListOverlay(itemFilter);
		guiEventHandler.setItemListOverlay(itemListOverlay);
	}

	@Override
	public void restartNEI() {
		if (!started) {
			return;
		}
		started = false;
		startNEI();
	}

	@Override
	public void resetItemFilter() {
		if (itemFilter != null) {
			itemFilter.reset();
		}
	}

	@Override
	public void sendPacketToServer(PacketNEI packet) {
		FMLProxyPacket proxyPacket = packet.getPacket();
		proxyPacket.setTarget(Side.SERVER);
		NearlyEnoughItems.getPacketHandler().sendToServer(proxyPacket);
	}

	// subscribe to event with low priority so that addon mods that use the config can do their stuff first
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onConfigChanged(@Nonnull ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (!Constants.MOD_ID.equals(eventArgs.modID)) {
			return;
		}

		if (Config.syncConfig()) {
			restartNEI(); // reload everything, configs can change available recipes
		}
	}
}
