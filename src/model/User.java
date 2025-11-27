package model;

import java.time.LocalDate;
import java.util.Objects;

public class User {
    private final String name;
    private final String password;
    private int streak;
    private final LocalDate lastDate;

    public User(String name, String password, int streak, LocalDate lastDate) {
        this.name = name;
        this.password = password;
        this.lastDate = lastDate;
        this.streak = streak;
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
        this.streak = 0;
        this.lastDate = LocalDate.now();
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public LocalDate getLastDate() {
        return lastDate;
    }

    @Override
    public String toString() {
        return name + '\t' + password + '\t' + streak + '\t' + lastDate;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(name, user.name) && Objects.equals(password, user.password);
    }
}
