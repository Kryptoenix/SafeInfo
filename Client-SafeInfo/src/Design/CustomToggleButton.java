package Design;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

public class CustomToggleButton extends JToggleButton {
    private Shape shape;
    private Color color1 = CustomColor.getPrimaryColor();   // Default gradient color 1
    private Color color2 = CustomColor.getSecondaryColor(); // Default gradient color 2
    private final Color borderColor = Color.BLACK; // Default border color
    private ImageIcon icon;
    private int iconWidth;
    private int iconHeight;

    // Constructor for a round toggle button without an icon
    public CustomToggleButton(int width, int height) {
        super();
        setOpaque(false); // Ensure button is transparent except for its contents
        setPreferredSize(new Dimension(width, height)); // Set custom size
        initMouseHoverEffects();
    }

    // Constructor for a round toggle button with an icon
    public CustomToggleButton(String iconPath, int iconWidth, int iconHeight) {
        this(iconWidth, iconHeight); // Call base constructor with width and height
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;

        if (iconPath != null) {
            ImageIcon rawIcon = new ImageIcon(iconPath);
            Image img = rawIcon.getImage();
            Image resizedImage = img.getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);
            this.icon = new ImageIcon(resizedImage); // Set the resized image as icon
        }
    }


    // Initialize mouse hover effect that changes button colors
    private void initMouseHoverEffects() {
        addMouseListener(new MouseAdapter() {
            final Color originalColor1 = CustomColor.getPrimaryColor();  // Store the original gradient colors
            final Color originalColor2 = CustomColor.getSecondaryColor();

            @Override
            public void mouseEntered(MouseEvent evt) {
                setGradientColors(originalColor1.brighter(), originalColor2.brighter());
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                setGradientColors(originalColor1, originalColor2);
            }
        });
    }

    // Setter for gradient colors
    public void setGradientColors(Color color1, Color color2) {
        this.color1 = color1;
        this.color2 = color2;
        repaint(); // Repaint the button when colors change
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Ensure transparency is handled
        if (isOpaque()) {
            super.paintComponent(g); // Calls default painting for transparency handling
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Create a gradient paint for the button background
        GradientPaint gradientPaint = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
        g2.setPaint(gradientPaint);

        // Fill the button area with the oval shape
        g2.fillOval(0, 0, getWidth(), getHeight());

        // Paint the icon if available
        if (icon != null) {
            int x = (getWidth() - iconWidth) / 2; // Center the icon horizontally
            int y = (getHeight() - iconHeight) / 2; // Center the icon vertically
            icon.paintIcon(this, g2, x, y); // Draw the icon
        }

        // If the button is selected, add an overlay to indicate selection
        if (isSelected()) {
            g2.setColor(new Color(255, 255, 255, 100)); // Light overlay to show the selected state
            g2.fillOval(0, 0, getWidth(), getHeight());
        }

        // Avoid calling super.paintComponent(g) after custom painting to ensure nothing paints over the custom content
    }


    // Paint the border as a circle with the specified border color
    @Override
    protected void paintBorder(Graphics g) {
        g.setColor(borderColor); // Use the custom border color
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Ensure the border is a circle by using the same smaller dimension (diameter)
        int diameter = Math.min(getWidth(), getHeight());
        g2.drawOval(0, 0, diameter - 1, diameter - 1); // Draw oval border
    }

    // Define the shape of the button (round)
    @Override
    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            int diameter = Math.min(getWidth(), getHeight());
            shape = new Ellipse2D.Float(0, 0, diameter - 1, diameter - 1); // Set button shape as an oval (circle)
        }
        return shape.contains(x, y);
    }
}