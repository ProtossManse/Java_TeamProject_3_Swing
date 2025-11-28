import manager.UserManager;
import screen.LoginPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class App extends JFrame {
    Container frame = getContentPane();
    JPanel CenterPanel;
    UserManager userManager = new UserManager();

    App() {
        setSize(800, 800);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        onLaunch();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onDispose();
            }
        });


    }

    private void onLaunch() {
        userManager.loadUser();
        showLoginDialog();

    }

    private void onDispose() {
        userManager.saveUsers();
        System.exit(0);

    }

    private void showLoginDialog() {
        LoginPanel loginPanel = new LoginPanel();
        Object[] options = {"로그인", "회원가입"};
        int result = JOptionPane.showOptionDialog(
                null,
                loginPanel,
                "로그인",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );
        String username = loginPanel.getUsername();
        String password = loginPanel.getPassword();

        loginDialogLogic(result, username, password);

    }

    private void loginDialogLogic(int result, String username, String password) {
        if (result == JOptionPane.CLOSED_OPTION) {
            onDispose();
            return;
        }

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "값을 입력해주세요!");
            showLoginDialog();
            return;
        }

        String message = "";
        boolean needsRecur = false;

        if (result == JOptionPane.YES_OPTION) {
            if (userManager.checkUser(username, password)) {
                message = "로그인에 성공했습니다!";
                userManager.setCurrentUser(username, password);

            } else {
                message = "없는 회원입니다!";
                needsRecur = true;
            }

        } else if (result == JOptionPane.NO_OPTION) {
            if (!userManager.checkUser(username, password)) {
                message = "회원가입에 성공했습니다!";
                userManager.registerUser(username, password);
            } else {
                message = "이미 존재하는 회원입니다!";
                needsRecur = true;
            }
        }

        if (needsRecur) {
            JOptionPane.showMessageDialog(null, message);
            showLoginDialog(); // 재귀 호출 (다시 시도)
            return;
        }

        JOptionPane.showMessageDialog(null, message);
        userManager.getCurrentUser().setStreak();
    }
}
