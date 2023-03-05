package com.measurements;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class MeasurementsPanel extends PluginPanel
{
	private final JTextArea measurementsEditor = new JTextArea();

	MeasurementsPanel()
	{
		init();
	}

	private void init()
	{
		getParent().setLayout(new BorderLayout());
		getParent().add(this, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		measurementsEditor.setTabSize(2);
		measurementsEditor.setLineWrap(true);
		measurementsEditor.setWrapStyleWord(true);

		JPanel notesContainer = new JPanel();
		notesContainer.setLayout(new BorderLayout());
		notesContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		measurementsEditor.setOpaque(false);

		notesContainer.add(measurementsEditor, BorderLayout.CENTER);
		notesContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

		add(notesContainer, BorderLayout.CENTER);
	}

	void appendText(String data)
	{
		measurementsEditor.append(data + "\n");
		measurementsEditor.setCaretPosition(measurementsEditor.getDocument().getLength());
	}
}
