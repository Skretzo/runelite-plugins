package com.videorecorder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;

@ConfigGroup("videorecorder")
public interface VideoRecorderConfig extends Config
{
	@ConfigSection(
		name = "Gameplay settings",
		description = "Gameplay related options for the video recording",
		position = 0
	)
	String sectionGameplay = "sectionGameplay";

	@ConfigItem(
		keyName = "stopOnLogout",
		name = "Stop on logout",
		description = "Whether to stop the video recording when your in-game character is logged out.",
		position = 1,
		section = sectionGameplay
	)
	default boolean stopOnLogout()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeLoginScreen",
		name = "Exclude login screen",
		description = "Whether to pause the video recording when the login screen is displayed.",
		position = 2,
		section = sectionGameplay
	)
	default boolean excludeLoginScreen()
	{
		return false;
	}

	@ConfigSection(
		name = "Video settings",
		description = "Technical video options",
		position = 3
	)
	String sectionVideo = "sectionVideo";

	@ConfigItem(
		keyName = "includeCursor",
		name = "Include cursor",
		description = "Whether to include the cursor in the video.<br>" +
			"To use a custom cursor place a file 'cursor.png' in the '.runelite' folder.",
		position = 4,
		section = sectionVideo
	)
	default boolean includeCursor()
	{
		return true;
	}

	@Range(
		min = 1,
		max = 50
	)
	@ConfigItem(
		keyName = "framerate",
		name = "FPS",
		description = "The framerate (frames/second) for the video.",
		position = 5,
		section = sectionVideo
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
		position = 6,
		section = sectionVideo
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
		position = 7,
		section = sectionVideo
	)
	default int compressionLevel()
	{
		return 6;
	}

	@ConfigItem(
		keyName = "hotkeyStart",
		name = "Start video hotkey",
		description = "The hotkey that will start the video recording.<br>Alternatively use the start button in the plugin panel.",
		position = 8,
		section = sectionVideo
	)
	default Keybind hotkeyStart()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "hotkeyStop",
		name = "Stop video hotkey",
		description = "The hotkey that will stop the video recording.<br>Alternatively use the stop button in the plugin panel.",
		position = 9,
		section = sectionVideo
	)
	default Keybind hotkeyStop()
	{
		return Keybind.NOT_SET;
	}
}
