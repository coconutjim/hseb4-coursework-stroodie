package model.user_util;

import model.User;
import model.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsService implementation
 */
@Service
public class CurrentUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public CurrentUser loadUserByUsername(String email) throws UsernameNotFoundException {
        User user;
        try {
            user = userService.getUserByEmail(email);
        }
        catch (Exception e) {
            throw new UsernameNotFoundException("User not found");
        }
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new CurrentUser(user);
    }
}
