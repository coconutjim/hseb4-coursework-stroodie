package model.user_util;

import model.User;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * Current user - util for authorization
 */
public class CurrentUser extends org.springframework.security.core.userdetails.User {

    private final User user;

    public CurrentUser(User user) {
        super(user.getEmail(), user.getPasswordHash(), AuthorityUtils.createAuthorityList(user.getRole().toString()));
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public Integer getId() {
        return user.getId();
    }

    public UserRole getRole() {
        return user.getRole();
    }

    public boolean isActivated() {
        return user.isActivated();
    }
}