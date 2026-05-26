package astryxion.nei.gui;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.client.config.GuiButtonExt;

import net.minecraft.inventory.Container;

import astryxion.nei.api.recipe.transfer.IRecipeTransferError;
import astryxion.nei.gui.TooltipRenderer;
import astryxion.nei.transfer.RecipeTransferErrorInternal;
import astryxion.nei.transfer.RecipeTransferUtil;
import astryxion.nei.util.Translator;

public class RecipeTransferButton extends GuiButtonExt {
	private RecipeLayout recipeLayout;
	private IRecipeTransferError recipeTransferError;

	public RecipeTransferButton(int id, int xPos, int yPos, int width, int height, String displayString) {
		super(id, xPos, yPos, width, height, displayString);
	}

	public void init(RecipeLayout recipeLayout, EntityPlayer player) {
		this.recipeLayout = recipeLayout;
		Container container = player != null ? player.openContainer : null;
		if (container != null) {
			this.recipeTransferError = RecipeTransferUtil.getTransferRecipeError(container, recipeLayout, player);
		} else {
			this.recipeTransferError = RecipeTransferErrorInternal.instance;
		}

		if (this.recipeTransferError == null) {
			this.enabled = true;
			this.visible = true;
		} else {
			this.enabled = false;
			IRecipeTransferError.Type type = this.recipeTransferError.getType();
			this.visible = (type == IRecipeTransferError.Type.USER_FACING);
		}
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		super.drawButton(mc, mouseX, mouseY);
		if (hovered && visible) {
			if (recipeTransferError != null) {
				recipeTransferError.showError(mc, mouseX, mouseY, recipeLayout);
			} else {
				List<String> tooltipLines = Arrays.asList(
					Translator.translateToLocal("nei.tooltip.transfer"),
					Translator.translateToLocal("nei.tooltip.transfer.shift")
				);
				TooltipRenderer.drawHoveringText(mc, tooltipLines, mouseX, mouseY);
			}
		}
	}
}
