package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.mappers.UserJSON;
import model.User;
import model.service.UserService;
import model.user_util.CurrentUser;
import model.user_util.EmailUtil;
import model.user_util.UserRole;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.util.List;

/**
 * Login controller
 */
@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    private static SecureRandom secureRandom = new SecureRandom();

    public static int DAYS_OF_TOKEN_EXPIRATION = 3;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    String login(Authentication authentication) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user != null) {
            return "alreadyIn";
        }
        return "login";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    String registration(Authentication authentication) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user != null) {
            return "alreadyIn";
        }
        return "registration";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    @ResponseBody
    String registerUser(Authentication authentication, @RequestBody String data, HttpServletRequest request) {

        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user != null) {
            return "error: user already logged in";
        }

        ObjectMapper mapper = new ObjectMapper();

        try {
            UserJSON userJSON = mapper.readValue(data, UserJSON.class);
            return validateAndProcessUserRegistration(userJSON, request);
        }
        catch (IOException e) {
            return "error: " + e.getMessage();
        }
        catch (Exception e1) {
            return "error: " + e1.getMessage();
        }
    }

    private String validateAndProcessUserRegistration(UserJSON userJSON, HttpServletRequest request) {
        String email = userJSON.getEmail();
        if (! Validation.isEmailValid(email)) {
            return Validation.EMAIL_ERROR_MESSAGE;
        }

        User found;
        try {
            found = userService.getUserByEmail(email);
        }
        catch (Exception e) {
            return "error: " + e.getMessage();
        }
        if (found != null) {
            return Validation.EMAIL_UN_ERROR_MESSAGE;
        }

        String password = userJSON.getPassword();
        if (! Validation.isPasswordValid(password)) {
            return Validation.PASSWORD_ERROR_MESSAGE;
        }

        String passwordConfirm = userJSON.getPasswordConfirm();
        if (! Validation.isPasswordValid(passwordConfirm) || ! password.equals(passwordConfirm)) {
            return Validation.PASSWORD_DIF_ERROR_MESSAGE;
        }

        String firstName = userJSON.getFirstName();
        if (! Validation.isFirstNameValid(firstName)) {
            return Validation.FIRSTNAME_ERROR_MESSAGE;
        }

        String lastName = userJSON.getLastName();
        if (! Validation.isLastNameValid(lastName)) {
            return Validation.LASTNAME_ERROR_MESSAGE;
        }

        String activationToken = getRandomToken();
        User user = new User(email, new BCryptPasswordEncoder().encode(password),
                firstName, lastName, UserRole.USER, activationToken, new DateTime().plusDays(3));
        try {
            userService.saveUser(user);
            EmailUtil.sendAccountActivationEmail(email, firstName, getAccountActivationUrl(request, user, activationToken));
            return "success";
        }
        catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @RequestMapping(value = "/repeatActivation", method = RequestMethod.GET)
    String repeatActivation(Authentication authentication, Model model, HttpServletRequest request) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "login";
        }
        if (user.isActivated()) {
            return "main";
        }
        User currUser = user.getUser();
        String activationToken = getRandomToken();
        currUser.setActivationToken(activationToken);
        currUser.setActivationTokenExpiresAt(new DateTime().plusDays(3));
        try {
            userService.updateUser(currUser);
        }
        catch (Exception e) {
            return "400";
        }
        EmailUtil.sendAccountActivationEmail(currUser.getEmail(), currUser.getFirstName(),
                getAccountActivationUrl(request, currUser, activationToken));
        model.addAttribute("sent", true);
        return "notActivated";
    }

    private static String getAccountActivationUrl(HttpServletRequest request, User user, String token) {
        String url;
        try {
            String str = request.getRequestURL().toString();
            url = str.substring(0, str.lastIndexOf("/"));
            url += "/activateAccount/" + user.getId() + "/" + token;
            return url;
        }
        catch (Exception e) {
            return "error";
        }
    }

    @RequestMapping(value = "/activateAccount/{id}/{token}", method = RequestMethod.GET)
    String activateAccount(Authentication authentication, @PathVariable("id") int id,
                           @PathVariable("token") String token, HttpServletRequest request, Model model) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "login";
        }
        if (user.isActivated()) {
            return "main";
        }
        User currUser;
        try {
            currUser = userService.getUserById(id);
        }
        catch (Exception e) {
            return "400";
        }
        if (currUser == null || currUser.getId() != user.getId()) {
            return "400";
        }
        String activationToken = currUser.getActivationToken();
        if (token == null || activationToken == null || ! token.equals(activationToken)) {
            return "400";
        }
        if (currUser.getActivationTokenExpiresAt().isBefore(new DateTime())) {
            model.addAttribute("tokenExpired", true);
            return "notActivated";
        }
        currUser.setActivated(true);
        try {
            userService.updateUser(currUser);
            request.logout();
        }
        catch (Exception e) {
            return "400";
        }
        model.addAttribute("accountActivated", true);
        return "login";
    }

    @RequestMapping(value = "/passwordRecovery", method = RequestMethod.GET)
    String passwordRecoveryForm(Authentication authentication) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user != null) {
            return "main";
        }
        return "passwordRecovery";
    }

    @RequestMapping(value = "/passwordRecovery", method = RequestMethod.POST)
    @ResponseBody
    String passwordRecoveryRequest(Authentication authentication,
                                   @RequestBody String data, HttpServletRequest request) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user != null) {
            return "error: user already logged in";
        }
        String email;
        try {
            JSONObject jsonObject = new JSONObject(data);
            email = jsonObject.getString("email");
        }
        catch (Exception e) {
            return "error: invalid email";
        }
        if (! Validation.isEmailValid(email)) {
            return "error: invalid email";
        }
        User foundUser;
        try {
            foundUser = userService.getUserByEmail(email);
        }
        catch (Exception e) {
            return "error: not found user";
        }
        if (foundUser == null) {
            return "error: not found user";
        }
        String token = getRandomToken();
        foundUser.setPasswordRecoveryToken(token);
        foundUser.setPasswordRecoveryTokenExpiresAt(new DateTime().plusDays(3));
        try {
            userService.updateUser(foundUser);
        }
        catch (Exception e) {
            return "error: unable to update user";
        }
        EmailUtil.sendPasswordRecoveryEmail(email, foundUser.getFirstName(),
                getPasswordRecoveryUrl(request, foundUser, token));
        return "success";
    }

    private static String getPasswordRecoveryUrl(HttpServletRequest request, User user, String token) {
        String url;
        try {
            String str = request.getRequestURL().toString();
            url = str.substring(0, str.lastIndexOf("/"));
            url += "/passwordRecovery/recoverPassword/" + user.getId() + "/" + token;
            return url;
        }
        catch (Exception e) {
            return "error";
        }
    }

    @RequestMapping(value = "passwordRecovery/recoverPassword/{id}/{token}", method = RequestMethod.GET)
    String recoverPasswordForm(Authentication authentication, @PathVariable("id") int id,
                           @PathVariable("token") String token, Model model) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user != null) {
            return "main";
        }
        User currUser;
        try {
            currUser = userService.getUserById(id);
        }
        catch (Exception e) {
            return "login";
        }
        if (currUser == null) {
            return "login";
        }
        String recoveryToken = currUser.getPasswordRecoveryToken();
        if (token == null || recoveryToken == null || ! token.equals(recoveryToken)) {
            return "login";
        }
        if (currUser.getPasswordRecoveryTokenExpiresAt().isBefore(new DateTime())) {
            model.addAttribute("recoveryTokenExpired", true);
            return "passwordRecovery";
        }
        model.addAttribute("passwordChanging", true);
        return "passwordRecovery";
    }

    @RequestMapping(value = "passwordRecovery/recoverPassword/{id}/{token}", method = RequestMethod.POST)
    @ResponseBody
    String recoverPassword(Authentication authentication, @PathVariable("id") int id,
                               @PathVariable("token") String token, @RequestBody String data) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user != null) {
            return "error: user already logged in";
        }
        User currUser;
        try {
            currUser = userService.getUserById(id);
        }
        catch (Exception e) {
            return "error: not found user";
        }
        if (currUser == null) {
            return "error: not found user";
        }
        String recoveryToken = currUser.getPasswordRecoveryToken();
        if (token == null || recoveryToken == null || ! token.equals(recoveryToken)) {
            return "error: invalid token";
        }
        if (currUser.getPasswordRecoveryTokenExpiresAt().isBefore(new DateTime())) {
            return "error: token expired";
        }
        String password;
        String passwordConfirm;
        try {
            JSONObject jsonObject = new JSONObject(data);
            password = jsonObject.getString("password");
            passwordConfirm = jsonObject.getString("passwordConfirm");
        }
        catch (Exception e) {
            return "error: invalid data";
        }
        if (! Validation.isPasswordValid(password)) {
            return Validation.PASSWORD_ERROR_MESSAGE;
        }
        if (! Validation.isPasswordValid(passwordConfirm) || ! password.equals(passwordConfirm)) {
            return Validation.PASSWORD_DIF_ERROR_MESSAGE;
        }
        currUser.setPasswordHash(new BCryptPasswordEncoder().encode(password));
        currUser.setPasswordRecoveryToken(null);
        currUser.setPasswordRecoveryTokenExpiresAt(null);
        try {
            userService.updateUser(currUser);
        }
        catch (Exception e) {
            return "error: error in updating user";
        }
        return "success";
    }

    private static String getRandomToken() {
        return new BigInteger(250, secureRandom).toString(32);
    }

    @RequestMapping(value = "/login/add", method = RequestMethod.GET)
    @ResponseBody
    String addUsers() {
        return addMockUsers();
    }

    private String addMockUsers() {
        String result = "";
        result += addMockUser(new User("osipov.enterprise@gmail.com",
                "$2a$10$5nKGp3w4KxNVOavQCtmEc.oKq/rhhl8ebDwH51HU/d/WajeT1FduK", "Лев", "Осипов",
                UserRole.ADMIN, "123", new DateTime()));
        result += addMockUser(new User("mock1@mock.com",
                "$2a$10$5nKGp3w4KxNVOavQCtmEc.oKq/rhhl8ebDwH51HU/d/WajeT1FduK", "Федор", "Достоевский",
                UserRole.ADMIN, "123", new DateTime()));
        result += addMockUser(new User("mock2@mock.com",
                "$2a$10$5nKGp3w4KxNVOavQCtmEc.oKq/rhhl8ebDwH51HU/d/WajeT1FduK", "Антон", "Чехов",
                UserRole.USER, "123", new DateTime()));
        result += addMockUser(new User("mock3@mock.com",
                "$2a$10$5nKGp3w4KxNVOavQCtmEc.oKq/rhhl8ebDwH51HU/d/WajeT1FduK", "Лев", "Толстой",
                UserRole.USER, "123", new DateTime()));
        result += addMockUser(new User("mock4@mock.com",
                "$2a$10$5nKGp3w4KxNVOavQCtmEc.oKq/rhhl8ebDwH51HU/d/WajeT1FduK", "Николай", "Гоголь",
                UserRole.USER, "123", new DateTime()));
        return result;
    }

    private String addMockUser(User user) {
        String result = "";
        User found = null;
        boolean f = false;
        try {
            found = userService.getUserByEmail(user.getEmail());
        }
        catch (Exception e) {
            result += "<div>error in getting user " + user.getEmail() + ": " + e.getMessage() + "</div>";
        }
        if (found != null) {
            f = true;
            found.setFirstName(user.getFirstName());
            found.setLastName(user.getLastName());
            found.setPasswordHash(user.getPasswordHash());
            found.setRole(user.getRole());
        }
        try {
            if (f) {
                found.setActivated(true);
                userService.updateUser(found);
            } else {
                user.setActivated(true);
                userService.saveUser(user);
            }
            result += "<div>" + (f ? "Updated" : "Created") + " user " + user.getEmail() + "</div>";
        }
        catch (Exception e) {
            result += "<div>error in creating/updating user " + user.getEmail() + ": " + e.getMessage() + "</div>";
        }
        return result;
    }

    @RequestMapping(value = "/getAdmin", method = RequestMethod.GET)
    @ResponseBody
    String getAdmin(Authentication authentication, HttpServletRequest request) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "error: user not found";
        }
        User u = user.getUser();
        boolean activated = user.isActivated();
        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        if (activated && isAdmin) {
            return "<div>User " + u.getEmail() + " is activated admin already</div>";
        }
        try {
            u.setActivated(true);
            u.setRole(UserRole.ADMIN);
            userService.updateUser(u);
            request.logout();
            return "<div>" + (activated ? "" : "User " + u.getEmail() + " is now activated\n") +
                    (isAdmin ? "" : "User " + u.getEmail() + " is now admin\n") + "</div>";
        }
        catch (Exception e) {
            return "<div>error in saving user " + u.getEmail() + ": " + e.getMessage() + "</div>";
        }
    }

    @RequestMapping(value = "/userMagic", method = RequestMethod.GET)
    @ResponseBody
    String getUsers(Authentication authentication) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "error: user not found";
        }
        if (! user.isActivated()) {
            return "error: user not activated";
        }
        if (user.getRole() != UserRole.ADMIN) {
            return "error: forbidden";
        }

        return getUserList();
    }

    @RequestMapping(value = "/userMagic/{id}", method = RequestMethod.GET)
    @ResponseBody
    String deleteUser(Authentication authentication, @PathVariable("id") int id) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "error: user not found";
        }
        if (! user.isActivated()) {
            return "error: user not activated";
        }
        if (user.getRole() != UserRole.ADMIN) {
            return "error: forbidden";
        }

        User found;
        try {
            found = userService.getUserById(id);
        }
        catch (Exception e) {
            return "error: "  + e.getMessage();
        }
        if (found == null) {
            return "<div>error: user " + Integer.toString(id) + " not found</div>" + getUserList();
        }
        try {
            userService.deleteUser(found);
            return "<div>successfully deleted user " + found.getEmail() + "</div>" + getUserList();
        }
        catch (Exception e) {
            return "<div>error: "  + e.getMessage() + "</div>" + getUserList();
        }
    }

    private String getUserList() {
        List<User> users;
        try {
            users = userService.getAllUsers();
        }
        catch (Exception e) {
            return "<div>error: "  + e.getMessage() + "</div>";
        }
        if (users == null || users.isEmpty()) {
            return "<div>no users found</div>";
        }
        String result = "<div>Current users:</div>";
        for (User u : users) {
            result += "<div>" + Integer.toString(u.getId()) + ": " + u.getEmail() + "</div>";
        }
        return result;
    }
}
