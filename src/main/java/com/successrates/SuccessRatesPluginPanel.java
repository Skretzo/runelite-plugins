package com.successrates;

import com.google.gson.Gson;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ComboBoxListRenderer;

class SuccessRatesPluginPanel extends PluginPanel
{
	private final JPanel successRatesPanel = new JPanel();
	private final JComboBox<Skill> skillSelection = new JComboBox<>();
	private final JComboBox<SuccessRatesTracker> trackerSelection = new JComboBox<>();

	private final SuccessRatesConfig config;
	private final SuccessRatesPlugin plugin;
	private final Client client;
	private final ConfigManager configManager;
	private final Gson gson;
	private final EventBus eventBus;

	@Getter
	private Map<Skill, List<SuccessRatesTracker>> trackers = new HashMap<>();

	public SuccessRatesPluginPanel(SuccessRatesConfig config, SuccessRatesPlugin plugin, Client client,
		ConfigManager configManager, Gson gson, EventBus eventBus)
	{
		this.config = config;
		this.plugin = plugin;
		this.client = client;
		this.configManager = configManager;
		this.gson = gson;
		this.eventBus = eventBus;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JLabel title = new JLabel("Success Rates");
		title.setForeground(Color.WHITE);

		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBorder(new EmptyBorder(1, 3, 10, 0));
		titlePanel.add(title, BorderLayout.WEST);

		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

		JLabel[] headerLabels = new JLabel[]
		{
			new JLabel("Successes"),
			new JLabel("Level"),
			new JLabel("Failures")
		};

		for (JLabel label : headerLabels)
		{
			label.setForeground(Color.WHITE);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setVerticalAlignment(SwingConstants.TOP);
			label.setFont(FontManager.getRunescapeSmallFont());
			label.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH / headerLabels.length, 10));
		}

		headerPanel.add(headerLabels[0], BorderLayout.WEST);
		headerPanel.add(headerLabels[1], BorderLayout.CENTER);
		headerPanel.add(headerLabels[2], BorderLayout.EAST);

		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
		optionPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

		successRatesPanel.setLayout(new BoxLayout(successRatesPanel, BoxLayout.Y_AXIS));
		successRatesPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		skillSelection.setRenderer(new ComboBoxListRenderer<>());
		trackerSelection.setRenderer(new ComboBoxListRenderer<>());

		loadTrackers();

		skillSelection.addActionListener(e ->
		{
			if (skillSelection.getSelectedItem() != null)
			{
				final Skill skill = (Skill) skillSelection.getSelectedItem();
				trackerSelection.removeAllItems();
				for (SuccessRatesTracker tracker : trackers.get(skill))
				{
					trackerSelection.addItem(tracker);
				}
			}
		});

		trackerSelection.addActionListener(e -> displaySelectedTracker());

		skillSelection.setSelectedIndex(skillSelection.getItemCount() > config.indexSkill() ? config.indexSkill() : 0);

		trackerSelection.setSelectedIndex(trackerSelection.getItemCount() > config.indexTracker() ? config.indexTracker() : 0);

		optionPanel.add(titlePanel);
		optionPanel.add(skillSelection);
		optionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		optionPanel.add(trackerSelection);
		// optionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		// optionPanel.add(exportButton); // "Export to clipboard"
		optionPanel.add(headerPanel);

		add(optionPanel, BorderLayout.NORTH);
		add(successRatesPanel, BorderLayout.CENTER);
	}

	private void loadTrackers()
	{
		SuccessRatesAction[] allTrackers = SuccessRatesAction.values();

		for (SuccessRatesAction action : allTrackers)
		{
			SuccessRatesTracker tracker = action.getTracker();

			// Start tracker
			tracker.register(eventBus, client, plugin, configManager, gson);
			tracker.loadTrackerData();

			final Map<Integer, SuccessRatesBar> trackerBars = new HashMap<>();
			for (int i = 0; i < 100; i++)
			{
				final SuccessRatesBar bar = new SuccessRatesBar(i, tracker.getSkill());
				if (tracker.getLevelRates().containsKey(i))
				{
					Integer[] data = tracker.getLevelRates().get(i);
					bar.update(data[0], data[1]);
				}
				trackerBars.put(i, bar);
			}
			tracker.setTrackerBars(trackerBars);

			// Prepare selections
			List<SuccessRatesTracker> skillTrackers = new ArrayList<>();
			if (trackers.containsKey(tracker.getSkill()))
			{
				skillTrackers = trackers.get(tracker.getSkill());
			}
			skillTrackers.add(tracker);
			Collections.sort(skillTrackers);
			trackers.put(tracker.getSkill(), skillTrackers);
		}

		// Populate skill selection
		List<Skill> skills = new ArrayList<>(trackers.keySet());
		skills.sort(Comparator.comparing(Skill::getName));
		skillSelection.removeAllItems();
		for (Skill skill : skills)
		{
			skillSelection.addItem(skill);
		}
	}

	private void displaySelectedTracker()
	{
		config.indexSkill(skillSelection.getSelectedIndex());
		config.indexTracker(trackerSelection.getSelectedIndex());

		if (trackerSelection.getSelectedItem() == null)
		{
			return;
		}
		SuccessRatesTracker tracker = (SuccessRatesTracker) trackerSelection.getSelectedItem();

		successRatesPanel.removeAll();

		for (SuccessRatesBar bar : tracker.getTrackerBars().values())
		{
			successRatesPanel.add(bar);
		}

		repaint();
		revalidate();
	}
}
