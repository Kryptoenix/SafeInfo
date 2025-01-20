package GUI;

import Service.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import Design.*;
import Utils.*;


class Login extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private String username;
    private String password;
    private JButton loginButton;
    private JLabel loginLabel;
    private JLabel messLabel;
    private JLabel mess2Label;

    public Login() {
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Initialize components and layout
        initComponents();
        setupLayout();
    }


    // Initialize all components
    private void initComponents() {
        loginLabel = new CustomLabel("Welcome to SafeInfo");

        messLabel = new CustomLabel("Chat&Relax",40);
        mess2Label = new CustomLabel("Login",40);


        // Text fields
        usernameField = new CustomTextField("Enter username",160,45);
        passwordField = new CustomPasswordField("Enter password",160,45);


        loginButton = new CustomJButton("Login","rect",160,50);
        loginButton.addActionListener(e->{
            username = usernameField.getText();
            password = new String(passwordField.getPassword());

            Controller.processRequest(
                    Controller.ServiceID.LoginRequest,
                    (error) ->{
                        if (!error.isEmpty()){
                            ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                            errorDialog.setVisible(true);
                        }
                    },
                    username,
                    password
            );
        });

        // bind the enter key to trigger the login action
        InputMap inputMap = loginButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = loginButton.getActionMap();

        // map the enter key to the "loginAction" command
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"loginAction");

        // define action for "loginAction"
        actionMap.put("loginAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                Controller.processRequest(
                        Controller.ServiceID.LoginRequest,
                        (error) ->{
                            if (!error.isEmpty()){
                                ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                                errorDialog.setVisible(true);
                            }
                        },
                        username,
                        password
                );
            }
        });

        JButton signupButton = new CustomJButton("Signup", "rect", 200, 45);
        signupButton.addActionListener(e -> Main.switchPanel("Registration"));

        JButton forgotPasswordButton = new CustomJButton("ForgotPassword", "rect", 200, 45);
        forgotPasswordButton.addActionListener(e -> Main.switchPanel("ForgotPassword"));

    }

    private void setupLayout() {
        GradientPanel topPanel = createTopPanel();
        GradientPanel centerPanel = createCenterPanel();
        GradientPanel bottomPanel = createBottomPanel();

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private GradientPanel createTopPanel() {

        GradientPanel topPanel = new GradientPanel();
        topPanel.setBackground(CustomColor.getBackgroundColor());

        topPanel.add(loginLabel);

        return topPanel;
    }

    private GradientPanel createCenterPanel() {

        GradientPanel centerPanel = new GradientPanel();
        centerPanel.setLayout(new GridBagLayout());
        centerPanel.setBackground(CustomColor.getBackgroundColor());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);

        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(messLabel,gbc);

        gbc.gridy=1;
        gbc.insets = new Insets(20, 0, 10, 0);
        centerPanel.add(mess2Label,gbc);


        gbc.gridy = 2;
        gbc.insets = new Insets(35, 0, 10, 0);
        centerPanel.add(usernameField, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(5, 0, 5, 0);

        centerPanel.add(passwordField, gbc);

        gbc.gridy = 4;

        gbc.insets = new Insets(15, 0, 5, 0);

        centerPanel.add(loginButton, gbc);

        return centerPanel;
    }

    private GradientPanel createBottomPanel() {
        GradientPanel bottomPanel = new GradientPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // horizontal gap of 10px between buttons
        bottomPanel.setBackground(CustomColor.getBackgroundColor());

        JButton signupButton = new CustomJButton("Signup", "rect");
        signupButton.setPreferredSize(new Dimension(140, 50));
        signupButton.addActionListener(e -> Main.switchPanel("Registration"));

        JButton forgotPasswordButton = new CustomJButton("ForgotPassword", "rect");
        forgotPasswordButton.setPreferredSize(new Dimension(140, 50));
        forgotPasswordButton.addActionListener(e -> Main.switchPanel("ForgotPassword"));

        bottomPanel.add(signupButton);
        bottomPanel.add(forgotPasswordButton);

        return bottomPanel;
    }

}