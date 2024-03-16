package bgu.spl.net.impl.tftp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoggedUsers {
    private final Map<String, Integer> loggedUsers;

    public LoggedUsers() {
        this.loggedUsers = new ConcurrentHashMap<>();
    }

    public boolean login(String username, int connectionId) {
        if (loggedUsers.containsKey(username)) {
            return false;
        }
        loggedUsers.put(username, connectionId);
        return true;
    }

    public boolean logout(String username) {
        if (loggedUsers.containsKey(username)) {
            loggedUsers.remove(username);
            return true;
        }
        return false;
    }

    public Map<String, Integer> getLoggedUsers() {
        return loggedUsers;
    }
}
