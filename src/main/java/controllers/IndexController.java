package controllers;
import model.service.SeminarService;
import model.user_util.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Main controller, returns index page
 */
@Controller
public class IndexController {

    @Autowired
    private SeminarService seminarService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Authentication authentication, Model model) {

        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "login";
        }
        if (! user.isActivated()) {
            return "notActivated";
        }
        model.addAttribute("seminars", seminarService.getSeminarsByUser(user.getUser()));
        return "main";
    }
}
