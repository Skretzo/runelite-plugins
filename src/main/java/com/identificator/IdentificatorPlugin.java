package com.identificator;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Identificator",
	description = "Show IDs for NPCs, objects, animations and more",
	tags = {"id", "identification"}
)
public class IdentificatorPlugin extends Plugin
{
	private static final String PANEL_DELIMITER = "\t";

	static final String CONFIG_GROUP = "identificator";
	static final int TILE_RADIUS = 20;
	static final List<MenuAction> OBJECT_MENU_TYPES = ImmutableList.of(
		MenuAction.EXAMINE_OBJECT,
		MenuAction.GAME_OBJECT_FIRST_OPTION,
		MenuAction.GAME_OBJECT_SECOND_OPTION,
		MenuAction.GAME_OBJECT_THIRD_OPTION,
		MenuAction.GAME_OBJECT_FOURTH_OPTION,
		MenuAction.GAME_OBJECT_FIFTH_OPTION
	);
	static final List<MenuAction> GROUND_ITEM_MENU_TYPES = ImmutableList.of(
		MenuAction.EXAMINE_ITEM_GROUND,
		MenuAction.GROUND_ITEM_FIRST_OPTION,
		MenuAction.GROUND_ITEM_SECOND_OPTION,
		MenuAction.GROUND_ITEM_THIRD_OPTION,
		MenuAction.GROUND_ITEM_FOURTH_OPTION,
		MenuAction.GROUND_ITEM_FIFTH_OPTION
	);

	String hoverText = null;
	boolean showHoverInfo;
	boolean showOverheadInfo;
	private boolean showMenuInfo;
	boolean triggerWithShift;
	boolean showNpcId;
	boolean showNpcAnimationId;
	boolean showNpcPoseAnimationId;
	boolean showNpcGraphicId;
	boolean showPlayerAnimationId;
	boolean showPlayerPoseAnimationId;
	boolean showPlayerGraphicId;
	boolean showGameObjectId;
	boolean showGroundObjectId;
	boolean showDecorativeObjectId;
	boolean showWallObjectId;
	boolean showGroundItemId;
	Color colourHover;
	Color colourOverhead;
	Color colourMenu;

	private IdentificatorPanel panel;
	private NavigationButton navigationButton;
	private int lastTickCount;
	private int loginTickCount;
	private int lastPlayerAnimationId;
	private int lastPlayerPoseAnimationId;
	private int lastPlayerGraphicId;

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private IdentificatorConfig config;

	@Inject
	private IdentificatorOverlay identificatorOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Provides
	IdentificatorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(IdentificatorConfig.class);
	}

	@Override
	protected void startUp()
	{
		loadOptions();
		overlayManager.add(identificatorOverlay);

		panel = new IdentificatorPanel();

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/panel_icon.png");

		navigationButton = NavigationButton.builder()
			.tooltip("Identificator")
			.icon(icon)
			.priority(100)
			.panel(panel)
			.build();

		if (config.logging())
		{
			clientToolbar.addNavigation(navigationButton);
		}
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(identificatorOverlay);
		clientToolbar.removeNavigation(navigationButton);
		panel = null;
		navigationButton = null;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!CONFIG_GROUP.equals(event.getGroup()))
		{
			return;
		}

		loadOptions();

		if ("logging".equals(event.getKey()))
		{
			if (config.logging())
			{
				clientToolbar.addNavigation(navigationButton);
			}
			else
			{
				clientToolbar.removeNavigation(navigationButton);
			}
		}
	}

	private void loadOptions()
	{
		showHoverInfo = config.showHoverInfo();
		showOverheadInfo = config.showOverheadInfo();
		showMenuInfo = config.showMenuInfo();
		triggerWithShift = config.triggerWithShift();
		showNpcId = config.showNpcId();
		showNpcAnimationId = config.showNpcAnimationId();
		showNpcPoseAnimationId = config.showNpcPoseAnimationId();
		showNpcGraphicId = config.showNpcGraphicId();
		showPlayerAnimationId = config.showPlayerAnimationId();
		showPlayerPoseAnimationId = config.showPlayerPoseAnimationId();
		showPlayerGraphicId = config.showPlayerGraphicId();
		showGameObjectId = config.showGameObjectId();
		showGroundObjectId = config.showGroundObjectId();
		showDecorativeObjectId = config.showDecorativeObjectId();
		showWallObjectId = config.showWallObjectId();
		showGroundItemId = config.showGroundItemId();
		colourHover = config.colourHover();
		colourOverhead = config.colourOverhead();
		colourMenu = config.colourMenu();
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		hoverText = null;

		if (triggerWithShift && !client.isKeyPressed(KeyCode.KC_SHIFT))
		{
			return;
		}

		MenuEntry entry = event.getMenuEntry();
		final MenuAction menuAction = entry.getType();
		final NPC npc = entry.getNpc();
		final Player player = entry.getPlayer();

		if (npc != null)
		{
			if (showNpcId)
			{
				String text = "(ID: " + npc.getId() + ")";
				hoverText = text;
				if (showMenuInfo)
				{
					entry.setTarget(entry.getTarget() + ColorUtil.wrapWithColorTag(" " + text, colourMenu));
				}
			}
			if (showNpcAnimationId)
			{
				String text = "(A: " + npc.getAnimation() + ")";
				if (hoverText == null)
				{
					hoverText = "";
				}
				hoverText += (hoverText.length() > 0 ? " " : "") + text;
				if (showMenuInfo)
				{
					entry.setTarget(entry.getTarget() + ColorUtil.wrapWithColorTag(" " + text, colourMenu));
				}
			}
			if (showNpcPoseAnimationId)
			{
				String text = "(P: " + npc.getPoseAnimation() + ")";
				if (hoverText == null)
				{
					hoverText = "";
				}
				hoverText += (hoverText.length() > 0 ? " " : "") + text;
				if (showMenuInfo)
				{
					entry.setTarget(entry.getTarget() + ColorUtil.wrapWithColorTag(" " + text, colourMenu));
				}
			}
			if (showNpcGraphicId)
			{
				String text = "(G: " + npc.getGraphic() + ")";
				if (hoverText == null)
				{
					hoverText = "";
				}
				hoverText += (hoverText.length() > 0 ? " " : "") + text;
				if (showMenuInfo)
				{
					entry.setTarget(entry.getTarget() + ColorUtil.wrapWithColorTag(" " + text, colourMenu));
				}
			}
		}
		else if (player != null)
		{
			if (showPlayerAnimationId)
			{
				String text = "(A: " + player.getAnimation() + ")";
				hoverText = text;
				if (showMenuInfo)
				{
					entry.setTarget(entry.getTarget() + ColorUtil.wrapWithColorTag(" " + text, colourMenu));
				}
			}
			if (showPlayerPoseAnimationId)
			{
				String text = "(P: " + player.getPoseAnimation() + ")";
				if (hoverText == null)
				{
					hoverText = "";
				}
				hoverText += (hoverText.length() > 0 ? " " : "") + text;
				if (showMenuInfo)
				{
					entry.setTarget(entry.getTarget() + ColorUtil.wrapWithColorTag(" " + text, colourMenu));
				}
			}
			if (showPlayerGraphicId)
			{
				String text = "(G: " + player.getGraphic() + ")";
				if (hoverText == null)
				{
					hoverText = "";
				}
				hoverText += (hoverText.length() > 0 ? " " : "") + text;
				if (showMenuInfo)
				{
					entry.setTarget(entry.getTarget() + ColorUtil.wrapWithColorTag(" " + text, colourMenu));
				}
			}
		}
		else if (OBJECT_MENU_TYPES.contains(menuAction))
		{
			if (showGameObjectId || showGroundObjectId || showDecorativeObjectId || showWallObjectId)
			{
				hoverText = "(ID: " + entry.getIdentifier() + ")";
				if (showMenuInfo)
				{
					entry.setTarget(entry.getTarget() + ColorUtil.wrapWithColorTag(" " + hoverText, colourMenu));
				}
			}
		}
		else if (GROUND_ITEM_MENU_TYPES.contains(menuAction))
		{
			if (showGroundItemId)
			{
				hoverText = "(ID: " + entry.getIdentifier() + ")";
				if (showMenuInfo)
				{
					entry.setTarget(entry.getTarget() + ColorUtil.wrapWithColorTag(" " + hoverText, colourMenu));
				}
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}

		int tickCount = client.getTickCount() - loginTickCount;
		int timestamp = config.logRelativeTickTimestamp() ? (tickCount - lastTickCount) : tickCount;

		int playerAnimationId = player.getAnimation();
		int playerPoseAnimationId = player.getPoseAnimation();
		int playerGraphicId = player.getGraphic();

		if (playerAnimationId != lastPlayerAnimationId)
		{
			if (config.logPlayerAnimationId())
			{
				panel.appendText(timestamp + PANEL_DELIMITER + playerAnimationId);
			}
			lastTickCount = tickCount;
			lastPlayerAnimationId = playerAnimationId;
		}

		if (playerPoseAnimationId != lastPlayerPoseAnimationId)
		{
			if (config.logPlayerPoseAnimationId())
			{
				panel.appendText(timestamp + PANEL_DELIMITER + playerPoseAnimationId);
			}
			lastTickCount = tickCount;
			lastPlayerPoseAnimationId = playerPoseAnimationId;
		}

		if (playerGraphicId != lastPlayerGraphicId)
		{
			if (config.logPlayerGraphicId())
			{
				panel.appendText(timestamp + PANEL_DELIMITER + playerGraphicId);
			}
			lastTickCount = tickCount;
			lastPlayerGraphicId = playerGraphicId;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (ChatMessageType.WELCOME.equals(event.getType()))
		{
			loginTickCount = client.getTickCount();
		}
	}
}
