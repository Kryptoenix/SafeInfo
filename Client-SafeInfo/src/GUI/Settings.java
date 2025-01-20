package GUI;

import javax.swing.*;
import java.awt.*;

import Design.*;
import Service.Controller;
import UserConfig.UserGlobals;
import Utils.ShowErrorMessage;
import Utils.Sound;
import Utils.TFA;

public class Settings extends JPanel {

    // Settings Label
    private JLabel settingsLabel;
    // Go Back button
    private JButton goBackButton;

    // Logout button
    private JButton logoutButton;

    // Username label
    private JLabel usernameLabel;

    // Username 2 label
    private JLabel usernameLabel2;

    // Password label
    private JLabel passwordLabel;

    // Phone Number label
    private JLabel phoneNumberLabel;

    // change button for Password
    private JButton changePasswordButton;

    // change button for Phone Number
    private JButton changePhoneButton;

    public Settings() {

        setSize(800, 600);
        setLayout(new BorderLayout());

        initComponents();
        setUpLayout();
    }

    private void initComponents(){
        settingsLabel = new CustomLabel("Settings");

        goBackButton = CustomJButton.createButton("Go Back");
        goBackButton.addActionListener(e-> Main.switchPanel("User"));

        logoutButton = CustomJButton.createButton("Logout");
        logoutButton.addActionListener(e->{
            Main.switchPanel("Home");
            Controller.processRequest(
                    Controller.ServiceID.DestroyCachedSession,
                    (error) ->{
                        if (!error.isEmpty()){
                            ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                            errorDialog.setVisible(true);
                        }
                    }
            );
            Sound.logout();
        });

        usernameLabel = new CustomLabel("Username: ",25);
        usernameLabel2 = new CustomLabel(UserGlobals.getUsername(),25);

        passwordLabel = new CustomLabel("Password",25);

        phoneNumberLabel = new CustomLabel("Phone Number",25);

        changePasswordButton = new CustomJButton("Change Password","rect",150);
        changePasswordButton.addActionListener(e-> {
            TFA.setSwitchTo("Settings");
            Main.switchPanel("TwoFactorAuth");
        });

        changePhoneButton = new CustomJButton("Change Phone","rect",150);
        changePhoneButton.addActionListener(e-> Main.switchPanel("NewPhoneNumber"));
    }

    private void setUpLayout(){
        GradientPanel topPanel = createTopPanel();
        GradientPanel centerPanel = createCenterPanel();
        GradientPanel bottomPanel = createBottomPanel();

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private GradientPanel createTopPanel(){

        // Top panel
        GradientPanel topPanel = new GradientPanel();
        topPanel.setBackground(CustomColor.getBackgroundColor());
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        topPanel.add(goBackButton);

        topPanel.add(Box.createHorizontalGlue());


        topPanel.add(logoutButton);

        return topPanel;
    }

    private GradientPanel createCenterPanel(){

        // Center panel
        GradientPanel centerPanel = new GradientPanel();
        centerPanel.setLayout(new GridBagLayout());  // Using GridBagLayout for flexibility
        centerPanel.setBackground(CustomColor.getBackgroundColor());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 10, 20, 10); // Default margin between components

        // 1st level: Settings label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;  // Span across 2 columns
        centerPanel.add(settingsLabel, gbc);

        // 2nd level: Username label with increased vertical insets
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;  // Align right for the label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(50, 10, 20, 10); // Increase top inset for extra space
        centerPanel.add(usernameLabel, gbc);

        gbc.insets = new Insets(50, 30, 20, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        gbc.gridy = 1;
        centerPanel.add(usernameLabel2, gbc);

        // 3rd level: (Password label | Change Password button)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 10, 20, 10);  // Reset insets to normal
        gbc.anchor = GridBagConstraints.EAST;  // Align right for the label
        centerPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;  // Align left for the button
        centerPanel.add(changePasswordButton, gbc);

        // 4th level: (Phone Number label | Change Phone button)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;  // align  right for the label
        centerPanel.add(phoneNumberLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;  // align left for the button
        centerPanel.add(changePhoneButton, gbc);

        return centerPanel;
    }


    private GradientPanel createBottomPanel(){
        // Bottom panel
        GradientPanel bottomPanel = new GradientPanel();
        bottomPanel.setBackground(CustomColor.getBackgroundColor());

        return bottomPanel;
    }
}