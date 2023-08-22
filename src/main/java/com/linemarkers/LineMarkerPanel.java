package com.linemarkers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.ComboBoxListRenderer;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

class LineMarkerPanel extends JPanel
{
	private static final Border NAME_BOTTOM_BORDER = new CompoundBorder(
		BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
		BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR));

	private static final ImageIcon SETTINGS_ICON;
	private static final ImageIcon SETTINGS_HOVER_ICON;
	private static final ImageIcon NO_SETTINGS_ICON;
	private static final ImageIcon NO_SETTINGS_HOVER_ICON;

	private static final ImageIcon VISIBLE_ICON;
	private static final ImageIcon VISIBLE_HOVER_ICON;
	private static final ImageIcon INVISIBLE_ICON;
	private static final ImageIcon INVISIBLE_HOVER_ICON;

	private static final ImageIcon DELETE_ICON;
	private static final ImageIcon DELETE_HOVER_ICON;

	private static final ImageIcon COLLAPSE_ICON;
	private static final ImageIcon COLLAPSE_HOVER_ICON;
	private static final ImageIcon EXPAND_ICON;
	private static final ImageIcon EXPAND_HOVER_ICON;

	private final LineMarkerPlugin plugin;
	private final LineGroup marker;

	private final JLabel visibilityMarker = new JLabel();
	private final JLabel deleteMarker = new JLabel();
	private final JButton expandToggle;
	private final JPanel markerContainer = new JPanel();

	private final FlatTextField nameInput = new FlatTextField();
	private final JLabel save = new JLabel("Save");
	private final JLabel cancel = new JLabel("Cancel");
	private final JLabel rename = new JLabel("Rename");

	static
	{
		final BufferedImage borderImg = ImageUtil.loadImageResource(LineMarkerPlugin.class, "settings_icon.png");
		final BufferedImage borderImgHover = ImageUtil.luminanceOffset(borderImg, -150);
		SETTINGS_ICON = new ImageIcon(borderImg);
		SETTINGS_HOVER_ICON = new ImageIcon(borderImgHover);

		NO_SETTINGS_ICON = new ImageIcon(borderImgHover);
		NO_SETTINGS_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(borderImgHover, -100));

		final BufferedImage visibleImg = ImageUtil.loadImageResource(LineMarkerPlugin.class, "visible_icon.png");
		VISIBLE_ICON = new ImageIcon(visibleImg);
		VISIBLE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(visibleImg, -100));

		final BufferedImage invisibleImg = ImageUtil.loadImageResource(LineMarkerPlugin.class, "invisible_icon.png");
		INVISIBLE_ICON = new ImageIcon(invisibleImg);
		INVISIBLE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(invisibleImg, -100));

		final BufferedImage deleteImg = ImageUtil.loadImageResource(LineMarkerPlugin.class, "delete_icon.png");
		DELETE_ICON = new ImageIcon(deleteImg);
		DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImg, -100));

		BufferedImage retractIcon = ImageUtil.loadImageResource(LineMarkerPlugin.class, "arrow_right.png");
		retractIcon = ImageUtil.luminanceOffset(retractIcon, -121);
		EXPAND_ICON = new ImageIcon(retractIcon);
		EXPAND_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(retractIcon, -100));
		final BufferedImage expandIcon = ImageUtil.rotateImage(retractIcon, Math.PI / 2);
		COLLAPSE_ICON = new ImageIcon(expandIcon);
		COLLAPSE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(expandIcon, -100));
	}

	LineMarkerPanel(LineMarkerPlugin plugin, LineMarkerConfig config, LineGroup marker)
	{
		this.plugin = plugin;
		this.marker = marker;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new EmptyBorder(0, 0, 0, 0));

		JPanel nameWrapper = new JPanel(new BorderLayout());
		nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameWrapper.setBorder(NAME_BOTTOM_BORDER);

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
				nameInput.setEditable(true);
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

		nameInput.setText(marker.getName());
		nameInput.setBorder(null);
		nameInput.setEditable(false);
		nameInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameInput.setPreferredSize(new Dimension(0, 24));
		nameInput.getTextField().setForeground(Color.WHITE);
		nameInput.getTextField().setBorder(new EmptyBorder(0, 5, 0, 0));
		nameInput.addKeyListener(new KeyAdapter()
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
		nameInput.getTextField().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					marker.setCollapsed(!marker.isCollapsed());
					updateCollapsed();
					plugin.saveMarkers();
				}
			}
		});

		expandToggle = new JButton(marker.isCollapsed() ? COLLAPSE_ICON : EXPAND_ICON);
		expandToggle.setRolloverIcon(marker.isCollapsed() ? COLLAPSE_HOVER_ICON : EXPAND_HOVER_ICON);
		expandToggle.setPreferredSize(new Dimension(15, 0));
		expandToggle.setBorder(new EmptyBorder(0, 6, 1, 0));
		expandToggle.setToolTipText((marker.isCollapsed() ? "Expand" : "Collapse") + " marker");
		SwingUtil.removeButtonDecorations(expandToggle);
		expandToggle.addActionListener(actionEvent ->
		{
			marker.setCollapsed(!marker.isCollapsed());
			updateCollapsed();
			plugin.saveMarkers();
		});

		visibilityMarker.setIcon(marker.isVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
		visibilityMarker.setToolTipText((marker.isVisible() ? "Hide" : "Show") + " marker");
		visibilityMarker.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setVisible(!marker.isVisible());
				visibilityMarker.setIcon(marker.isVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
				plugin.saveMarkers();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilityMarker.setIcon(marker.isVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				visibilityMarker.setIcon(marker.isVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
			}
		});

		deleteMarker.setIcon(DELETE_ICON);
		deleteMarker.setToolTipText("Delete marker");
		deleteMarker.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				int confirm = JOptionPane.showConfirmDialog(LineMarkerPanel.this,
					"Are you sure you want to permanently delete this line marker?",
					"Warning", JOptionPane.OK_CANCEL_OPTION);

				if (confirm == 0)
				{
					plugin.removeMarker(marker);
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				deleteMarker.setIcon(DELETE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				deleteMarker.setIcon(DELETE_ICON);
			}
		});

		nameActions.add(rename);
		nameActions.add(cancel);
		nameActions.add(save);
		nameActions.add(visibilityMarker);
		nameActions.add(deleteMarker);

		nameWrapper.add(expandToggle, BorderLayout.WEST);
		nameWrapper.add(nameInput, BorderLayout.CENTER);
		nameWrapper.add(nameActions, BorderLayout.EAST);

		markerContainer.setLayout(new BoxLayout(markerContainer, BoxLayout.Y_AXIS));
		markerContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		markerContainer.add(nameWrapper);

		for (Line line : marker.getLines())
		{
			JPanel container = new JPanel(new BorderLayout());
			container.setBorder(new EmptyBorder(5, 0, 5, 0));
			container.setBackground(ColorScheme.DARKER_GRAY_COLOR);

			JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
			leftActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

			JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
			rightActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

			JLabel colour = new JLabel();
			colour.setToolTipText("Edit line colour");
			colour.setForeground(line.getColour() == null ? config.defaultColour() : line.getColour());
			colour.setBorder(line.getWidth() == 0 ? null : new MatteBorder(0, 0, 3, 0, line.getColour()));
			colour.setIcon(line.getWidth() == 0 ? NO_SETTINGS_ICON : SETTINGS_ICON);
			colour.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					RuneliteColorPicker colourPicker = getColourPicker(line.getColour() == null ? config.defaultColour() : line.getColour());
					colourPicker.setOnColorChange(c ->
					{
						line.setColour(c);
						colour.setBorder(line.getWidth() == 0 ? null : new MatteBorder(0, 0, 3, 0, line.getColour()));
						colour.setIcon(line.getWidth() == 0 ? NO_SETTINGS_ICON : SETTINGS_ICON);
					});
					colourPicker.setVisible(true);
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					colour.setIcon(line.getWidth() == 0 ? NO_SETTINGS_HOVER_ICON : SETTINGS_HOVER_ICON);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					colour.setIcon(line.getWidth() == 0 ? NO_SETTINGS_ICON : SETTINGS_ICON);
				}
			});

			JButton edge = new JButton();
			edge.setPreferredSize(new Dimension(50, 24));
			edge.setBorder(new EmptyBorder(0, 0, 0, 0));
			edge.setText(line.getEdge().toString());
			edge.setToolTipText("Line edge");
			edge.setFont(FontManager.getRunescapeSmallFont());
			edge.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					line.setEdge(line.getEdge().next());
					edge.setText(line.getEdge().toString());
					plugin.saveMarkers();
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					edge.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					edge.setBackground(ColorScheme.DARK_GRAY_COLOR);
				}
			});

			JSpinner width = new JSpinner(new SpinnerNumberModel(3.0, 0, 10, 0.1));
			width.setValue(line.getWidth());
			width.setToolTipText("Line width");
			width.setPreferredSize(new Dimension(53, 20));
			width.addChangeListener(ce ->
			{
				line.setWidth((double) width.getValue());
				plugin.saveMarkers();
			});

			List<LineGroup> groups = plugin.getGroups();
			String[] names = new String[groups.size()];
			for (int i = 0; i < groups.size(); i++)
			{
				names[i] = groups.get(i).getName();
			}

			JLabel groupButton = new JLabel();
			JLabel deleteLine = new JLabel();
			JLabel groupText = new JLabel("Group:");
			JComboBox<String> groupSelection = new JComboBox<>(names);

			groupText.setFont(FontManager.getRunescapeSmallFont());
			groupText.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
			groupText.setVisible(false);

			JLabel groupCancel = new JLabel("Cancel");
			groupCancel.setVisible(false);
			groupCancel.setFont(FontManager.getRunescapeSmallFont());
			groupCancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
			groupCancel.setBorder(new EmptyBorder(6, 0, 0, 1));
			groupCancel.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					groupText.setVisible(false);
					groupSelection.setVisible(false);
					groupCancel.setVisible(false);

					colour.setVisible(true);
					edge.setVisible(true);
					width.setVisible(true);
					groupButton.setVisible(true);
					deleteLine.setVisible(true);
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					groupCancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR.darker());
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					groupCancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
				}
			});

			groupSelection.setVisible(false);
			groupSelection.setRenderer(new ComboBoxListRenderer<>());
			groupSelection.setPreferredSize(new Dimension(115, 24));
			groupSelection.setSelectedIndex(groups.indexOf(marker));
			groupSelection.setToolTipText("Marker group");
			groupSelection.addActionListener(e ->
			{
				marker.getLines().remove(line);
				groups.get(groupSelection.getSelectedIndex()).getLines().add(line);
				if (marker.getLines().isEmpty())
				{
					groups.remove(marker);
				}

				groupText.setVisible(false);
				groupSelection.setVisible(false);
				groupCancel.setVisible(false);

				colour.setVisible(true);
				edge.setVisible(true);
				width.setVisible(true);
				groupButton.setVisible(true);
				deleteLine.setVisible(true);

				plugin.saveMarkers();
				plugin.rebuild();
			});

			groupButton.setIcon(SETTINGS_ICON);
			groupButton.setToolTipText("Edit group");
			groupButton.setBorder(new EmptyBorder(1, 0, 0, 0));
			groupButton.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					ActionListener actionListener = groupSelection.getActionListeners()[0];
					groupSelection.removeActionListener(actionListener);
					groupSelection.removeAllItems();
					for (LineGroup group : plugin.getGroups())
					{
						groupSelection.addItem(group.getName());
					}
					groupSelection.setSelectedIndex(plugin.getGroups().indexOf(marker));
					groupSelection.addActionListener(actionListener);

					colour.setVisible(false);
					edge.setVisible(false);
					width.setVisible(false);
					groupButton.setVisible(false);
					deleteLine.setVisible(false);

					groupText.setVisible(true);
					groupSelection.setVisible(true);
					groupCancel.setVisible(true);
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					groupButton.setIcon(SETTINGS_HOVER_ICON);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					groupButton.setIcon(SETTINGS_ICON);
				}
			});

			deleteLine.setIcon(DELETE_ICON);
			deleteLine.setToolTipText("Delete line segment");
			deleteLine.setBorder(new EmptyBorder(1, 0, 0, 0));
			deleteLine.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					int confirm = JOptionPane.showConfirmDialog(LineMarkerPanel.this,
						"Are you sure you want to permanently delete this line segment?",
						"Warning", JOptionPane.OK_CANCEL_OPTION);

					if (confirm == 0)
					{
						plugin.removeMarker(line);
					}
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					deleteLine.setIcon(DELETE_HOVER_ICON);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					deleteLine.setIcon(DELETE_ICON);
				}
			});

			leftActions.add(colour);
			leftActions.add(edge);
			leftActions.add(width);
			leftActions.add(groupText);
			leftActions.add(groupSelection);

			rightActions.add(groupButton);
			rightActions.add(groupCancel);
			rightActions.add(deleteLine);

			container.add(leftActions, BorderLayout.WEST);
			container.add(rightActions, BorderLayout.EAST);

			markerContainer.add(container);
		}

		add(markerContainer);

		updateCollapsed();
	}

	private void save()
	{
		String newName = nameInput.getText();

		if (newName.equals(marker.getName()))
		{
			cancel();
			return;
		}

		LineGroup newGroup = null;
		for (LineGroup group : plugin.getGroups())
		{
			if (newName.equals(group.getName()) && !group.equals(marker))
			{
				newGroup = group;
				break;
			}
		}

		if (newGroup != null)
		{
			int confirm = JOptionPane.showConfirmDialog(LineMarkerPanel.this,
				"Are you sure you want to combine this line marker with the other line marker with the same name?",
				"Warning", JOptionPane.OK_CANCEL_OPTION);

			if (confirm == 0)
			{
				for (Line line : marker.getLines())
				{
					newGroup.getLines().add(line);
				}
				plugin.removeMarker(marker);
			}
			else
			{
				return;
			}
		}
		else
		{
			marker.setName(newName);
			plugin.saveMarkers();
		}

		nameInput.setEditable(false);
		updateNameActions(false);
		requestFocusInWindow();
	}

	private void cancel()
	{
		nameInput.setEditable(false);
		nameInput.setText(marker.getName());
		updateNameActions(false);
		requestFocusInWindow();
	}

	private void updateNameActions(boolean saveAndCancel)
	{
		save.setVisible(saveAndCancel);
		cancel.setVisible(saveAndCancel);
		rename.setVisible(!saveAndCancel);
		expandToggle.setVisible(!saveAndCancel);
		visibilityMarker.setVisible(!saveAndCancel);
		deleteMarker.setVisible(!saveAndCancel);

		if (saveAndCancel)
		{
			nameInput.getTextField().requestFocusInWindow();
			nameInput.getTextField().selectAll();
		}
	}

	private void updateCollapsed()
	{
		final boolean open = !marker.isCollapsed();

		rename.setVisible(open);

		for (int i = 1; i < markerContainer.getComponentCount(); i++)
		{
			markerContainer.getComponent(i).setVisible(open);
		}

		expandToggle.setIcon(open ? COLLAPSE_ICON : EXPAND_ICON);
		expandToggle.setRolloverIcon(open ? COLLAPSE_HOVER_ICON : EXPAND_HOVER_ICON);
		expandToggle.setToolTipText((open ? "Collapse" : "Expand") + " marker");
	}

	private RuneliteColorPicker getColourPicker(Color colour)
	{
		RuneliteColorPicker colourPicker = plugin.getColourPickerManager().create(
			SwingUtilities.windowForComponent(this),
			colour,
			marker.getName() + " line segment colour",
			false);
		colourPicker.setLocation(getLocationOnScreen());
		colourPicker.setOnClose(c -> plugin.saveMarkers());
		return colourPicker;
	}
}
