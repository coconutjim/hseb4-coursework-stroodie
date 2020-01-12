package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.mappers.UserJSON;
import model.User;
import model.service.UserService;
import model.user_util.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

/**
 * Profile controller
 */
@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String profile(Authentication authentication, Model model) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "login";
        }
        if (! user.isActivated()) {
            return "notActivated";
        }
        model.addAttribute("currUser", user.getUser());
        return "profile";
    }

    @RequestMapping(value = "/profile/user", method = RequestMethod.POST)
    @ResponseBody
    public String changeUser(Authentication authentication, @RequestBody String data) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "error: not found user";
        }
        if (! user.isActivated()) {
            return "error: not activated user";
        }

        ObjectMapper mapper = new ObjectMapper();

        try {
            UserJSON userJSON = mapper.readValue(data, UserJSON.class);
            return validateAndProcessUserUpdate(user.getUser(), userJSON);
        }
        catch (IOException e) {
            return "error: " + e.getMessage();
        }
        catch (Exception e1) {
            return "error: " + e1.getMessage();
        }
    }

    private String validateAndProcessUserUpdate(User user, UserJSON userJSON) {
        String firstName = userJSON.getFirstName();
        if (! Validation.isFirstNameValid(firstName)) {
            return Validation.FIRSTNAME_ERROR_MESSAGE;
        }
        String lastName = userJSON.getLastName();
        if (! Validation.isLastNameValid(lastName)) {
            return Validation.LASTNAME_ERROR_MESSAGE;
        }
        user.setFirstName(firstName);
        user.setLastName(lastName);

        try {
            userService.updateUser(user);
            return "success";
        }
        catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @RequestMapping(value = "/profile/password", method = RequestMethod.POST)
    @ResponseBody
    public String changeUserPassword(Authentication authentication, @RequestBody String data) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "error: not found user";
        }
        if (! user.isActivated()) {
            return "error: not activated user";
        }

        ObjectMapper mapper = new ObjectMapper();

        try {
            UserJSON userJSON = mapper.readValue(data, UserJSON.class);
            return validateAndProcessUserPasswordUpdate(user.getUser(), userJSON);
        }
        catch (IOException e) {
            return "error: " + e.getMessage();
        }
        catch (Exception e1) {
            return "error: " + e1.getMessage();
        }
    }

    private String validateAndProcessUserPasswordUpdate(User user, UserJSON userJSON) {
        String password = userJSON.getPassword();
        if (! Validation.isPasswordValid(password)) {
            return Validation.PASSWORD_ERROR_MESSAGE;
        }

        String passwordConfirm = userJSON.getPasswordConfirm();
        if (! Validation.isPasswordValid(passwordConfirm) || ! password.equals(passwordConfirm)) {
            return Validation.PASSWORD_DIF_ERROR_MESSAGE;
        }

        String oldPassword = userJSON.getOldPassword();

        if (! Validation.isPasswordValid(oldPassword)) {
            return Validation.PASSWORD_CURR_ERROR_MESSAGE;
        }

        if (oldPassword.equals(password)) {
            return Validation.PASSWORD_MATCH_ERROR_MESSAGE;
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (! encoder.matches(oldPassword, user.getPasswordHash())) {
            return Validation.PASSWORD_CURR_ERROR_MESSAGE;
        }

        user.setPasswordHash(encoder.encode(password));

        try {
            userService.updateUser(user);
            return "success";
        }
        catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
}
