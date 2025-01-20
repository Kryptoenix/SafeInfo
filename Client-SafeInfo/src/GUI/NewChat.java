package GUI;

import Service.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import java.awt.event.KeyEvent;

import Design.*;
import Utils.*;


public class NewChat extends JPanel {
    private  JLabel sendMessageLabel;
    private JButton goBackButton;
    private JButton logoutButton;
    private JButton okButton;
    private JTextField usernameField;

    private void initComponents(){

        // Send Message label
        sendMessageLabel = new CustomLabel("Send Message");

        // Go Back button
        goBackButton = CustomJButton.createButton("Go Back");
        goBackButton.addActionListener(e-> Main.switchPanel("User"));

        // Log out button
        logoutButton = CustomJButton.createButton("Logout");
        logoutButton.addActionListener(e -> {
            Main.switchPanel("Home");    // Switch the panel
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

        // OK button
        okButton = new CustomJButton("OK","rect");
        okButton.addActionListener(e->{
            String username = usernameField.getText();
            Controller.processRequest(
                    Controller.ServiceID.NewChat,
                    (error) ->{
                        if (!error.isEmpty()){
                            ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                            errorDialog.setVisible(true);
                        }
                    },
                    username
            );
            Main.switchPanel("Chat");
        });

        // bind the enter key to trigger the new chat action
        InputMap inputMap = okButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = okButton.getActionMap();

        // map the enter key to the "newChatAction" command
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"newChatAction");

        // define action for "newChatAction"
        actionMap.put("newChatAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                Controller.processRequest(
                        Controller.ServiceID.NewChat,
                        (error) ->{
                            if (!error.isEmpty()){
                                ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                                errorDialog.setVisible(true);
                            }
                        },
                        username
                );
                Main.switchPanel("Chat");
            }
        });

        // Username text field
        usernameField = new CustomTextField("Username",150);
    }

    public NewChat(){

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
        GridBagLayout gridBagLayout=new GridBagLayout();
        centerPanel.setLayout(gridBagLayout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);  // Add some vertical spacing

        gbc.gridx = 0;
        gbc.gridy = 0;

        centerPanel.add(sendMessageLabel,gbc);
        gbc.gridy=1;
        gbc.insets = new Insets(50, 0, 10, 0); // Increase top inset for extra space
        centerPanel.add(usernameField, gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 10, 0); // Increase top inset for extra space

        centerPanel.add(okButton, gbc);
        gbc.gridy = 3;

        return centerPanel;
    }

    private GradientPanel createBottomPanel(){
        GradientPanel bottomPanel = new GradientPanel();
        bottomPanel.setBackground(CustomColor.getBackgroundColor());

        return bottomPanel;
    }

}