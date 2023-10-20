package com.user.service.demo.services;

import com.user.service.demo.entites.User;

import java.util.List;

public interface UserService {
    User saveUser(User user);
    List<User> getAllUser();
    User getUserWithId(String userId);

}
