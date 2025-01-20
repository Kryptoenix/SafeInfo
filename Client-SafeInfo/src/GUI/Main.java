package GUI;

import javax.swing.*;
import java.awt.*;

import Utils.*;

public class Main {
    private static final CardLayout cardLayout = new CardLayout();
    private static final JPanel cardPanel = new JPanel(cardLayout);
    public static JFrame mainFrame;

    public static void init() {
        mainFrame = new JFrame("SafeInfo");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);

        final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        java.awt.Image image = defaultToolkit.getImage(ASSETS.appIcon);
        final Taskbar taskbar = Taskbar.getTaskbar();

        taskbar.setIconImage(image);


        // Only add the home panel initially
        cardPanel.add(new Login(), "Home");

        mainFrame.add(cardPanel);
        mainFrame.setVisible(true);

        // Initially show the "Home" panel or any default panel
        cardLayout.show(cardPanel, "Home");
    }

    public static void switchPanel(String panelName) {
        // Check if the panel already exists in the card panel
        Component[] components = cardPanel.getComponents();
        Component panelToRemove = null;
        boolean panelExists = false;

        for (Component component : components) {
            if (component.getClass().getSimpleName().equals(panelName + "Panel")) {
                panelExists = true;
            }
            else{
                // if panel is not target panel, mark it for removal
                panelToRemove = component;
            }
        }

        // lazy load: only create and add the panel if it doesn't already exist
        if (!panelExists) {
            switch (panelName) {
                case "Home":
                    cardPanel.add(new Login(), "Home");
                    break;
                case "Registration":
                    cardPanel.add(new Registration(), "Registration");
                    break;
                case "ForgotPassword":
                    cardPanel.add(new ForgotPassword(), "ForgotPassword");
                    break;
                case "TwoFactorAuth":
                    cardPanel.add(new TwoFactorAuth(), "TwoFactorAuth");
                    break;
                case "User":
                    cardPanel.add(new User(), "User");
                    break;
                case "Chat":
                    cardPanel.add(new Chat(), "Chat");
                    break;
                case "NewChat":
                    cardPanel.add(new NewChat(), "NewChat");
                    break;
                case "Settings":
                    cardPanel.add(new Settings(), "Settings");
                    break;
                case "NewPhoneNumber":
                    cardPanel.add(new NewPhoneNumber(), "NewPhoneNumber");
                    break;
                case "NewPassword":
                    cardPanel.add(new NewPassword(), "NewPassword");
                    break;
                default:
                    System.out.println("Unknown panel: " + panelName);
            }
        }

        // remove previous panel if it's not the same as the new one
        if(panelToRemove!=null && !panelToRemove.getClass().getSimpleName().equals(panelName+"Panel")) {
            cardPanel.remove(panelToRemove);
        }

        // Show the requested panel
        cardLayout.show(cardPanel, panelName);
    }
}

