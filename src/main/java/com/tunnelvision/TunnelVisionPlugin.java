package com.tunnelvision;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.IllegalComponentStateException;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ContainableFrame;

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
			if (direction > 0)
			{
				radius = Math.min(radius + 10, diagonal);
			}
			else
			{
				radius = Math.max(radius - 10, 10);
			}

			mouseWheelEvent.consume();

			update(mouseWheelEvent.getXOnScreen(), mouseWheelEvent.getYOnScreen());
		}
	};
	private final AWTEventListener awtMouseListener = awtEvent ->
	{
		if (awtEvent instanceof MouseEvent)
		{
			MouseEvent mouseEvent = (MouseEvent) awtEvent;
			update(mouseEvent.getXOnScreen(), mouseEvent.getYOnScreen());
		}
	};
	private final ComponentAdapter clientResizeListener = new ComponentAdapter()
	{
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

	@Override
	protected void startUp() throws Exception
	{
		findFrame(client);
		Toolkit.getDefaultToolkit().addAWTEventListener(awtKeyListener, AWTEvent.KEY_EVENT_MASK);
		Toolkit.getDefaultToolkit().addAWTEventListener(awtMouseWheelListener, AWTEvent.MOUSE_WHEEL_EVENT_MASK);
		Toolkit.getDefaultToolkit().addAWTEventListener(awtMouseListener, AWTEvent.MOUSE_MOTION_EVENT_MASK);
		frame.addComponentListener(clientResizeListener);

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
				+ "It is unfortunately not possible to resize the client while "
				+ "this plugin is activated.",
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
		Toolkit.getDefaultToolkit().removeAWTEventListener(awtMouseListener);
		Toolkit.getDefaultToolkit().removeAWTEventListener(awtMouseWheelListener);
		Toolkit.getDefaultToolkit().removeAWTEventListener(awtKeyListener);
		frame.removeComponentListener(clientResizeListener);

		try
		{
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

		try
		{
			frame.setShape(new Ellipse2D.Double(
				oldX - radius,
				oldY - radius,
				2 * radius,
				2 * radius));
		}
		catch (IllegalComponentStateException ignore)
		{
		}
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
