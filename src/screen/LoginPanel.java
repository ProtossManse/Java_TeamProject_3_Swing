package screen;

import javax.swing.*;

public class LoginPanel extends JPanel {

    public LoginPanel() {
        JTextField userNameField = new JTextField(10);
        JPasswordField passwordField = new JPasswordField(10);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


        this.add(new JLabel("Username:"));
        this.add(userNameField);
        this.add(Box.createVerticalStrut(15));
        this.add(new JLabel("Password:"));
        this.add(passwordField);
    }

}
