package controllers;

import model.user_util.CurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Notification controller
 */
@Controller
public class NotificationController {

    @RequestMapping(value = "/notifications", method = RequestMethod.GET)
    public String notifications(Authentication authentication) {

        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "login";
        }
        if (! user.isActivated()) {
            return "notActivated";
        }
        return "notifications";
    }
}
