package screen;

import data.Path;
import model.PublicWord;
import model.User;
import model.Word;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class QuizDialog extends JDialog {

    static Random ran = new Random();

    ArrayList<String> noteWords = new ArrayList<>();
    protected User currentUser;

    private String currentFilePath;
    private ArrayList<Word> currentAllWords;

    int publicBtn = 0; //1 = publicwordBtn, 2 = publicfreqBtn
    int quizNum = 0;
    int score = 0;

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
        infoLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 25));

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

        contentPane.add(northPanel, BorderLayout.NORTH);
        contentPane.add(centerPanel, BorderLayout.CENTER);

        personalwordBtn.addActionListener(
                new OpenFileAction(new File(Path.getUserVocasDirPath(currentUser.getName())), false)
        );

        personalnoteBtn.addActionListener(
                new OpenFileAction(new File(Path.getUserNotesDirPath(currentUser.getName())), false)
        );

        personalfavBtn.addActionListener(
                new OpenFileAction(new File(Path.getUserFavoriteVocaFilePath(currentUser.getName())), false)
        );

        publicwordBtn.addActionListener(e -> {
            this.publicBtn = 1;
            new OpenFileAction(new File(Path.PUBLIC_DIR), true).actionPerformed(e);
        });

        publicfreqBtn.addActionListener(e -> {
            this.publicBtn = 2;
            new OpenFileAction(new File(Path.PUBLIC_DIR), true).actionPerformed(e);
        });

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

            if (result != JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(null,
                        "파일을 선택하지 않았습니다!", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getPath();

            if (isPublic) {
                QuizDialog.this.currentFilePath = filePath;
            } else {
                QuizDialog.this.currentFilePath = null;
            }

            ArrayList<String> strings = loadWordsFromFile(filePath);

            if (strings == null || strings.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "파일에 단어가 존재하지 않습니다!",
                        "경고", JOptionPane.WARNING_MESSAGE);
            } else {
                setQuiz(strings, isPublic);
            }
        }
    }

    private void setQuiz(ArrayList<String> strings, boolean isPublic) {
        ArrayList<Word> allWords = new ArrayList<>();
        ArrayList<Word> quizWords = new ArrayList<>();

        if (!isPublic) {
            for (String line : strings) {
                String[] parts = line.split("\t");
                if (parts.length >= 2) {
                    Word w = new Word(parts[0].trim(), parts[1].trim());
                    allWords.add(w);
                    quizWords.add(w);
                }
            }
        } else {
            for (String line : strings) {
                String[] parts = line.split("\t");
                if (parts.length < 2) continue;

                String eng = parts[0].trim();
                String kor = parts[1].trim();

                int total = 0;
                int correct = 0;

                if (parts.length >= 4) {
                    try {
                        total = Integer.parseInt(parts[2].trim());
                        correct = Integer.parseInt(parts[3].trim());
                    } catch (NumberFormatException ignored) {
                    }
                }

                PublicWord word = new PublicWord(eng, kor, total, correct);
                allWords.add(word);

                if (this.publicBtn == 1) {
                    // 공용 단어장 퀴즈
                    quizWords.add(word);
                } else if (this.publicBtn == 2) {
                    // 많이 틀리는 단어 퀴즈
                    if (total > 0 && word.getCorrectionRate() < 50) {
                        quizWords.add(word);
                    }
                }
            }

            if (this.publicBtn == 2 && quizWords.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "정답률 50% 미만인 단어가 없습니다.",
                        "알림", JOptionPane.PLAIN_MESSAGE);
                return;
            }
        }

        // 전체 단어 목록 저장 (나중에 파일 저장 시 사용)
        this.currentAllWords = allWords;

        // 퀴즈 시작
        quizMenu(quizWords, isPublic);
    }

    private void quizMenu(ArrayList<Word> words, boolean isPublic) {
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

        if (optionResult == 0) {
            shortAnswerQuestion(words, isPublic);
        } else if (optionResult == 1) {
            multipleChoiceQuestion(words, isPublic);
        } else if (optionResult == -1) {
            JOptionPane.showMessageDialog(null,
                    "문제 유형을 선택하지 않았습니다!",
                    "경고", JOptionPane.WARNING_MESSAGE);
        }
    }


    public class ShortAnswerDialog extends JDialog {

        JLabel questionLabel;
        JTextField answerField;
        JButton submitBtn;
        JLabel resultLabel;
        JLabel descriptionLabel;

        ArrayList<Word> words;
        ArrayList<Word> mutableWords;
        Word currentWord;

        String aKor, aEng;

        int i, randquiz;

        boolean isPublic;


        public ShortAnswerDialog(JDialog parent, ArrayList<Word> words, boolean isPublic,
                                 ArrayList<Word> mutableWords) {
            super(parent, "주관식 퀴즈", true);
            this.setLayout(new BorderLayout());

            this.words = words;
            this.mutableWords = mutableWords;
            this.isPublic = isPublic;

            this.setSize(700, 400);
            this.setLocationRelativeTo(parent);
            initLayout();
            showShortQuestion();
            this.setVisible(true);
        }

        private void initLayout() {

            questionLabel = new JLabel();
            if (words != null && !words.isEmpty()) {
                questionLabel.setText(words.get(0).getEnglish());
            }
            questionLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
            answerField = new JTextField(20);
            submitBtn = new JButton("제출");
            resultLabel = new JLabel();
            resultLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 24));
            descriptionLabel = new JLabel();
            descriptionLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 17));

            JPanel northPanel = new JPanel();
            northPanel.add(questionLabel);

            JPanel centerPanel = new JPanel();
            centerPanel.add(answerField);
            centerPanel.add(submitBtn);

            JPanel southPanel = new JPanel();
            southPanel.add(resultLabel);
            southPanel.add(descriptionLabel);


            this.add(northPanel, BorderLayout.NORTH);
            this.add(centerPanel, BorderLayout.CENTER);
            this.add(southPanel, BorderLayout.SOUTH);

            submitBtn.addActionListener(e -> submitShortAnswer());

        }

        private void submitShortAnswer() {
            if (this.randquiz == 1) {
                String answer = this.answerField.getText().trim();

                String[] userAnswers = answer.split("/");
                boolean isCorrect = true;

                for (String userAnswer : userAnswers) {
                    if (!this.currentWord.koreanList.contains(userAnswer.trim())) {
                        isCorrect = false;
                        break;
                    }
                }

                if (isCorrect) {
                    this.resultLabel.setText("정답!");
                    if (isPublic && currentWord instanceof PublicWord) {
                        ((PublicWord) currentWord).correct++;
                    }
                    score++;
                } else {
                    this.resultLabel.setText("오답!");
                    addToNote(aEng, aKor);

                    this.descriptionLabel.setText("정답은 " + aEng + " = " + getKorStr(this.currentWord.koreanList));
                }

            } else {
                String answer = this.answerField.getText().trim();

                if (answer.toLowerCase().equals(aEng)) {
                    this.resultLabel.setText("정답!");
                    if (isPublic && currentWord instanceof PublicWord) {
                        ((PublicWord) currentWord).correct++;
                    }
                    score++; //점수 증가
                } else {
                    this.resultLabel.setText("오답!");
                    addToNote(aEng, aKor);
                    this.descriptionLabel.setText("정답은 " + aEng + " = " + getKorStr(this.currentWord.koreanList));
                }
            }
            JOptionPane.showMessageDialog(null, "확인이 끝나셨다면 OK를 눌러주세요.",
                    "알림", JOptionPane.INFORMATION_MESSAGE);

            this.i++;
            showShortQuestion();
        }

        public void showShortQuestion() {
            if (mutableWords.isEmpty() || this.i >= quizNum) {

                JOptionPane.showMessageDialog(
                        null,
                        String.format("총 %d문제 중 %d개 정답 (정답률 %.1f%%)",
                                quizNum, score, 100.0 * score / quizNum)
                );
                if (isPublic) {
                    // 퀴즈가 끝났을 때 전체 단어 목록(currentAllWords)을 저장
                    updateStatistics(QuizDialog.this.currentAllWords);
                }
                createNote();
                this.dispose();
                return;
            }

            int randomIndex = ran.nextInt(mutableWords.size());
            this.currentWord = mutableWords.get(randomIndex);
            mutableWords.remove(randomIndex);

            this.aEng = this.currentWord.getEnglish();
            this.aKor = this.currentWord.getKorean();

            if (this.aKor.contains("/")) {
                String[] aKorArr = this.aKor.split("/");
                for (String kor : aKorArr) {
                    this.currentWord.koreanList.add(kor.trim());
                }
            } else {
                this.currentWord.koreanList.add(this.aKor);
            }

            if (isPublic && this.currentWord instanceof PublicWord) {
                ((PublicWord) this.currentWord).questions++;
            }

            this.randquiz = ran.nextInt(2) + 1;
            if (this.randquiz == 1) {

                this.questionLabel.setText("[" + (this.i + 1) + "/" + quizNum + "] " + this.aEng + "의 뜻은?");


            } else {

                String questionStr = getKorStr(this.currentWord.koreanList);

                this.questionLabel.setText("[" + (this.i + 1) + "/" + quizNum + "] '"
                        + questionStr + "'를(을) 영어로 하면?");
            }

            this.answerField.setText("");
            this.resultLabel.setText("");
            this.descriptionLabel.setText("");
            this.answerField.requestFocus();
        }
    }

    public class MultipleChoiceDialog extends JDialog {

        JLabel questionLabel;
        JRadioButton[] optionButtons;
        ButtonGroup buttonGroup;
        JButton submitBtn;
        JLabel resultLabel;
        JLabel descriptionLabel;
        ArrayList<Word> words;
        ArrayList<Word> mutableWords;
        ArrayList<Word> currentChoices;
        Word currentWord;
        String aEng;
        String aKor;
        int i;
        boolean isPublic;

        public MultipleChoiceDialog(JDialog parent, ArrayList<Word> words, boolean isPublic, ArrayList<Word> mutableWords) {
            super(parent, "객관식 퀴즈", true);
            this.words = words;
            this.mutableWords = mutableWords;
            this.isPublic = isPublic;

            this.setLayout(new BorderLayout());
            this.setSize(700, 400);
            this.setLocationRelativeTo(parent);

            initLayout();
            showMultipleQuestion();
            this.setVisible(true);
        }

        private void initLayout() {
            questionLabel = new JLabel();
            questionLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 20));

            optionButtons = new JRadioButton[4];
            buttonGroup = new ButtonGroup();

            JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
            for (int idx = 0; idx < 4; idx++) {
                optionButtons[idx] = new JRadioButton();
                optionButtons[idx].setFont(new Font("Malgun Gothic", Font.PLAIN, 17));
                buttonGroup.add(optionButtons[idx]);
                optionsPanel.add(optionButtons[idx]);
            }

            submitBtn = new JButton("제출");

            resultLabel = new JLabel();
            resultLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 24));

            descriptionLabel = new JLabel();
            descriptionLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 17));

            JPanel northPanel = new JPanel();
            northPanel.add(questionLabel);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.add(optionsPanel, BorderLayout.CENTER);

            JPanel southPanel = new JPanel(new BorderLayout());
            JPanel southTop = new JPanel();
            southTop.add(submitBtn);
            JPanel southBottom = new JPanel();
            southBottom.add(resultLabel);
            southBottom.add(descriptionLabel);

            southPanel.add(southTop, BorderLayout.NORTH);
            southPanel.add(southBottom, BorderLayout.SOUTH);

            this.add(northPanel, BorderLayout.NORTH);
            this.add(centerPanel, BorderLayout.CENTER);
            this.add(southPanel, BorderLayout.SOUTH);

            submitBtn.addActionListener(e -> submitMultipleChoice());
        }

        private void submitMultipleChoice() {
            // 아무 보기도 선택 안 했을 때
            if (buttonGroup.getSelection() == null) {
                JOptionPane.showMessageDialog(null, "보기를 선택해주세요!", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 선택한 보기 찾기
            int selectedIndex = -1;
            for (int idx = 0; idx < optionButtons.length; idx++) {
                if (optionButtons[idx].isSelected()) {
                    selectedIndex = idx;
                    break;
                }
            }

            if (selectedIndex == -1) return;

            int correctNum = -1;
            // 정답 번호 계산
            for (int k = 0; k < currentChoices.size(); k++) {
                if (currentChoices.get(k).equals(currentWord)) {
                    correctNum = k + 1;
                    break;
                }
            }

            Word selectedWord = currentChoices.get(selectedIndex);

            if (selectedWord.equals(currentWord)) {
                // 정답일 때
                resultLabel.setText("정답!");
                if (isPublic && currentWord instanceof PublicWord) {
                    ((PublicWord) currentWord).correct++;
                }
                score++;

                JOptionPane.showMessageDialog(null, "정답입니다!", "정답", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // 오답일 때
                resultLabel.setText("오답!");
                addToNote(aEng, aKor);

                String msg = "오답입니다!\n" +
                        "정답: [" + correctNum + "번] " + aEng + " : " + aKor;

                descriptionLabel.setText(msg);

                JOptionPane.showMessageDialog(null, msg, "오답", JOptionPane.ERROR_MESSAGE);
            }

            i++;
            showMultipleQuestion();
        }

        private void showMultipleQuestion() {
            // 더 이상 낼 단어가 없거나, i가 quizNum이상이면 종료
            if (mutableWords.isEmpty() || i >= quizNum) {
                JOptionPane.showMessageDialog(
                        null,
                        String.format("총 %d문제 중 %d개 정답 (정답률 %.1f%%)",
                                quizNum, score, 100.0 * score / quizNum)
                );
                if (isPublic) {
                    // 퀴즈가 끝났을 때 전체 단어 목록(currentAllWords)을 저장
                    updateStatistics(QuizDialog.this.currentAllWords);
                }
                createNote(); // 오답노트 파일 생성
                this.dispose();
                return;
            }

            int randomIndex = ran.nextInt(mutableWords.size());
            currentWord = mutableWords.get(randomIndex);
            mutableWords.remove(randomIndex);

            aEng = currentWord.getEnglish();
            aKor = currentWord.getKorean();

            if (isPublic && currentWord instanceof PublicWord) {
                ((PublicWord) currentWord).questions++;
            }

            // 보기 temp 리스트
            ArrayList<Word> temp = new ArrayList<>(words);
            temp.remove(currentWord);
            Collections.shuffle(temp, ran);

            // 정답 + 오답 3개
            currentChoices = new ArrayList<>();
            currentChoices.add(currentWord);
            if (temp.size() >= 3) {
                currentChoices.addAll(temp.subList(0, 3));
            } else {
                currentChoices.addAll(temp);
            }
            Collections.shuffle(currentChoices, ran);

            // 문제 출력
            questionLabel.setText(
                    "[" + (i + 1) + "/" + quizNum + "] " + aEng + "의 뜻은?"
            );

            // 보기 세팅 (보기 개수가 4개보다 적어질 가능성을 대비)
            for (int j = 0; j < 4; j++) {
                if (j < currentChoices.size()) {
                    String kor = currentChoices.get(j).getKorean();
                    String showKor = kor.contains("/") ? kor.split("/")[0].trim() : kor;
                    optionButtons[j].setText((j + 1) + ") " + showKor);
                    optionButtons[j].setVisible(true);
                } else {
                    optionButtons[j].setVisible(false);
                }
            }

            // 선택/메시지 초기화
            buttonGroup.clearSelection();
            resultLabel.setText("");
            descriptionLabel.setText("");
        }
    }


    private void shortAnswerQuestion(ArrayList<Word> words, boolean isPublic) {
        if (words == null) {
            JOptionPane.showMessageDialog(null, "단어가 등록되어 있지 않습니다!",
                    "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        while (true) {
            String input = JOptionPane.showInputDialog("출제할 문제 수를 입력하세요.");
            if (input == null) return; // 취소 처리
            try {
                this.quizNum = Integer.parseInt(input);
                if (this.quizNum < 1) {
                    JOptionPane.showMessageDialog(null, "1문제 이상 출제해야 합니다.", "경고", JOptionPane.WARNING_MESSAGE);
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "숫자를 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            }
        }
        if (this.quizNum > words.size()) {
            this.quizNum = words.size();
        }

        ArrayList<Word> mutableWords = new ArrayList<>(words);

        this.dispose();
        new ShortAnswerDialog(this, words, isPublic, mutableWords);
    }


    private void multipleChoiceQuestion(ArrayList<Word> words, boolean isPublic) {
        if (words == null || words.isEmpty()) {
            JOptionPane.showMessageDialog(null, "단어가 등록되어 있지 않습니다!", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (words.size() < 4) {
            JOptionPane.showMessageDialog(null, "객관식 보기를 만들 단어(4개)가 부족합니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        while (true) {
            String input = JOptionPane.showInputDialog("출제할 문제 수를 입력하세요:");
            if (input == null) return;
            try {
                this.quizNum = Integer.parseInt(input.trim());
                if (this.quizNum < 1) {
                    JOptionPane.showMessageDialog(null, "1문제 이상 출제해야 합니다.", "경고", JOptionPane.WARNING_MESSAGE);
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "숫자를 입력하세요!", "경고", JOptionPane.WARNING_MESSAGE);
            }
        }
        if (this.quizNum > words.size()) {
            this.quizNum = words.size();
        }

        ArrayList<Word> mutableWords = new ArrayList<>(words);
        this.score = 0;
        this.dispose();
        new MultipleChoiceDialog(this, words, isPublic, mutableWords);
    }


    // 오답노트

    private void addToNote(String aEng, String aKor) {
        String entry = aEng + "\t" + aKor;
        if (!noteWords.contains(entry)) {
            noteWords.add(entry);
        }
    }

    private void createNote() {
        if (noteWords.isEmpty()) {
            JOptionPane.showMessageDialog(null, "틀린 단어가 없습니다.",
                    "알림", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        File notes = new File(Path.getUserNotesDirPath(currentUser.getName()));
        if (!notes.exists()) notes.mkdirs(); // 디렉토리 없으면 생성

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HH_mm_ss");
        String formatted = now.format(formatter);

        File noteFile = new File(notes, "note-" + formatted + ".txt");

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(noteFile, false), StandardCharsets.UTF_8))) {
            for (String word : noteWords) {
                pw.println(word);
            }
            JOptionPane.showMessageDialog(null, "오답노트 생성 완료",
                    "알림", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "파일 저장 중 오류가 발생했습니다!\n" + e.getMessage(),
                    "경고", JOptionPane.WARNING_MESSAGE);
        }

        noteWords.clear();
    }

    //기타 등등

    private String getKorStr(ArrayList<String> aKorList) {
        String korStr = ""; //출력할 정답
        for (String kor : aKorList) {
            korStr += kor + "/"; //한국어/한국어2/ ... / 형식
        }
        if (korStr.length() > 0)
            return korStr.substring(0, korStr.length() - 1); //맨 마지막의 /를 지우기
        return "";
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
                    "파일을 읽는 중 오류가 발생했습니다!\n" + e.getMessage(),
                    "경고", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    private void updateStatistics(ArrayList<Word> list) {
        // 저장할 파일이나 데이터가 없으면 아무 것도 안 함
        if (currentFilePath == null || list == null || list.isEmpty()) {
            return;
        }

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(currentFilePath, false), StandardCharsets.UTF_8))) {

            for (Word word : list) {
                if (word instanceof PublicWord pwWord) {
                    pw.println(pwWord);
                }
            }
            JOptionPane.showMessageDialog(null, "단어 통계를 파일에 저장했습니다.",
                    "알림", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "단어 통계 저장 중 오류가 발생했습니다!\n" + e.getMessage(),
                    "경고", JOptionPane.WARNING_MESSAGE);
        }
    }
}
