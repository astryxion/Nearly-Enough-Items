package astryxion.nei.network.packets;

import javax.annotation.Nonnull;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import astryxion.nei.network.IPacketId;
import astryxion.nei.network.PacketIdServer;
import astryxion.nei.util.StackUtil;

public class PacketDeletePlayerItem extends PacketNEI {
	private ItemStack itemStack;

	public PacketDeletePlayerItem() {

	}

	public PacketDeletePlayerItem(@Nonnull ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.DELETE_ITEM;
	}

	@Override
	public void writePacketData(PacketBuffer buf) throws IOException {
		buf.writeItemStackToBuffer(itemStack);
	}

	@Override
	public void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
		itemStack = buf.readItemStackFromBuffer();
		ItemStack playerItem = player.inventory.getItemStack();
		if (StackUtil.isIdentical(itemStack, playerItem)) {
			player.inventory.setItemStack(null);
		}
	}
}
