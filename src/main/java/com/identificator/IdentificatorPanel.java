package com.identificator;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class IdentificatorPanel extends PluginPanel
{
	private final JTextArea editor = new JTextArea();

	IdentificatorPanel()
	{
		getParent().setLayout(new BorderLayout());
		getParent().add(this, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		editor.setTabSize(2);
		editor.setLineWrap(true);
		editor.setWrapStyleWord(true);

		JPanel notesContainer = new JPanel();
		notesContainer.setLayout(new BorderLayout());
		notesContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		editor.setOpaque(false);

		notesContainer.add(editor, BorderLayout.CENTER);
		notesContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

		add(notesContainer, BorderLayout.CENTER);
	}

	void appendText(String data)
	{
		editor.append(data + "\n");
		editor.setCaretPosition(editor.getDocument().getLength());
	}
}
