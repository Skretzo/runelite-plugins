package com.successrates;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

public abstract class SuccessRatesTracker implements Comparable<SuccessRatesTracker>
{
	private static final String CONFIG_DELIM = "_";
	private static final String CONFIG_GROUP = "successrates";

	public Client client;
	public SuccessRatesPlugin plugin;

	private ConfigManager configManager;
	private EventBus eventBus;
	private Gson gson;

	@Getter
	private Map<Integer, Integer[]> levelRates = new HashMap<>();

	@Getter
	@Setter
	public Map<Integer, SuccessRatesBar> trackerBars = new HashMap<>();

	public abstract Skill getSkill();

	public void register(EventBus eventBus, Client client, SuccessRatesPlugin plugin,
		ConfigManager configManager, Gson gson)
	{
		this.eventBus = eventBus;
		this.client = client;
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
		SuccessRatesBar bar = trackerBars.get(lvl);
		if (bar != null)
		{
			bar.update(successes, fails);
		}

		updateTrackerData(lvl, successes, fails);

		if (plugin != null)
		{
			plugin.updatePanel();
		}

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
		}

		configManager.setConfiguration(CONFIG_GROUP, key, gson.toJson(levelRates));
	}

	private String getTrackerName()
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
	public int compareTo(SuccessRatesTracker other)
	{
		return toString().compareTo(other.toString());
	}
}
