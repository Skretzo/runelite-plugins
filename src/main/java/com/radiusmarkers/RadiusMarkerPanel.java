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
	private final JPanel containerMax = new JPanel(new BorderLayout());
	private final JPanel containerAggression = new JPanel(new BorderLayout());
	private final JPanel containerRetreatInteraction = new JPanel(new BorderLayout());
	private final JPanel containerNpcId = new JPanel(new BorderLayout());
	private final JPanel containerAttack = new JPanel(new BorderLayout());
	private final JPanel containerHunt = new JPanel(new BorderLayout());
	private final JPanel containerInteraction = new JPanel(new BorderLayout());
	private final JLabel colourSpawn = new JLabel();
	private final JLabel colourWander = new JLabel();
	private final JLabel colourMax = new JLabel();
	private final JLabel colourAggression = new JLabel();
	private final JLabel colourRetreatInteraction = new JLabel();
	private final JLabel colourAttack = new JLabel();
	private final JLabel colourHunt = new JLabel();
	private final JLabel colourInteraction = new JLabel();
	private final JLabel visibilityMarker = new JLabel();
	private final JLabel visibilitySpawn = new JLabel();
	private final JLabel visibilityWander = new JLabel();
	private final JLabel visibilityMax = new JLabel();
	private final JLabel visibilityAggression = new JLabel();
	private final JLabel visibilityRetreatInteraction = new JLabel();
	private final JLabel visibilityAttack = new JLabel();
	private final JLabel visibilityHunt = new JLabel();
	private final JLabel visibilityInteraction = new JLabel();
	private final JLabel deleteLabel = new JLabel();
	private final JButton expandToggle;

	private final FlatTextField nameInput = new FlatTextField();
	private final JLabel save = new JLabel("Save");
	private final JLabel cancel = new JLabel("Cancel");
	private final JLabel rename = new JLabel("Rename");

	private final JSpinner spinnerX = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerY = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerRadiusWander = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerRadiusMax = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerNpcId = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerRadiusAttack = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerRadiusHunt = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerRadiusInteraction = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));

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
					plugin.saveMarkers();
				}
			}
		});

		final JPanel[] containers = new JPanel[]
		{
			containerSpawn, containerWander, containerMax, containerAggression, containerRetreatInteraction,
			containerNpcId, containerAttack, containerHunt, containerInteraction
		};
		for (JPanel container : containers)
		{
			container.setBorder(new EmptyBorder(5, 0, 5, 0));
			container.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		}
		containerNpcId.setBorder(new EmptyBorder(5, 26, 5, 26));

		JPanel leftActionsSpawn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		JPanel leftActionsWander = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		JPanel leftActionsMax = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		JPanel leftActionsAggression = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		JPanel leftActionsRetreatInteraction = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		JPanel leftActionsNpcId = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		JPanel leftActionsAttack = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		JPanel leftActionsHunt = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		JPanel leftActionsInteraction = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

		leftActionsSpawn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		leftActionsWander.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		leftActionsMax.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		leftActionsAggression.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		leftActionsRetreatInteraction.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		leftActionsNpcId.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		leftActionsAttack.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		leftActionsHunt.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		leftActionsInteraction.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		colourSpawn.setToolTipText("Edit spawn point colour");
		colourSpawn.setForeground(marker.getSpawnColour() == null ?
			config.defaultColourSpawn() : marker.getSpawnColour());
		colourSpawn.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				openColourPickerSpawn();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				colourSpawn.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_HOVER_ICON : BORDER_COLOR_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				colourSpawn.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
			}
		});

		colourWander.setToolTipText("Edit wander range colour");
		colourWander.setForeground(marker.getWanderColour() == null ?
			config.defaultColourWander() : marker.getWanderColour());
		colourWander.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				openColourPickerWander();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				colourWander.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_HOVER_ICON : BORDER_COLOR_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				colourWander.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
			}
		});

		colourMax.setToolTipText("Edit max range colour");
		colourMax.setForeground(marker.getMaxColour() == null ? config.defaultColourMax() : marker.getMaxColour());
		colourMax.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				openColourPickerMax();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				colourMax.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_HOVER_ICON : BORDER_COLOR_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				colourMax.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
			}
		});

		colourAggression.setToolTipText("Edit aggression range colour");
		colourAggression.setForeground(marker.getAggressionColour() == null ?
			config.defaultColourAggression() : marker.getAggressionColour());
		colourAggression.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				openColourPickerAggression();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				colourAggression.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_HOVER_ICON : BORDER_COLOR_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				colourAggression.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
			}
		});

		colourRetreatInteraction.setToolTipText("Edit retreat interaction range colour");
		colourRetreatInteraction.setForeground(marker.getRetreatInteractionColour() == null ?
			config.defaultColourRetreatInteraction() : marker.getRetreatInteractionColour());
		colourRetreatInteraction.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				openColourPickerRetreatInteraction();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				colourRetreatInteraction.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_HOVER_ICON : BORDER_COLOR_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				colourRetreatInteraction.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
			}
		});

		colourAttack.setToolTipText("Edit attack range colour");
		colourAttack.setForeground(marker.getAttackColour() == null ?
			config.defaultColourAttack() : marker.getAttackColour());
		colourAttack.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				openColourPickerAttack();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				colourAttack.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_HOVER_ICON : BORDER_COLOR_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				colourAttack.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
			}
		});

		colourHunt.setToolTipText("Edit hunt range colour");
		colourHunt.setForeground(marker.getHuntColour() == null ? config.defaultColourHunt() : marker.getHuntColour());
		colourHunt.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				openColourPickerHunt();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				colourHunt.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_HOVER_ICON : BORDER_COLOR_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				colourHunt.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
			}
		});

		colourInteraction.setToolTipText("Edit interaction range colour");
		colourInteraction.setForeground(marker.getInteractionColour() == null ?
			config.defaultColourInteraction() : marker.getInteractionColour());
		colourInteraction.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				openColourPickerInteraction();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				colourInteraction.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_HOVER_ICON : BORDER_COLOR_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				colourInteraction.setIcon(config.borderWidth() == 0 ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
			}
		});

		spinnerX.setValue(marker.getSpawnX());
		spinnerY.setValue(marker.getSpawnY());
		spinnerRadiusWander.setValue(marker.getWanderRadius());
		spinnerRadiusMax.setValue(marker.getMaxRadius());
		spinnerNpcId.setValue(marker.getNpcId());
		spinnerRadiusAttack.setValue(marker.getAttackRadius());
		spinnerRadiusHunt.setValue(marker.getHuntRadius());
		spinnerRadiusInteraction.setValue(marker.getInteractionRadius());

		final JSpinner[] spinners = new JSpinner[]
		{
			spinnerX, spinnerY, spinnerRadiusWander, spinnerRadiusMax, spinnerNpcId,
			spinnerRadiusAttack, spinnerRadiusHunt, spinnerRadiusInteraction
		};
		for (JSpinner spinner : spinners)
		{
			spinner.setPreferredSize(new Dimension(53, 20));
			spinner.addChangeListener(ce -> updateMarker());
		}
		spinnerX.setPreferredSize(new Dimension(70, 20));
		spinnerY.setPreferredSize(new Dimension(70, 20));
		spinnerRadiusWander.setPreferredSize(new Dimension(62, 20));
		spinnerRadiusMax.setPreferredSize(new Dimension(62, 20));
		spinnerNpcId.setPreferredSize(new Dimension(80, 20));

		JLabel labelWander = new JLabel("Wander range");
		JLabel labelMax = new JLabel("Max range");
		JLabel labelAggression = new JLabel("Aggression range");
		JLabel labelRetreatInteraction = new JLabel("Retreat interaction range");
		JLabel labelNpcId = new JLabel("NPC ID");
		JLabel labelHunt = new JLabel("Hunt range");
		JLabel labelInteraction = new JLabel("Interaction range");

		final JLabel[] labels = new JLabel[]
		{
			labelWander, labelMax, labelAggression, labelRetreatInteraction, labelNpcId, labelHunt, labelInteraction
		};
		for (JLabel label : labels)
		{
			label.setFont(FontManager.getRunescapeSmallFont());
			label.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
		}
		labelNpcId.setPreferredSize(new Dimension(58, 21));

		JComboBox<AttackType> selectionAttackType = new JComboBox<>(AttackType.values());
		selectionAttackType.setRenderer(new ComboBoxListRenderer<>());
		selectionAttackType.setPreferredSize(new Dimension(84, 20));
		selectionAttackType.setSelectedIndex(marker.getAttackType() == null ? 0 : marker.getAttackType().ordinal());
		selectionAttackType.setToolTipText("Attack range type");
		selectionAttackType.addActionListener(e ->
		{
			marker.setAttackType((AttackType) selectionAttackType.getSelectedItem());
			plugin.saveMarkers();
		});

		leftActionsSpawn.add(colourSpawn);
		leftActionsSpawn.add(spinnerX);

		leftActionsWander.add(colourWander);
		leftActionsWander.add(labelWander);

		leftActionsMax.add(colourMax);
		leftActionsMax.add(labelMax);

		leftActionsAggression.add(colourAggression);
		leftActionsAggression.add(labelAggression);

		leftActionsRetreatInteraction.add(colourRetreatInteraction);
		leftActionsRetreatInteraction.add(labelRetreatInteraction);

		leftActionsNpcId.add(labelNpcId);

		leftActionsAttack.add(colourAttack);
		leftActionsAttack.add(selectionAttackType);

		leftActionsHunt.add(colourHunt);
		leftActionsHunt.add(labelHunt);

		leftActionsInteraction.add(colourInteraction);
		leftActionsInteraction.add(labelInteraction);

		JPanel rightActionsSpawn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		JPanel rightActionsWander = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		JPanel rightActionsMax = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		JPanel rightActionsAggression = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		JPanel rightActionsRetreatInteraction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		JPanel rightActionsNpcId = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		JPanel rightActionsAttack = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		JPanel rightActionsHunt = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		JPanel rightActionsInteraction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

		rightActionsSpawn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		rightActionsWander.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		rightActionsMax.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		rightActionsAggression.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		rightActionsRetreatInteraction.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		rightActionsNpcId.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		rightActionsAttack.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		rightActionsHunt.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		rightActionsInteraction.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		expandToggle = new JButton(marker.isCollapsed() ? COLLAPSE_ICON : EXPAND_ICON);
		expandToggle.setRolloverIcon(marker.isCollapsed() ? COLLAPSE_HOVER_ICON : EXPAND_HOVER_ICON);
		expandToggle.setPreferredSize(new Dimension(15, 0));
		expandToggle.setBorder(new EmptyBorder(0, 6, 1, 0));
		expandToggle.setToolTipText((marker.isCollapsed() ? "Expand" : "Collapse") + " marker");
		SwingUtil.removeButtonDecorations(expandToggle);
		expandToggle.addActionListener(actionEvent ->
		{
			final boolean open = containerSpawn.isVisible();
			marker.setCollapsed(open);
			updateCollapsed();
			plugin.saveMarkers();
		});

		visibilityMarker.setToolTipText((marker.isVisible() ? "Hide" : "Show") + " marker");
		visibilityMarker.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setVisible(!marker.isVisible());
				updateVisibility();
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

		visibilitySpawn.setToolTipText((marker.isSpawnVisible() ? "Hide" : "Show") + " spawn point");
		visibilitySpawn.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setSpawnVisible(!marker.isSpawnVisible());
				updateVisibility();
				plugin.saveMarkers();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilitySpawn.setIcon(marker.isSpawnVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				updateVisibility();
			}
		});

		visibilityWander.setToolTipText((marker.isWanderVisible() ? "Hide" : "Show") + " wander range");
		visibilityWander.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setWanderVisible(!marker.isWanderVisible());
				updateVisibility();
				plugin.saveMarkers();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilityWander.setIcon(marker.isWanderVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				updateVisibility();
			}
		});

		visibilityMax.setToolTipText((marker.isMaxVisible() ? "Hide" : "Show") + " max range");
		visibilityMax.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setMaxVisible(!marker.isMaxVisible());
				updateVisibility();
				plugin.saveMarkers();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilityMax.setIcon(marker.isMaxVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				updateVisibility();
			}
		});

		visibilityAggression.setToolTipText((marker.isAggressionVisible() ? "Hide" : "Show") + " aggression range");
		visibilityAggression.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setAggressionVisible(!marker.isAggressionVisible());
				updateVisibility();
				plugin.saveMarkers();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilityAggression.setIcon(marker.isAggressionVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				updateVisibility();
			}
		});

		visibilityRetreatInteraction.setToolTipText((marker.isRetreatInteractionVisible() ? "Hide" : "Show") + " retreat interaction range");
		visibilityRetreatInteraction.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setRetreatInteractionVisible(!marker.isRetreatInteractionVisible());
				updateVisibility();
				plugin.saveMarkers();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilityRetreatInteraction.setIcon(marker.isRetreatInteractionVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				updateVisibility();
			}
		});

		visibilityAttack.setToolTipText((marker.isAttackVisible() ? "Hide" : "Show") + " attack range");
		visibilityAttack.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setAttackVisible(!marker.isAttackVisible());
				updateVisibility();
				plugin.saveMarkers();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilityAttack.setIcon(marker.isAttackVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				updateVisibility();
			}
		});

		visibilityHunt.setToolTipText((marker.isHuntVisible() ? "Hide" : "Show") + " hunt range");
		visibilityHunt.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setHuntVisible(!marker.isHuntVisible());
				updateVisibility();
				plugin.saveMarkers();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilityHunt.setIcon(marker.isHuntVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				updateVisibility();
			}
		});

		visibilityInteraction.setToolTipText((marker.isInteractionVisible() ? "Hide" : "Show") + " interaction range");
		visibilityInteraction.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				marker.setInteractionVisible(!marker.isInteractionVisible());
				updateVisibility();
				plugin.saveMarkers();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilityInteraction.setIcon(marker.isInteractionVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
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
		nameActions.add(visibilityMarker);
		nameActions.add(deleteLabel);

		nameWrapper.add(expandToggle, BorderLayout.WEST);
		nameWrapper.add(nameInput, BorderLayout.CENTER);
		nameWrapper.add(nameActions, BorderLayout.EAST);

		rightActionsSpawn.add(spinnerY);
		rightActionsSpawn.add(visibilitySpawn);

		rightActionsWander.add(spinnerRadiusWander);
		rightActionsWander.add(visibilityWander);

		rightActionsMax.add(spinnerRadiusMax);
		rightActionsMax.add(visibilityMax);

		rightActionsAggression.add(visibilityAggression);
		rightActionsRetreatInteraction.add(visibilityRetreatInteraction);

		rightActionsNpcId.add(spinnerNpcId);

		rightActionsAttack.add(spinnerRadiusAttack);
		rightActionsAttack.add(visibilityAttack);

		rightActionsHunt.add(spinnerRadiusHunt);
		rightActionsHunt.add(visibilityHunt);

		rightActionsInteraction.add(spinnerRadiusInteraction);
		rightActionsInteraction.add(visibilityInteraction);

		containerSpawn.add(leftActionsSpawn, BorderLayout.WEST);
		containerSpawn.add(rightActionsSpawn, BorderLayout.EAST);

		containerWander.add(leftActionsWander, BorderLayout.WEST);
		containerWander.add(rightActionsWander, BorderLayout.EAST);

		containerMax.add(leftActionsMax, BorderLayout.WEST);
		containerMax.add(rightActionsMax, BorderLayout.EAST);

		containerAggression.add(leftActionsAggression, BorderLayout.WEST);
		containerAggression.add(rightActionsAggression, BorderLayout.EAST);

		containerRetreatInteraction.add(leftActionsRetreatInteraction, BorderLayout.WEST);
		containerRetreatInteraction.add(rightActionsRetreatInteraction, BorderLayout.EAST);

		containerNpcId.add(leftActionsNpcId, BorderLayout.WEST);
		containerNpcId.add(rightActionsNpcId, BorderLayout.EAST);

		containerAttack.add(leftActionsAttack, BorderLayout.WEST);
		containerAttack.add(rightActionsAttack, BorderLayout.EAST);

		containerHunt.add(leftActionsHunt, BorderLayout.WEST);
		containerHunt.add(rightActionsHunt, BorderLayout.EAST);

		containerInteraction.add(leftActionsInteraction, BorderLayout.WEST);
		containerInteraction.add(rightActionsInteraction, BorderLayout.EAST);

		JPanel markerContainer = new JPanel();
		markerContainer.setLayout(new BoxLayout(markerContainer, BoxLayout.Y_AXIS));
		markerContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		markerContainer.add(nameWrapper);
		markerContainer.add(containerSpawn);
		if (config.includeWanderRange())
		{
			markerContainer.add(containerWander);
		}
		if (config.includeMaxRange())
		{
			markerContainer.add(containerMax);
		}
		if (config.includeAggressionRange())
		{
			markerContainer.add(containerAggression);
		}
		if (config.includeRetreatInteractionRange())
		{
			markerContainer.add(containerRetreatInteraction);
		}
		if (config.includeAttackRange() || config.includeHuntRange() || config.includeInteractionRange())
		{
			markerContainer.add(containerNpcId);
		}
		if (config.includeAttackRange())
		{
			markerContainer.add(containerAttack);
		}
		if (config.includeHuntRange())
		{
			markerContainer.add(containerHunt);
		}
		if (config.includeInteractionRange())
		{
			markerContainer.add(containerInteraction);
		}

		add(markerContainer);

		updateVisibility();
		updateColourIndicators();
		updateCollapsed();
	}

	public void setMarkerText(final String text)
	{
		nameInput.setText(text);
	}

	public void setNpcId(final int npcId)
	{
		spinnerNpcId.setValue(npcId);
	}

	private void save()
	{
		marker.setName(nameInput.getText());
		plugin.saveMarkers();

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
		marker.setSpawnX((int) spinnerX.getValue());
		marker.setSpawnY((int) spinnerY.getValue());

		marker.setWanderRadius((int) spinnerRadiusWander.getValue());
		marker.setMaxRadius((int) spinnerRadiusMax.getValue());

		marker.setNpcId((int) spinnerNpcId.getValue());

		marker.setAttackRadius((int) spinnerRadiusAttack.getValue());
		marker.setHuntRadius((int) spinnerRadiusHunt.getValue());
		marker.setInteractionRadius((int) spinnerRadiusInteraction.getValue());

		updateVisibility();

		plugin.saveMarkers();
	}

	private void updateVisibility()
	{
		visibilityMarker.setIcon(marker.isVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
		visibilitySpawn.setIcon(marker.isSpawnVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
		visibilityWander.setIcon(marker.isWanderVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
		visibilityMax.setIcon(marker.isMaxVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
		visibilityAggression.setIcon(marker.isAggressionVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
		visibilityRetreatInteraction.setIcon(marker.isRetreatInteractionVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
		visibilityAttack.setIcon(marker.isAttackVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
		visibilityHunt.setIcon(marker.isHuntVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
		visibilityInteraction.setIcon(marker.isInteractionVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
	}

	private void updateCollapsed()
	{
		final boolean open = !marker.isCollapsed();

		rename.setVisible(open);

		containerSpawn.setVisible(open);
		containerWander.setVisible(open);
		containerMax.setVisible(open);
		containerAggression.setVisible(open);
		containerRetreatInteraction.setVisible(open);
		containerNpcId.setVisible(open);
		containerAttack.setVisible(open);
		containerHunt.setVisible(open);
		containerInteraction.setVisible(open);

		expandToggle.setIcon(open ? COLLAPSE_ICON : EXPAND_ICON);
		expandToggle.setRolloverIcon(open ? COLLAPSE_HOVER_ICON : EXPAND_HOVER_ICON);
		expandToggle.setToolTipText((open ? "Collapse" : "Expand") + " marker");
	}

	private void updateColourIndicators()
	{
		final boolean borderless = config.borderWidth() == 0;

		colourSpawn.setBorder(borderless ? null : new MatteBorder(0, 0, 3, 0, marker.getSpawnColour()));
		colourWander.setBorder(borderless ? null : new MatteBorder(0, 0, 3, 0, marker.getWanderColour()));
		colourMax.setBorder(borderless ? null : new MatteBorder(0, 0, 3, 0, marker.getMaxColour()));
		colourAggression.setBorder(borderless ? null : new MatteBorder(0, 0, 3, 0, marker.getAggressionColour()));
		colourRetreatInteraction.setBorder(borderless ? null : new MatteBorder(0, 0, 3, 0, marker.getRetreatInteractionColour()));
		colourAttack.setBorder(borderless ? null : new MatteBorder(0, 0, 3, 0, marker.getAttackColour()));
		colourHunt.setBorder(borderless ? null : new MatteBorder(0, 0, 3, 0, marker.getHuntColour()));
		colourInteraction.setBorder(borderless ? null : new MatteBorder(0, 0, 3, 0, marker.getInteractionColour()));

		colourSpawn.setIcon(borderless ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
		colourWander.setIcon(borderless ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
		colourMax.setIcon(borderless ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
		colourAggression.setIcon(borderless ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
		colourRetreatInteraction.setIcon(borderless ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
		colourAttack.setIcon(borderless ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
		colourHunt.setIcon(borderless ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
		colourInteraction.setIcon(borderless ? NO_BORDER_COLOR_ICON : BORDER_COLOR_ICON);
	}

	private void openColourPickerSpawn()
	{
		Color color = marker.getSpawnColour() == null ? config.defaultColourSpawn() : marker.getSpawnColour();
		RuneliteColorPicker colourPicker = getColourPicker(color, " - Spawn point colour");
		colourPicker.setOnColorChange(c ->
		{
			marker.setSpawnColour(c);
			updateColourIndicators();
		});
		colourPicker.setVisible(true);
	}

	private void openColourPickerWander()
	{
		Color color = marker.getWanderColour() == null ? config.defaultColourWander() : marker.getWanderColour();
		RuneliteColorPicker colourPicker = getColourPicker(color, " - Wander range colour");
		colourPicker.setOnColorChange(c ->
		{
			marker.setWanderColour(c);
			updateColourIndicators();
		});
		colourPicker.setVisible(true);
	}

	private void openColourPickerMax()
	{
		Color color = marker.getMaxColour() == null ? config.defaultColourMax() : marker.getMaxColour();
		RuneliteColorPicker colourPicker = getColourPicker(color, " - Max range colour");
		colourPicker.setOnColorChange(c ->
		{
			marker.setMaxColour(c);
			updateColourIndicators();
		});
		colourPicker.setVisible(true);
	}

	private void openColourPickerAggression()
	{
		Color color = marker.getAggressionColour() == null ? config.defaultColourAggression() : marker.getAggressionColour();
		RuneliteColorPicker colourPicker = getColourPicker(color, " - Aggression range colour");
		colourPicker.setOnColorChange(c ->
		{
			marker.setAggressionColour(c);
			updateColourIndicators();
		});
		colourPicker.setVisible(true);
	}

	private void openColourPickerRetreatInteraction()
	{
		Color color = marker.getRetreatInteractionColour() == null ?
			config.defaultColourRetreatInteraction() : marker.getRetreatInteractionColour();
		RuneliteColorPicker colourPicker = getColourPicker(color, " - Retreat interaction range colour");
		colourPicker.setOnColorChange(c ->
		{
			marker.setRetreatInteractionColour(c);
			updateColourIndicators();
		});
		colourPicker.setVisible(true);
	}

	private void openColourPickerAttack()
	{
		Color color = marker.getAttackColour() == null ? config.defaultColourAttack() : marker.getAttackColour();
		RuneliteColorPicker colourPicker = getColourPicker(color, " - Attack range colour");
		colourPicker.setOnColorChange(c ->
		{
			marker.setAttackColour(c);
			updateColourIndicators();
		});
		colourPicker.setVisible(true);
	}

	private void openColourPickerHunt()
	{
		Color color = marker.getHuntColour() == null ? config.defaultColourHunt() : marker.getHuntColour();
		RuneliteColorPicker colourPicker = getColourPicker(color, " - Hunt range colour");
		colourPicker.setOnColorChange(c ->
		{
			marker.setHuntColour(c);
			updateColourIndicators();
		});
		colourPicker.setVisible(true);
	}

	private void openColourPickerInteraction()
	{
		Color color = marker.getInteractionColour() == null ? config.defaultColourInteraction() : marker.getInteractionColour();
		RuneliteColorPicker colourPicker = getColourPicker(color, " - Interaction range colour");
		colourPicker.setOnColorChange(c ->
		{
			marker.setInteractionColour(c);
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
		colourPicker.setLocationRelativeTo(this);
		colourPicker.setOnClose(c -> plugin.saveMarkers());
		return colourPicker;
	}
}
