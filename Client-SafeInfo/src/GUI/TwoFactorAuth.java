package GUI;

import Service.Controller;
import UserConfig.UserGlobals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import Design.*;
import Utils.*;


public class TwoFactorAuth extends JPanel {

    // SMS verification label
    private JLabel smsVerificationLabel;

    // phone message label
    private JLabel phoneMessageLabel;

    // Count digits for OTP code
    private int digitCount;

    // check if OTP was sent
    private boolean otpSent = false;

    // Go back Button
    private JButton goBackButton;

    // Resend OTP
    private JButton resendOTPButton;

    private String fullOtp="";

    public TwoFactorAuth() {

        setSize(800, 600);
        setLayout(new BorderLayout());


        initComponents();
        setUpLayout();
        Controller.processRequest(
                Controller.ServiceID.TwoFactorPhoneRequest,
                (error) ->{
                    if (!error.isEmpty()){
                        ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                        errorDialog.setVisible(true);
                    }
                }
        );
    }

    private void initComponents() {
        smsVerificationLabel = new CustomLabel("SMS Verification",50);

        phoneMessageLabel = new CustomLabel(" - We sent a verification code to "+ UserGlobals.getPhoneNumber()+" -",30);

        digitCount = 0;


        // Error label (hidden by default)
        // Error label
        JLabel errorLabel = new CustomLabel("Error");
        errorLabel.setForeground(Color.RED);
        errorLabel.setVisible(false);

        goBackButton = CustomJButton.createButton("Go Back");
        goBackButton.addActionListener(e -> Main.switchPanel("Home"));

        resendOTPButton = new CustomJButton("Resend OTP","rect");
        resendOTPButton.addActionListener(e -> Controller.processRequest(
                Controller.ServiceID.TwoFactorPhoneRequest,
                (error) ->{
                    if (!error.isEmpty()){
                        ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                        errorDialog.setVisible(true);
                    }
                }
        ));
    }
    private JTextField createOtpTextField() {
        JTextField textField = new CustomTextField("", 50, 70); // One character width
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setFont(new Font("Arial", Font.BOLD, 20));

        // Limit input to a single digit and handle focus transition
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) || !textField.getText().isEmpty()) {
                    e.consume(); // Prevent invalid input
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // Handle BACK_SPACE key to delete the last digit from fullOtp
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if (!textField.getText().isEmpty()) {
                        // Remove the last character from fullOtp when BACK_SPACE is pressed
                        if (!fullOtp.isEmpty()) {
                            fullOtp = fullOtp.substring(0, fullOtp.length() - 1);
                        }
                        digitCount--;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Automatically move to the next field if a digit is entered
                if (textField.getText().length() == 1 && Character.isDigit(e.getKeyChar())) {
                    fullOtp += textField.getText();
                    digitCount++;
                    textField.transferFocus();
                }

                if (digitCount == 6 && !otpSent) {
                    System.out.println("OTP sent!");
                     Controller.processRequest(
                                Controller.ServiceID.TwoFactorOTP,
                                (error) ->{
                                    if (!error.isEmpty()){
                                        ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                                        errorDialog.setVisible(true);
                                    }
                                },
                                fullOtp
                     );

                    otpSent = true;
                }
            }
        });

        return textField;
    }


    private void setUpLayout(){
        GradientPanel topPanel = createTopPanel();
        GradientPanel centerPanel = createCenterPanel();
        GradientPanel bottomPanel = createBottomPanel();

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private GradientPanel createTopPanel() {
        // Top panel
        GradientPanel topPanel = new GradientPanel();
        topPanel.setBackground(CustomColor.getBackgroundColor());
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        // Add 'go back' button on the left
        topPanel.add(goBackButton);

        // Add horizontal glue to push the next component (centered) towards the middle
        topPanel.add(Box.createHorizontalGlue());

        // Add the centered smsVerificationLabel
        topPanel.add(smsVerificationLabel);

        // Add horizontal glue to push the next component towards the right
        topPanel.add(Box.createHorizontalGlue());

        return topPanel;
    }


    private GradientPanel createCenterPanel() {
        // Center panel
        GradientPanel centerPanel = new GradientPanel();
        centerPanel.setBackground(CustomColor.getBackgroundColor());
        centerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Add phone message label at top
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 6; // Span across all 6 columns
        gbc.insets = new Insets(5, 5, 70, 5);
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(phoneMessageLabel, gbc);

        // Reset gridwidth for the OTP fields
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;

        // Create a JPanel to hold the OTP fields with FlowLayout
        GradientPanel otpPanel = new GradientPanel();
        otpPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5)); // Center aligned with gaps

        // Create and add OTP text fields
        JTextField[] otpFields = new JTextField[6];
        for (int i = 0; i < otpFields.length; i++) {
            otpFields[i] = createOtpTextField();
            otpFields[i].setPreferredSize(new Dimension(45, 65));  // Set preferred size for each OTP field
            otpPanel.add(otpFields[i]);
        }

        // Add the otpPanel to the centerPanel
        gbc.gridx = 0; // Reset gridx
        gbc.gridy = 1; // Set to the next row
        gbc.gridwidth = 6; // Span across all 6 columns for the panel
        centerPanel.add(otpPanel, gbc); // Add the panel containing the OTP fields

        return centerPanel;
    }


    private GradientPanel createBottomPanel(){
        // Bottom panel
        GradientPanel bottomPanel = new GradientPanel();
        bottomPanel.setBackground(CustomColor.getBackgroundColor());
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(resendOTPButton);

        return bottomPanel;
    }
}


