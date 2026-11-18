package com.tunnelvision;

import com.google.inject.Provides;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.IllegalComponentStateException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.beans.PropertyChangeListener;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ContainableFrame;
import net.runelite.client.util.HotkeyListener;

@PluginDescriptor(
	name = "Tunnel vision",
	description = "Crop the client to secretly multitask",
	tags = {"circle", "resize", "small", "hide"}
)
public class TunnelVisionPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private RuneLiteConfig runeLiteConfig;

	@Inject
	private TunnelVisionConfig config;

	@Inject
	private ConfigManager configManager;

	private static final int CURSOR_POLL_MS = 50;
	private final Timer cursorPollTimer = new Timer(CURSOR_POLL_MS, e -> pollCursor());
	private boolean hidden = false;
	private boolean fullWindow = false;

	@Inject
	private KeyManager keyManager;

	private final HotkeyListener fullWindowHotkeyListener = new HotkeyListener(() -> config.fullWindowHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			SwingUtilities.invokeLater(() ->
			{
				fullWindow = !fullWindow;
				if (frame != null)
				{
					applyShape();
				}
			});
		}
	};

	private int oldX;
	private int oldY;
	private int oldWidth;
	private int oldHeight;
	private int radius = 200;
	private int diagonal = Integer.MAX_VALUE;
	private ContainableFrame frame = null;
	private boolean isHotkeyPressed = false;

	private final AWTEventListener awtKeyListener = awtEvent ->
	{
		if (awtEvent instanceof KeyEvent)
		{
			KeyEvent keyEvent = (KeyEvent) awtEvent;

			if (keyEvent.getKeyCode() != KeyEvent.VK_ALT)
			{
				return;
			}

			if (keyEvent.getID() == KeyEvent.KEY_PRESSED)
			{
				isHotkeyPressed = true;
			}
			else if (keyEvent.getID() == KeyEvent.KEY_RELEASED)
			{
				isHotkeyPressed = false;
			}
		}
	};
	private final AWTEventListener awtMouseWheelListener = awtEvent ->
	{
		if (!isHotkeyPressed)
		{
			return;
		}
		if (awtEvent instanceof MouseWheelEvent)
		{
			MouseWheelEvent mouseWheelEvent = (MouseWheelEvent) awtEvent;

			int direction = mouseWheelEvent.getWheelRotation();
			int step = config.scrollStep();
			if (direction > 0)
			{
				radius = Math.min(radius + step, diagonal);
			}
			else
			{
				radius = Math.max(radius - step, 10);
			}

			mouseWheelEvent.consume();

			// Remember the new size so the settings slider matches and it survives restarts
			configManager.setConfiguration(TunnelVisionConfig.GROUP, TunnelVisionConfig.RADIUS_KEY, radius);

			update(mouseWheelEvent.getXOnScreen(), mouseWheelEvent.getYOnScreen());
		}
	};
	private final AWTEventListener awtMouseListener = awtEvent ->
	{
		if (awtEvent instanceof MouseEvent)
		{
			MouseEvent mouseEvent = (MouseEvent) awtEvent;
			// Don't drag the circle onto the title bar or sidebar while using them
			if (frame != null && isOverPinnedArea(mouseEvent.getXOnScreen() - frame.getX(), mouseEvent.getYOnScreen() - frame.getY()))
			{
				return;
			}
			update(mouseEvent.getXOnScreen(), mouseEvent.getYOnScreen());
		}
	};
	// --- Edge-drag resizing -------------------------------------------------------------
	// The plugin makes the window borderless (so it can be cut into a circle), which also
	// removes the normal resize edges. This re-implements dragging the window's edges.
	private static final int RESIZE_EDGE = 6;
	private int resizeEdges = 0; // bit flags: 1 = left, 2 = right, 4 = top, 8 = bottom
	private boolean resizing = false;
	private Point resizeStartMouse;
	private Rectangle resizeStartBounds;
	private Component cursorComponent;
	private Cursor savedCursor;

	private final AWTEventListener awtResizeListener = awtEvent ->
	{
		if (!(awtEvent instanceof MouseEvent) || frame == null || !frame.isResizable())
		{
			return;
		}
		MouseEvent e = (MouseEvent) awtEvent;
		if (!resizing && SwingUtilities.getWindowAncestor(e.getComponent()) != frame && e.getComponent() != frame)
		{
			return;
		}

		switch (e.getID())
		{
			case MouseEvent.MOUSE_MOVED:
				updateResizeCursor(e);
				break;
			case MouseEvent.MOUSE_PRESSED:
				if (SwingUtilities.isLeftMouseButton(e))
				{
					int edges = edgesAt(e.getXOnScreen() - frame.getX(), e.getYOnScreen() - frame.getY());
					if (edges != 0)
					{
						resizing = true;
						resizeEdges = edges;
						resizeStartMouse = e.getLocationOnScreen();
						resizeStartBounds = frame.getBounds();
						e.consume();
					}
				}
				break;
			case MouseEvent.MOUSE_DRAGGED:
				if (resizing)
				{
					doResize(e.getLocationOnScreen());
					e.consume();
				}
				break;
			case MouseEvent.MOUSE_RELEASED:
				if (resizing)
				{
					resizing = false;
					e.consume();
				}
				break;
			case MouseEvent.MOUSE_EXITED:
				if (!resizing)
				{
					restoreCursor();
				}
				break;
		}
	};

	private int edgesAt(int x, int y)
	{
		int edges = 0;
		if (x < RESIZE_EDGE)
		{
			edges |= 1;
		}
		else if (x >= frame.getWidth() - RESIZE_EDGE)
		{
			edges |= 2;
		}
		if (y < RESIZE_EDGE)
		{
			edges |= 4;
		}
		else if (y >= frame.getHeight() - RESIZE_EDGE)
		{
			edges |= 8;
		}
		return edges;
	}

	private void updateResizeCursor(MouseEvent e)
	{
		int edges = edgesAt(e.getXOnScreen() - frame.getX(), e.getYOnScreen() - frame.getY());
		if (edges == 0)
		{
			restoreCursor();
			return;
		}

		int type;
		switch (edges)
		{
			case 1: type = Cursor.W_RESIZE_CURSOR; break;
			case 2: type = Cursor.E_RESIZE_CURSOR; break;
			case 4: type = Cursor.N_RESIZE_CURSOR; break;
			case 8: type = Cursor.S_RESIZE_CURSOR; break;
			case 1 | 4: type = Cursor.NW_RESIZE_CURSOR; break;
			case 2 | 4: type = Cursor.NE_RESIZE_CURSOR; break;
			case 1 | 8: type = Cursor.SW_RESIZE_CURSOR; break;
			default: type = Cursor.SE_RESIZE_CURSOR; break;
		}

		Component c = e.getComponent();
		if (c != cursorComponent)
		{
			restoreCursor();
			cursorComponent = c;
			savedCursor = c.isCursorSet() ? c.getCursor() : null;
		}
		c.setCursor(Cursor.getPredefinedCursor(type));
	}

	private void restoreCursor()
	{
		if (cursorComponent != null)
		{
			cursorComponent.setCursor(savedCursor);
			cursorComponent = null;
			savedCursor = null;
		}
	}

	private void doResize(Point mouse)
	{
		int dx = mouse.x - resizeStartMouse.x;
		int dy = mouse.y - resizeStartMouse.y;
		Rectangle b = new Rectangle(resizeStartBounds);
		Dimension min = frame.getMinimumSize();

		if ((resizeEdges & 2) != 0)
		{
			b.width = Math.max(min.width, resizeStartBounds.width + dx);
		}
		if ((resizeEdges & 8) != 0)
		{
			b.height = Math.max(min.height, resizeStartBounds.height + dy);
		}
		if ((resizeEdges & 1) != 0)
		{
			b.width = Math.max(min.width, resizeStartBounds.width - dx);
			b.x = resizeStartBounds.x + resizeStartBounds.width - b.width;
		}
		if ((resizeEdges & 4) != 0)
		{
			b.height = Math.max(min.height, resizeStartBounds.height - dy);
			b.y = resizeStartBounds.y + resizeStartBounds.height - b.height;
		}
		frame.setBounds(b);
	}

	// Moving to another monitor (possibly with different Windows scaling) needs the shape re-applied
	private final PropertyChangeListener screenChangeListener = e -> SwingUtilities.invokeLater(this::reapplyShape);

	private void reapplyShape()
	{
		if (frame != null)
		{
			applyShape();
		}
	}

	private final ComponentAdapter clientResizeListener = new ComponentAdapter()
	{
		@Override
		public void componentMoved(ComponentEvent componentEvent)
		{
			reapplyShape();
		}

		@Override
		public void componentResized(ComponentEvent componentEvent)
		{
			int width = frame.getWidth();
			int height = frame.getHeight();
			diagonal = (int) Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2)) + 1;

			if (width < oldWidth || height < oldHeight)
			{
				update(oldX + frame.getX(), oldY + frame.getY());
			}

			oldWidth = width;
			oldHeight = height;
		}
	};

	@Provides
	TunnelVisionConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TunnelVisionConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		radius = config.radius();
		hidden = false;
		fullWindow = false;
		findFrame(client);
		cursorPollTimer.start();
		keyManager.registerKeyListener(fullWindowHotkeyListener);
		Toolkit.getDefaultToolkit().addAWTEventListener(awtResizeListener, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
		Toolkit.getDefaultToolkit().addAWTEventListener(awtKeyListener, AWTEvent.KEY_EVENT_MASK);
		Toolkit.getDefaultToolkit().addAWTEventListener(awtMouseWheelListener, AWTEvent.MOUSE_WHEEL_EVENT_MASK);
		Toolkit.getDefaultToolkit().addAWTEventListener(awtMouseListener, AWTEvent.MOUSE_MOTION_EVENT_MASK);
		frame.addComponentListener(clientResizeListener);
		frame.addPropertyChangeListener("graphicsConfiguration", screenChangeListener);

		if (!frame.isVisible())
		{
			try
			{
				frame.setUndecorated(true);
				frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
			}
			catch (IllegalComponentStateException e)
			{
				SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
					"Unable to start the plugin.\n"
					+ "The plugin was unable to remove the client window UI "
					+ "decoration, which is necessary to enable this plugin. "
					+ "Please restart the client to try again.",
					"Unable to start the plugin", JOptionPane.ERROR_MESSAGE));
			}
		}
		else if (!frame.isUndecorated())
		{
			SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
				"Please restart the client to properly start the plugin.\n"
				+ (!runeLiteConfig.enableCustomChrome() ? ("This plugin will "
				+ "re-enable the custom chrome taskbar while the plugin is active. "
				+ "Otherwise the client no longer has an exit button. The taskbar "
				+ "will be removed again once the plugin is deactivated.\n") : "")
				+ "While using the plugin you can hold down the ALT key and use "
				+ "the mouse scroll wheel to change the size of the cropped area.\n"
				+ "Resize the client by dragging its edges where the circle "
				+ "covers them, or press the full window hotkey (Ctrl+Shift+F "
				+ "by default) to show the whole window.",
				"Restart the client", JOptionPane.INFORMATION_MESSAGE));
		}
		else
		{
			update(oldX + frame.getX(), oldY + frame.getY());
		}
	}

	@Override
	protected void shutDown()
	{
		cursorPollTimer.stop();
		keyManager.unregisterKeyListener(fullWindowHotkeyListener);
		Toolkit.getDefaultToolkit().removeAWTEventListener(awtResizeListener);
		resizing = false;
		restoreCursor();
		hidden = false;
		fullWindow = false;
		Toolkit.getDefaultToolkit().removeAWTEventListener(awtMouseListener);
		Toolkit.getDefaultToolkit().removeAWTEventListener(awtMouseWheelListener);
		Toolkit.getDefaultToolkit().removeAWTEventListener(awtKeyListener);
		frame.removeComponentListener(clientResizeListener);
		frame.removePropertyChangeListener("graphicsConfiguration", screenChangeListener);

		try
		{
			setInvisible(false);
			frame.setShape(null);
			frame.setUndecorated(false);
		}
		catch (IllegalComponentStateException e)
		{
			if (!runeLiteConfig.enableCustomChrome())
			{
				SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
					"Please restart the client to restore the window UI "
					+ "decoration and remove the custom chrome taskbar.",
					"Restart the client", JOptionPane.INFORMATION_MESSAGE));
			}
		}
	}

	private void update(int x, int y)
	{
		int left = frame.getX();
		int top = frame.getY();
		int right = left + frame.getWidth();
		int bottom = top + frame.getHeight();

		if (x < left)
		{
			x = left;
		}
		else if (x > right)
		{
			x = right;
		}

		if (y < top)
		{
			y = top;
		}
		else if (y > bottom)
		{
			y = bottom;
		}

		oldX = x - left;
		oldY = y - top;

		applyShape();
	}

	private void applyShape()
	{
		try
		{
			boolean hide = hidden && !fullWindow;
			setInvisible(hide);

			if (fullWindow)
			{
				// No shape: the whole window, including its edges and title bar, is usable
				frame.setShape(null);
			}
			else if (hide)
			{
				// Windows treats an empty shape as "no shape" (full window), so shrink the
				// window to a single pixel instead; setInvisible() makes that pixel transparent.
				frame.setShape(new Rectangle(0, 0, 1, 1));
			}
			else
			{
				Area shape = new Area(new Ellipse2D.Double(
					oldX - radius,
					oldY - radius,
					2 * radius,
					2 * radius));
				for (Rectangle pinned : getPinnedAreas())
				{
					shape.add(new Area(pinned));
				}
				frame.setShape(shape);
			}
		}
		catch (IllegalComponentStateException ignore)
		{
		}
	}

	// Opacity the window had before we hid it (RuneLite has its own window opacity setting)
	private Float opacityBeforeHide = null;

	private void setInvisible(boolean invisible)
	{
		try
		{
			if (invisible && opacityBeforeHide == null)
			{
				opacityBeforeHide = frame.getOpacity();
				frame.setOpacity(0f);
			}
			else if (!invisible && opacityBeforeHide != null)
			{
				frame.setOpacity(opacityBeforeHide);
				opacityBeforeHide = null;
			}
		}
		catch (IllegalComponentStateException | UnsupportedOperationException ignore)
		{
		}
	}

	/**
	 * The window only receives mouse events while the cursor is inside the circle,
	 * so poll the global cursor position to know when it leaves and comes back.
	 */
	private void pollCursor()
	{
		if (frame == null)
		{
			return;
		}

		if (!config.hideWhenCursorAway() || fullWindow || resizing)
		{
			if (hidden)
			{
				hidden = false;
				applyShape();
			}
			return;
		}

		PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		if (pointerInfo == null)
		{
			return;
		}
		Point cursor = pointerInfo.getLocation();

		// Hide whenever the cursor is outside the client's area on screen
		boolean shouldHide = !frame.getBounds().contains(cursor);
		if (shouldHide)
		{
			if (!hidden)
			{
				hidden = true;
				applyShape();
			}
			lastPolledCursor = null;
			return;
		}

		// Cursor is somewhere over the client's area: show the circle under it.
		// The window only gets mouse events inside the circle, so follow the cursor from here.
		boolean wasHidden = hidden;
		hidden = false;
		if (wasHidden || !cursor.equals(lastPolledCursor))
		{
			lastPolledCursor = cursor;
			if (isOverPinnedArea(cursor.x - frame.getX(), cursor.y - frame.getY()))
			{
				applyShape();
			}
			else
			{
				update(cursor.x, cursor.y);
			}
		}
	}

	private Point lastPolledCursor;

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!TunnelVisionConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}

		SwingUtilities.invokeLater(() ->
		{
			radius = Math.min(config.radius(), diagonal);
			if (frame != null)
			{
				applyShape();
			}
		});
	}

	/**
	 * Parts of the window that stay visible alongside the circle, in window coordinates.
	 */
	private java.util.List<Rectangle> getPinnedAreas()
	{
		java.util.List<Rectangle> areas = new java.util.ArrayList<>();
		if (frame == null)
		{
			return areas;
		}

		// Everything above the content pane is the title bar
		int titleHeight = SwingUtilities.convertPoint(frame.getContentPane(), 0, 0, frame).y;
		if (config.alwaysShowTitleBar() && titleHeight > 0)
		{
			areas.add(new Rectangle(0, 0, frame.getWidth(), titleHeight));
		}

		// Everything to the right of the game is the sidebar
		if (config.keepSidebar())
		{
			Component game = (Component) client;
			int gameRight = SwingUtilities.convertPoint(game, game.getWidth(), 0, frame).x;
			if (gameRight < frame.getWidth())
			{
				areas.add(new Rectangle(gameRight, titleHeight, frame.getWidth() - gameRight, frame.getHeight() - titleHeight));
			}
		}
		return areas;
	}

	private boolean isOverPinnedArea(int x, int y)
	{
		for (Rectangle pinned : getPinnedAreas())
		{
			if (pinned.contains(x, y))
			{
				return true;
			}
		}
		return false;
	}

	private void findFrame(Client client)
	{
		Component component = (Component) client;
		Container container = component.getParent();
		while (container != null)
		{
			if (container instanceof ContainableFrame)
			{
				frame = (ContainableFrame) container;
				break;
			}
			container = container.getParent();
		}
	}
}
