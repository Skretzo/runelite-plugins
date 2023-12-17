package com.chatsuccessrates;

import com.google.gson.Gson;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.TitleCaseListCellRenderer;

class ChatSuccessRatesPluginPanel extends PluginPanel
{
	private final JPanel successRatesPanel = new JPanel();
	private final JComboBox<ChatSuccessRatesSkill> skillSelection = new JComboBox<>();
	private final JComboBox<ChatSuccessRatesTracker> trackerSelection = new JComboBox<>();

	private final ChatSuccessRatesConfig config;
	private final ChatSuccessRatesPlugin plugin;
	private final Client client;
	private final ConfigManager configManager;
	private final Gson gson;
	private final EventBus eventBus;

	@Getter
	private Map<ChatSuccessRatesSkill, List<ChatSuccessRatesTracker>> trackers = new HashMap<>();

	public ChatSuccessRatesPluginPanel(ChatSuccessRatesConfig config, ChatSuccessRatesPlugin plugin, Client client,
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

		JLabel title = new JLabel("Chat Success Rates");
		title.setForeground(Color.WHITE);

		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBorder(new EmptyBorder(1, 3, 10, 0));
		titlePanel.add(title, BorderLayout.WEST);

		JPanel settingsPanel = new JPanel(new BorderLayout());
		settingsPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		JButton deleteButton = new JButton("Reset data");
		JButton exportButton = new JButton("Export to clipboard");
		deleteButton.setPreferredSize(new Dimension(85, 25));
		exportButton.setPreferredSize(new Dimension(128, 25));
		deleteButton.setForeground(Color.WHITE);
		exportButton.setForeground(Color.WHITE);
		deleteButton.setFont(FontManager.getRunescapeSmallFont());
		exportButton.setFont(FontManager.getRunescapeSmallFont());
		deleteButton.addActionListener(actionEvent ->
		{
			int confirm = JOptionPane.showConfirmDialog(ChatSuccessRatesPluginPanel.this,
				"Are you sure you want to permanently delete the data associated with this tracker?",
				"Warning", JOptionPane.OK_CANCEL_OPTION);

			if (confirm == 0)
			{
				if (trackerSelection.getSelectedItem() == null)
				{
					return;
				}
				ChatSuccessRatesTracker tracker = (ChatSuccessRatesTracker) trackerSelection.getSelectedItem();
				tracker.reset();
			}
		});
		exportButton.addActionListener(actionEvent ->
		{
			if (trackerSelection.getSelectedItem() == null)
			{
				return;
			}
			ChatSuccessRatesTracker tracker = (ChatSuccessRatesTracker) trackerSelection.getSelectedItem();
			StringBuilder text = new StringBuilder(tracker.toString() + ":\nLevel\tSuccesses\tFailures");
			for (ChatSuccessRatesBar bar : tracker.getTrackerBars())
			{
				text.append(text.length() > 0 ? "\n" : "").append(bar.toString());
			}
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text.toString()), null);
		});
		settingsPanel.add(deleteButton, BorderLayout.WEST);
		settingsPanel.add(exportButton, BorderLayout.EAST);

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

		skillSelection.setRenderer(new TitleCaseListCellRenderer());
		trackerSelection.setRenderer(new TitleCaseListCellRenderer());

		loadTrackers();

		skillSelection.addActionListener(e ->
		{
			if (skillSelection.getSelectedItem() != null)
			{
				final ChatSuccessRatesSkill skill = (ChatSuccessRatesSkill) skillSelection.getSelectedItem();
				trackerSelection.removeAllItems();
				for (ChatSuccessRatesTracker tracker : trackers.get(skill))
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
		optionPanel.add(settingsPanel);
		optionPanel.add(headerPanel);

		add(optionPanel, BorderLayout.NORTH);
		add(successRatesPanel, BorderLayout.CENTER);
	}

	private void loadTrackers()
	{
		ChatSuccessRatesAction[] allTrackers = ChatSuccessRatesAction.values();

		for (ChatSuccessRatesAction action : allTrackers)
		{
			ChatSuccessRatesTracker tracker = action.getTracker();

			// Start tracker
			tracker.register(eventBus, client, config, plugin, configManager, gson);
			tracker.loadTrackerData();

			final Map<Integer, ChatSuccessRatesBar> trackerBars = new HashMap<>();
			for (Map.Entry<Integer, Integer[]> levelRate : tracker.getLevelRates().entrySet())
			{
				final ChatSuccessRatesBar bar = new ChatSuccessRatesBar(levelRate.getKey(), tracker.getColor());
				bar.update(levelRate.getValue()[0], levelRate.getValue()[1]);
				trackerBars.put(levelRate.getKey(), bar);
			}
			tracker.setTrackerBars(trackerBars);

			// Prepare selections
			List<ChatSuccessRatesTracker> skillTrackers = new ArrayList<>();
			if (trackers.containsKey(tracker.getSkill()))
			{
				skillTrackers = trackers.get(tracker.getSkill());
			}
			skillTrackers.add(tracker);
			skillTrackers.sort(null);
			trackers.put(tracker.getSkill(), skillTrackers);
		}

		// Populate skill selection
		List<ChatSuccessRatesSkill> skills = new ArrayList<>(trackers.keySet());
		skills.sort(Comparator.comparing(ChatSuccessRatesSkill::ordinal));
		skillSelection.removeAllItems();
		for (ChatSuccessRatesSkill skill : skills)
		{
			skillSelection.addItem(skill);
		}
	}

	public void displaySelectedTracker()
	{
		config.indexSkill(skillSelection.getSelectedIndex());
		config.indexTracker(trackerSelection.getSelectedIndex());

		if (trackerSelection.getSelectedItem() == null)
		{
			return;
		}
		ChatSuccessRatesTracker tracker = (ChatSuccessRatesTracker) trackerSelection.getSelectedItem();

		successRatesPanel.removeAll();

		for (ChatSuccessRatesBar bar : tracker.getTrackerBars())
		{
			successRatesPanel.add(bar);
		}

		repaint();
		revalidate();
	}
}
