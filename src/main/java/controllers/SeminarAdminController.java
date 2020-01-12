package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.mappers.*;
import model.*;
import model.service.DiscussionService;
import model.service.SeminarService;
import model.service.UserService;
import model.user_util.CurrentUser;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seminar admin controller
 */
@Controller
public class SeminarAdminController {

    @Autowired
    private SeminarService seminarService;

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussionService discussionService;

    @RequestMapping(value = "/seminarAdmin/{id}", method = RequestMethod.GET)
    String seminarAdmin(Authentication authentication, @PathVariable("id") int id, Model model) {
        Seminar seminar;
        try {
            seminar = seminarService.getSeminarById(id, true, true, true);
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
        User master = seminar.getMaster();
        if (master == null || master.getId() != user.getId()) {
            return "403";
        }

        model.addAttribute("seminar", seminar);
        model.addAttribute("users", userService.getAllUsers());
        List<Seminar> list = new ArrayList<Seminar>();
        list.add(seminar);
        model.addAttribute("typesJSON", AdminController.getSeminarsTypesJSON(list));
        model.addAttribute("roles", SeminarRole.values());
        model.addAttribute("rolesJSON", SeminarRole.toJSON());
        return "seminar_admin";
    }

    @RequestMapping(value = "/seminarAdmin/{id}/discussion", method = RequestMethod.POST)
    @ResponseBody
    public String discussionCRUD(Authentication authentication, @PathVariable("id") int id, @RequestBody String data) {

        ObjectMapper mapper = new ObjectMapper();

        try {
            DiscussionJSON discussionJSON = mapper.readValue(data, DiscussionJSON.class);
            return validateAndProcessDiscussionCRUD(authentication, id, discussionJSON);
        }
        catch (IOException e) {
            return "error: " + e.getMessage();
        }
        catch (Exception e1) {
            return "error: " + e1.getMessage();
        }
    }

    private String validateAndProcessDiscussionCRUD(Authentication authentication, int semId, DiscussionJSON discussionJSON) {
        if (semId != discussionJSON.getSeminar()) {
            return "error: invalid seminar";
        }
        Seminar seminar;
        try {
            seminar = seminarService.getSeminarById(semId, true, true, true);
        }
        catch (Exception e) {
            return "error: " + e.getMessage();
        }
        if (seminar == null) {
            return "error: seminar not found";
        }

        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "error: user not found";
        }
        if (! user.isActivated()) {
            return "error: user not activated";
        }
        User master = seminar.getMaster();
        if (master == null || master.getId() != user.getId()) {
            return "error: forbidden";
        }

        String mode = discussionJSON.getMode();
        if (! Validation.isModeValid(mode)) {
            return "error: mode is not valid";
        }
        if (mode.equals("create") || mode.equals("update")) {

            if (! seminar.hasOntology()) {
                return Validation.SEMINAR_NO_ONTOLOGY_ERROR_MESSAGE;
            }

            Discussion discussion;
            if (mode.equals("create")) {
                discussion = new Discussion(seminar);
            }
            else {
                try {
                    discussion = discussionService.getDiscussionById(discussionJSON.getId(), true);
                }
                catch (Exception e) {
                    return "error: " + e.getMessage();
                }
                if (discussion == null) {
                    return "error: discussion not found";
                }
            }

            String name = discussionJSON.getName();
            if (! Validation.isDiscussionNameValid(name)) {
                return Validation.DISCUSSION_NAME_ERROR_MESSAGE;
            }
            discussion.setName(name);

            String description = discussionJSON.getDescription();
            if (! Validation.isDiscussionDescriptionValid(description)) {
                return Validation.DISCUSSION_DESCR_ERROR_MESSAGE;
            }
            discussion.setDescription(description);

            List<SeminarParticipation> changedParticipations = new ArrayList<SeminarParticipation>();

            if (mode.equals("create") || discussion.getMaster() == null ||
                    discussion.getMaster().getId() != discussionJSON.getMaster()) {
                User disMaster = null;
                List<SeminarParticipation> participations = seminar.getParticipations();
                if (participations == null) {
                    return Validation.DISCUSSION_NO_MASTER_ERROR_MESSAGE;
                }
                for (SeminarParticipation participation : participations) {
                    if (participation.getUser().getId() == discussionJSON.getMaster()) {
                        disMaster = participation.getUser();
                        if (mode.equals("update") && discussion.getMaster() != null &&
                                discussion.getMaster().getId() != discussionJSON.getMaster()) {
                            changedParticipations.add(participation);
                            for (SeminarParticipation p : participations) {
                                if (p.getUser().getId() == discussion.getMaster().getId()) {
                                    changedParticipations.add(p);
                                }
                            }
                        }
                    }
                }
                if (disMaster == null) {
                    return "error: user not found";
                }
                discussion.setMaster(disMaster);
            }

            if (mode.equals("create")) {
                try {
                    discussion.setCreated(new DateTime());
                    discussionService.saveDiscussion(seminar, discussion);
                    return "success";
                }
                catch (Exception e) {
                    return "error: " + e.getMessage();
                }
            }
            else {
                try {
                    discussionService.updateDiscussion(discussion, changedParticipations);
                    return "success";
                }
                catch (Exception e) {
                    return "error: " + e.getMessage();
                }
            }
        }
        else {
            try {
                Discussion discussion = discussionService.getDiscussionById(discussionJSON.getId(), false);
                if (discussion == null) {
                    return "error: discussion not found";
                }
                discussionService.deleteDiscussion(discussion);
                return "success";
            }
            catch (Exception e) {
                return "error: " + e.getMessage();
            }
        }
    }

    @RequestMapping(value = "/seminarAdmin/{id}", method = RequestMethod.POST)
    @ResponseBody
    public String seminarUpdate(Authentication authentication, @PathVariable("id") int id, @RequestBody String data) {

        ObjectMapper mapper = new ObjectMapper();

        try {
            SeminarJSON seminarJSON = mapper.readValue(data, SeminarJSON.class);
            return validateAndProcessSeminarUpdate(authentication, id, seminarJSON);
        }
        catch (IOException e) {
            return "error: " + e.getMessage();
        }
        catch (Exception e1) {
            return "error: " + e1.getMessage();
        }
    }

    protected String validateAndProcessSeminarUpdate(Authentication authentication, int semId, SeminarJSON seminarJSON) {
        String mode = seminarJSON.getMode();
        if (! (mode != null && mode.equals("update"))) {
            return "error: mode is not valid";
        }

        Seminar seminar;
        try {
            seminar = seminarService.getSeminarById(seminarJSON.getId(), true, false, true);
        }
        catch (Exception e) {
            return "error: " + e.getMessage();
        }
        if (seminar == null || seminar.getId() != semId) {
            return "error: seminar not found";
        }

        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "error: user not found";
        }
        if (! user.isActivated()) {
            return "error: user not activated";
        }
        User master = seminar.getMaster();
        if (master == null || master.getId() != user.getId()) {
            return "error: forbidden";
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

        try {
            seminarService.updateSeminar(seminar, changedParticipations);
            return "success";
        }
        catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
}
