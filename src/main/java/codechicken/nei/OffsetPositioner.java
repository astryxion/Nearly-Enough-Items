package codechicken.nei;

import java.util.ArrayList;

import codechicken.nei.api.IStackPositioner;

public class OffsetPositioner implements IStackPositioner {
	public int offsetx;
	public int offsety;

	public OffsetPositioner(int x, int y) {
		offsetx = x;
		offsety = y;
	}

	@Override
	public ArrayList<PositionedStack> positionStacks(ArrayList<PositionedStack> ai) {
		for (int i = 0; i < ai.size(); i++) {
			PositionedStack stack = ai.get(i);
			stack.relx += offsetx;
			stack.rely += offsety;
		}
		return ai;
	}
}
