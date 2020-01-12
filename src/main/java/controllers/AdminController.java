package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.mappers.MessageConnectionJSON;
import controllers.mappers.MessageTypeJSON;
import controllers.mappers.ParticipationJSON;
import controllers.mappers.SeminarJSON;
import model.*;
import model.service.SeminarService;
import model.service.UserService;
import model.user_util.CurrentUser;
import model.user_util.UserRole;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin controller
 */
@Controller
public class AdminController {

    @Autowired
    private SeminarService seminarService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String admin(Authentication authentication, Model model) {
        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "login";
        }
        if (! user.isActivated()) {
            return "notActivated";
        }
        if (user.getRole() != UserRole.ADMIN) {
            return "403";
        }
        List<Seminar> seminars = seminarService.getAllSeminars(true, true, true);
        model.addAttribute("seminars", seminars);
        model.addAttribute("typesJSON", getSeminarsTypesJSON(seminars));
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", SeminarRole.values());
        model.addAttribute("rolesJSON", SeminarRole.toJSON());
        return "admin";
    }

    public static String getSeminarsTypesJSON(List<Seminar> seminars) {
        JSONObject jsonObject = new JSONObject();
        Map<Integer, Map<Integer, String>> result = new HashMap<Integer, Map<Integer, String>>();
        if (seminars == null) {
            jsonObject.put("data", result);
            return jsonObject.toString();
        }
        for (Seminar seminar : seminars) {
            Map<Integer, String> ts = new HashMap<Integer, String>();
            List<MessageType> types = seminar.getTypes();
            if (types == null || types.isEmpty()) {
                continue;
            }
            for (MessageType type : types) {
                ts.put(type.getId(), type.getIconName());
            }
            result.put(seminar.getId(), ts);
        }

        jsonObject.put("data", result);
        return jsonObject.toString();
    }

    @RequestMapping(value = "/admin", method = RequestMethod.POST)
    @ResponseBody
    public String seminarCRUD(Authentication authentication, @RequestBody String data) {

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

        ObjectMapper mapper = new ObjectMapper();

        try {
            SeminarJSON seminarJSON = mapper.readValue(data, SeminarJSON.class);
            return validateAndProcessSeminarCRUD(seminarJSON);
        }
        catch (IOException e) {
            return "error: " + e.getMessage();
        }
        catch (Exception e1) {
            return "error: " + e1.getMessage();
        }
    }

    protected String validateAndProcessSeminarCRUD(SeminarJSON seminarJSON) {
        String mode = seminarJSON.getMode();
        if (! Validation.isModeValid(mode)) {
            return "error: mode is not valid";
        }
        if (mode.equals("create") || mode.equals("update")) {

            Seminar seminar;
            if (mode.equals("create")) {
                seminar = new Seminar();
            }
            else {
                try {
                    seminar = seminarService.getSeminarById(seminarJSON.getId(), true, false, true);
                }
                catch (Exception e) {
                    return "error: " + e.getMessage();
                }
                if (seminar == null) {
                    return "error: seminar not found";
                }
            }

            String name = seminarJSON.getName();
            if (! Validation.isSeminarNameValid(name)) {
                return Validation.SEMINAR_NAME_ERROR_MESSAGE;
            }
            seminar.setName(name);

            String description = seminarJSON.getDescription();
            if (! Validation.isSeminarDescriptionValid(description)) {
                return Validation.SEMINAR_DESCR_ERROR_MESSAGE;
            }
            seminar.setDescription(description);

            List<SeminarParticipation> oldParticips = seminar.getParticipations();
            Map<Integer, UserColor> oldColors = new HashMap<Integer, UserColor>();
            Map<Integer, SeminarRole> oldRoles = new HashMap<Integer, SeminarRole>();

            List<ParticipationJSON> participationsJSON = seminarJSON.getParticipations();
            List<SeminarParticipation> participations = new ArrayList<SeminarParticipation>();
            if (participationsJSON != null && ! participationsJSON.isEmpty()) {
                if (oldParticips != null && ! oldParticips.isEmpty()) {
                    for (SeminarParticipation p : oldParticips) {
                        int userId = p.getUser().getId();
                        oldColors.put(userId, p.getUserColor());
                        oldRoles.put(userId, p.getSeminarRole());
                    }
                }
                boolean masterExists = false;
                List<UserColor> colors = UserColor.getRandomList();
                for (ParticipationJSON participationJSON : participationsJSON) {
                    int userId = participationJSON.getId();
                    User semMaster = userService.getUserById(userId);
                    if (semMaster == null) {
                        return "error: user with id " + userId + " not found";
                    }

                    SeminarRole role = SeminarRole.fromString(participationJSON.getRole());
                    if (role == null) {
                        return "error: error in parsing roles";
                    }
                    if (role == SeminarRole.MASTER) {
                        if (masterExists) {
                            return Validation.SEMINAR_MANY_MASTERS_ERROR_MESSAGE;
                        }
                        else {
                            masterExists = true;
                        }
                    }

                    for (SeminarParticipation p : participations) {
                        if (p.getUser().getId() == userId) {
                            return "error: duplicate users";
                        }
                    }
                    if (colors.isEmpty()) {
                        colors = UserColor.getRandomList();
                    }
                    UserColor color = oldColors.get(semMaster.getId());
                    if (color != null) {
                        colors.remove(color);
                    }
                    else {
                        color = colors.remove(0);
                    }
                    participations.add(new SeminarParticipation(seminar, semMaster, role, color));
                }
                if (! masterExists) {
                    return Validation.SEMINAR_NO_MASTER_ERROR_MESSAGE;
                }
            }
            if (! participations.isEmpty()) {
                seminar.setParticipations(participations);
            }

            if (seminar.canChangeOntology()) {
                List<MessageTypeJSON> typesJSON = seminarJSON.getTypes();
                List<MessageType> types = new ArrayList<MessageType>();
                if (typesJSON != null) {
                    for (MessageTypeJSON typeJSON : typesJSON) {
                        String typeName = typeJSON.getName();
                        if (! Validation.isTypeNameValid(typeName)) {
                            return Validation.TYPE_NAME_ERROR_MESSAGE;
                        }

                        for (MessageType t : types) {
                            if (t.getName().equals(typeName)) {
                                return Validation.TYPE_NAME_DIF_ERROR_MESSAGE;
                            }
                        }

                        String iconName = typeJSON.getIconName();
                        if (! Validation.isTypeIconNameValid(iconName)) {
                            return "error: invalid icon name";
                        }

                        String iconValue = typeJSON.getIconValue();
                        if (! Validation.isTypeIconValueValid(iconValue)) {
                            return "error: invalid icon value";
                        }

                        MessageType type = new MessageType(typeName);
                        type.setIconName(iconName);
                        type.setIconValue(iconValue);

                        types.add(type);
                    }
                }
                if (!types.isEmpty()) {
                    seminar.setTypes(types);
                }

                List<MessageConnection> connections = new ArrayList<MessageConnection>();
                List<MessageConnectionJSON> connectionsJSON = seminarJSON.getConnections();
                if (connectionsJSON != null) {
                    for (MessageConnectionJSON connectionJSON : connectionsJSON) {
                        String typeFrom = connectionJSON.getType1();
                        String typeTo = connectionJSON.getType2();
                        boolean found1 = false;
                        boolean found2 = false;
                        for (MessageType type : types) {
                            if (type.getName().equals(typeFrom)) {
                                found1 = true;
                            }
                            if (type.getName().equals(typeTo)) {
                                found2 = true;
                            }
                            if (found1 && found2) {
                                break;
                            }
                        }
                        if (!(found1 && found2)) {
                            return "error: connection with undefined type";
                        }

                        MessageConnection connection =
                                new MessageConnection(new MessageType(typeFrom),
                                        new MessageType(typeTo));

                        for (MessageConnection c : connections) {
                            if (c.equals(connection)) {
                                return "error: duplicate connections";
                            }
                        }

                        connections.add(connection);
                    }
                }
                if (!connections.isEmpty()) {
                    seminar.setConnections(connections);
                }
            }

            List<SeminarParticipation> changedParticipations = new ArrayList<SeminarParticipation>();
            if (! participations.isEmpty()) {
                for (SeminarParticipation p : participations) {
                    SeminarRole old = oldRoles.get(p.getUser().getId());
                    if (old == null || old != p.getSeminarRole()) {
                        changedParticipations.add(p);
                    }
                }
            }


            if (mode.equals("create")) {
                try {
                    seminar.setCreated(new DateTime());
                    seminarService.saveSeminar(seminar);
                    return "success";
                }
                catch (Exception e) {
                    return "error: " + e.getMessage();
                }
            }
            else {
                try {
                    seminarService.updateSeminar(seminar, changedParticipations);
                    return "success";
                }
                catch (Exception e) {
                    return "error: " + e.getMessage();
                }
            }
        }
        else {
            try {
                Seminar seminar = seminarService.getSeminarById(seminarJSON.getId(), false, false, false);
                if (seminar == null) {
                    return "error: seminar not found";
                }
                seminarService.deleteSeminar(seminar);
                return "success";
            }
            catch (Exception e) {
                return "error: " + e.getMessage();
            }
        }
    }

    @RequestMapping(value = "/admin/role", method = RequestMethod.POST)
    @ResponseBody
    public String changeUserRole(Authentication authentication, HttpServletRequest request,
                                 @RequestBody String data) {

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

        int userId;
        boolean admin;

        try {
            JSONObject jsonObject = new JSONObject(data);
            userId = jsonObject.getInt("id");
            admin = jsonObject.getBoolean("admin");
        }
        catch (Exception e) {
            return "error: " + e.getMessage();
        }

        User found;
        try {
            found = userService.getUserById(userId);
        }
        catch (Exception e) {
            return "error: user not found";
        }
        if (found == null) {
            return "error: user not found";
        }

        found.setRole(admin ? UserRole.ADMIN : UserRole.USER);

        try {
            userService.updateUser(found);
            if (userId == user.getId()) {
                request.logout();
            }
            return "success";
        }
        catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
}
