package GUI;

import Service.Controller;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import Design.*;
import Utils.*;

public class NewPassword extends JPanel {

    // Password label
    private JPasswordField passwordField;

    // Password Confirmation label
    private JPasswordField passwordConfirmField;

    // change button
    private JButton changeButton;

    // New Phone Number label
    private JLabel newPasswordLabel;

    // Go Back button
    private JButton goBackButton;

    // Logout button
    private JButton logoutButton;

    private void initComponents() {

        passwordField = new CustomPasswordField("Password",150);

        passwordConfirmField = new CustomPasswordField("Confirm Password",150);

        changeButton = new CustomJButton("Change Password","rect",130,50);
        changeButton.addActionListener(e-> {

            // check if password = confirmation password
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(passwordConfirmField.getPassword());
            if(password.equals(confirmPassword)) {
                Controller.processRequest(
                        Controller.ServiceID.NewPasswordRequest,
                        (error) ->{
                            if (!error.isEmpty()){
                                ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                                errorDialog.setVisible(true);
                            }else{
                                Main.switchPanel("Settings");
                            }
                        },
                        password
                );
            }
            else{
                ShowErrorMessage error = new ShowErrorMessage(Main.mainFrame, "Password and confirmation password do not match");
                error.setVisible(true);
            }

        });

        // bind the enter key to trigger the change password action
        InputMap inputMap = changeButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = changeButton.getActionMap();

        // map the enter key to the "changePasswordAction" command
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"changePasswordAction");

        // define action for "changePasswordAction"
        actionMap.put("changePasswordAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String password = new String(passwordField.getPassword());
                Controller.processRequest(
                        Controller.ServiceID.NewPasswordRequest,
                        (error) ->{
                            if (!error.isEmpty()){
                                ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                                errorDialog.setVisible(true);
                            }else{
                                Main.switchPanel("Settings");
                            }
                        },
                        password
                );

            }
        });

        newPasswordLabel = new CustomLabel("New Password",50);

        goBackButton = CustomJButton.createButton("Go Back");
        goBackButton.addActionListener(e-> Main.switchPanel("Settings"));

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
        });
    }

    public NewPassword() {

        setSize(800, 600);
        setLayout(new BorderLayout());

        initComponents();
        setUpLayout();
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
        centerPanel.setBackground(CustomColor.getBackgroundColor());
        GridBagLayout gridBagLayout = new GridBagLayout();
        centerPanel.setLayout(gridBagLayout);
        GridBagConstraints gbc= new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.insets = new Insets(10,0,50,0);
        centerPanel.add(newPasswordLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 10, 0);
        centerPanel.add(passwordField,gbc);
        gbc.gridy = 2;
        centerPanel.add(passwordConfirmField,gbc);
        gbc.gridy = 3;
        centerPanel.add(changeButton,gbc);

        return centerPanel;
    }

    private GradientPanel createBottomPanel(){
        // Bottom panel
        GradientPanel bottomPanel = new GradientPanel();
        bottomPanel.setBackground(CustomColor.getBackgroundColor());

        return bottomPanel;
    }

}
