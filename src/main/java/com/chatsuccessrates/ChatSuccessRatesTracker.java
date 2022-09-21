package com.chatsuccessrates;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.SkillColor;
import org.apache.commons.lang3.StringUtils;
import static com.chatsuccessrates.ChatSuccessRatesPlugin.CONFIG_GROUP;

public abstract class ChatSuccessRatesTracker implements Comparable<ChatSuccessRatesTracker>
{
	private static final String CONFIG_DELIM = "_";

	public Client client;
	public ChatSuccessRatesConfig config;
	public ChatSuccessRatesPlugin plugin;

	private ConfigManager configManager;
	private EventBus eventBus;
	private Gson gson;

	@Getter
	private Map<Integer, Integer[]> levelRates = new HashMap<>();

	@Getter
	@Setter
	public Map<Integer, ChatSuccessRatesBar> trackerBars = new HashMap<>();

	public abstract ChatSuccessRatesSkill getSkill();

	public Color getColor()
	{
		return getSkill().getSkill().equals(Skill.OVERALL) ? Color.RED : SkillColor.find(getSkill().getSkill()).getColor();
	}

	public void register(EventBus eventBus, Client client, ChatSuccessRatesConfig config,
		ChatSuccessRatesPlugin plugin, ConfigManager configManager, Gson gson)
	{
		this.eventBus = eventBus;
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		this.configManager = configManager;
		this.gson = gson;

		eventBus.register(this);
	}

	public void unregister()
	{
		eventBus.unregister(this);
	}

	public void update(int lvl, int successes, int fails)
	{
		ChatSuccessRatesBar bar = trackerBars.get(lvl);
		if (bar == null)
		{
			SwingUtilities.invokeLater(() ->
			{
				ChatSuccessRatesBar newBar = new ChatSuccessRatesBar(lvl, getColor());
				trackerBars.put(lvl, newBar);
				newBar.update(successes, fails);
				if (plugin != null)
				{
					plugin.rebuildPanel();
				}
			});
		}
		else
		{
			bar.update(successes, fails);
			if (plugin != null)
			{
				plugin.updatePanel();
			}
		}

		updateTrackerData(lvl, successes, fails);

		saveTrackerData();
	}

	public void updateTrackerData(int level, int successes, int failures)
	{
		int previousSuccesses = 0;
		int previousFailures = 0;

		if (levelRates.containsKey(level))
		{
			Integer[] data = levelRates.get(level);
			previousSuccesses = data[0];
			previousFailures = data[1];
		}

		levelRates.put(level, new Integer[]{previousSuccesses + successes, previousFailures + failures});
	}

	public void loadTrackerData()
	{
		final String key = getSkill() + CONFIG_DELIM + getTrackerName();

		String json = configManager.getConfiguration(CONFIG_GROUP, key);

		if (Strings.isNullOrEmpty(json))
		{
			return;
		}

		levelRates = gson.fromJson(json, new TypeToken<Map<Integer, Integer[]>>(){}.getType());
	}

	private void saveTrackerData()
	{
		final String key = getSkill() + CONFIG_DELIM + getTrackerName();

		if (levelRates == null || levelRates.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, key);
			return;
		}

		configManager.setConfiguration(CONFIG_GROUP, key, gson.toJson(levelRates));
	}

	public void reset()
	{
		levelRates.clear();
		trackerBars.clear();
		saveTrackerData();
		if (plugin != null)
		{
			plugin.rebuildPanel();
		}
	}

	public String getTrackerName()
	{
		return getClass().getSimpleName();
	}

	@Override
	public String toString()
	{
		return StringUtils.capitalize(
			StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(getTrackerName()), " ").toLowerCase());
	}

	@Override
	public int compareTo(ChatSuccessRatesTracker other)
	{
		return toString().compareTo(other.toString());
	}
}
