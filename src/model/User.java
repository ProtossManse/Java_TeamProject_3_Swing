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
        this.streak = 1;
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

    public void setStreak() {
        int currentStreak = this.streak;
        if (this.lastDate.equals(LocalDate.now())) {
            return;
            // 오늘 이미 접속했다면 스트릭 유지
        } else if (this.lastDate.plusDays(1).equals(LocalDate.now())) {
            streak = currentStreak + 1;
            // 마지막 접속일이 어제라면 스트릭 1 증가 (연속 접속 성공!)
        } else {
            streak = 1;
            // 연속 접속이 끊겼으므로 1일부터 다시 시작
        }

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
