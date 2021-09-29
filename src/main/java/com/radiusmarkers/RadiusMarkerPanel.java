package com.radiusmarkers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import net.runelite.client.util.SwingUtil;

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

	private static final ImageIcon COLLAPSE_ICON;
	private static final ImageIcon COLLAPSE_HOVER_ICON;
	private static final ImageIcon EXPAND_ICON;
	private static final ImageIcon EXPAND_HOVER_ICON;

	private final RadiusMarkerPlugin plugin;
	private final RadiusMarkerConfig config;
	private final ColourRadiusMarker marker;

	private final JPanel containerSpawn = new JPanel(new BorderLayout());
	private final JPanel containerWander = new JPanel(new BorderLayout());
	private final JPanel containerRetreat = new JPanel(new BorderLayout());
	private final JPanel containerMax = new JPanel(new BorderLayout());
	private final JLabel colourIndicatorSpawn = new JLabel();
	private final JLabel colourIndicatorWander = new JLabel();
	private final JLabel colourIndicatorRetreat = new JLabel();
	private final JLabel colourIndicatorMax = new JLabel();
	private final JLabel visibilityLabel = new JLabel();
	private final JLabel visibilityLabelSpawn = new JLabel();
	private final JLabel visibilityLabelWander = new JLabel();
	private final JLabel visibilityLabelRetreat = new JLabel();
	private final JLabel visibilityLabelMax = new JLabel();
	private final JLabel deleteLabel = new JLabel();
	private final JButton expandToggle;

	private final FlatTextField nameInput = new FlatTextField();
	private final JLabel save = new JLabel("Save");
	private final JLabel cancel = new JLabel("Cancel");
	private final JLabel rename = new JLabel("Rename");

	private final JSpinner spinnerX = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerY = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerRadiusWander = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerRadiusRetreat = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerRadiusMax = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));

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

		BufferedImage retractIcon = ImageUtil.loadImageResource(RadiusMarkerPlugin.class, "arrow_right.png");
		retractIcon = ImageUtil.luminanceOffset(retractIcon, -121);
		EXPAND_ICON = new ImageIcon(retractIcon);
		EXPAND_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(retractIcon, -100));
		final BufferedImage expandIcon = ImageUtil.rotateImage(retractIcon, Math.PI / 2);
		COLLAPSE_ICON = new ImageIcon(expandIcon);
		COLLAPSE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(expandIcon, -100));
	}

	RadiusMarkerPanel(RadiusMarkerPlugin plugin, RadiusMarkerConfig config, ColourRadiusMarker marker)
	{
		this.plugin = plugin;
		this.config = config;
		this.marker = marker;

		marker.setPanel(this);

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
					final boolean open = containerSpawn.isVisible();
					marker.setCollapsed(open);
					updateCollapsed();
					plugin.saveMarkers(marker.getWorldPoint().getRegionID());
				}
			}
		});

		containerSpawn.setBorder(new EmptyBorder(5, 0, 5, 0));
		containerSpawn.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		containerWander.setBorder(new EmptyBorder(5, 0, 5, 0));
		containerWander.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		containerRetreat.setBorder(new EmptyBorder(5, 0, 5, 0));
		containerRetreat.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		containerMax.setBorder(new EmptyBorder(5, 0, 5, 0));
		containerMax.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel leftActionsSpawn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		leftActionsSpawn.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel leftActionsWander = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		leftActionsWander.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel leftActionsRetreat = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		leftActionsRetreat.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel leftActionsMax = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		leftActionsMax.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		colourIndicatorSpawn.setToolTipText("Edit spawn point colour");
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

		colourIndicatorWander.setToolTipText("Edit wander range colour");
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

		colourIndicatorRetreat.setToolTipText("Edit retreat range colour");
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

		colourIndicatorMax.setToolTipText("Edit max range colour");
		colourIndicatorMax.setForeground(marker.getMaxColour());
		colourIndicatorMax.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				openColourPickerMax();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				colourIndicatorMax.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_HOVER_ICON : BORDER_COLOR_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				colourIndicatorMax.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
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

		spinnerRadiusMax.setValue(marker.getMaxRadius());
		spinnerRadiusMax.setPreferredSize(new Dimension(80, 20));
		spinnerRadiusMax.addChangeListener(ce -> updateMarker());

		leftActionsSpawn.add(colourIndicatorSpawn);
		leftActionsSpawn.add(spinnerX);
		leftActionsSpawn.add(spinnerY);

		leftActionsWander.add(colourIndicatorWander);
		leftActionsWander.add(spinnerRadiusWander);

		leftActionsRetreat.add(colourIndicatorRetreat);
		leftActionsRetreat.add(spinnerRadiusRetreat);

		leftActionsMax.add(colourIndicatorMax);
		leftActionsMax.add(spinnerRadiusMax);

		JPanel rightActionsSpawn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActionsSpawn.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel rightActionsWander = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActionsWander.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel rightActionsRetreat = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActionsRetreat.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel rightActionsMax = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActionsMax.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		expandToggle = new JButton(marker.isCollapsed() ? COLLAPSE_ICON : EXPAND_ICON);
		expandToggle.setRolloverIcon(marker.isCollapsed() ? COLLAPSE_HOVER_ICON : EXPAND_HOVER_ICON);
		expandToggle.setPreferredSize(new Dimension(15, 0));
		expandToggle.setBorder(new EmptyBorder(0, 6, 1, 0));
		expandToggle.setToolTipText(marker.isCollapsed() ? "Expand marker" : "Collapse marker");
		SwingUtil.removeButtonDecorations(expandToggle);
		expandToggle.addActionListener(actionEvent ->
		{
			final boolean open = containerSpawn.isVisible();
			marker.setCollapsed(open);
			updateCollapsed();
			plugin.saveMarkers(marker.getWorldPoint().getRegionID());
		});

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

		visibilityLabelSpawn.setToolTipText(marker.isSpawnVisible() ? "Hide spawn point" : "Show spawn point");
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

		visibilityLabelWander.setToolTipText(marker.isVisible() ? "Hide wander range" : "Show wander range");
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

		visibilityLabelRetreat.setToolTipText(marker.isVisible() ? "Hide retreat range" : "Show retreat range");
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

		visibilityLabelMax.setToolTipText(marker.isVisible() ? "Hide max range" : "Show max range");
		visibilityLabelMax.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setMaxVisible(!marker.isMaxVisible());
				updateVisibility();
				plugin.saveMarkers(marker.getWorldPoint().getRegionID());
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilityLabelMax.setIcon(marker.isMaxVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
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

		nameWrapper.add(expandToggle, BorderLayout.WEST);
		nameWrapper.add(nameInput, BorderLayout.CENTER);
		nameWrapper.add(nameActions, BorderLayout.EAST);

		rightActionsSpawn.add(visibilityLabelSpawn);
		rightActionsWander.add(visibilityLabelWander);
		rightActionsRetreat.add(visibilityLabelRetreat);
		rightActionsMax.add(visibilityLabelMax);

		containerSpawn.add(leftActionsSpawn, BorderLayout.WEST);
		containerSpawn.add(rightActionsSpawn, BorderLayout.EAST);

		containerWander.add(leftActionsWander, BorderLayout.WEST);
		containerWander.add(rightActionsWander, BorderLayout.EAST);

		containerRetreat.add(leftActionsRetreat, BorderLayout.WEST);
		containerRetreat.add(rightActionsRetreat, BorderLayout.EAST);

		containerMax.add(leftActionsMax, BorderLayout.WEST);
		containerMax.add(rightActionsMax, BorderLayout.EAST);

		JPanel markerContainer = new JPanel();
		markerContainer.setLayout(new BoxLayout(markerContainer, BoxLayout.Y_AXIS));
		markerContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		markerContainer.add(nameWrapper);
		markerContainer.add(containerSpawn);
		markerContainer.add(containerWander);
		markerContainer.add(containerRetreat);
		markerContainer.add(containerMax);

		add(markerContainer);

		updateVisibility();
		updateColourIndicators();
		updateCollapsed();
	}

	public void setMarkerText(final String text)
	{
		nameInput.setText(text);
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
		expandToggle.setVisible(!saveAndCancel);
		visibilityLabel.setVisible(!saveAndCancel);
		deleteLabel.setVisible(!saveAndCancel);

		if (saveAndCancel)
		{
			nameInput.getTextField().requestFocusInWindow();
			nameInput.getTextField().selectAll();
			plugin.setRenameMarker(marker);
		}
		else
		{
			plugin.setRenameMarker(null);
		}
	}

	private void updateMarker()
	{
		final WorldPoint wp = new WorldPoint((Integer) spinnerX.getValue(), (Integer) spinnerY.getValue(), marker.getZ());

		marker.setWorldPoint(wp);

		marker.setWanderRadius((Integer) spinnerRadiusWander.getValue());
		marker.setRetreatRadius((Integer) spinnerRadiusRetreat.getValue());
		marker.setMaxRadius((Integer) spinnerRadiusMax.getValue());

		marker.setSpawnColour(colourIndicatorSpawn.getForeground());
		marker.setWanderColour(colourIndicatorWander.getForeground());
		marker.setRetreatColour(colourIndicatorRetreat.getForeground());
		marker.setMaxColour(colourIndicatorMax.getForeground());

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
		visibilityLabelMax.setIcon(marker.isMaxVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
	}

	private void updateCollapsed()
	{
		final boolean open = !marker.isCollapsed();

		rename.setVisible(open);

		containerSpawn.setVisible(open);
		containerWander.setVisible(open);
		containerRetreat.setVisible(open);
		containerMax.setVisible(open);

		expandToggle.setIcon(open ? COLLAPSE_ICON : EXPAND_ICON);
		expandToggle.setRolloverIcon(open ? COLLAPSE_HOVER_ICON : EXPAND_HOVER_ICON);
		expandToggle.setToolTipText(open ?  "Collapse marker" : "Expand marker");
	}

	private void updateColourIndicators()
	{
		if (config.borderWidth() == 0)
		{
			colourIndicatorSpawn.setBorder(null);
			colourIndicatorWander.setBorder(null);
			colourIndicatorRetreat.setBorder(null);
			colourIndicatorMax.setBorder(null);
		}
		else
		{
			colourIndicatorSpawn.setBorder(new MatteBorder(0, 0, 3, 0, marker.getSpawnColour()));
			colourIndicatorWander.setBorder(new MatteBorder(0, 0, 3, 0, marker.getWanderColour()));
			colourIndicatorRetreat.setBorder(new MatteBorder(0, 0, 3, 0, marker.getRetreatColour()));
			colourIndicatorMax.setBorder(new MatteBorder(0, 0, 3, 0, marker.getMaxColour()));
		}

		colourIndicatorSpawn.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
		colourIndicatorWander.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
		colourIndicatorRetreat.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
		colourIndicatorMax.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
	}

	private void openColourPickerSpawn()
	{
		RuneliteColorPicker colourPicker = getColourPicker(marker.getSpawnColour(), " - Spawn point colour");
		colourPicker.setOnColorChange(c ->
		{
			marker.setSpawnColour(c);
			updateColourIndicators();
		});
		colourPicker.setVisible(true);
	}

	private void openColourPickerWander()
	{
		RuneliteColorPicker colourPicker = getColourPicker(marker.getWanderColour(), " - Wander range colour");
		colourPicker.setOnColorChange(c ->
		{
			marker.setWanderColour(c);
			updateColourIndicators();
		});
		colourPicker.setVisible(true);
	}

	private void openColourPickerRetreat()
	{
		RuneliteColorPicker colourPicker = getColourPicker(marker.getRetreatColour(), " - Retreat range colour");
		colourPicker.setOnColorChange(c ->
		{
			marker.setRetreatColour(c);
			updateColourIndicators();
		});
		colourPicker.setVisible(true);
	}

	private void openColourPickerMax()
	{
		RuneliteColorPicker colourPicker = getColourPicker(marker.getMaxColour(), " - Max range colour");
		colourPicker.setOnColorChange(c ->
		{
			marker.setMaxColour(c);
			updateColourIndicators();
		});
		colourPicker.setVisible(true);
	}

	private RuneliteColorPicker getColourPicker(Color colour, String text)
	{
		RuneliteColorPicker colourPicker = plugin.getColourPickerManager().create(
			SwingUtilities.windowForComponent(this),
			colour,
			marker.getName() + text,
			false);
		colourPicker.setLocation(getLocationOnScreen());
		colourPicker.setOnClose(c -> plugin.saveMarkers(marker.getWorldPoint().getRegionID()));
		return colourPicker;
	}
}
