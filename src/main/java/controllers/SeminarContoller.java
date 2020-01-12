package controllers;

import model.*;
import model.service.NotificationService;
import model.service.SeminarService;
import model.user_util.CurrentUser;
import model.user_util.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Seminar controller
 */
@Controller
public class SeminarContoller {

    @Autowired
    private SeminarService seminarService;

    @Autowired
    private NotificationService notificationService;

    @RequestMapping(value = "/seminar/{id}", method = RequestMethod.GET)
    String seminar(Authentication authentication, @PathVariable("id") int id, Model model) {
        Seminar seminar;
        try {
            seminar = seminarService.getSeminarById(id, true, false, true);
        }
        catch (Exception e) {
            return "404";
        }
        if (seminar == null) {
            return "404";
        }

        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "login";
        }
        if (! user.isActivated()) {
            return "notActivated";
        }
        if (! seminar.hasUser(user.getUser())) {
            return "403";
        }

        try {
            notificationService.setNotificationsForSeminarRead(seminar, user.getUser());
        }
        catch (Exception e) {
            // no need of actions here
        }

        boolean isMaster = false;

        User master = seminar.getMaster();
        if (master != null && master.getId() == user.getId()) {
            isMaster = true;
        }

        model.addAttribute("seminar", seminar);
        model.addAttribute("isMaster", isMaster);
        return "seminar";
    }
}
