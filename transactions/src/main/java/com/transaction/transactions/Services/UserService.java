package com.transaction.transactions.Services;

import java.util.Collections;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    // This method is required by the UserDetailsService interface.
    // In this service, we don't need to load a user from a database because
    // the user's information is already in the JWT token. This method simply
    // creates a UserDetails object that Spring Security needs to complete the authentication.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // You would typically load the user from a database here.
        // For this microservice, we assume the user is valid if the JWT is valid.
        return new User(username, "", Collections.emptyList());
    }
}

