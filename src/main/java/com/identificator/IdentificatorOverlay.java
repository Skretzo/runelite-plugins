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
import net.runelite.api.NPC;
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
			final int radius = IdentificatorPlugin.TILE_RADIUS;
			final WorldPoint location = client.getLocalPlayer().getWorldLocation();
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
					if (gameObjects != null && gameObjects.length > 0)
					{
						renderGameObject(graphics, gameObjects[0]);
					}

					renderGroundObject(graphics, tile.getGroundObject());

					renderDecorativeObject(graphics, tile.getDecorativeObject());

					renderWallObject(graphics, tile.getWallObject());

					List<TileItem> tileItems = tile.getGroundItems();
					if (tileItems != null && !tileItems.isEmpty())
					{
						renderGroundItem(graphics, tileItems.get(0), tile);
					}
				}
			}
		}

		if (plugin.showHoverInfo)
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
					String text = "";

					GameObject[] gameObjects = selectedTile.getGameObjects();
					GroundObject groundObject = selectedTile.getGroundObject();
					DecorativeObject decorativeObject = selectedTile.getDecorativeObject();
					WallObject wallObject = selectedTile.getWallObject();
					List<TileItem> tileItems = selectedTile.getGroundItems();

					if (gameObjects != null && gameObjects.length > 0 && gameObjects[0] != null)
					{
						text = "(ID: " + gameObjects[0].getId() + ")";
					}
					else if (groundObject != null)
					{
						text = "(ID: " + groundObject.getId() + ")";
					}
					else if (decorativeObject != null)
					{
						text = "(ID: " + decorativeObject.getId() + ")";
					}
					else if (wallObject != null)
					{
						text = "(ID: " + wallObject.getId() + ")";
					}
					else if (tileItems != null && !tileItems.isEmpty() && tileItems.get(0) != null)
					{
						text = "(ID: " + tileItems.get(0).getId() + ")";
					}

					if (text.length() > 0)
					{
						tooltipManager.add(new Tooltip(ColorUtil.wrapWithColorTag(text, plugin.colourHover)));
					}
				}
			}
		}

		return null;
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

	private void renderGameObject(Graphics2D graphics, GameObject object)
	{
		if (object == null)
		{
			return;
		}

		String text = "";

		if (plugin.showGameObjectId)
		{
			text += "(ID: " + object.getId() + ")";
		}

		if (text.length() <= 0)
		{
			return;
		}

		final Point textLocation = object.getCanvasTextLocation(graphics, text, 40);

		if (textLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, textLocation, text, plugin.colourOverhead);
		}
	}

	private void renderGroundObject(Graphics2D graphics, GroundObject object)
	{
		if (object == null)
		{
			return;
		}

		String text = "";

		if (plugin.showGroundObjectId)
		{
			text += "(ID: " + object.getId() + ")";
		}

		if (text.length() <= 0)
		{
			return;
		}

		final Point textLocation = object.getCanvasTextLocation(graphics, text, 40);

		if (textLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, textLocation, text, plugin.colourOverhead);
		}
	}

	private void renderDecorativeObject(Graphics2D graphics, DecorativeObject object)
	{
		if (object == null)
		{
			return;
		}

		String text = "";

		if (plugin.showDecorativeObjectId)
		{
			text += "(ID: " + object.getId() + ")";
		}

		if (text.length() <= 0)
		{
			return;
		}

		final Point textLocation = object.getCanvasTextLocation(graphics, text, 40);

		if (textLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, textLocation, text, plugin.colourOverhead);
		}
	}

	private void renderWallObject(Graphics2D graphics, WallObject object)
	{
		if (object == null)
		{
			return;
		}

		String text = "";

		if (plugin.showWallObjectId)
		{
			text += "(ID: " + object.getId() + ")";
		}

		if (text.length() <= 0)
		{
			return;
		}

		final Point textLocation = object.getCanvasTextLocation(graphics, text, 40);

		if (textLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, textLocation, text, plugin.colourOverhead);
		}
	}

	private void renderGroundItem(Graphics2D graphics, TileItem item, Tile tile)
	{
		if (item == null)
		{
			return;
		}

		String text = "";

		if (plugin.showGroundItemId)
		{
			text += "(ID: " + item.getId() + ")";
		}

		if (text.length() <= 0)
		{
			return;
		}

		final Point textLocation = Perspective.getCanvasTextLocation(client, graphics, tile.getLocalLocation(), text, 40);

		if (textLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, textLocation, text, plugin.colourOverhead);
		}
	}
}
