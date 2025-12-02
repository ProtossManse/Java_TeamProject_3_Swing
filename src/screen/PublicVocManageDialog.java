package screen;

import model.User;

import javax.swing.*;
import java.io.File;

public class PublicVocManageDialog extends VocManageDialog {


    public PublicVocManageDialog(JFrame parent, User currentUser) {
        super(parent, "공용 단어장 관리", currentUser);

    }

    @Override
    protected void setInitialDir() {
        super.initialDirectory = new File("res/public/vocas");
    }
}
