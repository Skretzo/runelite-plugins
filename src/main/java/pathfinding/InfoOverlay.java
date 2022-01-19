package pathfinding;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Tile;
import net.runelite.api.WallObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

public class InfoOverlay extends Overlay
{
	private final Client client;
	private final PathfindingConfig config;
	private final TooltipManager tooltipManager;

	@Inject
	public InfoOverlay(Client client, PathfindingConfig config, TooltipManager tooltipManager)
	{
		this.client = client;
		this.config = config;
		this.tooltipManager = tooltipManager;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showInfoOverlay())
		{
			return null;
		}

		Tile[][] tiles = client.getScene().getTiles()[client.getPlane()];

		for (Tile[] row : tiles)
		{
			for (Tile tile : row)
			{
				if (tile == null)
				{
					continue;
				}

				Polygon poly = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());
				if (poly == null || !poly.contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY()))
				{
					continue;
				}

				String info = "";
				if (tile.getWallObject() != null)
				{
					WallObject wall = tile.getWallObject();
					for (int o : new int[] {wall.getOrientationA(), wall.getOrientationB()})
					{
						if (o == 0) continue;
						info += "Orientation = " + o + " (";
						if (o == 1) info += "west";
						else if (o == 2) info += "north";
						else if (o == 4) info += "east";
						else if (o == 8) info += "south";
						else if (o == 16) info += "north-west";
						else if (o == 32) info += "north-east";
						else if (o == 64) info += "south-east";
						else if (o == 128) info += "south-west";
						info += ") ";
					}
				}

				OverlayUtil.renderPolygon(graphics, poly, new Color(0, 0, 0, 127));

				if (!info.isEmpty())
				{
					tooltipManager.add(new Tooltip(info));
				}

				return null;
			}
		}

		return null;
	}
}
