package com.chatsuccessrates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import static net.runelite.api.ChatMessageType.GAMEMESSAGE;
import static net.runelite.api.ChatMessageType.SPAM;

@PluginDescriptor(
	name = "Chat Success Rates",
	description = "Track and display skilling success rates",
	tags = {"skilling", "level", "success", "failure", "rate", "tracking", "counter", "distribution"}
)
public class ChatSuccessRatesPlugin extends Plugin
{
	public static final Set<ChatMessageType> COLLAPSIBLE_MESSAGETYPES = ImmutableSet.of(
		GAMEMESSAGE,
		SPAM
	);
	public static final String CONFIG_GROUP = "chatsuccessrates";
	private static final String DUPLICATE_PREFIX = " (";
	private static final String DUPLICATE_SUFFIX = ")";
	private static final String LEVEL_DELIMITER = ": ";
	private static final String COPY_TO_CLIPBOARD_OPTION = "Copy";
	private static final String COPY_TO_CLIPBOARD_TARGET = "Chat success rates";

	private static class Duplicate
	{
		int messageId;
		int count;
	}

	private final LinkedHashMap<String, Duplicate> duplicateChatCache = new LinkedHashMap<String, Duplicate>()
	{
		private static final int MAX_ENTRIES = 100;

		@Override
		protected boolean removeEldestEntry(Map.Entry<String, Duplicate> eldest)
		{
			return size() > MAX_ENTRIES;
		}
	};

	private NavigationButton navigationButton;
	private ChatSuccessRatesPluginPanel pluginPanel;

	@Inject
	private Gson gson;

	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ChatSuccessRatesConfig config;

	@Provides
	ChatSuccessRatesConfig providesConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ChatSuccessRatesConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		client.refreshChat();

		pluginPanel = new ChatSuccessRatesPluginPanel(config, this, client, configManager, gson, eventBus);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "chat_success_rates_icon.png");

		navigationButton = NavigationButton.builder()
			.tooltip("Chat Success Rates")
			.icon(icon)
			.priority(5)
			.panel(pluginPanel)
			.build();

		clientToolbar.addNavigation(navigationButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		for (ChatSuccessRatesSkill skill : pluginPanel.getTrackers().keySet())
		{
			for (ChatSuccessRatesTracker tracker : pluginPanel.getTrackers().get(skill))
			{
				tracker.unregister();
			}
		}

		clientToolbar.removeNavigation(navigationButton);

		pluginPanel = null;
		navigationButton = null;

		duplicateChatCache.clear();
		client.refreshChat();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!CONFIG_GROUP.equals(event.getGroup()))
		{
			return;
		}

		client.refreshChat();
	}

	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		if (!"chatFilterCheck".equals(event.getEventName()))
		{
			return;
		}

		int[] intStack = client.getIntStack();
		int intStackSize = client.getIntStackSize();
		String[] stringStack = client.getStringStack();
		int stringStackSize = client.getStringStackSize();

		final int messageId = intStack[intStackSize - 1];
		String message = stringStack[stringStackSize - 1];

		Duplicate duplicate = duplicateChatCache.get(message);
		if (duplicate == null)
		{
			return;
		}

		final boolean blockMessage = messageId < duplicate.messageId;

		if (blockMessage)
		{
			intStack[intStackSize - 3] = 0;
		}
		else if (duplicate.count > 1)
		{
			stringStack[stringStackSize - 1] = message + DUPLICATE_PREFIX + duplicate.count + DUPLICATE_SUFFIX;
		}
	}

	@Subscribe(priority = -2)
	public void onChatMessage(ChatMessage event)
	{
		String message = event.getMessage();
		if (isTrackedMessage(message, event.getType()))
		{
			message = formatMessage(message);

			event.getMessageNode().setValue(message);

			Duplicate duplicate = duplicateChatCache.remove(message);
			if (duplicate == null)
			{
				duplicate = new Duplicate();
			}

			duplicate.count++;
			duplicate.messageId = event.getMessageNode().getId();
			duplicateChatCache.put(message, duplicate);
		}
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		final Widget chatboxMessageLines = client.getWidget(ComponentID.CHATBOX_MESSAGE_LINES);
		if (duplicateChatCache.isEmpty() ||
			chatboxMessageLines == null ||
			!chatboxMessageLines.getBounds().contains(
				client.getMouseCanvasPosition().getX(),
				client.getMouseCanvasPosition().getY()))
		{
			return;
		}

		client.createMenuEntry(1)
			.setOption(COPY_TO_CLIPBOARD_OPTION)
			.setTarget(COPY_TO_CLIPBOARD_TARGET)
			.setType(MenuAction.RUNELITE)
			.onClick(e ->
			{
				final StringSelection stringSelection = new StringSelection(chatSuccessRatesSummary());
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
			});
	}

	private String chatSuccessRatesSummary()
	{
		StringBuilder summary = new StringBuilder();
		for (String key : duplicateChatCache.keySet())
		{
			summary.append(summary.length() > 0 ? "\n" : "")
				.append(key)
				.append(DUPLICATE_PREFIX)
				.append(duplicateChatCache.get(key).count)
				.append(DUPLICATE_SUFFIX);
		}
		return summary.toString();
	}

	private boolean isTrackedMessage(String message, ChatMessageType type)
	{
		return COLLAPSIBLE_MESSAGETYPES.contains(type) &&
			((!config.messageSuccess().isEmpty() && message.equals(config.messageSuccess())) ||
			(!config.messageFailure().isEmpty() && message.equals(config.messageFailure())));
	}

	private String formatMessage(String message)
	{
		if (config.addLevelPrefix())
		{
			final ChatSuccessRatesSkill skill = config.levelPrefix();
			final int level = ChatSuccessRatesSkill.CUSTOM.equals(skill)
				? client.getTotalLevel()
				: (config.useBoostedLevel()
					? client.getBoostedSkillLevel(skill.getSkill())
					: client.getRealSkillLevel(skill.getSkill()));
			message = level + LEVEL_DELIMITER + message;
		}
		return message;
	}

	public void updatePanel()
	{
		pluginPanel.repaint();
		pluginPanel.revalidate();
	}

	public void rebuildPanel()
	{
		pluginPanel.displaySelectedTracker();
	}
}
