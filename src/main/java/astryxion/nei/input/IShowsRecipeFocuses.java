package astryxion.nei.input;

import javax.annotation.Nullable;

import astryxion.nei.gui.Focus;

public interface IShowsRecipeFocuses {

	@Nullable
	Focus getFocusUnderMouse(int mouseX, int mouseY);

}
