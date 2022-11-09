package com.transcriber;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Transcriber",
	description = "Automatically copy text in books, scrolls, parchments, flyers, etc and NPC overhead text to a panel when you see it",
	tags = {"transcribe", "copy", "text"}
)
public class TranscriberPlugin extends Plugin
{
	private static final int TRANSCRIBE_OFFSET = 1;
	private static final String OVERHEAD_TEXT_DELIM = ": ";

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TranscriberConfig config;

	@Inject
	private TranscriberOverlay overlay;

	private int scheduledTranscribe;
	private Widget widgetBook;
	@Getter
	private Widget widgetLoaded;
	private TranscriberPanel pluginPanel;
	private NavigationButton navigationButton;
	private List<Integer> blacklist = new ArrayList<>();

	@Provides
	TranscriberConfig providesConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TranscriberConfig.class);
	}

	@Override
	protected void startUp()
	{
		pluginPanel = new TranscriberPanel(config);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/panel_icon.png");

		navigationButton = NavigationButton.builder()
			.tooltip("Transcriber")
			.icon(icon)
			.priority(100)
			.panel(pluginPanel)
			.build();

		clientToolbar.addNavigation(navigationButton);

		populateBlacklist();

		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		clientToolbar.removeNavigation(navigationButton);
		pluginPanel = null;
		navigationButton = null;
		blacklist.clear();
	}

	private void transcribeWidget(Widget widget)
	{
		if (widget == null)
		{
			return;
		}

		if (!widget.isHidden())
		{
			int animationId = widget.getAnimationId();
			if (animationId > -1 && config.animationIds())
			{
				pluginPanel.appendText("<animationID=" + animationId + ">");
			}

			int itemId = widget.getItemId();
			if (itemId > -1 && config.itemIds())
			{
				pluginPanel.appendText("<itemID=" + itemId + ">");
			}

			int modelId = widget.getModelId();
			if (modelId > -1 && config.modelIds())
			{
				pluginPanel.appendText("<modelID=" + modelId + ">");
			}

			int spriteId = widget.getSpriteId();
			if (spriteId > -1 && config.spriteIds())
			{
				pluginPanel.appendText("<spriteID=" + spriteId + ">");
			}

			String text = widget.getText();
			if (!Strings.isNullOrEmpty(text))
			{
				int fontId = widget.getFontId();
				if (fontId > -1 && config.fontIds())
				{
					pluginPanel.appendText("<fontID=" + fontId + ">");
				}

				pluginPanel.appendText(text);
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
					transcribeWidget(child);
				}
			}
		}
		catch (NullPointerException ignore)
		{
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (widgetBook != null && scheduledTranscribe == client.getTickCount())
		{
			transcribeWidget(widgetBook);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		final int groupId = event.getGroupId();

		if (blacklist.contains(groupId))
		{
			return;
		}

		widgetLoaded = client.getWidget(groupId, 0);
		if (widgetLoaded != null)
		{
			widgetBook = widgetLoaded.getParent();
		}

		scheduledTranscribe = client.getTickCount() + TRANSCRIBE_OFFSET;
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		final int groupId = event.getGroupId();

		if (widgetLoaded != null && groupId == WidgetInfo.TO_GROUP(widgetLoaded.getId()))
		{
			widgetLoaded = null;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if ("transcriber".equals(event.getGroup()))
		{
			populateBlacklist();
		}
	}

	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged event)
	{
		if (!config.transcribeNpcOverheadText() || !(event.getActor() instanceof NPC))
		{
			return;
		}

		pluginPanel.appendText(event.getActor().getName() + OVERHEAD_TEXT_DELIM + event.getOverheadText());
	}

	public String[] getSelected()
	{
		return pluginPanel.getSelected();
	}

	private void populateBlacklist()
	{
		String[] parts = config.widgetBlacklist().replace(' ', ',').replace(';', ',').replace('\n', ',').split(",");

		blacklist.clear();
		for (String s : parts)
		{
			try
			{
				int id = Integer.parseInt(s);
				blacklist.add(id);
			}
			catch (NumberFormatException ignore)
			{
			}
		}
	}
}
