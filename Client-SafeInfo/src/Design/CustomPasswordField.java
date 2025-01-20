package Design;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

public class CustomPasswordField extends JPasswordField {
    private Shape shape;

    public CustomPasswordField(String placeholder, Integer ...size) {
        // To store the placeholder text
        setOpaque(false); // Allow custom painting
        setFont(new Font("Arial", Font.BOLD, 16));
        setBackground(CustomColor.getTextFieldBackground());
        setForeground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createLineBorder(CustomColor.getSecondaryColor(), 2, true));
        setEchoChar((char) 0); // Initially show the placeholder without masking
        setText(placeholder); // Set the placeholder text initially
        setPreferredSize(CustomSize.getDimension(size.length > 0 ? size[0] : 300, size.length > 1 ? size[1] : 40));

        // Add focus listener to manage placeholder behavior
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(placeholder)) {
                    setText(""); // Clear the placeholder
                    setForeground(Color.BLACK); // Change text color to black
                    setEchoChar('*'); // Enable password masking
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setEchoChar((char) 0); // Remove masking for placeholder
                    setForeground(Color.LIGHT_GRAY); // Change color back to light gray
                    setText(placeholder); // Reset placeholder text
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        g.setColor(getForeground());
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
    }

    @Override
    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        }
        return shape.contains(x, y);
    }
}