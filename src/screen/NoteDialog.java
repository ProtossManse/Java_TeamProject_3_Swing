package screen;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class NoteDialog extends VocManageDialog {


    public NoteDialog(JFrame parent, User currentUser) {
        super(parent, "오답노트 관리", currentUser);

    }

    @Override
    protected void setInitialDir() {
        super.initialDirectory = new File("res/" + currentUser.getName() + "/notes");
    }
}
