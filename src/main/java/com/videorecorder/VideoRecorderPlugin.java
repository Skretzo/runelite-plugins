package com.videorecorder;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.videorecorder.video.avi.AVIWriter;
import com.videorecorder.video.Format;
import com.videorecorder.video.FormatKeys;
import com.videorecorder.video.math.Rational;
import static com.videorecorder.video.FormatKeys.EncodingKey;
import static com.videorecorder.video.FormatKeys.FrameRateKey;
import static com.videorecorder.video.FormatKeys.KeyFrameIntervalKey;
import static com.videorecorder.video.FormatKeys.MediaTypeKey;
import static com.videorecorder.video.VideoFormatKeys.CompressionLevelKey;
import static com.videorecorder.video.VideoFormatKeys.DepthKey;
import static com.videorecorder.video.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static com.videorecorder.video.VideoFormatKeys.HeightKey;
import static com.videorecorder.video.VideoFormatKeys.WidthKey;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneScapeProfileType;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Video Recorder",
	description = "Capture the in-game screen as a video",
	tags = {"video", "screencast",  "capture", "movie", "AVI"}
)
public class VideoRecorderPlugin extends Plugin
{
	private static final BufferedImage ICON = ImageUtil.loadImageResource(VideoRecorderPlugin.class, "/panel_icon.png");
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	private static final String VIDEO_EXTENSION = ".avi";
	protected static final String VIDEO_RECORDER = "Video Recorder";
	protected static final File VIDEO_DIR = new File(RuneLite.RUNELITE_DIR, "videos");

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private DrawManager drawManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ScheduledExecutorService imageExecutor;

	@Inject
	private VideoRecorderConfig config;

	private boolean running;
	private AVIWriter video;
	private NavigationButton navButton;
	private VideoRecorderPanel panel;
	private ScheduledThreadPoolExecutor timerExecutor;

	private final HotkeyListener hotkeyStartListener = new HotkeyListener(() -> config.hotkeyStart())
	{
		@Override
		public void hotkeyPressed()
		{
			if (!running)
			{
				panel.toggleVideo(true);
			}
			else if (config.hotkeyStart().equals(config.hotkeyStop()))
			{
				panel.toggleVideo(false);
			}
		}
	};

	private final HotkeyListener hotkeyStopListener = new HotkeyListener(() -> config.hotkeyStop())
	{
		@Override
		public void hotkeyPressed()
		{
			if (running)
			{
				panel.toggleVideo(false);
			}
		}
	};

	@Provides
	VideoRecorderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VideoRecorderConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		VIDEO_DIR.mkdirs();

		panel = injector.getInstance(VideoRecorderPanel.class);

		navButton = NavigationButton.builder()
			.tooltip(VIDEO_RECORDER)
			.icon(ICON)
			.panel(panel)
			.priority(4)
			.build();

		clientToolbar.addNavigation(navButton);
		keyManager.registerKeyListener(hotkeyStartListener);
		keyManager.registerKeyListener(hotkeyStopListener);
	}

	@Override
	protected void shutDown() throws Exception
	{
		if (running && video != null)
		{
			panel.toggleVideo(false);
		}
		clientToolbar.removeNavigation(navButton);
		keyManager.unregisterKeyListener(hotkeyStartListener);
		keyManager.unregisterKeyListener(hotkeyStopListener);
	}

	protected void toggleVideo(boolean start)
	{
		if (start && !running)
		{
			video = null;
			running = true;

			timerExecutor = new ScheduledThreadPoolExecutor(1);
			int delay = Math.max(1, 1000 / config.framerate());
			timerExecutor.scheduleAtFixedRate(() ->
			{
				try
				{
					if (running)
					{
						Consumer<Image> imageCallback = (img) -> imageExecutor.submit(() -> screenshot(img));
						drawManager.requestNextFrameListener(imageCallback);
					}
					else
					{
						timerExecutor.shutdown();
						if (video != null)
						{
							video.close();
						}
					}
				}
				catch (Throwable ex)
				{
					log.warn("Error while writing video", ex);
				}
			}, delay, delay, TimeUnit.MILLISECONDS);
		}
		if (!start && running)
		{
			running = false;
		}
	}

	private void screenshot(Image image)
	{
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		if (video != null)
		{
			Dimension videoSize = video.getVideoDimension(0);
			width = videoSize.width;
			height = videoSize.height;
		}
		BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics graphics = frame.createGraphics();
		graphics.drawImage(image, 0, 0, null);
		graphics.dispose();

		try
		{
			if (video == null)
			{
				video = new AVIWriter(createOutputFile());

				Format videoFormat = new Format(
					MediaTypeKey, FormatKeys.MediaType.VIDEO,
					FrameRateKey, new Rational(config.framerate(), 1),
					WidthKey, frame.getWidth(),
					HeightKey, frame.getHeight(),
					EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
					DepthKey, 24,
					KeyFrameIntervalKey, config.keyframeInterval(),
					CompressionLevelKey, config.compressionLevel()
				);

				video.addTrack(videoFormat);
				video.setPalette(0, frame.getColorModel());
			}

			video.write(0, frame, 1);
		}
		catch (IOException ex)
		{
			log.warn("Error while writing video frame", ex);
		}
	}

	private File createOutputFile()
	{
		File playerFolder;
		if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null)
		{
			String playerDir = client.getLocalPlayer().getName();
			RuneScapeProfileType profileType = RuneScapeProfileType.getCurrent(client);
			if (profileType != RuneScapeProfileType.STANDARD)
			{
				playerDir += "-" + Text.titleCase(profileType);
			}

			playerFolder = new File(VIDEO_DIR, playerDir);
		}
		else
		{
			playerFolder = VIDEO_DIR;
		}

		playerFolder.mkdirs();

		String fileName = TIME_FORMAT.format(new Date());

		File videoFile = new File(playerFolder, fileName + VIDEO_EXTENSION);

		int i = 1;
		while (videoFile.exists())
		{
			videoFile = new File(playerFolder, fileName + String.format("(%d)", i++) + VIDEO_EXTENSION);
		}

		return videoFile;
	}
}
