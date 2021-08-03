package service;

import cn.hutool.core.util.StrUtil;
import domain.User;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class UserService {
    private static  UserService instance;

    public static synchronized UserService getInstance() {
        if (instance == null){
            instance = new UserService();
        }
        return instance;
    }

    private final Map<String, User> users;
    private final Map<String, String> online;

    
    public UserService() {
        users = new ConcurrentHashMap<>();
        online = new ConcurrentHashMap<>();
        users.put("admin", new User("admin", "admin", "管理员", 20));
        users.put("user1", new User("user1", "pwd1", "用户1", 23));
    }

    public boolean login(String username, String password) {
        User user = users.get(username);
        if (password.equals(user.getPassword())) {
            online.put(username, "");
            return true;
        }
        return false;
    }

    public User findByUsername(String username) {
        if (StrUtil.isEmpty(username)){
            return null;
        }
        return users.get(username);
    }
    
    
    public void update(User user) {
        users.put(user.getUsername(),user);
    }
}
