package Design;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

public class CustomTextField extends JTextField {
    private Shape shape;

    // Constructor to initialize RoundJTextField with rounded corners
    public CustomTextField(String placeholder, Integer ...size) {
        setText(placeholder);  // Set placeholder text
        setFont(new Font("Arial", Font.BOLD, 16));
        setForeground(Color.LIGHT_GRAY);  // Placeholder color
        setBackground(CustomColor.getTextFieldBackground());
        setBorder(BorderFactory.createLineBorder(CustomColor.getSecondaryColor(), 2, true));
        setOpaque(false); // Set opaque to false to allow for custom painting

        Dimension dimension = CustomSize.getDimension(size.length > 0 ? size[0] : 300, size.length > 1 ? size[1] : 40);
        setPreferredSize(dimension);

        // Add focus listener to handle placeholder behavior
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(placeholder)) {
                    setText("");  // Clear placeholder text when focused
                    setForeground(Color.BLACK);  // Set normal text color
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setForeground(Color.LIGHT_GRAY);  // Set placeholder color when empty
                    setText(placeholder);  // Restore placeholder text
                }
            }
        });
    }

    // Override paintComponent to draw the rounded background
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the background with rounded corners
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);  // Rounded rectangle
        super.paintComponent(g);  // Paint the text field content
    }

    // Override paintBorder to draw a rounded border
    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getForeground());  // Border color
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);  // Rounded border
    }

    // Override contains method to ensure proper hit detection for the rounded shape
    @Override
    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);  // Define rounded shape
        }
        return shape.contains(x, y);  // Check if point is within the rounded shape
    }

    // You can add other methods or features specific to this round text field
}
