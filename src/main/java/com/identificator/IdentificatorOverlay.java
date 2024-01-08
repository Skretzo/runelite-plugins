package com.identificator;

import com.google.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.NpcOverrides;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.WallObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;

public class IdentificatorOverlay extends Overlay
{
	private final IdentificatorPlugin plugin;
	private final Client client;
	private final TooltipManager tooltipManager;

	@Inject
	IdentificatorOverlay(IdentificatorPlugin plugin, Client client, TooltipManager tooltipManager)
	{
		this.plugin = plugin;
		this.client = client;
		this.tooltipManager = tooltipManager;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.triggerWithShift && !client.isKeyPressed(KeyCode.KC_SHIFT))
		{
			return null;
		}

		if (plugin.showOverheadInfo)
		{
			for (NPC npc : client.getCachedNPCs())
			{
				renderNpc(graphics, npc);
			}

			for (Player player : client.getPlayers())
			{
				renderPlayer(graphics, player);
			}

			Tile[][] tiles = client.getScene().getTiles()[client.getPlane()];

			final WorldPoint location = client.getLocalPlayer().getWorldLocation();
			final int radius = IdentificatorPlugin.TILE_RADIUS;
			final int width = tiles.length;
			final int height = tiles[0].length;

			for (int i = 0; i < width; i++)
			{
				for (int j = 0; j < height; j++)
				{
					Tile tile = tiles[i][j];
					if (tile == null)
					{
						continue;
					}

					if (location.distanceTo(tile.getWorldLocation()) > radius)
					{
						continue;
					}

					GameObject[] gameObjects = tile.getGameObjects();
					GroundObject groundObject = tile.getGroundObject();
					DecorativeObject decorativeObject = tile.getDecorativeObject();
					WallObject wallObject = tile.getWallObject();
					List<TileItem> groundItems = tile.getGroundItems();

					StringBuilder ids = new StringBuilder();
					StringBuilder animations = new StringBuilder();

					if (plugin.showGameObjectId)
					{
						plugin.appendId(ids, plugin.gameObjectsToText(gameObjects));
					}
					if (plugin.showGameObjectMorphId)
					{
						plugin.appendId(ids, plugin.morphedGameObjectsToText(gameObjects));
					}
					if (plugin.showGameObjectAnimationId)
					{
						plugin.appendId(animations, plugin.gameObjectAnimationsToText(gameObjects));
					}
					if (plugin.showGroundObjectId && groundObject != null)
					{
						plugin.appendId(ids, groundObject.getId());
					}
					if (plugin.showGroundObjectMorphId)
					{
						plugin.appendId(ids, plugin.morphedTileObjectToText(groundObject));
					}
					if (plugin.showGroundObjectAnimationId && groundObject != null)
					{
						plugin.appendAnimation(animations, groundObject.getRenderable());
					}
					if (plugin.showDecorativeObjectId && decorativeObject != null)
					{
						plugin.appendId(ids, decorativeObject.getId());
					}
					if (plugin.showDecorativeObjectMorphId)
					{
						plugin.appendId(ids, plugin.morphedTileObjectToText(decorativeObject));
					}
					if (plugin.showDecorativeObjectAnimationId && decorativeObject != null)
					{
						plugin.appendAnimation(animations, decorativeObject.getRenderable());
						plugin.appendAnimation(animations, decorativeObject.getRenderable2());
					}
					if (plugin.showWallObjectId && wallObject != null)
					{
						plugin.appendId(ids, wallObject.getId());
					}
					if (plugin.showWallObjectMorphId)
					{
						plugin.appendId(ids, plugin.morphedTileObjectToText(wallObject));
					}
					if (plugin.showWallObjectAnimationId && wallObject != null)
					{
						plugin.appendAnimation(animations, wallObject.getRenderable1());
						plugin.appendAnimation(animations, wallObject.getRenderable2());
					}
					if (plugin.showGroundItemId)
					{
						plugin.appendId(ids, plugin.groundItemsToText(groundItems));
					}

					StringBuilder text = new StringBuilder();
					plugin.wrapId(text, "ID", ids.toString());
					plugin.wrapId(text, "A", animations.toString());

					if (text.length() <= 0)
					{
						continue;
					}

					final Point textLocation = Perspective.getCanvasTextLocation(client, graphics, tile.getLocalLocation(), text.toString(), 40);

					if (textLocation != null)
					{
						OverlayUtil.renderTextLocation(graphics, textLocation, text.toString(), plugin.colourOverhead);
					}
				}
			}
		}

		if (plugin.showHoverInfo && plugin.hoverText.length() > 0 && isHoveringGameScene())
		{
			tooltipManager.add(new Tooltip(ColorUtil.wrapWithColorTag(plugin.hoverText.toString(), plugin.colourHover)));
		}

		return null;
	}

	private boolean isHoveringGameScene()
	{
		MenuEntry[] menuEntries = client.getMenuEntries();
		for (int i = menuEntries.length - 1; i >= 0; i--)
		{
			if (MenuAction.WALK.equals(menuEntries[i].getType()))
			{
				return true;
			}
		}
		return false;
	}

	private void renderNpc(Graphics2D graphics, NPC npc)
	{
		if (plugin.exclude(npc))
		{
			return;
		}

		StringBuilder text = new StringBuilder();

		if (plugin.showNpcId)
		{
			// Both npc.getId() and npc.getTransformedComposition.getId() returns the transformed NPC id.
			// However npc.getComposition.getId() returns the original non-transformed NPC id.
			NPCComposition npcComposition = npc.getComposition();
			plugin.wrapId(text, "ID", (npcComposition != null ? npcComposition.getId() : npc.getId()));
		}

		if (plugin.showNpcMorphId)
		{
			// Both npc.getId() and npc.getTransformedComposition.getId() returns the transformed NPC id.
			// However npc.getComposition.getId() returns the original non-transformed NPC id.
			NPCComposition npcComposition = npc.getComposition();
			if (npcComposition != null && npcComposition.getId() != npc.getId())
			{
				plugin.wrapId(text, "Morph ID", npc.getId());
			}
		}

		if (plugin.showNpcAnimationId)
		{
			plugin.wrapId(text, "A", npc.getAnimation());
		}

		if (plugin.showNpcPoseAnimationId)
		{
			plugin.wrapId(text, "P", npc.getPoseAnimation());
		}

		if (plugin.showNpcGraphicId)
		{
			plugin.wrapId(text, "G", npc.getGraphic());
		}

		NpcOverrides modelOverrides = npc.getModelOverrides();
		if (modelOverrides != null)
		{
			if (plugin.showNpcOverrideModelIds && modelOverrides.getModelIds() != null)
			{
				plugin.wrapId(text, "M", Arrays.toString(modelOverrides.getModelIds()));
			}

			if (plugin.showNpcOverrideColours && modelOverrides.getColorToReplaceWith() != null)
			{
				plugin.wrapId(text, "C", Arrays.toString(modelOverrides.getColorToReplaceWith()));
			}

			if (plugin.showNpcOverrideTextures && modelOverrides.getTextureToReplaceWith() != null)
			{
				plugin.wrapId(text, "T", Arrays.toString(modelOverrides.getTextureToReplaceWith()));
			}
		}

		if (text.length() <= 0)
		{
			return;
		}

		final Point textLocation = npc.getCanvasTextLocation(graphics, text.toString(), npc.getLogicalHeight() + 40);

		if (textLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, textLocation, text.toString(), plugin.colourOverhead);
		}
	}

	private void renderPlayer(Graphics2D graphics, Player player)
	{
		if (player == null)
		{
			return;
		}

		StringBuilder text = new StringBuilder();

		if (plugin.showPlayerAnimationId)
		{
			plugin.wrapId(text, "A", player.getAnimation());
		}

		if (plugin.showPlayerPoseAnimationId)
		{
			plugin.wrapId(text, "P", player.getPoseAnimation());
		}

		if (plugin.showPlayerGraphicId)
		{
			plugin.wrapId(text, "G", player.getGraphic());
		}

		if (text.length() <= 0)
		{
			return;
		}

		final Point textLocation = player.getCanvasTextLocation(graphics, text.toString(), player.getLogicalHeight() + 40);

		if (textLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, textLocation, text.toString(), plugin.colourOverhead);
		}
	}
}
