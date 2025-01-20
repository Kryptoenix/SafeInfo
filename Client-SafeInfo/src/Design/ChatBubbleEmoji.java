package Design;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatBubbleEmoji extends GradientPanel {

    private final JTextField messageField;

    public ChatBubbleEmoji(String[] emojis, JTextField messageField) {
        this.messageField = messageField;
        setLayout(new GridBagLayout());
        setBackground(CustomColor.getTextFieldBackground());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2); // margins between emojis
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE; // dd not resize the buttons

        int emojisPerRow = 6;

        for(String emoji : emojis) {
            // create a button for the emoji
            CustomJButton emojiButton = new CustomJButton(emoji, "oval",30,30);
            emojiButton.addActionListener(new EmojiButtonListener(emoji)); // Add action listener

            add(emojiButton, gbc); // Add the button to the panel

            // next emoji on the row
            gbc.gridx += 1;

            // if end of row, column ++
            if (gbc.gridx == emojisPerRow) {
                gbc.gridx = 0;
                gbc.gridy += 1;
            }
        }

        // fill remaining space with empty panels (if there is space left in the last row)
        int remainingSlots = emojisPerRow - gbc.gridx; // Remaining slots in the current row
        for (int i = 0; i < remainingSlots; i++) {
            JPanel emptyPanel = new JPanel(); // Empty placeholder
            emptyPanel.setOpaque(false); // Make sure it is transparent

            add(emptyPanel, gbc); // Add the empty panel to the grid
            gbc.gridx += 1;
        }
    }

    private class EmojiButtonListener implements ActionListener {
        private final String emoji;

        public EmojiButtonListener(String emoji) {
            this.emoji = emoji;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Handle emoji click

            String currentText = messageField.getText();

            if(messageField.getText().equals("Enter your message") && messageField.getForeground().equals(Color.LIGHT_GRAY)){
                messageField.setText(emoji);
                messageField.setForeground(Color.BLACK);
            }else {
                messageField.setText(currentText + emoji);
                messageField.setForeground(Color.BLACK);
            }
        }
    }
}