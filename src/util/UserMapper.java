package util;

import model.User;

import java.time.LocalDate;

public class UserMapper {
    public static User lineToUser(String line){
        String[] data = line.split(" ");
        return new User(data[0], data[1], Integer.parseInt(data[2]), LocalDate.parse(data[3]));
    }
}
