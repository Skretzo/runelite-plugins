package com.measurements;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("measurements")
public interface MeasurementsConfig extends Config
{
	@ConfigItem(
		keyName = "copyToClipboard",
		name = "Copy to clipboard",
		description = "Whether to copy the distance to the clipboard when measured"
	)
	default boolean copyToClipboard()
	{
		return false;
	}

	@Range(
		min = 1,
		max = 2
	)
	@ConfigItem(
		keyName = "diagonalLength",
		name = "Diagonal length",
		description = "The length of a diagonal in the distance measurements.<br>" +
			"Manhattan/taxicab distance has diagonal length 2,<br>" +
			"while Chebyshev/chessboard distance has diagonal length 1"
	)
	default int diagonalLength()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "outputFormat",
		name = "Output format",
		description = "The output format of distance measurements in the panel<br>Example:<br>{ID}#{index}\t{distance}"
	)
	default String outputFormat()
	{
		return "{ID}\t{distance}";
	}
}
