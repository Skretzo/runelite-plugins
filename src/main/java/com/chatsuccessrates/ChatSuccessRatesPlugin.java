package com.chatsuccessrates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MessageNode;
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
import static net.runelite.api.ChatMessageType.MESBOX;
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
		SPAM,
		MESBOX
	);
	public static final String CONFIG_GROUP = "chatsuccessrates";
	public static final String MESSAGE_DELIM = "\n";
	private static final String DUPLICATE_PREFIX = " (";
	private static final String DUPLICATE_SUFFIX = ")";
	private static final String LEVEL_DELIMITER = ": ";
	private static final String COPY_TO_CLIPBOARD_OPTION = "Copy";
	private static final String COPY_TO_CLIPBOARD_TARGET = "Chat success rates";

	private static final Map<ChatMessageType, EvictingLinkedHashMap<String, Duplicate>> DUPLICATE_CACHE = new HashMap<>();

	static
	{
		for (ChatMessageType chatMessageType : COLLAPSIBLE_MESSAGETYPES)
		{
			DUPLICATE_CACHE.put(chatMessageType, new EvictingLinkedHashMap<String, Duplicate>());
		}
		DUPLICATE_CACHE.get(MESBOX).MAX_ENTRIES = 300;
	}

	private static class Duplicate
	{
		int messageId;
		int count;
	}

	private static class EvictingLinkedHashMap<E, V> extends LinkedHashMap<E, V>
	{
		int MAX_ENTRIES = 100;

		@Override
		protected boolean removeEldestEntry(Map.Entry<E, V> eldest)
		{
			return size() > MAX_ENTRIES;
		}
	}

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

		for (ChatMessageType chatMessageType : DUPLICATE_CACHE.keySet())
		{
			DUPLICATE_CACHE.get(chatMessageType).clear();
		}
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

		Duplicate duplicate = null;
		for (ChatMessageType chatMessageType : DUPLICATE_CACHE.keySet())
		{
			Duplicate candidate = DUPLICATE_CACHE.get(chatMessageType).get(message);
			if (candidate != null)
			{
				duplicate = candidate;
				break;
			}
		}
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
		ChatMessageType messageType = event.getType();
		if (isTrackedMessage(message, messageType))
		{
			MessageNode node;
			if (messageType == MESBOX) {
				node = client.addChatMessage(GAMEMESSAGE, "", message, "");
			}
			else {
				node = event.getMessageNode();
			}

			message = formatMessage(message);
			node.setValue(message);

			Duplicate duplicate = DUPLICATE_CACHE.get(messageType).remove(message);
			if (duplicate == null)
			{
				duplicate = new Duplicate();
			}

			duplicate.count++;
			duplicate.messageId = event.getMessageNode().getId();
			DUPLICATE_CACHE.get(messageType).put(message, duplicate);
		}
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		final Widget chatboxMessageLines = client.getWidget(ComponentID.CHATBOX_MESSAGE_LINES);
		if (chatboxMessageLines == null ||
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
		for (ChatMessageType messageType : new ChatMessageType[]{GAMEMESSAGE, SPAM})
		{
			Map<String, Duplicate> duplicateCache = DUPLICATE_CACHE.get(messageType);
			for (String key : duplicateCache.keySet())
			{
				summary.append(summary.length() > 0 ? "\n" : "")
					.append(key)
					.append(DUPLICATE_PREFIX)
					.append(duplicateCache.get(key).count)
					.append(DUPLICATE_SUFFIX);
			}
		}
		return summary.toString();
	}

	private boolean isTrackedMessage(String message, ChatMessageType type)
	{
		if (COLLAPSIBLE_MESSAGETYPES.contains(type))
		{
			for (String successMessage : config.messageSuccess().split(MESSAGE_DELIM))
			{
				if (message.equals(successMessage))
				{
					return true;
				}
			}
			for (String failureMessage : config.messageFailure().split(MESSAGE_DELIM))
			{
				if (message.equals(failureMessage))
				{
					return true;
				}
			}
		}
		return false;
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
