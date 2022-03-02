package com.chatsuccessrates;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.runelite.api.ChatMessageType;
import static net.runelite.api.ChatMessageType.GAMEMESSAGE;
import static net.runelite.api.ChatMessageType.SPAM;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Chat Success Rates",
	description = "Track skilling success rates with game chat messages",
	tags = {"chat", "message", "success", "failure", "skill", "levels", "counter"}
)
public class ChatSuccessRatesPlugin extends Plugin
{
	private static final String DUPLICATE_PREFIX = " (";
	private static final String DUPLICATE_SUFFIX = ")";
	private static final String LEVEL_DELIMITER = ": ";
	private static final String COPY_TO_CLIPBOARD_OPTION = "Copy";
	private static final String COPY_TO_CLIPBOARD_TARGET = "Chat success rates";
	private static final Set<ChatMessageType> COLLAPSIBLE_MESSAGETYPES = ImmutableSet.of(
		GAMEMESSAGE,
		SPAM
	);

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

	@Inject
	private Client client;

	@Inject
	private ChatSuccessRatesConfig config;

	@Provides
	ChatSuccessRatesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ChatSuccessRatesConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		client.refreshChat();
	}

	@Override
	protected void shutDown() throws Exception
	{
		duplicateChatCache.clear();
		client.refreshChat();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!"chatsuccessrates".equals(event.getGroup()))
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
		final Widget chatboxMessageLines = client.getWidget(WidgetInfo.CHATBOX_MESSAGE_LINES);
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
			final Skill skill = config.levelPrefix();
			final int level = (!config.useBoostedLevel() || Skill.OVERALL.equals(skill)) ?
				client.getRealSkillLevel(skill) : client.getBoostedSkillLevel(skill);
			message = level + LEVEL_DELIMITER + message;
		}
		return message;
	}
}
