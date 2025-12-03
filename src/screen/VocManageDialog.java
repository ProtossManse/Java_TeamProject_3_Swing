package screen;

import manager.FileManager;
import model.User;
import data.Path;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

public class VocManageDialog extends JDialog {
    protected DefaultTableModel tableModel;
    protected JTable wordTable;
    protected JPanel bottomPanel;
    protected User currentUser;
    protected File initialDirectory;
    protected File currentFile; // 현재 작업 중인 파일

    // 자식 클래스(FavoriteDialog)에서 제어하기 위해 protected 필드로 변경
    protected JButton fileBtn;
    protected JButton newFileBtn;

    public VocManageDialog(JFrame parent, String title, User currentUser) {
        super(parent, title, true);
        this.currentUser = currentUser;
        setSize(850, 500);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);
        setInitialDir();
        initComponent();
    }

    protected void setInitialDir() {
        initialDirectory = new File("res/" + currentUser.getName() + "/vocas");
        if (!initialDirectory.exists()) {
            boolean created = initialDirectory.mkdirs();
            if (!created) {
                System.err.println("경고: 단어장 디렉토리 생성에 실패했습니다! 경로: " + initialDirectory.getPath());
            }
        }
    }

    protected void initComponent() {
        String[] header = {"영단어", "뜻"};

        tableModel = new DefaultTableModel(header, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 편집 불가
            }
        };

        wordTable = new JTable(tableModel);
        wordTable.setRowHeight(25);
        wordTable.getTableHeader().setReorderingAllowed(false);
        wordTable.getTableHeader().setResizingAllowed(true);
        wordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(wordTable);
        add(scrollPane, BorderLayout.CENTER);

        bottomPanel = new JPanel();

        // 필드로 승격된 버튼 초기화
        fileBtn = new JButton("파일 열기");
        newFileBtn = new JButton("파일 생성");

        JButton addBtn = new JButton("단어 추가");
        JButton editBtn = new JButton("단어 수정");
        JButton delBtn = new JButton("단어 삭제");
        JButton searchBtn = new JButton("단어 검색");
        JButton favBtn = new JButton("즐겨찾기 등록/해제");
        JButton closeBtn = new JButton("저장 및 종료");

        // --- 이벤트 리스너 연결 ---
        fileBtn.addActionListener(e -> openFile());
        newFileBtn.addActionListener(e -> createFile());
        addBtn.addActionListener(e -> addWord());
        editBtn.addActionListener(e -> editWord());
        delBtn.addActionListener(e -> deleteWord());
        searchBtn.addActionListener(e -> searchWord());
        favBtn.addActionListener(e -> addToFavorites());
        closeBtn.addActionListener(e -> dispose());

        bottomPanel.add(fileBtn);
        bottomPanel.add(newFileBtn);
        bottomPanel.add(addBtn);
        bottomPanel.add(editBtn);
        bottomPanel.add(delBtn);
        bottomPanel.add(searchBtn);
        bottomPanel.add(favBtn);
        bottomPanel.add(closeBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // 1. 파일 열기
    protected void openFile() {
        JFileChooser fileChooser = new JFileChooser(initialDirectory);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(".txt 파일", "txt");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            this.setTitle("단어장 관리 - " + currentFile.getName());
            refreshTable(); // 테이블 갱신
        }
    }

    // 2. 파일 생성
    protected void createFile() {
        String name = JOptionPane.showInputDialog(this, "새 단어장 이름을 입력하세요:");
        if (name == null || name.trim().isEmpty()) return;
        if (!name.endsWith(".txt")) name += ".txt";

        File newFile = new File(initialDirectory, name);
        try {
            if (newFile.createNewFile()) {
                JOptionPane.showMessageDialog(this, "생성 완료: " + name);
                currentFile = newFile;
                this.setTitle("단어장 관리 - " + currentFile.getName());
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "이미 존재하는 파일입니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 3. 단어 추가 (중복 체크 및 뜻 추가 로직 적용)
    protected void addWord() {
        if (currentFile == null) {
            JOptionPane.showMessageDialog(this, "파일을 먼저 열거나 생성해주세요.");
            return;
        }

        JTextField engField = new JTextField();
        JTextField korField = new JTextField();
        Object[] message = {"영단어:", engField, "뜻:", korField};

        int option = JOptionPane.showConfirmDialog(this, message, "단어 추가", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String eng = engField.getText().trim();
            String kor = korField.getText().trim();

            if (eng.isEmpty() || kor.isEmpty()) {
                JOptionPane.showMessageDialog(this, "값을 입력해주세요.");
                return;
            }
            if (eng.contains("\t") || kor.contains("\t")) {
                JOptionPane.showMessageDialog(this, "탭 문자는 사용할 수 없습니다.");
                return;
            }

            // 중복 체크 및 뜻 병합 로직
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String existingEng = (String) tableModel.getValueAt(i, 0);
                String existingKor = (String) tableModel.getValueAt(i, 1);

                if (existingEng.equals(eng)) {
                    if (existingKor.equals(kor)) {
                        JOptionPane.showMessageDialog(this, "이미 존재하는 단어입니다.");
                        return;
                    } else {
                        // 영단어는 같지만 뜻이 다르면 뜻 추가
                        String newMeaning = existingKor + ", " + kor;
                        tableModel.setValueAt(newMeaning, i, 1);
                        saveTableDataToFile(); // 변경된 내용 파일에 반영
                        JOptionPane.showMessageDialog(this, "기존 단어에 뜻이 추가되었습니다.");
                        return;
                    }
                }
            }

            // 중복이 없으면 새로 추가
            tableModel.addRow(new Object[]{eng, kor});
            saveTableDataToFile(); // 전체 저장 (덮어쓰기)
        }
    }

    // 4. 단어 수정 (유효성 검사 추가)
    protected void editWord() {
        if (currentFile == null) return;
        int row = wordTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "수정할 단어를 선택하세요.");
            return;
        }

        String oldEng = (String) tableModel.getValueAt(row, 0);
        String oldKor = (String) tableModel.getValueAt(row, 1);

        JTextField engField = new JTextField(oldEng);
        JTextField korField = new JTextField(oldKor);
        Object[] message = {"영단어:", engField, "뜻:", korField};

        if (JOptionPane.showConfirmDialog(this, message, "수정", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String newEng = engField.getText().trim();
            String newKor = korField.getText().trim();

            // 유효성 검사 추가
            if (newEng.isEmpty() || newKor.isEmpty()) {
                JOptionPane.showMessageDialog(this, "값을 입력해주세요.");
                return;
            }
            if (newEng.contains("\t") || newKor.contains("\t")) {
                JOptionPane.showMessageDialog(this, "탭 문자는 사용할 수 없습니다.");
                return;
            }

            tableModel.setValueAt(newEng, row, 0);
            tableModel.setValueAt(newKor, row, 1);
            saveTableDataToFile(); // 전체 저장
        }
    }

    // 5. 단어 삭제
    protected void deleteWord() {
        if (currentFile == null) return;
        int row = wordTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 단어를 선택하세요.");
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            tableModel.removeRow(row);
            saveTableDataToFile(); // 전체 저장
        }
    }

    // 6. 단어 검색
    protected void searchWord() {
        String query = JOptionPane.showInputDialog(this, "검색어:");
        if (query == null) return;
        query = query.toLowerCase();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String eng = ((String) tableModel.getValueAt(i, 0)).toLowerCase();
            String kor = ((String) tableModel.getValueAt(i, 1)).toLowerCase();
            if (eng.contains(query) || kor.contains(query)) {
                wordTable.setRowSelectionInterval(i, i);
                wordTable.scrollRectToVisible(new Rectangle(wordTable.getCellRect(i, 0, true)));
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "결과 없음");
    }

    // 7. 즐겨찾기 등록/해제
    protected void addToFavorites() {
        int row = wordTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "단어를 선택하세요.");
            return;
        }
        String eng = (String) tableModel.getValueAt(row, 0);
        String kor = (String) tableModel.getValueAt(row, 1);
        String favPath = Path.getUserFavoriteVocaFilePath(currentUser.getName());
        String targetLine = eng + "\t" + kor;

        String content = FileManager.read(favPath);

        if (content.contains(targetLine)) {
            // 있으면 -> 해제 (삭제)
            String[] lines = content.split("\\r?\\n");
            StringBuilder sb = new StringBuilder();
            boolean removed = false;
            for (String line : lines) {
                if (!line.trim().equals(targetLine.trim())) {
                    sb.append(line).append("\n");
                } else {
                    removed = true;
                }
            }
            if (removed) {
                FileManager.write(favPath, sb.toString(), false);
                JOptionPane.showMessageDialog(this, "즐겨찾기 해제됨");
            }
        } else {
            // 없으면 -> 등록 (추가)
            FileManager.write(favPath, targetLine + "\n", true);
            JOptionPane.showMessageDialog(this, "즐겨찾기 등록됨");
        }
    }

    // [헬퍼] 테이블 갱신 (자식 클래스 사용을 위해 protected로 변경)
    protected void refreshTable() {
        tableModel.setRowCount(0);
        String content = FileManager.read(currentFile.getPath());
        if (content.isEmpty()) return;
        for (String line : content.split("\\r?\\n")) {
            String[] parts = line.split("\t");
            if (parts.length >= 2) tableModel.addRow(new Object[]{parts[0], parts[1]});
        }
    }

    // [헬퍼] 파일 덮어쓰기
    private void saveTableDataToFile() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            sb.append(tableModel.getValueAt(i, 0)).append("\t")
                    .append(tableModel.getValueAt(i, 1)).append("\n");
        }
        FileManager.write(currentFile.getPath(), sb.toString(), false);
    }
}