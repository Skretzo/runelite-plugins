package com.damagetracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

public class DamageTrackerPanel extends JPanel
{
	private static final ImageIcon COLLAPSE_ICON;
	private static final ImageIcon COLLAPSE_HOVER_ICON;
	private static final ImageIcon DELETE_ICON;
	private static final ImageIcon DELETE_HOVER_ICON;
	private static final ImageIcon EXPAND_ICON;
	private static final ImageIcon EXPAND_HOVER_ICON;

	@Getter(AccessLevel.PACKAGE)
	private final DamageTracker tracker;
	private final DamageTrackerPlugin plugin;

	private final JButton collapse;
	private final JCheckBox toggle = new JCheckBox();
	private final JPanel containerBars = new JPanel();
	private final JPanel containerOptions = new JPanel();

	private final FlatTextField inputName = new FlatTextField();
	private final JLabel save = new JLabel("Save");
	private final JLabel cancel = new JLabel("Cancel");
	private final JLabel rename = new JLabel("Edit");
	private final JLabel delete = new JLabel();

	private final FlatTextField inputTargetName = new FlatTextField();

	static
	{
		final BufferedImage deleteImg = ImageUtil.loadImageResource(DamageTrackerPlugin.class, "/delete_icon.png");
		DELETE_ICON = new ImageIcon(deleteImg);
		DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImg, -100));

		BufferedImage retractIcon = ImageUtil.loadImageResource(DamageTrackerPlugin.class, "/arrow_right.png");
		retractIcon = ImageUtil.luminanceOffset(retractIcon, -121);
		EXPAND_ICON = new ImageIcon(retractIcon);
		EXPAND_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(retractIcon, -100));

		final BufferedImage expandIcon = ImageUtil.rotateImage(retractIcon, Math.PI / 2);
		COLLAPSE_ICON = new ImageIcon(expandIcon);
		COLLAPSE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(expandIcon, -100));
	}

	DamageTrackerPanel(DamageTrackerPlugin plugin, DamageTrackerPluginPanel panel, DamageTracker tracker)
	{
		this.plugin = plugin;
		this.tracker = tracker;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel nameWrapper = new JPanel(new BorderLayout());
		nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameWrapper.setBorder(new CompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
			BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR)));
		nameWrapper.setPreferredSize(new Dimension(0, 30));

		JPanel nameActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 3));
		nameActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		save.setVisible(false);
		save.setFont(FontManager.getRunescapeSmallFont());
		save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		save.setBorder(new EmptyBorder(3, 0, 0, 3));
		save.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				save();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
			}
		});

		cancel.setVisible(false);
		cancel.setFont(FontManager.getRunescapeSmallFont());
		cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		cancel.setBorder(new EmptyBorder(3, 0, 0, 3));
		cancel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				cancel();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
			}
		});

		rename.setFont(FontManager.getRunescapeSmallFont());
		rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
		rename.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				inputName.setEditable(true);
				updateNameActions(true);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker().darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
			}
		});

		inputName.setText(tracker.getName());
		inputName.setBorder(null);
		inputName.setEditable(false);
		inputName.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		inputName.setPreferredSize(new Dimension(0, 24));
		inputName.getTextField().setForeground(Color.WHITE);
		inputName.getTextField().setBorder(new EmptyBorder(0, 5, 0, 0));
		inputName.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					save();
				}
				else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					cancel();
				}
			}
		});
		inputName.getTextField().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					final boolean open = containerOptions.isVisible();
					tracker.setCollapsed(open);
					updateCollapsed();
					plugin.saveTracker(tracker);
				}
			}
		});

		JPanel containerOwnDamage = new JPanel(new BorderLayout());
		JPanel containerTargetName = new JPanel(new BorderLayout());

		containerOwnDamage.setBorder(new EmptyBorder(2, 0, 2, 0));
		containerTargetName.setBorder(new EmptyBorder(2, 0, 2, 0));
		containerOptions.setBorder(new EmptyBorder(5, 5, 2, 5));
		containerBars.setBorder(new EmptyBorder(5, 10, 5, 10));
		containerOptions.setLayout(new BoxLayout(containerOptions, BoxLayout.Y_AXIS));
		containerBars.setLayout(new BoxLayout(containerBars, BoxLayout.Y_AXIS));
		containerOwnDamage.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		containerTargetName.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		containerOptions.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		containerBars.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel leftActionsOwnDamage = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		JPanel leftActionsTargetName = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

		leftActionsOwnDamage.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		leftActionsTargetName.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JCheckBox ownDamage = new JCheckBox();
		ownDamage.setSelected(tracker.isOnlyOwnDamage());
		ownDamage.setBorder(new EmptyBorder(0, 6, 1, 0));
		ownDamage.setToolTipText((ownDamage.isSelected() ? "Disable" : "Enable") + " own damage");
		SwingUtil.removeButtonDecorations(ownDamage);
		ownDamage.addActionListener(actionEvent ->
		{
			tracker.setOnlyOwnDamage(!tracker.isOnlyOwnDamage());
			ownDamage.setToolTipText((ownDamage.isSelected() ? "Disable" : "Enable") + " own damage");
			plugin.saveTracker(tracker);
		});

		JLabel labelOwnDamage = new JLabel("Only track own damage");
		JLabel labelTargetName = new JLabel("Target name");

		inputTargetName.setText(tracker.getTargetName());
		inputTargetName.setBorder(null);
		inputTargetName.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
		inputTargetName.setPreferredSize(new Dimension(120, 20));
		inputTargetName.getTextField().setForeground(Color.WHITE);
		inputTargetName.getTextField().setBorder(new EmptyBorder(0, 5, 0, 0));
		inputTargetName.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent e)
			{
				tracker.setTargetName(inputTargetName.getText());
				plugin.saveTracker(tracker);
			}
		});

		labelOwnDamage.setFont(FontManager.getRunescapeSmallFont());
		labelTargetName.setFont(FontManager.getRunescapeSmallFont());
		labelOwnDamage.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
		labelTargetName.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
		labelOwnDamage.setPreferredSize(new Dimension(162, 21));
		labelTargetName.setPreferredSize(new Dimension(70, 21));
		labelOwnDamage.setToolTipText("Whether to only track damage dealt by your own character");
		labelTargetName.setToolTipText("<html>The name of the NPC or Player to track<br>" +
			"(blank to track all opponents, append #number to specify NPC id)</html>");

		leftActionsOwnDamage.add(ownDamage);
		leftActionsTargetName.add(labelTargetName);

		JPanel rightActionsOwnDamage = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		JPanel rightActionsTargetName = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

		rightActionsOwnDamage.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		rightActionsTargetName.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		rightActionsOwnDamage.add(labelOwnDamage);
		rightActionsTargetName.add(inputTargetName);

		collapse = new JButton(tracker.isCollapsed() ? COLLAPSE_ICON : EXPAND_ICON);
		collapse.setRolloverIcon(tracker.isCollapsed() ? COLLAPSE_HOVER_ICON : EXPAND_HOVER_ICON);
		collapse.setPreferredSize(new Dimension(15, 0));
		collapse.setBorder(new EmptyBorder(0, 6, 1, 0));
		collapse.setToolTipText((tracker.isCollapsed() ? "Expand" : "Collapse") + " tracker");
		SwingUtil.removeButtonDecorations(collapse);
		collapse.addActionListener(e ->
		{
			final boolean open = containerOptions.isVisible();
			tracker.setCollapsed(open);
			updateCollapsed();
			plugin.saveTracker(tracker);
		});

		toggle.setSelected(tracker.isEnabled());
		toggle.setBorder(new EmptyBorder(0, 6, 1, 0));
		toggle.setToolTipText((tracker.isEnabled() ? "Disable" : "Enable") + " tracker");
		SwingUtil.removeButtonDecorations(toggle);
		toggle.addActionListener(actionEvent ->
		{
			tracker.setEnabled(!tracker.isEnabled());
			toggle.setToolTipText((tracker.isEnabled() ? "Disable" : "Enable") + " tracker");
			plugin.saveTracker(tracker);
		});

		delete.setIcon(DELETE_ICON);
		delete.setToolTipText("Delete tracker");
		delete.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				int confirm = JOptionPane.showConfirmDialog(DamageTrackerPanel.this,
					"Are you sure you want to permanently delete this damage tracker?",
					"Warning", JOptionPane.OK_CANCEL_OPTION);

				if (confirm == 0)
				{
					panel.removeTracker(DamageTrackerPanel.this);
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				delete.setIcon(DELETE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				delete.setIcon(DELETE_ICON);
			}
		});

		nameActions.add(rename);
		nameActions.add(cancel);
		nameActions.add(save);
		nameActions.add(toggle);
		nameActions.add(delete);

		nameWrapper.add(collapse, BorderLayout.WEST);
		nameWrapper.add(inputName, BorderLayout.CENTER);
		nameWrapper.add(nameActions, BorderLayout.EAST);

		containerOwnDamage.add(leftActionsOwnDamage, BorderLayout.WEST);
		containerOwnDamage.add(rightActionsOwnDamage, BorderLayout.EAST);

		containerTargetName.add(leftActionsTargetName, BorderLayout.WEST);
		containerTargetName.add(rightActionsTargetName, BorderLayout.EAST);

		containerOptions.add(containerOwnDamage);
		containerOptions.add(containerTargetName);

		JPanel trackerContainer = new JPanel();
		trackerContainer.setLayout(new BoxLayout(trackerContainer, BoxLayout.Y_AXIS));
		trackerContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		trackerContainer.add(nameWrapper);
		trackerContainer.add(containerOptions);
		trackerContainer.add(containerBars);

		add(trackerContainer);

		KeyAdapter copyToClipbard = new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C)
				{
					StringBuilder text = new StringBuilder();
					for (DamageTrackerBar bar : tracker.getTrackerBars().values())
					{
						text.append(bar);
						text.append("\n");
					}
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text.toString()), null);
				}
			}
		};
		labelTargetName.addKeyListener(copyToClipbard);
		inputTargetName.addKeyListener(copyToClipbard);
		containerBars.addKeyListener(copyToClipbard);

		updateBars();
		updateCollapsed();
	}

	public void setTrackerText(final String text)
	{
		inputName.setText(text);
	}

	public void setTargetInfo(final String targetName)
	{
		inputTargetName.setText(targetName);
	}

	public void updateBars()
	{
		containerBars.removeAll();

		List<DamageTrackerBar> trackerBars = new ArrayList<>(tracker.getTrackerBars().values());
		Collections.sort(trackerBars);

		for (DamageTrackerBar bar : trackerBars)
		{
			containerBars.add(bar);
		}

		repaint();
		revalidate();
	}

	private void save()
	{
		tracker.setName(inputName.getText());
		plugin.saveTracker(tracker);

		inputName.setEditable(false);
		updateNameActions(false);
		requestFocusInWindow();
	}

	private void cancel()
	{
		inputName.setEditable(false);
		inputName.setText(tracker.getName());
		updateNameActions(false);
		requestFocusInWindow();
	}

	private void updateNameActions(boolean saveAndCancel)
	{
		save.setVisible(saveAndCancel);
		cancel.setVisible(saveAndCancel);
		rename.setVisible(!saveAndCancel);
		collapse.setVisible(!saveAndCancel);
		toggle.setVisible(!saveAndCancel);
		delete.setVisible(!saveAndCancel);

		if (saveAndCancel)
		{
			inputName.getTextField().requestFocusInWindow();
			inputName.getTextField().selectAll();
			plugin.setRenameTracker(this);
		}
		else
		{
			plugin.setRenameTracker(null);
		}
	}

	private void updateCollapsed()
	{
		final boolean open = !tracker.isCollapsed();

		rename.setVisible(open);
		toggle.setVisible(open);

		containerOptions.setVisible(open);
		containerBars.setVisible(open);

		collapse.setIcon(open ? COLLAPSE_ICON : EXPAND_ICON);
		collapse.setRolloverIcon(open ? COLLAPSE_HOVER_ICON : EXPAND_HOVER_ICON);
		collapse.setToolTipText((open ? "Collapse" : "Expand") + " tracker");
	}
}
