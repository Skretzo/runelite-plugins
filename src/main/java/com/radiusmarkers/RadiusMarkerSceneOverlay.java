package com.radiusmarkers;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.util.Collection;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class RadiusMarkerSceneOverlay extends Overlay
{
	private static final int MAX_DRAW_DISTANCE = 32;
	private static final int LOCAL_TILE_SIZE = Perspective.LOCAL_TILE_SIZE;

	private final Client client;
	private final RadiusMarkerConfig config;
	private final RadiusMarkerPlugin plugin;

	private int x;
	private int y;

	@Inject
	private RadiusMarkerSceneOverlay(Client client, RadiusMarkerConfig config, RadiusMarkerPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(Overlay.PRIORITY_LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final Collection<ColourRadiusMarker> markers = plugin.getMarkers();
		final List<NPC> npcs = client.getNpcs();

		if (markers.isEmpty())
		{
			return null;
		}

		Stroke stroke = new BasicStroke((float) config.borderWidth());

		for (final ColourRadiusMarker marker : markers)
		{
			if (!marker.isVisible())
			{
				continue;
			}

			final boolean excludeCorner = AttackType.MELEE.equals(marker.getAttackType());

			final Collection<WorldPoint> worldPoints = plugin.getInstanceWorldPoints(marker.getWorldPoint());

			for (WorldPoint worldPoint : worldPoints)
			{
				if (worldPoint.getPlane() != client.getPlane())
				{
					continue;
				}

				if (config.includeRetreatInteractionRange() && marker.isRetreatInteractionVisible())
				{
					drawBox(graphics, worldPoint, marker.getRetreatInteractionRadius(),
						marker.getRetreatInteractionColour(), stroke, 1, false);
				}

				if (config.includeAggressionRange() && marker.isAggressionVisible())
				{
					drawBox(graphics, worldPoint, marker.getAggressionRadius(), marker.getAggressionColour(),
						stroke, client.getNpcDefinition(marker.getNpcId()).getSize(), excludeCorner);
				}

				if (config.includeMaxRange() && marker.isMaxVisible())
				{
					drawBox(graphics, worldPoint, marker.getMaxRadius(), marker.getMaxColour(), stroke, 1, false);
				}

				if (config.includeWanderRange() && marker.isWanderVisible())
				{
					drawBox(graphics, worldPoint, marker.getWanderRadius(), marker.getWanderColour(), stroke, 1, false);
				}

				if (marker.isSpawnVisible())
				{
					drawBox(graphics, worldPoint, 0, marker.getSpawnColour(), stroke, 1, false);
				}
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
					drawBox(graphics, npcLocation, marker.getInteractionRadius(),
						marker.getInteractionColour(), stroke, size, false);
				}

				if (config.includeHuntRange() && marker.isHuntVisible())
				{
					drawBox(graphics, npcLocation, marker.getHuntRadius(), marker.getHuntColour(), stroke, 1, false);
				}

				if (config.includeAttackRange() && marker.isAttackVisible())
				{
					drawBox(graphics, npcLocation, marker.getAttackRadius(), marker.getAttackColour(),
						stroke, size, excludeCorner);
				}
			}
		}

		return null;
	}

	private void drawBox(Graphics2D graphics, WorldPoint worldPoint, int radius,
		Color borderColour, Stroke borderStroke, int size, boolean excludeCorner)
	{
		graphics.setStroke(borderStroke);
		graphics.setColor(borderColour);
		graphics.draw(getSquare(worldPoint, radius, size, excludeCorner));
	}

	private GeneralPath getSquare(final WorldPoint worldPoint, final int radius, final int size, boolean excludeCorner)
	{
		GeneralPath path = new GeneralPath();

		if (client.getLocalPlayer() == null)
		{
			return path;
		}

		final int startX = worldPoint.getX() - radius;
		final int startY = worldPoint.getY() - radius;
		final int z = worldPoint.getPlane();

		final int diameter = 2 * radius + size;

		excludeCorner = excludeCorner && radius > 0;

		x = startX;
		y = startY;

		final WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

		final int[] xs = new int[4 * diameter + 1];
		final int[] ys = new int[xs.length];

		for (int i = 0; i < xs.length; i++)
		{
			if (i < diameter)
			{
				xs[0 * diameter + i] = startX + i;
				xs[1 * diameter + i] = startX + diameter;
				xs[2 * diameter + i] = startX + diameter - i;
				xs[3 * diameter + i] = startX;
				ys[0 * diameter + i] = startY;
				ys[1 * diameter + i] = startY + i;
				ys[2 * diameter + i] = startY + diameter;
				ys[3 * diameter + i] = startY + diameter - i;
			}
			else if (i == diameter)
			{
				xs[xs.length - 1] = xs[0];
				ys[ys.length - 1] = ys[0];
			}
			if (excludeCorner && i == 0)
			{
				xs[0 * diameter + i] += 1;
				xs[1 * diameter + i] -= 1;
				xs[2 * diameter + i] -= 1;
				xs[3 * diameter + i] += 1;
				ys[0 * diameter + i] += 1;
				ys[1 * diameter + i] += 1;
				ys[2 * diameter + i] -= 1;
				ys[3 * diameter + i] -= 1;
				x = xs[i];
				y = ys[i];
			}

			boolean hasFirst = false;
			if (playerLocation.distanceTo(new WorldPoint(x, y, z)) < MAX_DRAW_DISTANCE)
			{
				hasFirst = moveTo(path, x, y, z);
			}

			x = xs[i];
			y = ys[i];

			if (hasFirst && playerLocation.distanceTo(new WorldPoint(x, y, z)) < MAX_DRAW_DISTANCE)
			{
				lineTo(path, x, y, z);
			}
		}

		return path;
	}

	private boolean moveTo(GeneralPath path, final int x, final int y, final int z)
	{
		Point point = XYToPoint(x, y, z);
		if (point != null)
		{
			path.moveTo(point.getX(), point.getY());
			return true;
		}
		return false;
	}

	private void lineTo(GeneralPath path, final int x, final int y, final int z)
	{
		Point point = XYToPoint(x, y, z);
		if (point != null)
		{
			path.lineTo(point.getX(), point.getY());
		}
	}

	private Point XYToPoint(int x, int y, int z)
	{
		LocalPoint localPoint = LocalPoint.fromWorld(client, x, y);

		if (localPoint == null)
		{
			return null;
		}

		return Perspective.localToCanvas(
			client,
			new LocalPoint(localPoint.getX() - LOCAL_TILE_SIZE / 2, localPoint.getY() - LOCAL_TILE_SIZE / 2),
			z);
	}
}
