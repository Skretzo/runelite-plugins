package com.plugintoggler;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class CombatLevelOverlay extends OverlayPanel
{
	private final Client client;
	private final PluginTogglerConfig config;
	private final OverlayMenuEntry overlayMenuEntry = new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY,
		PluginTogglerMenu.MENU_DISPLAY_OPTION, "Accurate combat level");

	@Inject
	private CombatLevelOverlay(Client client, PluginTogglerConfig config)
	{
		this.client = client;
		this.config = config;

		setPosition(OverlayPosition.DETACHED);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final boolean hasMenu = getMenuEntries().contains(overlayMenuEntry);
		if (config.combatLevel() && !hasMenu)
		{
			getMenuEntries().add(overlayMenuEntry);
		}
		else if (!config.combatLevel() && hasMenu)
		{
			getMenuEntries().remove(overlayMenuEntry);
		}

		Widget combatLevelWidget = client.getWidget(WidgetInfo.COMBAT_LEVEL);
		if (combatLevelWidget == null || combatLevelWidget.isHidden())
		{
			return super.render(graphics);
		}

		Rectangle combatLevelCanvas = combatLevelWidget.getBounds();
		if (combatLevelCanvas != null)
		{
			setPreferredLocation(new Point((int) combatLevelCanvas.getX(),
				(int) (combatLevelCanvas.getY() - combatLevelCanvas.getHeight() / 3)));
			setPreferredSize(combatLevelCanvas.getSize());
			setPreferredColor(new Color(0, 0, 0, 0));
			panelComponent.getChildren().add(TitleComponent.builder().text("").build());
		}

		return super.render(graphics);
	}
}
