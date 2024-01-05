package com.identificator;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import net.runelite.api.Animation;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.DynamicObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.NpcOverrides;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.WallObject;
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

	String hoverText = "";
	boolean showHoverInfo;
	boolean showOverheadInfo;
	private boolean showMenuInfo;
	boolean triggerWithShift;
	boolean showNpcId;
	boolean showNpcMorphId;
	boolean showNpcAnimationId;
	boolean showNpcPoseAnimationId;
	boolean showNpcGraphicId;
	boolean showPlayerAnimationId;
	boolean showPlayerPoseAnimationId;
	boolean showPlayerGraphicId;
	boolean showGameObjectId;
	boolean showGameObjectMorphId;
	boolean showGroundObjectId;
	boolean showDecorativeObjectId;
	boolean showWallObjectId;
	boolean showGroundItemId;
	boolean showNpcOverrideModelIds;
	boolean showNpcOverrideColours;
	boolean showNpcOverrideTextures;
	boolean showGameObjectAnimationId;
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
		showNpcMorphId = config.showNpcMorphId();
		showNpcAnimationId = config.showNpcAnimationId();
		showNpcPoseAnimationId = config.showNpcPoseAnimationId();
		showNpcGraphicId = config.showNpcGraphicId();
		showPlayerAnimationId = config.showPlayerAnimationId();
		showPlayerPoseAnimationId = config.showPlayerPoseAnimationId();
		showPlayerGraphicId = config.showPlayerGraphicId();
		showGameObjectId = config.showGameObjectId();
		showGameObjectMorphId = config.showGameObjectMorphId();
		showGroundObjectId = config.showGroundObjectId();
		showDecorativeObjectId = config.showDecorativeObjectId();
		showWallObjectId = config.showWallObjectId();
		showGroundItemId = config.showGroundItemId();
		showNpcOverrideModelIds = config.showNpcOverrideModelIds();
		showNpcOverrideColours = config.showNpcOverrideColours();
		showNpcOverrideTextures = config.showNpcOverrideTextures();
		showGameObjectAnimationId = config.showGameObjectAnimationId();
		colourHover = config.colourHover();
		colourOverhead = config.colourOverhead();
		colourMenu = config.colourMenu();
	}

	public boolean exclude(NPC npc)
	{
		return npc == null || npc.getName() == null || npc.getName().isEmpty() || "null".equals(npc.getName());
	}

	public boolean isGameObject(GameObject gameObject)
	{
		// 0 = Player
		// 1 = NPC
		// 2 = Object
		// 3 = Item
		return gameObject != null && (gameObject.getHash() >> 14 & 3) == 2;
	}

	public ObjectComposition getMorphedGameObject(GameObject gameObject)
	{
		if (isGameObject(gameObject))
		{
			ObjectComposition objectComposition = client.getObjectDefinition(gameObject.getId());
			if (objectComposition != null && objectComposition.getImpostorIds() != null)
			{
				return objectComposition.getImpostor();
			}
		}
		return null;
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		hoverText = "";

		if (triggerWithShift && !client.isKeyPressed(KeyCode.KC_SHIFT))
		{
			return;
		}

		MenuEntry entry = event.getMenuEntry();
		final MenuAction menuAction = entry.getType();
		final NPC npc = entry.getNpc();
		final Player player = entry.getPlayer();

		if (!exclude(npc))
		{
			if (showNpcId)
			{
				// Both npc.getId() and npc.getTransformedComposition.getId() returns the transformed NPC id.
				// However npc.getComposition.getId() returns the original non-transformed NPC id.
				NPCComposition npcComposition = npc.getComposition();
				hoverText += (hoverText.length() > 0 ? " " : "") + "(ID: " + (npcComposition != null ? npcComposition.getId() : npc.getId()) + ")";
			}
			if (showNpcMorphId)
			{
				// Both npc.getId() and npc.getTransformedComposition.getId() returns the transformed NPC id.
				// However npc.getComposition.getId() returns the original non-transformed NPC id.
				NPCComposition npcComposition = npc.getComposition();
				if (npcComposition != null && npcComposition.getId() != npc.getId())
				{
					hoverText += (hoverText.length() > 0 ? " " : "") + "(Morph ID: " + npc.getId() + ")";
				}
			}
			if (showNpcAnimationId)
			{
				hoverText += (hoverText.length() > 0 ? " " : "") + "(A: " + npc.getAnimation() + ")";
			}
			if (showNpcPoseAnimationId)
			{
				hoverText += (hoverText.length() > 0 ? " " : "") + "(P: " + npc.getPoseAnimation() + ")";
			}
			if (showNpcGraphicId)
			{
				hoverText += (hoverText.length() > 0 ? " " : "") + "(G: " + npc.getGraphic() + ")";
			}
			NpcOverrides modelOverrides = npc.getModelOverrides();
			if (modelOverrides != null)
			{
				if (showNpcOverrideModelIds && modelOverrides.getModelIds() != null)
				{
					hoverText += (hoverText.length() > 0 ? " " : "") + "(M: " + Arrays.toString(modelOverrides.getModelIds()) + ")";
				}
				if (showNpcOverrideColours && modelOverrides.getColorToReplaceWith() != null)
				{
					hoverText += (hoverText.length() > 0 ? " " : "") + "(C: " + Arrays.toString(modelOverrides.getColorToReplaceWith()) + ")";
				}
				if (showNpcOverrideTextures && modelOverrides.getTextureToReplaceWith() != null)
				{
					hoverText += (hoverText.length() > 0 ? " " : "") + "(T: " + Arrays.toString(modelOverrides.getTextureToReplaceWith()) + ")";
				}
			}
			if (showMenuInfo)
			{
				entry.setTarget(entry.getTarget() + ColorUtil.wrapWithColorTag(" " + hoverText, colourMenu));
			}
		}
		else if (player != null)
		{
			if (showPlayerAnimationId)
			{
				hoverText += (hoverText.length() > 0 ? " " : "") + "(A: " + player.getAnimation() + ")";
			}
			if (showPlayerPoseAnimationId)
			{
				hoverText += (hoverText.length() > 0 ? " " : "") + "(P: " + player.getPoseAnimation() + ")";
			}
			if (showPlayerGraphicId)
			{
				hoverText += (hoverText.length() > 0 ? " " : "") + "(G: " + player.getGraphic() + ")";
			}
			if (showMenuInfo)
			{
				entry.setTarget(entry.getTarget() + ColorUtil.wrapWithColorTag(" " + hoverText, colourMenu));
			}
		}
		else if (OBJECT_MENU_TYPES.contains(menuAction) && client.getSelectedSceneTile() != null)
		{
			Tile tile = client.getSelectedSceneTile();

			GameObject[] gameObjects = tile.getGameObjects();
			GroundObject groundObject = tile.getGroundObject();
			DecorativeObject decorativeObject = tile.getDecorativeObject();
			WallObject wallObject = tile.getWallObject();

			if (showGameObjectId && gameObjects != null)
			{
				String text = "";
				for (GameObject gameObject : gameObjects)
				{
					if (isGameObject(gameObject))
					{
						text += (text.length() > 0 ? ", " : "") + gameObject.getId();
					}
				}
				text = text.length() > 0 ? ("(ID: " + text + ")") : text;
				hoverText += (hoverText.length() > 0 ? " " : "") + text;
			}
			if (showGameObjectMorphId && gameObjects != null)
			{
				String text = "";
				for (GameObject gameObject : gameObjects)
				{
					ObjectComposition morphedGameObject = getMorphedGameObject(gameObject);
					if (morphedGameObject != null)
					{
						text += (text.length() > 0 ? ", " : "") + morphedGameObject.getId();
					}
				}
				text = text.length() > 0 ? ("(Morph ID: " + text + ")") : text;
				hoverText += (hoverText.length() > 0 ? " " : "") + text;
			}
			if (showGroundObjectId && groundObject != null)
			{
				hoverText += (hoverText.length() > 0 ? " " : "") + "(ID: " + groundObject.getId() + ")";
			}
			if (showDecorativeObjectId && decorativeObject != null)
			{
				hoverText += (hoverText.length() > 0 ? " " : "") + "(ID: " + decorativeObject.getId() + ")";
			}
			if (showWallObjectId && wallObject != null)
			{
				hoverText += (hoverText.length() > 0 ? " " : "") + "(ID: " + wallObject.getId() + ")";
			}
			if (showGameObjectAnimationId && gameObjects != null)
			{
				String text = "";
				for (GameObject gameObject : gameObjects)
				{
					if (isGameObject(gameObject) && gameObject.getRenderable() instanceof DynamicObject)
					{
						Animation animation = ((DynamicObject) gameObject.getRenderable()).getAnimation();
						if (animation != null)
						{
							text += (text.length() > 0 ? ", " : "") + animation.getId();
						}
					}
				}
				text = text.length() > 0 ? ("(A: " + text + ")") : text;
				hoverText += (hoverText.length() > 0 ? " " : "") + text;
			}
			if (showMenuInfo)
			{
				entry.setTarget(entry.getTarget() + ColorUtil.wrapWithColorTag(" " + hoverText, colourMenu));
			}
		}
		else if (GROUND_ITEM_MENU_TYPES.contains(menuAction) && client.getSelectedSceneTile() != null)
		{
			Tile tile = client.getSelectedSceneTile();
			List<TileItem> groundItems = tile.getGroundItems();

			if (showGroundItemId && groundItems != null)
			{
				String text = "(ID: " + event.getIdentifier() + ")";
				hoverText += (hoverText.length() > 0 ? " " : "") + text;
			}
			if (showMenuInfo)
			{
				entry.setTarget(entry.getTarget() + ColorUtil.wrapWithColorTag(" " + hoverText, colourMenu));
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
