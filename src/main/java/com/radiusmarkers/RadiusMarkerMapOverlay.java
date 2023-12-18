package com.radiusmarkers;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.RenderOverview;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.worldmap.WorldMapOverlay;

class RadiusMarkerMapOverlay extends Overlay
{
	private final Client client;
	private final RadiusMarkerConfig config;
	private final RadiusMarkerPlugin plugin;

	@Inject
	private WorldMapOverlay worldMapOverlay;

	@Inject
	RadiusMarkerMapOverlay(Client client, RadiusMarkerConfig config, RadiusMarkerPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.MANUAL);
		drawAfterLayer(ComponentID.WORLD_MAP_MAPVIEW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.showWorldMap() && client.getWidget(ComponentID.WORLD_MAP_MAPVIEW) != null)
		{
			drawWorldMap(graphics);
		}
		return null;
	}

	private void drawWorldMap(Graphics2D graphics)
	{
		final Widget worldMapView = client.getWidget(ComponentID.WORLD_MAP_MAPVIEW);
		if (worldMapView == null)
		{
			return;
		}
		final Rectangle bounds = worldMapView.getBounds();
		if (bounds == null)
		{
			return;
		}
		final Area mapClipArea = getWorldMapClipArea(bounds);

		final List<ColourRadiusMarker> markers = plugin.getMarkers();
		final List<NPC> npcs = client.getNpcs();

		for (final ColourRadiusMarker marker : markers)
		{
			if (!marker.isVisible())
			{
				continue;
			}

			final boolean excludeCorner = AttackType.MELEE.equals(marker.getAttackType());

			final WorldPoint worldPoint = marker.getWorldPoint();

			if (config.includeRetreatInteractionRange() && marker.isRetreatInteractionVisible())
			{
				drawSquare(graphics, worldPoint, marker.getRetreatInteractionColour(), mapClipArea,
					marker.getRetreatInteractionRadius(), 1, false);
			}

			if (config.includeAggressionRange() && marker.isAggressionVisible())
			{
				drawSquare(graphics, worldPoint, marker.getAggressionColour(), mapClipArea,
					marker.getAggressionRadius(), client.getNpcDefinition(marker.getNpcId()).getSize(), excludeCorner);
			}

			if (config.includeMaxRange() && marker.isMaxVisible())
			{
				drawSquare(graphics, worldPoint, marker.getMaxColour(), mapClipArea,
					marker.getMaxRadius(), 1, false);
			}

			if (config.includeWanderRange() && marker.isWanderVisible())
			{
				drawSquare(graphics, worldPoint, marker.getWanderColour(), mapClipArea,
					marker.getWanderRadius(), 1, false);
			}

			if (marker.isSpawnVisible())
			{
				drawSquare(graphics, worldPoint, marker.getSpawnColour(), mapClipArea, 0, 1, false);
			}

			for (NPC npc : npcs)
			{
				if (npc.getId() != marker.getNpcId() || plugin.exclude(npc))
				{
					continue;
				}

				final WorldPoint npcLocation = npc.getWorldLocation();
				final int size = npc.getComposition().getSize();

				if (config.includeInteractionRange() && marker.isInteractionVisible())
				{
					drawSquare(graphics, npcLocation, marker.getInteractionColour(), mapClipArea,
						marker.getInteractionRadius(), size, false);
				}

				if (config.includeHuntRange() && marker.isHuntVisible())
				{
					drawSquare(graphics, npcLocation, marker.getHuntColour(), mapClipArea,
						marker.getHuntRadius(), 1, false);
				}

				if (config.includeAttackRange() && marker.isAttackVisible())
				{
					drawSquare(graphics, npcLocation, marker.getAttackColour(), mapClipArea,
						marker.getAttackRadius(), size, excludeCorner);
				}
			}
		}
	}

	private void drawSquare(Graphics2D graphics, WorldPoint worldPoint, Color color, Area mapClipArea,
		int radius, int size, boolean excludeCorner)
	{
		final Point start = worldMapOverlay.mapWorldPointToGraphicsPoint(worldPoint.dx(-radius).dy(radius + size - 1));
		final Point end = worldMapOverlay.mapWorldPointToGraphicsPoint(worldPoint.dx(radius + size).dy(-(radius + 1)));

		if (start == null || end == null)
		{
			return;
		}

		RenderOverview renderOverview = client.getRenderOverview();
		float pixelsPerTile = renderOverview.getWorldMapZoom();
		final int tileSize = (int) pixelsPerTile;

		int x = start.getX();
		int y = start.getY();
		final int width = end.getX() - x - 1;
		final int height = end.getY() - y - 1;
		x = x - tileSize / 2;
		y = y - tileSize / 2 + 1;

		graphics.setColor(color);
		graphics.setClip(mapClipArea);

		Area square = new Area(new Rectangle(x, y, width, height));
		if (radius > 0 && excludeCorner)
		{
			Area corners = new Area(new Rectangle(x, y, tileSize, tileSize));
			corners.add(new Area(new Rectangle(x, y + height - tileSize, tileSize, tileSize)));
			corners.add(new Area(new Rectangle(x + width - tileSize, y, tileSize, tileSize)));
			corners.add(new Area(new Rectangle(x + width - tileSize, y + height - tileSize, tileSize, tileSize)));
			Rectangle r = new Rectangle();
			square.subtract(corners);
		}

		graphics.draw(square);
	}

	private Area getWorldMapClipArea(Rectangle baseRectangle)
	{
		final Widget overview = client.getWidget(ComponentID.WORLD_MAP_OVERVIEW_MAP);
		final Widget surfaceSelector = client.getWidget(ComponentID.WORLD_MAP_SURFACE_SELECTOR);

		Area clipArea = new Area(baseRectangle);

		if (overview != null && !overview.isHidden())
		{
			clipArea.subtract(new Area(overview.getBounds()));
		}
		if (surfaceSelector != null && !surfaceSelector.isHidden())
		{
			clipArea.subtract(new Area(surfaceSelector.getBounds()));
		}

		return clipArea;
	}
}
