package screen;

import javax.swing.*;
import java.awt.*;

public class QuizDialog extends JDialog {
    public QuizDialog(JFrame parent) {
        super(parent, "퀴즈 메뉴", true);
        setSize(500, 400);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);
    }
}
