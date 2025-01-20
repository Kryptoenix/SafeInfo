package GUI;

import Service.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import Design.*;
import Utils.*;


public class Registration extends JPanel {

    private JButton goBackButton;
    private  JLabel registrationLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField passwordConfirmField;
    private JTextField phoneNumberField;
    private JButton createAccountButton;
    private JLabel errorLabel;

    public Registration() {

        setSize(800, 600);
        setLayout(new BorderLayout()); // Set layout for the main frame


        initComponents();
        setUpLayout();
    }

    private void initComponents() {
        goBackButton = CustomJButton.createButton("Go Back");
        goBackButton.addActionListener(e -> Main.switchPanel("Home"));

        // Registration label
        registrationLabel =  new CustomLabel("Registration");

        // Username text field
        usernameField = new CustomTextField("Enter username",170,45);

        // Password text field
        passwordField = new CustomPasswordField("Enter password",170,45);

        // Password confirmation text field
        passwordConfirmField = new CustomPasswordField("Confirm password",170,45);
        // Phone number text field
        phoneNumberField = new CustomTextField("Enter phone number",170,45);

        createAccountButton = new CustomJButton("Create Account","rect",150,50);
        createAccountButton.addActionListener(e->{
            String username = usernameField.getText();
            String password = String.valueOf(passwordField.getPassword());
            String passwordConfirm = String.valueOf(passwordConfirmField.getPassword());
            String phoneNumber = phoneNumberField.getText();


            Controller.processRequest(
                    Controller.ServiceID.RegisterRequest,
                    (error) ->{
                        if (!error.isEmpty()){
                            ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                            errorDialog.setVisible(true);
                        }
                    },
                    username,
                    password,
                    passwordConfirm,
                    phoneNumber
            );
        });

        // bind the enter key to trigger the registration action
        InputMap inputMap = createAccountButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = createAccountButton.getActionMap();

        // map the enter key to the "registrationAction" command
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"registrationAction");

        // define action for "registrationAction"
        actionMap.put("registrationAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = String.valueOf(passwordField.getPassword());
                String passwordConfirm = String.valueOf(passwordConfirmField.getPassword());
                String phoneNumber = phoneNumberField.getText();


                Controller.processRequest(
                        Controller.ServiceID.RegisterRequest,
                        (error) ->{
                            if (!error.isEmpty()){
                                ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                                errorDialog.setVisible(true);
                            }
                        },
                        username,
                        password,
                        passwordConfirm,
                        phoneNumber
                );
            }
        });



        // Error label (hidden by default)
        errorLabel = new CustomLabel("Error");
        errorLabel.setForeground(Color.RED);
        errorLabel.setVisible(false);
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
        GradientPanel topPanel = new GradientPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBackground(CustomColor.getBackgroundColor());


        topPanel.add(goBackButton, BorderLayout.WEST);

        return topPanel;
    }

    private GradientPanel createCenterPanel() {
        // Center panel
        GradientPanel centerPanel = new GradientPanel();
        centerPanel.setBackground(CustomColor.getBackgroundColor());
        GridBagLayout gridBagLayout = new GridBagLayout();
        centerPanel.setLayout(gridBagLayout);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 0, 5, 0);  // Add some vertical spacing

        gbc.gridx = 0;
        gbc.gridy = 0;

        // Add components
        centerPanel.add(registrationLabel, gbc);
        gbc.gridy = 1;
        centerPanel.add(errorLabel, gbc);

        gbc.insets = new Insets(50, 0, 5, 0);
        gbc.gridy = 2;
        centerPanel.add(usernameField, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(15, 0, 5, 0);
        centerPanel.add(passwordField, gbc);

        gbc.gridy = 4;
        centerPanel.add(passwordConfirmField, gbc);

        // Adjust phone number fields
        gbc.gridy = 5;
        centerPanel.add(phoneNumberField, gbc);

        // Reset for the next component
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;  // Make sure the button spans both columns
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(createAccountButton, gbc);

        return centerPanel;
    }


    private GradientPanel createBottomPanel(){
        // bottom panel
        GradientPanel bottomPanel = new GradientPanel();
        bottomPanel.setBackground(CustomColor.getBackgroundColor());

        return  bottomPanel;
    }
}