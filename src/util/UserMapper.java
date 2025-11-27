package util;

import model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserMapper {
    public static List<User> toUserList(String rawString) {
        String[] lines = rawString.split("\n");
        List<User> userList = new ArrayList<>();
        for (String line : lines) {
            String[] data = line.split("\t");
            try {
                userList.add(new User(data[0].trim(), data[1].trim(),
                        Integer.parseInt(data[2].trim()), LocalDate.parse(data[3].trim())));

            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }
        return userList;
    }
}
