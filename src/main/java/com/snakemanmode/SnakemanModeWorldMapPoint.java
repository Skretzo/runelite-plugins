package com.snakemanmode;

import java.awt.image.BufferedImage;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;

public class SnakemanModeWorldMapPoint extends WorldMapPoint
{
	SnakemanModeWorldMapPoint(WorldPoint worldPoint, BufferedImage image)
	{
		super(worldPoint, image);

		this.setSnapToEdge(true);
		this.setJumpOnClick(true);
		this.setName("Snake fruit");
	}
}
