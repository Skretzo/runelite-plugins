package com.transcriber;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Transcriber",
	description = "Automatically copy text in books, scrolls, parchments, flyers, etc to a panel when you read it",
	tags = {"transcribe", "copy", "text"}
)
public class TranscriberPlugin extends Plugin
{
	private static final int BOOK_OPENING_MAX_OFFSET = 5;
	private static final int TRANSCRIBE_OFFSET = 1;
	private static final String BOOK_OPTION_NEXT_PAGE = "Continue";
	private static final String BOOK_OPTION_READ = "Read";

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private TranscriberConfig config;

	private boolean readingBook;
	private int openedBook = -BOOK_OPENING_MAX_OFFSET;
	private int scheduledTranscribe;
	private Widget widgetBook;
	private TranscriberPanel pluginPanel;
	private NavigationButton navigationButton;

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
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navigationButton);
		pluginPanel = null;
		navigationButton = null;
	}

	private void transcribeWidget(Widget widget)
	{
		if (widget == null)
		{
			return;
		}

		if (!widget.isHidden())
		{
			String text = widget.getText();
			if (!Strings.isNullOrEmpty(text))
			{
				pluginPanel.appendText(text);
			}

			int itemId = widget.getItemId();
			if (itemId > -1)
			{
				pluginPanel.appendText("<itemID=" + itemId + ">");
			}

			int spriteId = widget.getSpriteId();
			if (spriteId > -1)
			{
				pluginPanel.appendText("<spriteID=" + spriteId + ">");
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
		final int tickCount = client.getTickCount();

		if (readingBook || (tickCount - openedBook) < BOOK_OPENING_MAX_OFFSET)
		{
			readingBook = true;
			widgetBook = client.getWidget(groupId, 0);
			if (widgetBook != null)
			{
				widgetBook = widgetBook.getParent();
			}
			scheduledTranscribe = tickCount + TRANSCRIBE_OFFSET;
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		readingBook = false;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		final String option = event.getMenuOption();
		if (option.startsWith(BOOK_OPTION_READ) ||
			(option.equals(BOOK_OPTION_NEXT_PAGE) && readingBook))
		{
			openedBook = client.getTickCount();
		}
	}
}
