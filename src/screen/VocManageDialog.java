package screen;

import manager.UserManager;
import model.User;

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

    public VocManageDialog(JFrame parent, String title, User currentUser) {
        super(parent, title, true);
        this.currentUser = currentUser;
        setSize(500, 400);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);
        initComponent();
        setInitialDir();

    }

    protected void setInitialDir() {
        initialDirectory = new File("res/" + currentUser.getName() + "/vocas");
    }

    protected void initComponent() {
        String[] header = {"영단어", "뜻"};

        tableModel = new DefaultTableModel(header, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 모든 셀 편집 불가
            }
        };

        // --- [2] JTable 생성 및 꾸미기 ---
        wordTable = new JTable(tableModel);

        // 스타일 설정
        wordTable.setRowHeight(25);
        wordTable.getTableHeader().setReorderingAllowed(false);
        wordTable.getTableHeader().setResizingAllowed(true);
        wordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 스크롤 페인 장착 (헤더가 보이려면 필수!)
        JScrollPane scrollPane = new JScrollPane(wordTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- [3] 하단 버튼 패널 ---
        bottomPanel = new JPanel();
        JButton addBtn = new JButton("단어 추가");
        JButton fileBtn = new JButton("파일 열기");
        JButton newFileBtn = new JButton("파일 생성");
        JButton delBtn = new JButton("삭제");
        JButton closeBtn = new JButton("저장 및 종료");

        // 추가 버튼 로직 (입력 필드 2개가 필요하므로 패널 사용)
//        addBtn.addActionListener(e -> addWordDialog());
//
//        // 삭제 버튼 로직
//        delBtn.addActionListener(e -> deleteSelectedRow());

        fileBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(initialDirectory);
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    ".txt 파일",
                    "txt");
            fileChooser.setFileFilter(filter);
            int result = fileChooser.showOpenDialog(VocManageDialog.this);

            //TODO: 파일 읽어서 단어 변환 및 테이블에 추가 로직 구현
            openFile();
        });

        newFileBtn.addActionListener(e -> {
            //TODO: 파일 생성 및 읽기 로직 구현 -> InputDialog 띄워서?
            createFile();
        });

        // 닫기
        closeBtn.addActionListener(e -> dispose());


        bottomPanel.add(addBtn);
        bottomPanel.add(delBtn);
        bottomPanel.add(fileBtn);
        bottomPanel.add(newFileBtn);
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    protected void openFile() {
    }

    protected void createFile() {
        openFile();
    }

}
