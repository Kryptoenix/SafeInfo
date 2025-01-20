package Design;

import javax.swing.*;
import java.awt.*;

public class ChatBubbleText extends JPanel {
    private final String message;
    private final boolean isSender;
    private final String bubbleTime;

    // Set fixed bubble width and height
    private static final int BUBBLE_WIDTH = 250;
    private static final int BUBBLE_PADDING = 15; // Adjusted padding for spacing
    private static final int ARC_SIZE = 15;
    private static final int TRIANGLE_SIZE = 10; // Size of the triangle

    public ChatBubbleText(String message, String bubbleTime, boolean isSender) {
        this.message = message;
        this.isSender = isSender;
        this.bubbleTime = bubbleTime;
        setOpaque(false); // Transparent JPanel
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set bubble color (Primary for sender, Secondary for receiver)
        Color bubbleColor = isSender ? CustomColor.getPrimaryColor() : CustomColor.getSecondaryColor();
        g2.setColor(bubbleColor);

        // Calculate position of the bubble (based on sender/receiver)
        int bubbleX = isSender ? getWidth() - BUBBLE_WIDTH - BUBBLE_PADDING : BUBBLE_PADDING;
        int bubbleY = 0;

        // Calculate text height in advance to adjust the bubble height
        int textHeight = calculateTextHeight(g2, message) + g2.getFontMetrics().getHeight(); // Extra space for time

        // Draw the rounded rectangle for the chat bubble (drawn before the text to avoid overlapping)
        // Draw the rounded rectangle for the chat bubble (drawn before the text to avoid overlapping)
        g2.fillRoundRect(bubbleX, bubbleY, BUBBLE_WIDTH, textHeight + BUBBLE_PADDING * 2, ARC_SIZE, ARC_SIZE);

        // Draw the triangle pointing to the bubble
        drawTriangle(g2, bubbleX, bubbleY);

        // Draw the message text
        g2.setColor(isSender ? Color.BLACK : Color.WHITE); // Set text color based on sender/receiver
        drawWrappedText(g2, message, bubbleX + 10, bubbleY + BUBBLE_PADDING);

        // Draw the time inside the bubble
        g2.setColor(isSender ? Color.BLACK : Color.WHITE); // Adjust time color as well
        Font originalFont = g2.getFont();
        g2.setFont(originalFont.deriveFont(originalFont.getSize() * 0.7f)); // Smaller font for the time
        int timeY = bubbleY + textHeight + BUBBLE_PADDING;
        int timeX = bubbleX + BUBBLE_WIDTH - g2.getFontMetrics().stringWidth(bubbleTime) - 10;
        g2.drawString(bubbleTime, timeX, timeY);

        // Restore original font
        g2.setFont(originalFont);
    }

    private void drawWrappedText(Graphics2D g2, String message, int x, int y) {
        FontMetrics metrics = g2.getFontMetrics();
        String[] words = message.split(" ");
        StringBuilder line = new StringBuilder();
        int lineHeight = metrics.getHeight();

        for (String word : words) {
            String tempLine = line + word + " ";
            if (metrics.stringWidth(tempLine) > 230) {
                g2.drawString(line.toString(), x+5, y+10);
                y += lineHeight; // Move to the next line
                line = new StringBuilder(word + " "); // Start a new line with the current word
            } else {
                line.append(word).append(" ");
            }
        }

        g2.drawString(line.toString(), x+5, y+10); // Draw last line
    }

    private void drawTriangle(Graphics2D g2, int bubbleX, int bubbleY) {
        int[] xPoints;
        int[] yPoints;

        if (isSender) { // Sender
            xPoints = new int[]{bubbleX + BUBBLE_WIDTH, bubbleX + BUBBLE_WIDTH + TRIANGLE_SIZE, bubbleX + BUBBLE_WIDTH};
        } else { // Receiver
            xPoints = new int[]{bubbleX, bubbleX - TRIANGLE_SIZE, bubbleX};
        }
        yPoints = new int[]{bubbleY + ARC_SIZE, bubbleY + ARC_SIZE + TRIANGLE_SIZE / 2, bubbleY + ARC_SIZE + TRIANGLE_SIZE};

        g2.fillPolygon(xPoints, yPoints, 3); // Draw the triangle
    }

    private int calculateTextHeight(Graphics2D g2, String message) {
        FontMetrics metrics = g2.getFontMetrics();
        String[] words = message.split(" ");
        StringBuilder line = new StringBuilder();
        int lineHeight = metrics.getHeight();
        int totalHeight = lineHeight; // Start with the height of one line

        for (String word : words) {
            String tempLine = line + word + " ";
            if (metrics.stringWidth(tempLine) > 230) {
                totalHeight += lineHeight; // Move to the next line
                line = new StringBuilder(word + " "); // Start a new line with the current word
            } else {
                line.append(word).append(" ");
            }
        }

        return totalHeight; // Total height of wrapped text
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics metrics = getFontMetrics(getFont());

        // Wrap text manually to calculate the number of lines
        int maxWidth = BUBBLE_WIDTH - 20; // 10 padding on each side
        int lines = 0;
        String[] words = message.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String tempLine = line + word + " ";
            if (metrics.stringWidth(tempLine) > maxWidth) {
                lines++; // New line
                line = new StringBuilder(word + " ");
            } else {
                line.append(word).append(" ");
            }
        }
        lines++; // For the last line if there are any words

        // calculate the height based on the number of lines and include space for time
        int height = metrics.getHeight() * lines + metrics.getHeight() + BUBBLE_PADDING * 2;

        return new Dimension(BUBBLE_WIDTH + BUBBLE_PADDING * 2, height);
    }
}
