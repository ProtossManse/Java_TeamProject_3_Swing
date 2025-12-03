import manager.UserManager;
import screen.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class App extends JFrame {
    Container frame = getContentPane();
    JPanel centerPanel = new JPanel();
    UserManager userManager = new UserManager();

    App() {
        setSize(700, 300);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle("3조 단어장 앱");
        setLocationRelativeTo(null);
        onLaunch();
        initComponents();
        setVisible(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onDispose();
            }
        });


    }


    private void onLaunch() {
        initFileSystem();
        userManager.loadUser();
        showLoginDialog();

    }

    private void initFileSystem() {
        try {
            File vocasDir = new File(data.Path.PUBLIC_VOCAS_DIR);
            if (!vocasDir.exists()) {
                vocasDir.mkdirs();
            }

            File usersFile = new File(data.Path.USERS_TXT_PATH);
            if (!usersFile.exists()) {
                usersFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "파일 시스템 초기화 오류", "ERROR", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void initComponents() {
        JLabel userLabel = new JLabel(userManager.getCurrentUser().getName() + "님, " + userManager.getCurrentUser().getStreak() + "일 연속 공부중입니다!", SwingConstants.CENTER);
        this.add(userLabel, BorderLayout.NORTH);

        JButton vocMenuButton = new JButton("개인 단어장 관리");
        JButton publicVocMenuButton = new JButton("공용 단어장 관리");
        JButton quizMenuButton = new JButton("퀴즈 풀기");
        JButton noteManagerButton = new JButton("오답노트 관리");
        JButton favoriteButton = new JButton("즐겨찾기");

        vocMenuButton.addActionListener(e -> {
            VocManageDialog vocManageDialog = new VocManageDialog(this, "단어장 관리", userManager.getCurrentUser());
            vocManageDialog.setVisible(true);
        });

        publicVocMenuButton.addActionListener(e -> {
            PublicVocManageDialog publicVocManageDialog = new PublicVocManageDialog(this, userManager.getCurrentUser());
            publicVocManageDialog.setVisible(true);
        });

        quizMenuButton.addActionListener(e -> {
            QuizDialog quizDialog = new QuizDialog(this, userManager.getCurrentUser());
            quizDialog.setVisible(true);
        });

        noteManagerButton.addActionListener(e -> {
            NoteDialog noteDialog = new NoteDialog(this, userManager.getCurrentUser());
            noteDialog.setVisible(true);
        });

        favoriteButton.addActionListener(e -> {
            FavoriteDialog favoriteDialog = new FavoriteDialog(this, userManager.getCurrentUser());
            favoriteDialog.setVisible(true);
        });


        centerPanel.add(vocMenuButton);
        centerPanel.add(publicVocMenuButton);
        centerPanel.add(quizMenuButton);
        centerPanel.add(noteManagerButton);
        centerPanel.add(favoriteButton);
        frame.add(centerPanel, BorderLayout.CENTER);

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
