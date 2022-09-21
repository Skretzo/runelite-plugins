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

	void init()
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
		transcriberEditor.append(data.replaceAll("<br>", "\n").replaceAll("<col=000000>", "") + "\n");
		transcriberEditor.setCaretPosition(transcriberEditor.getDocument().getLength());
	}
}
