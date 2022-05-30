package com.plugintoggler;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

public class PluginTogglerType
{
	private MenuAction menuAction;
	private WidgetInfo widgetInfo;

	PluginTogglerType(MenuAction menuAction)
	{
		this.menuAction = menuAction;
	}

	PluginTogglerType(WidgetInfo widgetInfo)
	{
		this.widgetInfo = widgetInfo;
	}

	public boolean equals(MenuAction menuAction, Client client)
	{
		if (this.menuAction != null && menuAction != null)
		{
			return this.menuAction.equals(menuAction);
		}

		Widget widget = client.getWidget(this.widgetInfo);
		if (widget != null && !widget.isHidden())
		{
			return widget.getBounds().contains(
				client.getMouseCanvasPosition().getX(),
				client.getMouseCanvasPosition().getY());
		}

		return false;
	}
}
