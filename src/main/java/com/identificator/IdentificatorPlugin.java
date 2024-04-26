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
import net.runelite.api.Renderable;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
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

	StringBuilder hoverText = new StringBuilder();
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
	boolean showGroundObjectMorphId;
	boolean showGroundObjectAnimationId;
	boolean showDecorativeObjectId;
	boolean showDecorativeObjectMorphId;
	boolean showDecorativeObjectAnimationId;
	boolean showWallObjectId;
	boolean showWallObjectMorphId;
	boolean showWallObjectAnimationId;
	boolean showGroundItemId;
	boolean showInventoryItemId;
	boolean showNpcOverrideModelIds;
	boolean showNpcOverrideColours;
	boolean showNpcOverrideTextures;
	boolean showGameObjectAnimationId;
	boolean showNpcChatheadModelId;
	boolean showNpcChatheadAnimationId;
	Color colourHover;
	Color colourOverhead;
	Color colourMenu;
	Color colourChathead;
	Color colourInventory;

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
	private IdentificatorTextOverlay identificatorTextOverlay;

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
		overlayManager.add(identificatorTextOverlay);

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
		overlayManager.remove(identificatorTextOverlay);
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
		showGroundObjectMorphId = config.showGroundObjectMorphId();
		showGroundObjectAnimationId = config.showGroundObjectAnimationId();
		showDecorativeObjectId = config.showDecorativeObjectId();
		showDecorativeObjectMorphId = config.showDecorativeObjectMorphId();
		showDecorativeObjectAnimationId = config.showDecorativeObjectAnimationId();
		showWallObjectId = config.showWallObjectId();
		showWallObjectMorphId = config.showWallObjectMorphId();
		showWallObjectAnimationId = config.showWallObjectAnimationId();
		showGroundItemId = config.showGroundItemId();
		showInventoryItemId = config.showInventoryItemId();
		showNpcOverrideModelIds = config.showNpcOverrideModelIds();
		showNpcOverrideColours = config.showNpcOverrideColours();
		showNpcOverrideTextures = config.showNpcOverrideTextures();
		showGameObjectAnimationId = config.showGameObjectAnimationId();
		showNpcChatheadModelId = config.showNpcChatheadModelId();
		showNpcChatheadAnimationId = config.showNpcChatheadAnimationId();
		colourHover = config.colourHover();
		colourOverhead = config.colourOverhead();
		colourMenu = config.colourMenu();
		colourChathead = config.colourChathead();
		colourInventory = config.colourInventory();
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

	public ObjectComposition getMorphedTileObject(TileObject tileObject)
	{
		if (tileObject != null)
		{
			ObjectComposition objectComposition = client.getObjectDefinition(tileObject.getId());
			if (objectComposition != null && objectComposition.getImpostorIds() != null)
			{
				return objectComposition.getImpostor();
			}
		}
		return null;
	}

	public void wrapId(StringBuilder original, String prefix, int id)
	{
		wrapId(original, prefix, "" + id);
	}

	public void wrapId(StringBuilder original, String prefix, String text)
	{
		if (text.length() > 0)
		{
			original.append(original.length() > 0 ? " " : "").append("(").append(prefix).append(": ").append(text).append(")");
		}
	}

	public void appendId(StringBuilder text, int id)
	{
		appendId(text, "" + id);
	}

	public void appendId(StringBuilder original, String text)
	{
		if (text.length() > 0)
		{
			original.append(original.length() > 0 ? ", " : "").append(text);
		}
	}

	public void appendAnimation(StringBuilder text, Renderable renderable)
	{
		if (renderable instanceof DynamicObject)
		{
			Animation animation = ((DynamicObject) renderable).getAnimation();
			if (animation != null)
			{
				text.append(text.length() > 0 ? ", " : "").append(animation.getId());
			}
		}
	}

	public String gameObjectsToText(GameObject[] gameObjects)
	{
		StringBuilder text = new StringBuilder();

		if (gameObjects == null)
		{
			return text.toString();
		}

		for (GameObject gameObject : gameObjects)
		{
			if (isGameObject(gameObject))
			{
				appendId(text, gameObject.getId());
			}
		}

		return text.toString();
	}

	public String morphedGameObjectsToText(GameObject[] gameObjects)
	{
		StringBuilder text = new StringBuilder();

		if (gameObjects == null)
		{
			return text.toString();
		}

		for (GameObject gameObject : gameObjects)
		{
			if (isGameObject(gameObject))
			{
				ObjectComposition morphedGameObject = getMorphedGameObject(gameObject);
				if (morphedGameObject != null)
				{
					appendId(text, morphedGameObject.getId());
				}
			}
		}

		return text.toString();
	}

	public String gameObjectAnimationsToText(GameObject[] gameObjects)
	{
		StringBuilder text = new StringBuilder();

		if (gameObjects == null)
		{
			return text.toString();
		}

		for (GameObject gameObject : gameObjects)
		{
			if (isGameObject(gameObject))
			{
				appendAnimation(text, gameObject.getRenderable());
			}
		}

		return text.toString();
	}

	public String morphedTileObjectToText(TileObject tileObject)
	{
		ObjectComposition morphedTileObjectComposition = getMorphedTileObject(tileObject);
		if (morphedTileObjectComposition == null)
		{
			return "";
		}
		return "" + morphedTileObjectComposition.getId();
	}

	public String groundItemsToText(List<TileItem> tileItems)
	{
		StringBuilder text = new StringBuilder();

		if (tileItems == null)
		{
			return text.toString();
		}

		for (TileItem tileItem : tileItems)
		{
			if (tileItem != null)
			{
				appendId(text, tileItem.getId());
			}
		}

		return text.toString();
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (triggerWithShift && !client.isKeyPressed(KeyCode.KC_SHIFT))
		{
			return;
		}

		MenuEntry entry = event.getMenuEntry();
		final MenuAction menuAction = entry.getType();
		final NPC npc = entry.getNpc();
		final Player player = entry.getPlayer();
		final int itemOp = entry.getItemOp();
		final int itemId = entry.getItemId();

		if (!exclude(npc))
		{
			hoverText = new StringBuilder();

			if (showNpcId)
			{
				// Both npc.getId() and npc.getTransformedComposition.getId() returns the transformed NPC id.
				// However npc.getComposition.getId() returns the original non-transformed NPC id.
				NPCComposition npcComposition = npc.getComposition();
				wrapId(hoverText, "ID", (npcComposition != null ? npcComposition.getId() : npc.getId()));
			}
			if (showNpcMorphId)
			{
				// Both npc.getId() and npc.getTransformedComposition.getId() returns the transformed NPC id.
				// However npc.getComposition.getId() returns the original non-transformed NPC id.
				NPCComposition npcComposition = npc.getComposition();
				if (npcComposition != null && npcComposition.getId() != npc.getId())
				{
					wrapId(hoverText, "Morph ID", npc.getId());
				}
			}
			if (showNpcAnimationId)
			{
				wrapId(hoverText, "A", npc.getAnimation());
			}
			if (showNpcPoseAnimationId)
			{
				wrapId(hoverText, "P", npc.getPoseAnimation());
			}
			if (showNpcGraphicId)
			{
				wrapId(hoverText, "G", npc.getGraphic());
			}
			NpcOverrides modelOverrides = npc.getModelOverrides();
			if (modelOverrides != null)
			{
				if (showNpcOverrideModelIds && modelOverrides.getModelIds() != null)
				{
					wrapId(hoverText, "M", Arrays.toString(modelOverrides.getModelIds()));
				}
				if (showNpcOverrideColours && modelOverrides.getColorToReplaceWith() != null)
				{
					wrapId(hoverText, "C", Arrays.toString(modelOverrides.getColorToReplaceWith()));
				}
				if (showNpcOverrideTextures && modelOverrides.getTextureToReplaceWith() != null)
				{
					wrapId(hoverText, "T", Arrays.toString(modelOverrides.getTextureToReplaceWith()));
				}
			}
		}
		else if (player != null)
		{
			hoverText = new StringBuilder();

			if (showPlayerAnimationId)
			{
				wrapId(hoverText, "A", player.getAnimation());
			}
			if (showPlayerPoseAnimationId)
			{
				wrapId(hoverText, "P", player.getPoseAnimation());
			}
			if (showPlayerGraphicId)
			{
				wrapId(hoverText, "G", player.getGraphic());
			}
		}
		else if (OBJECT_MENU_TYPES.contains(menuAction) && client.getSelectedSceneTile() != null)
		{
			hoverText = new StringBuilder();

			Tile tile = client.getSelectedSceneTile();

			GameObject[] gameObjects = tile.getGameObjects();
			GroundObject groundObject = tile.getGroundObject();
			DecorativeObject decorativeObject = tile.getDecorativeObject();
			WallObject wallObject = tile.getWallObject();

			if (showGameObjectId)
			{
				wrapId(hoverText, "ID", gameObjectsToText(gameObjects));
			}
			if (showGameObjectMorphId)
			{
				wrapId(hoverText, "Morph ID", morphedGameObjectsToText(gameObjects));
			}
			if (showGameObjectAnimationId)
			{
				wrapId(hoverText, "A", gameObjectAnimationsToText(gameObjects));
			}
			if (showGroundObjectId && groundObject != null)
			{
				wrapId(hoverText, "ID", groundObject.getId());
			}
			if (showGroundObjectMorphId)
			{
				wrapId(hoverText, "Morph ID", morphedTileObjectToText(groundObject));
			}
			if (showGroundObjectAnimationId && groundObject != null)
			{
				StringBuilder text = new StringBuilder();
				appendAnimation(text, groundObject.getRenderable());
				wrapId(hoverText, "A", text.toString());
			}
			if (showDecorativeObjectId && decorativeObject != null)
			{
				wrapId(hoverText, "ID", decorativeObject.getId());
			}
			if (showDecorativeObjectMorphId)
			{
				wrapId(hoverText, "Morph ID", morphedTileObjectToText(decorativeObject));
			}
			if (showDecorativeObjectAnimationId && decorativeObject != null)
			{
				StringBuilder text = new StringBuilder();
				appendAnimation(text, decorativeObject.getRenderable());
				appendAnimation(text, decorativeObject.getRenderable2());
				wrapId(hoverText, "A", text.toString());
			}
			if (showWallObjectId && wallObject != null)
			{
				wrapId(hoverText, "ID", wallObject.getId());
			}
			if (showWallObjectMorphId)
			{
				wrapId(hoverText, "Morph ID", morphedTileObjectToText(wallObject));
			}
			if (showWallObjectAnimationId && wallObject != null)
			{
				StringBuilder text = new StringBuilder();
				appendAnimation(text, wallObject.getRenderable1());
				appendAnimation(text, wallObject.getRenderable2());
				wrapId(hoverText, "A", text.toString());
			}
		}
		else if (GROUND_ITEM_MENU_TYPES.contains(menuAction) && client.getSelectedSceneTile() != null)
		{
			hoverText = new StringBuilder();

			Tile tile = client.getSelectedSceneTile();
			List<TileItem> groundItems = tile.getGroundItems();

			if (showGroundItemId && groundItems != null)
			{
				wrapId(hoverText, "ID", event.getIdentifier());
			}
		}
		else if (itemOp > -1 && itemId > 0)
		{
			hoverText = new StringBuilder();

			if (showInventoryItemId)
			{
				wrapId(hoverText, "ID", itemId);
			}
		}
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		if (!showMenuInfo || (triggerWithShift && !client.isKeyPressed(KeyCode.KC_SHIFT)))
		{
			return;
		}

		MenuEntry[] menuEntries = client.getMenuEntries();
		for (int i = menuEntries.length - 1; i >= 0; i--)
		{
			if (!exclude(menuEntries[i].getNpc()) ||
				menuEntries[i].getPlayer() != null ||
				OBJECT_MENU_TYPES.contains(menuEntries[i].getType()) ||
				GROUND_ITEM_MENU_TYPES.contains(menuEntries[i].getType()) ||
				(menuEntries[i].getItemOp() > -1 && menuEntries[i].getItemId() > 0))
			{
				menuEntries[i].setTarget(menuEntries[i].getTarget() + ColorUtil.wrapWithColorTag(" " + hoverText, colourMenu));
			}
		}
		client.setMenuEntries(menuEntries);
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
