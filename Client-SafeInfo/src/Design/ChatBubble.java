package Design;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ChatBubble extends JPanel {
    private final boolean isSender;
    private BufferedImage image;
    private final String bubbleTime;

    private static final int BUBBLE_WIDTH = 250;
    private static final int BUBBLE_PADDING = 15; // Padding for text and image
    private static final int ARC_SIZE = 15;
    private static final int TRIANGLE_SIZE = 10; // Size of the triangle

    public ChatBubble(byte[] imageBytes, String bubbleTime, boolean isSender) {
        this.isSender = isSender;
        this.bubbleTime = bubbleTime;
        setOpaque(false); // Transparent JPanel

        // Convert bytes to image if not empty
        if (imageBytes != null && imageBytes.length > 0) {
            try {
                this.image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        int padding=10;
        int y = padding;

        Graphics2D g2;

        super.paintComponent(g);
        g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set bubble color (Primary for sender, Secondary for receiver)
        Color bubbleColor = isSender ? CustomColor.getPrimaryColor() : CustomColor.getSecondaryColor(); // Temporary colors
        g2.setColor(bubbleColor);

        // Calculate position of the bubble
        int bubbleX = isSender ? getWidth() - BUBBLE_WIDTH - BUBBLE_PADDING : BUBBLE_PADDING;
        int bubbleY = 0;

        // Draw the rounded rectangle for the chat bubble
        g2.fillRoundRect(bubbleX, bubbleY, BUBBLE_WIDTH, getHeight(), ARC_SIZE, ARC_SIZE);

        // Draw the triangle pointing to the bubble
        drawTriangle(g2, bubbleX, bubbleY);

        // Draw message text or image
        if (image != null) {
            g2.drawImage(image, bubbleX + BUBBLE_PADDING, bubbleY + BUBBLE_PADDING,
                    BUBBLE_WIDTH - 2 * BUBBLE_PADDING,
                    getHeight() - 2 * BUBBLE_PADDING, null); // Draw image with padding
            y += image.getHeight() ;  // Adjust y to be below the image

        } else {
            // Draw placeholder text if no image
            g2.setColor(Color.BLACK);
            g2.drawString("No Image", bubbleX + 10, bubbleY + 20); // Placeholder text
            y += g2.getFontMetrics().getHeight();  // Adjust y to be below the placeholder text

        }

        // save original font (for messages)
        Font originalFont = g2.getFont();
        g2.setFont(originalFont.deriveFont(originalFont.getSize() * 0.7f)); // 70% of the original font size


        y += g2.getFontMetrics().getHeight() + padding;  // Ensure we move below the content + some padding


        int timeX; // adjust X coordinate of time message based on whether it's from sender or receiver
        if (isSender) {
            // Align the time to the right inside the sender's bubble
            timeX = bubbleX + BUBBLE_WIDTH - g2.getFontMetrics().stringWidth(bubbleTime) -20;
            g2.setColor(Color.BLACK);
        } else {
            // Align the time to the left inside the receiver's bubble
            timeX = bubbleX + 20;
            g2.setColor(Color.WHITE);
        }
        g2.drawString(bubbleTime, timeX, y);

        // restore original font
        g2.setFont(originalFont);
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

    @Override
    public Dimension getPreferredSize() {
        FontMetrics metrics = getFontMetrics(getFont());

        // Calculate the height for the text and time.
        int textHeight = metrics.getHeight(); // Height of a single line of text
        int timeHeight = (int) (metrics.getHeight() * 0.7); // Smaller font for time (70% of original text size)

        // If there is an image, calculate height based on image dimensions
        if (image != null) {
            return new Dimension(
                    BUBBLE_WIDTH + BUBBLE_PADDING * 2,
                    image.getHeight() + BUBBLE_PADDING * 2 + timeHeight); // Add space for time
        }

        // If no image, calculate height based on text and time
        return new Dimension(
                BUBBLE_WIDTH + BUBBLE_PADDING * 2,
                textHeight + timeHeight + BUBBLE_PADDING * 2); // Include both text and time in the height
    }
}