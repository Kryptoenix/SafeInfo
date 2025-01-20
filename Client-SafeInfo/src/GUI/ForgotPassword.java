package GUI;

import Service.Controller;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import Design.*;
import Utils.*;


public class ForgotPassword extends JPanel {
    private JButton goBackButton;
    private JLabel forgotPasswordLabel;
    private JTextField usernameField;
    private JButton submitButton;
    private JLabel errorLabel;
    private String username;

    public ForgotPassword(){

        setSize(800, 600);
        setLayout(new BorderLayout());

        initComponents();
        setUpLayout();

    }

    private void initComponents(){
        // Go Back button
        goBackButton = CustomJButton.createButton("Go Back");
        goBackButton.addActionListener(e -> Main.switchPanel("Home"));

        // Forgot Password label
        forgotPasswordLabel = new CustomLabel("Forgot Password");

        // Username text field
        usernameField = new CustomTextField("Username",150);

        // Submit button
        submitButton = new CustomJButton("Submit","rect");
        submitButton.addActionListener(e->{
            username = usernameField.getText();
            Controller.processRequest(
                    Controller.ServiceID.ForgotPasswordRequest,
                    (error) ->{
                        if (!error.isEmpty()){
                            ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                            errorDialog.setVisible(true);
                        }
                    },
                    username
            );
        });

        // bind the enter key to trigger the forgot password action
        InputMap inputMap = submitButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = submitButton.getActionMap();

        // map the enter key to the "forgotPasswordAction" command
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"forgotPasswordAction");

        // define action for "forgotPasswordAction"
        actionMap.put("forgotPasswordAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                username = usernameField.getText();
                Controller.processRequest(
                        Controller.ServiceID.ForgotPasswordRequest,
                        (error) ->{
                            if (!error.isEmpty()){
                                ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                                errorDialog.setVisible(true);
                            }
                        },
                        username
                );
            }
        });

        // Error label (hidden by default)
        errorLabel = new CustomLabel("Error");
        errorLabel.setForeground(Color.RED);
        errorLabel.setVisible(false);
    }

    private void setUpLayout(){
        JPanel topPanel = createTopPanel();
        JPanel centerPanel = createCenterPanel();
        JPanel bottomPanel = createBottomPanel();

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

    private GradientPanel createCenterPanel(){
        GradientPanel centerPanel = new GradientPanel();
        centerPanel.setBackground(CustomColor.getBackgroundColor());


        GridBagLayout gridBagLayout = new GridBagLayout();
        centerPanel.setLayout(gridBagLayout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);

        gbc.gridx = 0;
        gbc.gridy = 0;

        centerPanel.add(forgotPasswordLabel,gbc);
        gbc.gridy = 1;

        centerPanel.add(errorLabel,gbc);

        gbc.insets = new Insets(50, 0, 10, 0);
        centerPanel.add(usernameField,gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 10, 0);
        centerPanel.add(submitButton,gbc);

        return centerPanel;
    }

    private GradientPanel createBottomPanel(){
        // Bottom panel
        GradientPanel bottomPanel = new GradientPanel();
        bottomPanel.setBackground(CustomColor.getBackgroundColor());

        return bottomPanel;
    }

}