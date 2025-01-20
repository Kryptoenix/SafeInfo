package Design;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import Utils.ASSETS;

public class CustomJButton extends JButton {
    private Shape shape;
    private Color color1 = CustomColor.getPrimaryColor(); // Default gradient color 1
    private Color color2 = CustomColor.getSecondaryColor(); // Default gradient color 2
    private final String type; // For defining shape (rect or oval)

    // Constructor for label-based buttons with optional size parameters
    public CustomJButton(String label, String type, Integer... size) {
        super(label); // Set label text
        this.type = type; // Set button type (rect or oval)
        setOpaque(false); // Allow for custom painting
        setButtonProperties(size); // Set common button properties
    }

    // Constructor for icon-based buttons with custom image path and dimensions
    public CustomJButton(String imagePath, Integer iconWidth, Integer iconHeight) {
        super(); // No label, call JButton's default constructor
        this.type = "oval"; // Default to rectangular button for icon-based buttons
        setOpaque(false); // Allow for custom painting
        setIconButtonProperties(imagePath, iconWidth, iconHeight); // Set properties for icon button
    }

    // Set common properties for text-based buttons
    private void setButtonProperties(Integer... size) {
        setFont(new Font("Sans-serif", Font.BOLD, 16));
        setForeground(CustomColor.getButtonTextColor());
        setPreferredSize(getDimension(size)); // Use the helper method for dimension
        setFocusPainted(false);
        setBorder(BorderFactory.createLineBorder(CustomColor.getButtonTextColor(), 2));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addHoverEffect(); // Add hover effect for background change
    }

    // Set properties for icon-based buttons
    private void setIconButtonProperties(String imagePath, Integer iconWidth, Integer iconHeight) {
        ImageIcon icon = new ImageIcon(imagePath);
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);
        setIcon(new ImageIcon(resizedImage)); // Set the resized image as icon

        setBorder(BorderFactory.createLineBorder(CustomColor.getButtonTextColor(), 8)); // Custom border for icon buttons
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setText(""); // No text for icon buttons
        addHoverEffect(); // Add hover effect for icon buttons
    }

    // Helper method to get Dimension based on optional size parameters
    private static Dimension getDimension(Integer... size) {
        return CustomSize.getDimension(size);
    }

    // Helper method to create hover effect for all buttons
    private void addHoverEffect() {
        Color originalColor1 = CustomColor.getPrimaryColor();
        Color originalColor2 = CustomColor.getSecondaryColor();

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setGradientColors(originalColor1.brighter(), originalColor2.brighter()); // Brighten on hover
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setGradientColors(originalColor1, originalColor2); // Reset after hover
            }
        });
    }

    // Setter for custom gradient colors
    public void setGradientColors(Color color1, Color color2) {
        this.color1 = color1;
        this.color2 = color2;
        repaint(); // Repaint the button when colors are set
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Create Gradient Paint for the button background
        GradientPaint gradientPaint = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
        g2.setPaint(gradientPaint);

        if ("rect".equals(type)) {
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15); // Draw rectangle
        } else if ("oval".equals(type)) {
            g2.fillOval(0, 0, getWidth(), getHeight()); // Draw oval
        }

        // Check if there is an icon, and paint it manually
        if (getIcon() != null) {
            Icon icon = getIcon();
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();
            int x = (getWidth() - iconWidth) / 2; // Center the icon horizontally
            int y = (getHeight() - iconHeight) / 2; // Center the icon vertically
            icon.paintIcon(this, g, x, y); // Draw the icon
        }

        // Call super to ensure any default painting (e.g., text) is handled
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        g.setColor(getForeground());
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if ("rect".equals(type)) {
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15); // Draw rectangle border
        } else if ("oval".equals(type)) {
            g.drawOval(0, 0, getWidth() - 1, getHeight() - 1); // Draw oval border
        }
    }

    @Override
    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            if ("rect".equals(type)) {
                shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 30, 30); // Rectangle shape
            } else if ("oval".equals(type)) {
                shape = new Ellipse2D.Float(0, 0, getWidth() - 1, getHeight() - 1); // Oval shape
            }
        }
        assert shape != null;
        return shape.contains(x, y);
    }

    // Method to create a button based on label (integrated into the class)
    public static CustomJButton createButton(String label, Integer... size) {
        return switch (label) {
            case "Go Back" -> new CustomJButton(ASSETS.goBack, 45, 45);
            case "Logout" -> new CustomJButton(ASSETS.logout, 45, 45);
            case "Settings" -> new CustomJButton(ASSETS.settings, 45, 45);
            case "Send" -> new CustomJButton(ASSETS.send, 45, 45);
            case "Add Image" -> new CustomJButton(ASSETS.addImage, 45, 45);
            case "Send Emoji" -> new CustomJButton(ASSETS.sendEmoji, 45, 45);
            default -> new CustomJButton(label, "rect", size); // Default text button with label

        };
    }
}