package astryxion.nei.config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import cpw.mods.fml.client.IModGuiFactory;

public class NeiModGuiFactory implements IModGuiFactory {
	@Override
	public void initialize(Minecraft minecraftInstance) {

	}

	@Nonnull
	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return NeiModConfigGui.class;
	}

	@Nullable
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	@Nullable
	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}
}
