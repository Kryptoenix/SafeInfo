package GUI;

import Service.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import Design.*;
import Utils.*;


public class NewPhoneNumber extends JPanel {
    private JLabel phoneNumberLabel;

    // Go Back button
    private JButton goBackButton;

    // Logout button
    private JButton logoutButton;

    // Password field
    private JPasswordField paswsordField;

    // New Phone Number field
    private JTextField newPhoneNumberField;

    // change button
    private JButton changeButton;

    private void initComponents(){

        phoneNumberLabel = new CustomLabel("Phone Number",50);

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

        paswsordField = new CustomPasswordField("Password",150);

        newPhoneNumberField = new CustomTextField("New Phone Number",150);

        changeButton = new CustomJButton("Change Phone","rect",150);
        changeButton.addActionListener(e-> {

            String password =new String(paswsordField.getPassword());
            String newPhone = newPhoneNumberField.getText();

            Controller.processRequest(
                    Controller.ServiceID.NewPhoneRequest,
                    (error) ->{
                        if (!error.equals("success")){
                            ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                            errorDialog.setVisible(true);
                        }else{
                            Main.switchPanel("Settings");
                        }
                    },
                    password,
                    newPhone
            );
        });

        // bind the enter key to trigger the change phone action
        InputMap inputMap = changeButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = changeButton.getActionMap();

        // map the enter key to the "changePhoneAction" command
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"changePhoneAction");

        // define action for "changePhoneAction"
        actionMap.put("changePhoneAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String password =new String(paswsordField.getPassword());
                String newPhone = newPhoneNumberField.getText();

                Controller.processRequest(
                        Controller.ServiceID.NewPhoneRequest,
                        (error) ->{
                            if (!error.equals("success")){
                                ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                                errorDialog.setVisible(true);
                            }else{
                                Main.switchPanel("Settings");
                            }
                        },
                        password,
                        newPhone
                );
            }
        });


    }

    public NewPhoneNumber() {

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
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.insets = new Insets(10, 0, 50, 0);
        centerPanel.add(phoneNumberLabel, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 10, 0);
        centerPanel.add(paswsordField,gbc);
        gbc.gridy = 2;
        centerPanel.add(newPhoneNumberField,gbc);
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