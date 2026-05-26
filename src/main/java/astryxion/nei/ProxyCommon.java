package astryxion.nei;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.common.util.FakePlayer;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import astryxion.nei.network.packets.PacketNEI;
import astryxion.nei.util.Log;

public class ProxyCommon {

	public void preInit(@Nonnull FMLPreInitializationEvent event) {

	}

	public void init(@Nonnull FMLInitializationEvent event) {

	}

	public void startNEI() {

	}

	/** Runs {@link #startNEI()} on the client thread after registry remapping (integrated server). */
	public void scheduleStartNEI() {

	}

	public void restartNEI() {

	}

	public void resetItemFilter() {

	}

	public void sendPacketToServer(PacketNEI packet) {
		Log.error("Tried to send packet to the server from the server: {}", packet);
	}

	public void sendPacketToPlayer(PacketNEI packet, EntityPlayer entityplayer) {
		if (!(entityplayer instanceof EntityPlayerMP) || (entityplayer instanceof FakePlayer)) {
			return;
		}

		EntityPlayerMP player = (EntityPlayerMP) entityplayer;
		NearlyEnoughItems.getPacketHandler().sendPacket(packet.getPacket(), player);
	}
}
