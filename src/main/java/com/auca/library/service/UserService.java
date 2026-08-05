package com.auca.library.service;

import com.auca.library.dao.UserDAO;
import com.auca.library.domain.User;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public User save(User user) {
        return userDAO.save(user);
    }

    public String getProvinceNameByPersonId(String personId) {
        String provinceName = userDAO.getProvinceNameByPersonId(personId);
        if (provinceName == null) {
            throw new IllegalArgumentException("No province found for person id: " + personId);
        }
        return provinceName;
    }
}
