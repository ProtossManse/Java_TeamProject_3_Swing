import screen.LoginPanel;

import javax.swing.*;
import java.awt.*;

public class App extends JFrame {
    Container frame = getContentPane();
    JPanel CenterPanel;

    App() {
        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);


        showLoginDialog();


    }

    void showLoginDialog() {
        LoginPanel loginPanel = new LoginPanel();
        int result = JOptionPane.showConfirmDialog(
                null,
                loginPanel,
                "로그인",
                JOptionPane.OK_CANCEL_OPTION
        );

    }


}
