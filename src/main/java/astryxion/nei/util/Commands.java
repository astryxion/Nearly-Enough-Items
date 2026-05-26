package astryxion.nei.util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

import astryxion.nei.NearlyEnoughItems;
import astryxion.nei.config.Config;
import astryxion.nei.network.packets.PacketGiveItemMessageBig;

public class Commands {

	public static void giveFullStack(@Nonnull ItemStack itemstack) {
		giveStack(itemstack, itemstack.getMaxStackSize());
	}

	public static void giveOneFromStack(@Nonnull ItemStack itemstack) {
		giveStack(itemstack, 1);
	}

	/**
	 * /give <player> <item> [amount] [data] [dataTag]
	 */
	public static void giveStack(@Nonnull ItemStack itemStack, int amount) {
		EntityPlayerSP sender = Minecraft.getMinecraft().thePlayer;
		String senderName = sender.getCommandSenderName();
		
		List<String> commandStrings = new ArrayList<>();
		commandStrings.add("/give");
		commandStrings.add(senderName);
		commandStrings.add(Item.itemRegistry.getNameForObject(itemStack.getItem()).toString());
		commandStrings.add(String.valueOf(amount));
		commandStrings.add(String.valueOf(itemStack.getMetadata()));

		if (itemStack.hasTagCompound()) {
			commandStrings.add(itemStack.getTagCompound().toString());
		}

		String fullCommand = StringUtils.join(commandStrings, " ");
		sendChatMessage(sender, fullCommand);
	}

	private static void sendChatMessage(EntityPlayerSP sender, String chatMessage) {
		if (chatMessage.length() <= 100) {
			if (net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(sender, chatMessage) != 0) {
				return;
			}
			NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
			if (netHandler != null) {
				netHandler.addToSendQueue(new C01PacketChatMessage(chatMessage));
			}
		} else {
			if (Config.isNeiOnServer()) {
				PacketGiveItemMessageBig packet = new PacketGiveItemMessageBig(chatMessage);
				NearlyEnoughItems.getProxy().sendPacketToServer(packet);
			} else {
				ChatComponentTranslation errorMessage = new ChatComponentTranslation("nei.chat.error.command.too.long");
				errorMessage.getChatStyle().setColor(EnumChatFormatting.RED);
				sender.addChatComponentMessage(errorMessage);

				ChatComponentText chatMessageComponent = new ChatComponentText(chatMessage);
				chatMessageComponent.getChatStyle().setColor(EnumChatFormatting.RED);
				sender.addChatComponentMessage(chatMessageComponent);
			}
		}
	}
}
