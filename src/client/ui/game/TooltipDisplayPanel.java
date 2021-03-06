package client.ui.game;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;

public class TooltipDisplayPanel extends UIBox {
	Tooltip tooltip;
	Text name, description;

	public TooltipDisplayPanel(UI ui) {
		super(ui, new Vector2f(0, 0), new Vector2f(280, 0), "src/res/ui/uiboxborder.png");
		this.margins.set(10, 10);
		this.alignv = -1;
		this.name = new Text(ui, new Vector2f((float) this.getLocalLeft(true), (float) this.getLocalTop(true)), "name",
				this.getWidth(true), 32, "Univers Condensed", 32, -1, -1);
		this.addChild(name);
		this.description = new Text(ui,
				new Vector2f((float) this.getLocalLeft(true), (float) this.name.getBottom(false, false) + 10), "jeff",
				this.getWidth(true), 24, "Univers Condensed", 24, -1, -1);
		this.addChild(description);

	}

	public void setTooltip(Tooltip tooltip) {
		this.tooltip = tooltip;
		this.name.setText("<b> " + tooltip.name + " </b>");
		this.description.setText(tooltip.description);
		this.description.setPos(
				new Vector2f((float) this.getLocalLeft(true), (float) this.name.getBottom(false, false) + 10), 1);
		this.setDim(new Vector2f((float) this.getWidth(false), (float) this.description.getBottom(false, false)));
	}
}
