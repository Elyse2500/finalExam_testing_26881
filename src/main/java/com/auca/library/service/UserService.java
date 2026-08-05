package com.auca.library.service;

import com.auca.library.dao.UserDAO;
import com.auca.library.domain.User;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public User save(User user) {
        return userDAO.save(user);
    }

    /*
     * Verifies login credentials by matching the submitted username
     * and password against what is stored in the database.
     * Returns false immediately for blank or null inputs to avoid
     * unnecessary database hits.
     */
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
        String provinceName = userDAO.getProvinceNameByPersonId(personId);
        if (provinceName == null) {
            throw new IllegalArgumentException("No province found for person id: " + personId);
        }
        return provinceName;
    }
}
