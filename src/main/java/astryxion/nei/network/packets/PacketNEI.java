package astryxion.nei.network.packets;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;

import io.netty.buffer.Unpooled;
import astryxion.nei.network.IPacketId;
import astryxion.nei.network.PacketHandler;
import astryxion.nei.util.Log;

public abstract class PacketNEI {
	private final IPacketId id = getPacketId();

	public final FMLProxyPacket getPacket() {
		PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());

		packetBuffer.writeByte(id.ordinal());
		try {
			writePacketData(packetBuffer);
		} catch (IOException e) {
			Log.error("Error creating packet", e);
		}

		return new FMLProxyPacket(packetBuffer, PacketHandler.CHANNEL_ID);
	}

	public abstract IPacketId getPacketId();

	public abstract void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException;

	public abstract void writePacketData(PacketBuffer buf) throws IOException;
}
