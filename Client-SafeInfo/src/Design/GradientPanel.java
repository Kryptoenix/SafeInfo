package Design;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class GradientPanel extends JPanel {

    private final Color color1 = CustomColor.getPrimaryColor();   // Default color 1 (light blue)
    private final Color color2 = CustomColor.getSecondaryColor(); // Default color 2 (red)

    // Constructor
    public GradientPanel() {
        setOpaque(false);  // Ensures the background is painted by our custom paint method

        // Set a black border with 2 pixels width
        setPanelBorder(CustomColor.getSecondaryColor(), 1);  // You can adjust the color and width as needed
    }


    // Method to set a custom border for the panel
    public void setPanelBorder(Color borderColor, int thickness) {
        Border lineBorder = BorderFactory.createLineBorder(borderColor, thickness);
        setBorder(lineBorder);  // Set the custom border
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable antialiasing for smoother graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Create a GradientPaint from color1 to color2
        int width = getWidth();
        int height = getHeight();
        GradientPaint gradient = new GradientPaint(0, 0, color1, width, height, color2);

        // Apply the gradient as the panel's background
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
    }
}
