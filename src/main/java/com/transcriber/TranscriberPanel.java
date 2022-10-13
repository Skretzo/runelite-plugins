package com.transcriber;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class TranscriberPanel extends PluginPanel
{
	private final JTextArea transcriberEditor = new JTextArea();
	private final TranscriberConfig config;

	TranscriberPanel(TranscriberConfig config)
	{
		this.config = config;
		init();
	}

	private void init()
	{
		getParent().setLayout(new BorderLayout());
		getParent().add(this, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		transcriberEditor.setTabSize(2);
		transcriberEditor.setLineWrap(true);
		transcriberEditor.setWrapStyleWord(true);

		JPanel notesContainer = new JPanel();
		notesContainer.setLayout(new BorderLayout());
		notesContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		transcriberEditor.setOpaque(false);

		notesContainer.add(transcriberEditor, BorderLayout.CENTER);
		notesContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

		add(notesContainer, BorderLayout.CENTER);
	}

	void appendText(String data)
	{
		data = data.replaceAll("<br>", "\n");
		if (config.removeUnnecessaryTags())
		{
			data = data.replaceAll("<col=000000>", "");
		}
		transcriberEditor.append(data + "\n");
		transcriberEditor.setCaretPosition(transcriberEditor.getDocument().getLength());
	}

	String[] getSelected()
	{
		String text = transcriberEditor.getSelectedText();

		if (text == null)
		{
			return new String[0];
		}

		return text.split("\n");
	}
}
