package com.identificator;

import com.google.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
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

					StringBuilder text = new StringBuilder();

					if (plugin.showGameObjectId)
					{
						text.append(gameObjectsToText(tile.getGameObjects()));
					}
					if (plugin.showGroundObjectId)
					{
						text.append(objectToText(text, tile.getGroundObject()));
					}
					if (plugin.showDecorativeObjectId)
					{
						text.append(objectToText(text, tile.getDecorativeObject()));
					}
					if (plugin.showWallObjectId)
					{
						text.append(objectToText(text, tile.getWallObject()));
					}
					if (plugin.showGroundItemId)
					{
						String textGroundItems = groundItemsToText(tile.getGroundItems());
						text.append((text.length() > 0 && textGroundItems.length() > 0) ? ", " : "").append(textGroundItems);
					}

					final Point textLocation = Perspective.getCanvasTextLocation(client, graphics, tile.getLocalLocation(), text.toString(), 40);

					if (text.length() > 0 && textLocation != null)
					{
						OverlayUtil.renderTextLocation(graphics, textLocation, "(ID: " + text.toString() + ")", plugin.colourOverhead);
					}
				}
			}
		}

		if (plugin.showHoverInfo && isHoveringGameScene())
		{
			if (plugin.hoverText != null)
			{
				tooltipManager.add(new Tooltip(ColorUtil.wrapWithColorTag(plugin.hoverText, plugin.colourHover)));
			}
			else
			{
				Tile selectedTile = client.getSelectedSceneTile();
				if (selectedTile != null)
				{
					StringBuilder text = new StringBuilder();

					GameObject[] gameObjects = selectedTile.getGameObjects();
					GroundObject groundObject = selectedTile.getGroundObject();
					DecorativeObject decorativeObject = selectedTile.getDecorativeObject();
					WallObject wallObject = selectedTile.getWallObject();
					List<TileItem> tileItems = selectedTile.getGroundItems();

					if (gameObjects != null && plugin.showGameObjectId)
					{
						for (GameObject gameObject : gameObjects)
						{
							if (gameObject != null)
							{
								text.append(text.length() > 0 ? ", " : "").append(gameObject.getId());
							}
						}
					}
					if (groundObject != null && plugin.showGroundObjectId)
					{
						text.append(text.length() > 0 ? ", " : "").append(groundObject.getId());
					}
					if (decorativeObject != null && plugin.showDecorativeObjectId)
					{
						text.append(text.length() > 0 ? ", " : "").append(decorativeObject.getId());
					}
					if (wallObject != null && plugin.showWallObjectId)
					{
						text.append(text.length() > 0 ? ", " : "").append(wallObject.getId());
					}
					if (tileItems != null && plugin.showGroundItemId)
					{
						for (TileItem tileItem : tileItems)
						{
							if (tileItem != null)
							{
								text.append(text.length() > 0 ? ", " : "").append(tileItem.getId());
							}
						}
					}

					if (text.length() > 0)
					{
						tooltipManager.add(new Tooltip(ColorUtil.wrapWithColorTag("(ID: " + text + ")", plugin.colourHover)));
					}
				}
			}
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
		if (npc == null)
		{
			return;
		}

		String text = "";

		if (plugin.showNpcId)
		{
			text += "(ID: " + npc.getId() + ")";
		}

		if (plugin.showNpcAnimationId)
		{
			text += (text.length() == 0 ? "" : " ") + "(A: " + npc.getAnimation() + ")";
		}

		if (plugin.showNpcPoseAnimationId)
		{
			text += (text.length() == 0 ? "" : " ") + "(P: " + npc.getPoseAnimation() + ")";
		}

		if (plugin.showNpcGraphicId)
		{
			text += (text.length() == 0 ? "" : " ") + "(G: " + npc.getGraphic() + ")";
		}

		if (text.length() <= 0)
		{
			return;
		}

		final Point textLocation = npc.getCanvasTextLocation(graphics, text, npc.getLogicalHeight() + 40);

		if (textLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, textLocation, text, plugin.colourOverhead);
		}
	}

	private void renderPlayer(Graphics2D graphics, Player player)
	{
		if (player == null)
		{
			return;
		}

		String text = "";

		if (plugin.showPlayerAnimationId)
		{
			text += "(A: " + player.getAnimation() + ")";
		}

		if (plugin.showPlayerPoseAnimationId)
		{
			text += (text.length() == 0 ? "" : " ") + "(P: " + player.getPoseAnimation() + ")";
		}

		if (plugin.showPlayerGraphicId)
		{
			text += (text.length() == 0 ? "" : " ") + "(G: " + player.getGraphic() + ")";
		}

		if (text.length() <= 0)
		{
			return;
		}

		final Point textLocation = player.getCanvasTextLocation(graphics, text, player.getLogicalHeight() + 40);

		if (textLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, textLocation, text, plugin.colourOverhead);
		}
	}

	private String objectToText(StringBuilder text, TileObject tileObject)
	{
		if (tileObject == null)
		{
			return "";
		}
		return (text.length() > 0 ? ", " : "") + tileObject.getId();
	}

	private String gameObjectsToText(GameObject[] gameObjects)
	{
		StringBuilder text = new StringBuilder();

		if (!plugin.showGameObjectId || gameObjects == null)
		{
			return text.toString();
		}

		for (GameObject gameObject : gameObjects)
		{
			if (gameObject != null)
			{
				text.append(text.length() > 0 ? ", " : "").append(gameObject.getId());
			}
		}

		return text.toString();
	}

	private String groundItemsToText(List<TileItem> tileItems)
	{
		StringBuilder text = new StringBuilder();

		if (!plugin.showGroundItemId || tileItems == null)
		{
			return text.toString();
		}

		for (TileItem tileItem : tileItems)
		{
			if (tileItem != null)
			{
				text.append(text.length() > 0 ? ", " : "").append(tileItem.getId());
			}
		}

		return text.toString();
	}
}
