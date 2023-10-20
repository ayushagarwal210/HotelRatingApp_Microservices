package com.user.service.demo.implementation;

import com.user.service.demo.entites.Hotel;
import com.user.service.demo.entites.Rating;
import com.user.service.demo.entites.User;
import com.user.service.demo.exception.UserNotFoundException;
import com.user.service.demo.external.services.HotelService;
import com.user.service.demo.repositories.UserRepository;
import com.user.service.demo.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HotelService hotelService;

    private Logger loggerFactory=LoggerFactory.getLogger(UserServiceImpl.class);
    @Override
    public User saveUser(User user) {
        String randomUserID=UUID.randomUUID().toString();
        user.setUserId(randomUserID);
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUser() {

        List<User> users=userRepository.findAll();
        for(User user:users){
            ArrayList<Rating> ratingsOfUser=restTemplate.getForObject("http://localhost:8083/rating/users/"+user.getUserId(), ArrayList.class);
            user.setRatings(ratingsOfUser);
        }
        return users;
    }

    @Override
    public User getUserWithId(String userId) {

        User user=userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException("User with id not found "+userId));
        Rating[] ratingsOfUser=restTemplate.getForObject("http://RATING-SERVICE/rating/users/"+userId, Rating[].class);
        loggerFactory.info("{}",ratingsOfUser);
        List<Rating> ratings=Arrays.stream(ratingsOfUser).toList();
        List<Rating> ratingList = ratings.stream().map(rating -> {
//            ResponseEntity<Hotel> forEntity=restTemplate.getForEntity("http://HOTEL-SERVICE/hotels/"+rating.getHotelId(),Hotel.class);
//            Hotel hotel=forEntity.getBody();
            Hotel hotel=hotelService.getHotel(rating.getHotelId());
            rating.setHotel(hotel);
            return rating;
        }).collect(Collectors.toList());
        user.setRatings(ratingList);
        return user;
    }
}
