package com.transcriber;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class TranscriberOverlay extends Overlay
{
	private final TranscriberPlugin plugin;
	private final TranscriberConfig config;

	@Inject
	private TranscriberOverlay(TranscriberPlugin plugin, TranscriberConfig config)
	{
		this.plugin = plugin;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.showTranscriptionOutline() && plugin.getWidgetLoaded() != null)
		{
			outlineWidget(graphics, plugin.getWidgetLoaded().getParent(), plugin.getSelected());
		}
		return null;
	}

	private void outlineWidget(Graphics2D graphics, Widget widget, String[] selected)
	{
		if (widget == null)
		{
			return;
		}

		if (!widget.isHidden())
		{
			if (widget.getAnimationId() > -1)
			{
				draw(graphics, widget, selected, "<animationID=" + widget.getAnimationId() + ">");
			}
			else if (widget.getItemId() > -1)
			{
				draw(graphics, widget, selected, "<itemID=" + widget.getItemId() + ">");
			}
			else if (widget.getModelId() > -1)
			{
				draw(graphics, widget, selected, "<modelID=" + widget.getModelId() + ">");
			}
			else if (widget.getSpriteId() > -1)
			{
				draw(graphics, widget, selected, "<spriteID=" + widget.getSpriteId() + ">");
			}
			else if (!Strings.isNullOrEmpty(widget.getText()))
			{
				if (!draw(graphics, widget, selected, "<fontID=" + widget.getFontId() + ">"))
				{
					draw(graphics, widget, selected, widget.getText());
				}
			}
		}

		try
		{
			Widget[][] childrens = new Widget[][]
			{
				widget.getStaticChildren(), widget.getDynamicChildren(), widget.getNestedChildren()
			};
			for (Widget[] children : childrens)
			{
				for (Widget child : children)
				{
					outlineWidget(graphics, child, selected);
				}
			}
		}
		catch (NullPointerException ignore)
		{
		}
	}

	private boolean selectionContains(String[] selection, String text)
	{
		for (String s : selection)
		{
			if (text.contains(s))
			{
				return true;
			}
		}
		return false;
	}

	private boolean draw(Graphics2D graphics, Widget widget, String[] selection, String text)
	{
		if (!selectionContains(selection, text))
		{
			return false;
		}

		Rectangle rectangle =  widget.getBounds();
		graphics.setColor(Color.CYAN);
		graphics.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

		return true;
	}
}
