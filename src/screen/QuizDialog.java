package screen;

import data.Path;
import model.User;
import model.Word;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class QuizDialog extends JDialog {

    protected User currentUser;

    public QuizDialog(JFrame parent, User currentUser) {
        super(parent, "퀴즈 메뉴", true);
        this.currentUser = currentUser;

        setSize(500, 400);
        setLayout(new BorderLayout());
        initLayout();
        setLocationRelativeTo(parent);
    }

    private void initLayout() {

        // UI 디자인
        Container contentPane = getContentPane();

        JPanel northPanel = new JPanel();
        JPanel centerPanel = new JPanel();

        JLabel infoLabel = new JLabel("원하는 퀴즈를 선택하세요.");
        infoLabel.setFont(new Font("고딕체", Font.BOLD, 25));

        JButton personalwordBtn = new JButton("개인 단어장 퀴즈");
        JButton personalnoteBtn = new JButton("개인 오답노트 퀴즈");
        JButton personalfavBtn = new JButton("즐겨찾기 단어 퀴즈");
        JButton publicwordBtn = new JButton("공용 단어장 퀴즈");
        JButton publicfreqBtn = new JButton("많이 틀리는 단어 퀴즈");

        northPanel.add(infoLabel);

        centerPanel.add(personalwordBtn);
        centerPanel.add(personalnoteBtn);
        centerPanel.add(personalfavBtn);
        centerPanel.add(publicwordBtn);
        centerPanel.add(publicfreqBtn);

        contentPane.add(northPanel, "North");
        contentPane.add(centerPanel, "Center");

        // 버튼 이벤트

        personalwordBtn.addActionListener(
                new OpenFileAction(new File(Path.getUserVocasDirPath(currentUser.getName())), false)
        );

        personalnoteBtn.addActionListener(
                new OpenFileAction(new File(Path.getUserNotesDirPath(currentUser.getName())), false)
        );

        personalfavBtn.addActionListener(
                new OpenFileAction(new File(Path.getUserFavoriteVocaFilePath(currentUser.getName())), false)
        );

        publicwordBtn.addActionListener(
                new OpenFileAction(new File(Path.PUBLIC_DIR), true)
        );

        publicfreqBtn.addActionListener(
                new OpenFileAction(new File(Path.PUBLIC_DIR), true)
        );

    }

    private class OpenFileAction implements ActionListener {

        private final File directory;
        private final boolean isPublic;

        public OpenFileAction(File directory, boolean isPublic) {
            this.directory = directory;
            this.isPublic = isPublic;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(directory);
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    ".txt 파일",
                    "txt");
            fileChooser.setFileFilter(filter);
            int result = fileChooser.showOpenDialog(QuizDialog.this);

            if(result != JFileChooser.APPROVE_OPTION){
                JOptionPane.showMessageDialog(null,
                        "파일을 선택하지 않았습니다!", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String filePath = fileChooser.getSelectedFile().getPath();
            ArrayList<String> strings = loadWordsFromFile(filePath);

            if (strings == null || strings.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "파일에 단어가 존재하지 않습니다!",
                        "경고", JOptionPane.WARNING_MESSAGE);
            }else{
                setQuiz(strings, isPublic);
            }
        }
    }

    private void setQuiz(ArrayList<String> strings, boolean isPublic) {
        ArrayList<Word> words = new ArrayList<>();
        for (String line : strings) {
            String[] parts = line.split("\t");
            words.add(new Word(parts[0].trim(), parts[1].trim()));
        }

        Object[] options = {"주관식 퀴즈", "객관식 퀴즈"};

        int optionResult = JOptionPane.showOptionDialog(
                null,
                "문제 유형을 선택해주세요.",
                "문제 유형",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if(optionResult==0){
            shortAnswerQuestion(words, isPublic);
        }else if (optionResult==1){
            multipleChoiceQuestion(words, isPublic);
        }else if (optionResult==-1){
            JOptionPane.showMessageDialog(null,
                    "문제 유형을 선택하지 않았습니다!",
                    "경고", JOptionPane.WARNING_MESSAGE);
        }

    }


    private void shortAnswerQuestion(ArrayList<Word> words, boolean isPublic) {
        // TODO: 주관식 퀴즈
        System.out.println("주관식");
    }


    private void multipleChoiceQuestion(ArrayList<Word> words, boolean isPublic) {
        // TODO: 객관식 퀴즈
        System.out.println("객관식");
    }


    private ArrayList<String> loadWordsFromFile(String pathStr) {
        try {
            java.nio.file.Path p = java.nio.file.Paths.get(pathStr);
            ArrayList<String> out = new ArrayList<>();

            for (String line : java.nio.file.Files.readAllLines(p, StandardCharsets.UTF_8)) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("#"))
                    continue;
                int tab = t.indexOf('\t');
                if (tab <= 0 || tab == t.length() - 1)
                    continue;
                out.add(t);
            }
            return out;

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "파일을 읽는 중 오류가 발생했습니다!\n"+e.getMessage(),
                    "경고", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }
}
