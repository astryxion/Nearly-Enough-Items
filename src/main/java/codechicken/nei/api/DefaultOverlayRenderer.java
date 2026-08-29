package codechicken.nei.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.Slot;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.guihook.GuiContainerManager;

public class DefaultOverlayRenderer implements IRecipeOverlayRenderer {
	final IStackPositioner positioner;
	ArrayList<PositionedStack> ingreds;

	public DefaultOverlayRenderer(List<PositionedStack> ai, IStackPositioner positioner) {
		this.positioner = positioner;
		ingreds = new ArrayList<PositionedStack>();
		for (int i = 0; i < ai.size(); i++) {
			ingreds.add(ai.get(i).copy());
		}
		ingreds = positioner.positionStacks(ingreds);
	}

	@Override
	public void renderOverlay(GuiContainerManager gui, Slot slot) {
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

		for (int i = 0; i < ingreds.size(); i++) {
			PositionedStack stack = ingreds.get(i);
			if (stack.relx == slot.xDisplayPosition && stack.rely == slot.yDisplayPosition && !slot.getHasStack()) {
				GuiContainerManager.drawItem(stack.relx, stack.rely, stack.item);
				GuiDraw.drawRect(stack.relx, stack.rely, 16, 16, 0x66555555);
			}
		}

		GL11.glPopAttrib();
	}
}
