package Design;

import javax.swing.*;
import java.awt.*;

public class CustomLabel extends JLabel {
    private final Color borderColor;
    private final int borderThickness;

    // Constructor to create a Custom JLabel with text, font size, text color, border color, and border thickness
    public CustomLabel(String text, int fontSize, Color textColor, Color borderColor, int borderThickness) {

        super(text, SwingConstants.CENTER);  // Center align the text
        setFont(new Font("Sans-serif", Font.BOLD, fontSize));  // Set custom font size
        setForeground(textColor);  // Set the text color
        this.borderColor = borderColor;
        this.borderThickness = borderThickness;
        setOpaque(false);  // Ensure the background is transparent
    }

    public CustomLabel(String text,Integer ...size) {
        super(text, SwingConstants.CENTER);  // Center align the text

        int fontSize = (size.length > 0) ? size[0] : 40;  // Default font size is 40

        setFont(new Font("Sans-serif", Font.BOLD, fontSize));  // Set custom font size
        setForeground(Color.WHITE);  // Set the text color
        this.borderColor = Color.BLACK;
        this.borderThickness = 1;
        setOpaque(false);  // Ensure the background is transparent
    }

    // Custom paint method to draw the text with a border around it
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // get the text properties
        String text = getText(); // in case getText() returns null

        if (text == null) {
            text = ""; // if text is null, use an empty string to avoid errors
        }

        // Get the text properties
        FontMetrics fm = g2.getFontMetrics(getFont());
        int x = (getWidth() - fm.stringWidth(text)) / 2;  // Calculate x position to center the text
        int y = (getHeight() + fm.getAscent()) / 2 - fm.getDescent();  // Calculate y position to center the text vertically

        // Draw the border (outline) for the text
        g2.setColor(borderColor);  // Set the border color
        g2.setStroke(new BasicStroke(borderThickness));  // Set the border thickness
        g2.drawString(text, x - borderThickness, y);  // Left outline
        g2.drawString(text, x + borderThickness, y);  // Right outline
        g2.drawString(text, x, y - borderThickness);  // Top outline
        g2.drawString(text, x, y + borderThickness);  // Bottom outline

        // Draw the actual text over the border
        g2.setColor(getForeground());  // Set the actual text color
        g2.drawString(text, x, y);  // Draw the text

        g2.dispose();  // Dispose of the graphics context to release resources
    }
}
