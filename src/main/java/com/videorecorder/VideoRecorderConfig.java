package com.videorecorder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;

@ConfigGroup("videorecorder")
public interface VideoRecorderConfig extends Config
{
	@Range(
		min = 1,
		max = 50
	)
	@ConfigItem(
		keyName = "framerate",
		name = "FPS",
		description = "The framerate (frames/second) for the video.",
		position = 0
	)
	default int framerate()
	{
		return 25;
	}

	@Range(
		min = 1
	)
	@ConfigItem(
		keyName = "keyframeInterval",
		name = "Keyframe interval",
		description = "The interval (number of frames) between keyframes (versus delta frames).<br>" +
			"Examples:<br>" +
			"- A lower interval (e.g. 1) gives a bigger output file, but faster seeking (good for editing)<br>" +
			"- A moderate interval (e.g. 80) gives a good trade-off between output file size and seeking (recommended)<br>" +
			"- A higher interval (e.g. 50 FPS * 60 seconds = 3000) gives a smaller output file, but slower seeking (good for archiving)",
		position = 1
	)
	default int keyframeInterval()
	{
		return 80;
	}

	@Range(
		min = 1,
		max = 9
	)
	@ConfigItem(
		keyName = "compressionLevel",
		name = "Compression level",
		description = "The amount of compression for each video frame.<br>" +
			"Examples:<br>" +
			"1: fast compression (bigger file size, faster seeking)<br>" +
			"6: default compression (recommended)<br>" +
			"9: high compression (smaller file size, slower seeking)",
		position = 2
	)
	default int compressionLevel()
	{
		return 6;
	}

	@ConfigItem(
		keyName = "hotkeyStart",
		name = "Start video hotkey",
		description = "The hotkey that will start the video recording.<br>Alternatively use the start button in the plugin panel.",
		position = 3
	)
	default Keybind hotkeyStart()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "hotkeyStop",
		name = "Stop video hotkey",
		description = "The hotkey that will stop the video recording.<br>Alternatively use the stop button in the plugin panel.",
		position = 4
	)
	default Keybind hotkeyStop()
	{
		return Keybind.NOT_SET;
	}
}
