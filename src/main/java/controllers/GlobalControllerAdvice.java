package controllers;

import model.Notification;
import model.service.NotificationService;
import model.user_util.CurrentUser;
import model.user_util.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * Global contoller advice
 */
@ControllerAdvice(basePackages = {"controllers"} )
public class GlobalControllerAdvice {

    @Autowired
    private NotificationService notificationService;

    @ModelAttribute
    public void globalAttributes(Authentication authentication, Model model) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        boolean isAdmin = user != null && user.getRole() == UserRole.ADMIN;
        model.addAttribute("isAdmin", isAdmin);
        List<Notification> notifications = null;
        int notReadNotifications = 0;
        if (user != null) {
            notifications = notificationService.getAllNotifications(user.getUser());
            if (notifications != null && ! notifications.isEmpty()) {
                for (Notification notification : notifications) {
                    if (! notification.isRead()) {
                        ++ notReadNotifications;
                    }
                }
            }
        }
        model.addAttribute("notifications", notifications);
        model.addAttribute("notReadNotifications", notReadNotifications);
    }
}
