package Utils;

import Design.CustomColor;
import Design.GradientPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ShowErrorMessage extends JDialog {
    public ShowErrorMessage(JFrame frame, String message) {
        // Calculate position to place dialog at the top of the parent frame
        int x = (frame.getWidth() - getWidth()) / 3; // center horizontally
        int y = (frame.getHeight() - getHeight()) / 3; // position at the top
        setLocation(x, y);

        setUndecorated(true);
        setSize(600, 60);

        // Set the shape of the JDialog to a rounded rectangle
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

        // Create a custom JPanel for background and label
        GradientPanel panel = new GradientPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill the background with rounded corners
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                // Set the color and thickness for the border
                g2.setColor(CustomColor.getSecondaryColor()); // border color
                g2.setStroke(new BasicStroke(4)); // set thickness to 4 pixels
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30); // draw the border with rounded corners
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(600, 60);
            }
        };

        panel.setBackground(Color.decode("#FFCCCB"));
        panel.setLayout(new BorderLayout());

        JLabel messageLabel = new JLabel("⚠ " + message, SwingConstants.CENTER);
        messageLabel.setForeground(Color.decode("#D8000C"));  // set text color
        messageLabel.setFont(new Font("Arial", Font.BOLD, 20));

        panel.add(messageLabel, BorderLayout.CENTER);

        setContentPane(panel);

        // Hide the error message after 3 seconds without freezing the UI thread
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SwingUtilities.invokeLater(this::dispose);
        }).start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
    }
}
