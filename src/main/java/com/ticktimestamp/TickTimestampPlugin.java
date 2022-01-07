package com.ticktimestamp;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Tick Timestamp",
	description = "Display a game client tick count as a timestamp on game chat messages.<br>" +
		"Useful for knowing if you are on pace when skilling or timing actions.",
	tags = {"tick", "count", "timestamp"}
)
public class TickTimestampPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private TickTimestampConfig config;

	private int lastTickCount;

	@Provides
	TickTimestampConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TickTimestampConfig.class);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		ChatMessageType type = event.getType();
		if (!ChatMessageType.GAMEMESSAGE.equals(type) && !ChatMessageType.SPAM.equals(type))
		{
			return;
		}

		int tickCount = client.getTickCount();
		int timestamp = config.deltaTick() ? (tickCount - lastTickCount) : tickCount;
		lastTickCount = tickCount;

		event.getMessageNode().setValue(timestamp + ": " + event.getMessageNode().getValue());
	}
}
