package codechicken.nei.guihook;

import net.minecraft.client.gui.inventory.GuiContainer;

public interface IContainerInputHandler {
	boolean keyTyped(GuiContainer gui, char keyChar, int keyCode);

	void onKeyTyped(GuiContainer gui, char keyChar, int keyID);

	boolean lastKeyTyped(GuiContainer gui, char keyChar, int keyID);

	boolean mouseClicked(GuiContainer gui, int mousex, int mousey, int button);

	void onMouseClicked(GuiContainer gui, int mousex, int mousey, int button);

	void onMouseUp(GuiContainer gui, int mousex, int mousey, int button);

	boolean mouseScrolled(GuiContainer gui, int mousex, int mousey, int scrolled);

	void onMouseScrolled(GuiContainer gui, int mousex, int mousey, int scrolled);

	void onMouseDragged(GuiContainer gui, int mousex, int mousey, int button, long heldTime);
}
