package com.auca.library.service;

import com.auca.library.dao.UserDAO;
import com.auca.library.domain.User;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public User save(User user) {
        return userDAO.save(user);
    }

    public boolean authenticate(String username, String rawPassword) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return false;
        }
        User user = userDAO.findByUsername(username);
        if (user == null) {
            return false;
        }
        return rawPassword.equals(user.getPassword());
    }

    public String getProvinceNameByPersonId(String personId) {
        String name = userDAO.getProvinceNameByPersonId(personId);
        if (name == null) {
            throw new IllegalArgumentException("no province found for this person: " + personId);
        }
        return name;
    }
}
