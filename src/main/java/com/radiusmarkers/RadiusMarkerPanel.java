package com.radiusmarkers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.ImageUtil;

class RadiusMarkerPanel extends JPanel
{
	private static final Border NAME_BOTTOM_BORDER = new CompoundBorder(
		BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
		BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR));

	private static final ImageIcon BORDER_COLOR_ICON;
	private static final ImageIcon BORDER_COLOR_HOVER_ICON;
	private static final ImageIcon NO_BORDER_COLOR_ICON;
	private static final ImageIcon NO_BORDER_COLOR_HOVER_ICON;

	private static final ImageIcon VISIBLE_ICON;
	private static final ImageIcon VISIBLE_HOVER_ICON;
	private static final ImageIcon INVISIBLE_ICON;
	private static final ImageIcon INVISIBLE_HOVER_ICON;

	private static final ImageIcon DELETE_ICON;
	private static final ImageIcon DELETE_HOVER_ICON;

	private final RadiusMarkerPlugin plugin;
	private final RadiusMarkerConfig config;
	private final ColourRadiusMarker marker;

	private final JLabel colourIndicatorSpawn = new JLabel();
	private final JLabel colourIndicatorWander = new JLabel();
	private final JLabel colourIndicatorRetreat = new JLabel();
	private final JLabel colourIndicatorAggro = new JLabel();
	private final JLabel visibilityLabel = new JLabel();
	private final JLabel visibilityLabelSpawn = new JLabel();
	private final JLabel visibilityLabelWander = new JLabel();
	private final JLabel visibilityLabelRetreat = new JLabel();
	private final JLabel visibilityLabelAggro = new JLabel();
	private final JLabel deleteLabel = new JLabel();

	private final FlatTextField nameInput = new FlatTextField();
	private final JLabel save = new JLabel("Save");
	private final JLabel cancel = new JLabel("Cancel");
	private final JLabel rename = new JLabel("Rename");

	private final JSpinner spinnerX = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerY = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerRadiusWander = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerRadiusRetreat = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerRadiusAggro = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));

	static
	{
		final BufferedImage borderImg = ImageUtil.loadImageResource(RadiusMarkerPlugin.class, "border_color_icon.png");
		final BufferedImage borderImgHover = ImageUtil.luminanceOffset(borderImg, -150);
		BORDER_COLOR_ICON = new ImageIcon(borderImg);
		BORDER_COLOR_HOVER_ICON = new ImageIcon(borderImgHover);

		NO_BORDER_COLOR_ICON = new ImageIcon(borderImgHover);
		NO_BORDER_COLOR_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(borderImgHover, -100));

		final BufferedImage visibleImg = ImageUtil.loadImageResource(RadiusMarkerPlugin.class, "visible_icon.png");
		VISIBLE_ICON = new ImageIcon(visibleImg);
		VISIBLE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(visibleImg, -100));

		final BufferedImage invisibleImg = ImageUtil.loadImageResource(RadiusMarkerPlugin.class, "invisible_icon.png");
		INVISIBLE_ICON = new ImageIcon(invisibleImg);
		INVISIBLE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(invisibleImg, -100));

		final BufferedImage deleteImg = ImageUtil.loadImageResource(RadiusMarkerPlugin.class, "delete_icon.png");
		DELETE_ICON = new ImageIcon(deleteImg);
		DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImg, -100));
	}

	RadiusMarkerPanel(RadiusMarkerPlugin plugin, RadiusMarkerConfig config, ColourRadiusMarker marker)
	{
		this.plugin = plugin;
		this.config = config;
		this.marker = marker;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel nameWrapper = new JPanel(new BorderLayout());
		nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameWrapper.setBorder(NAME_BOTTOM_BORDER);

		JPanel nameActions = new JPanel(new FlowLayout(FlowLayout.RIGHT,3, 3));
		nameActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		save.setVisible(false);
		save.setFont(FontManager.getRunescapeSmallFont());
		save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
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
		nameInput.getTextField().setBorder(new EmptyBorder(0, 8, 0, 0));
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
			public void mouseEntered(MouseEvent mouseEvent)
			{
				preview(true);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				preview(false);
			}
		});

		JPanel containerSpawn = new JPanel(new BorderLayout());
		containerSpawn.setBorder(new EmptyBorder(5, 0, 5, 0));
		containerSpawn.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel containerWander = new JPanel(new BorderLayout());
		containerWander.setBorder(new EmptyBorder(5, 0, 5, 0));
		containerWander.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel containerRetreat = new JPanel(new BorderLayout());
		containerRetreat.setBorder(new EmptyBorder(5, 0, 5, 0));
		containerRetreat.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel containerAggro = new JPanel(new BorderLayout());
		containerAggro.setBorder(new EmptyBorder(5, 0, 5, 0));
		containerAggro.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel leftActionsSpawn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		leftActionsSpawn.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel leftActionsWander = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		leftActionsWander.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel leftActionsRetreat = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		leftActionsRetreat.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel leftActionsAggro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		leftActionsAggro.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		colourIndicatorSpawn.setToolTipText("Edit spawn colour");
		colourIndicatorSpawn.setForeground(marker.getSpawnColour());
		colourIndicatorSpawn.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				openColourPickerSpawn();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				colourIndicatorSpawn.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_HOVER_ICON : BORDER_COLOR_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				colourIndicatorSpawn.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
			}
		});

		colourIndicatorWander.setToolTipText("Edit spawn colour");
		colourIndicatorWander.setForeground(marker.getWanderColour());
		colourIndicatorWander.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				openColourPickerWander();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				colourIndicatorWander.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_HOVER_ICON : BORDER_COLOR_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				colourIndicatorWander.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
			}
		});

		colourIndicatorRetreat.setToolTipText("Edit spawn colour");
		colourIndicatorRetreat.setForeground(marker.getRetreatColour());
		colourIndicatorRetreat.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				openColourPickerRetreat();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				colourIndicatorRetreat.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_HOVER_ICON : BORDER_COLOR_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				colourIndicatorRetreat.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
			}
		});

		colourIndicatorAggro.setToolTipText("Edit spawn colour");
		colourIndicatorAggro.setForeground(marker.getAggroColour());
		colourIndicatorAggro.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				openColourPickerAggro();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				colourIndicatorAggro.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_HOVER_ICON : BORDER_COLOR_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				colourIndicatorAggro.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
			}
		});

		spinnerX.setValue(marker.getWorldPoint().getX());
		spinnerX.setPreferredSize(new Dimension(70, 20));
		spinnerX.addChangeListener(ce -> updateMarker());

		spinnerY.setValue(marker.getWorldPoint().getY());
		spinnerY.setPreferredSize(new Dimension(70, 20));
		spinnerY.addChangeListener(ce -> updateMarker());

		spinnerRadiusWander.setValue(marker.getWanderRadius());
		spinnerRadiusWander.setPreferredSize(new Dimension(80, 20));
		spinnerRadiusWander.addChangeListener(ce -> updateMarker());

		spinnerRadiusRetreat.setValue(marker.getRetreatRadius());
		spinnerRadiusRetreat.setPreferredSize(new Dimension(80, 20));
		spinnerRadiusRetreat.addChangeListener(ce -> updateMarker());

		spinnerRadiusAggro.setValue(marker.getAggroRadius());
		spinnerRadiusAggro.setPreferredSize(new Dimension(80, 20));
		spinnerRadiusAggro.addChangeListener(ce -> updateMarker());

		leftActionsSpawn.add(colourIndicatorSpawn);
		leftActionsSpawn.add(spinnerX);
		leftActionsSpawn.add(spinnerY);

		leftActionsWander.add(colourIndicatorWander);
		leftActionsWander.add(spinnerRadiusWander);

		leftActionsRetreat.add(colourIndicatorRetreat);
		leftActionsRetreat.add(spinnerRadiusRetreat);

		leftActionsAggro.add(colourIndicatorAggro);
		leftActionsAggro.add(spinnerRadiusAggro);

		JPanel rightActionsSpawn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActionsSpawn.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel rightActionsWander = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActionsWander.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel rightActionsRetreat = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActionsRetreat.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel rightActionsAggro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActionsAggro.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		visibilityLabel.setToolTipText(marker.isVisible() ? "Hide marker" : "Show marker");
		visibilityLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setVisible(!marker.isVisible());
				updateVisibility();
				plugin.saveMarkers(marker.getWorldPoint().getRegionID());
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilityLabel.setIcon(marker.isVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				updateVisibility();
			}
		});

		deleteLabel.setIcon(DELETE_ICON);
		deleteLabel.setToolTipText("Delete marker");
		deleteLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				int confirm = JOptionPane.showConfirmDialog(RadiusMarkerPanel.this,
					"Are you sure you want to permanently delete this radius marker?",
					"Warning", JOptionPane.OK_CANCEL_OPTION);

				if (confirm == 0)
				{
					plugin.removeMarker(marker);
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				deleteLabel.setIcon(DELETE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				deleteLabel.setIcon(DELETE_ICON);
			}
		});

		visibilityLabelSpawn.setToolTipText(marker.isSpawnVisible() ? "Hide spawn" : "Show spawn");
		visibilityLabelSpawn.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setSpawnVisible(!marker.isSpawnVisible());
				updateVisibility();
				plugin.saveMarkers(marker.getWorldPoint().getRegionID());
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilityLabelSpawn.setIcon(marker.isSpawnVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				updateVisibility();
			}
		});

		visibilityLabelWander.setToolTipText(marker.isVisible() ? "Hide wander" : "Show wander");
		visibilityLabelWander.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setWanderVisible(!marker.isWanderVisible());
				updateVisibility();
				plugin.saveMarkers(marker.getWorldPoint().getRegionID());
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilityLabelWander.setIcon(marker.isWanderVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				updateVisibility();
			}
		});

		visibilityLabelRetreat.setToolTipText(marker.isVisible() ? "Hide retreat" : "Show retreat");
		visibilityLabelRetreat.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setRetreatVisible(!marker.isRetreatVisible());
				updateVisibility();
				plugin.saveMarkers(marker.getWorldPoint().getRegionID());
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilityLabelRetreat.setIcon(marker.isRetreatVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				updateVisibility();
			}
		});

		visibilityLabelAggro.setToolTipText(marker.isVisible() ? "Hide aggro" : "Show aggro");
		visibilityLabelAggro.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setAggroVisible(!marker.isAggroVisible());
				updateVisibility();
				plugin.saveMarkers(marker.getWorldPoint().getRegionID());
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilityLabelAggro.setIcon(marker.isAggroVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				updateVisibility();
			}
		});

		nameActions.add(rename);
		nameActions.add(cancel);
		nameActions.add(save);
		nameActions.add(visibilityLabel);
		nameActions.add(deleteLabel);

		nameWrapper.add(nameInput, BorderLayout.CENTER);
		nameWrapper.add(nameActions, BorderLayout.EAST);

		rightActionsSpawn.add(visibilityLabelSpawn);
		rightActionsWander.add(visibilityLabelWander);
		rightActionsRetreat.add(visibilityLabelRetreat);
		rightActionsAggro.add(visibilityLabelAggro);

		containerSpawn.add(leftActionsSpawn, BorderLayout.WEST);
		containerSpawn.add(rightActionsSpawn, BorderLayout.EAST);

		containerWander.add(leftActionsWander, BorderLayout.WEST);
		containerWander.add(rightActionsWander, BorderLayout.EAST);

		containerRetreat.add(leftActionsRetreat, BorderLayout.WEST);
		containerRetreat.add(rightActionsRetreat, BorderLayout.EAST);

		containerAggro.add(leftActionsAggro, BorderLayout.WEST);
		containerAggro.add(rightActionsAggro, BorderLayout.EAST);

		JPanel markerContainer = new JPanel(new GridLayout(5, 1));
		markerContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		markerContainer.add(nameWrapper);
		markerContainer.add(containerSpawn);
		markerContainer.add(containerWander);
		markerContainer.add(containerRetreat);
		markerContainer.add(containerAggro);

		add(markerContainer);

		updateVisibility();
		updateColourIndicators();
		updateColourIndicators();
	}

	private void preview(boolean on)
	{
		if (marker.isVisible())
		{
			return;
		}

		marker.setVisible(on);
		plugin.saveMarkers(marker.getWorldPoint().getRegionID());
	}

	private void save()
	{
		marker.setName(nameInput.getText());
		plugin.saveMarkers(marker.getWorldPoint().getRegionID());

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

		if (saveAndCancel)
		{
			nameInput.getTextField().requestFocusInWindow();
			nameInput.getTextField().selectAll();
		}
	}

	private void updateMarker()
	{
		final WorldPoint wp = new WorldPoint((Integer) spinnerX.getValue(), (Integer) spinnerY.getValue(), marker.getZ());

		marker.setWorldPoint(wp);

		marker.setWanderRadius((Integer) spinnerRadiusWander.getValue());
		marker.setRetreatRadius((Integer) spinnerRadiusRetreat.getValue());
		marker.setAggroRadius((Integer) spinnerRadiusAggro.getValue());

		marker.setSpawnColour(colourIndicatorSpawn.getForeground());
		marker.setWanderColour(colourIndicatorWander.getForeground());
		marker.setRetreatColour(colourIndicatorRetreat.getForeground());
		marker.setAggroColour(colourIndicatorAggro.getForeground());

		updateColourIndicators();
		updateVisibility();

		plugin.saveMarkers(marker.getWorldPoint().getRegionID());
	}

	private void updateVisibility()
	{
		visibilityLabel.setIcon(marker.isVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
		visibilityLabelSpawn.setIcon(marker.isSpawnVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
		visibilityLabelWander.setIcon(marker.isWanderVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
		visibilityLabelRetreat.setIcon(marker.isRetreatVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
		visibilityLabelAggro.setIcon(marker.isAggroVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
	}

	private void updateColourIndicators()
	{
		if (config.borderWidth() == 0)
		{
			colourIndicatorSpawn.setBorder(null);
			colourIndicatorWander.setBorder(null);
			colourIndicatorRetreat.setBorder(null);
			colourIndicatorAggro.setBorder(null);
		}
		else
		{
			colourIndicatorSpawn.setBorder(new MatteBorder(0, 0, 3, 0, marker.getSpawnColour()));
			colourIndicatorWander.setBorder(new MatteBorder(0, 0, 3, 0, marker.getWanderColour()));
			colourIndicatorRetreat.setBorder(new MatteBorder(0, 0, 3, 0, marker.getRetreatColour()));
			colourIndicatorAggro.setBorder(new MatteBorder(0, 0, 3, 0, marker.getAggroColour()));
		}

		colourIndicatorSpawn.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
		colourIndicatorWander.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
		colourIndicatorRetreat.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
		colourIndicatorAggro.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
	}

	private void openColourPickerSpawn()
	{
		RuneliteColorPicker colourPicker = getColourPicker(marker.getSpawnColour());
		colourPicker.setOnColorChange(c ->
		{
			marker.setSpawnColour(c);
			updateColourIndicators();
		});
		colourPicker.setVisible(true);
	}

	private void openColourPickerWander()
	{
		RuneliteColorPicker colourPicker = getColourPicker(marker.getWanderColour());
		colourPicker.setOnColorChange(c ->
		{
			marker.setWanderColour(c);
			updateColourIndicators();
		});
		colourPicker.setVisible(true);
	}

	private void openColourPickerRetreat()
	{
		RuneliteColorPicker colourPicker = getColourPicker(marker.getRetreatColour());
		colourPicker.setOnColorChange(c ->
		{
			marker.setRetreatColour(c);
			updateColourIndicators();
		});
		colourPicker.setVisible(true);
	}

	private void openColourPickerAggro()
	{
		RuneliteColorPicker colourPicker = getColourPicker(marker.getAggroColour());
		colourPicker.setOnColorChange(c ->
		{
			marker.setAggroColour(c);
			updateColourIndicators();
		});
		colourPicker.setVisible(true);
	}

	private RuneliteColorPicker getColourPicker(Color colour)
	{
		RuneliteColorPicker colourPicker = plugin.getColourPickerManager().create(
			SwingUtilities.windowForComponent(this),
			colour,
			marker.getName(),
			false);
		colourPicker.setLocation(getLocationOnScreen());
		colourPicker.setOnClose(c -> plugin.saveMarkers(marker.getWorldPoint().getRegionID()));
		return colourPicker;
	}
}
