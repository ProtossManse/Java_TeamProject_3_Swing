package manager;

import data.Path;
import model.User;
import util.UserMapper;

import java.time.LocalDate;
import java.util.List;

public class UserManager {
    public List<User> users;
    private User currentUser = new User("", "", 1, LocalDate.now());


    public void loadUser() {
        users = UserMapper.toUserList(FileManager.read(Path.USERS_TXT_PATH));
        System.out.println(users);
    }

    public boolean checkUser(String username, String password) {
        return users.contains(new User(username, password));
    }

    public void registerUser(String username, String password) {

        User registeredUser = new User(username, password);
        users.add(registeredUser);
        setCurrentUser(registeredUser);
        FileManager.createUserDirectories(registeredUser.getName());
        System.out.println(users);
    }

    public void saveUsers() {
        if (!users.isEmpty()) {
            String data = "";
            for (User user : users) {
                data += user.getName() + "\t" + user.getPassword() + "\t" + user.getStreak() + "\t" + user.getLastDate() + "\n";
            }
            FileManager.write(Path.USERS_TXT_PATH, data);
        }
    }

    public void setCurrentUser(String username, String password) {
        currentUser = new User(username, password);
    }

    private void setCurrentUser(User user) {
        currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
