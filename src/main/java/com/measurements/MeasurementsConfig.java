package com.measurements;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range

@ConfigGroup("measurements")
public interface MeasurementsConfig extends Config
{
	@Range(
		min = 1,
		max = 2
	)
	@ConfigItem(
		keyName = "diagonalLength",
		name = "Diagonal length",
		description = "The length of a diagonal. Manhattan/taxicab distance has length 2, while Chebyshev/chessboard distance has length 1"
	)
	default int diagonalLength()
	{
		return 1;
	}
}
