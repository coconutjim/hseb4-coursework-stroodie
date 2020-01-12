package controllers;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.mappers.MessageSearchDataJSON;
import controllers.mappers.ThesaurusUnitJSON;
import controllers.mappers.UnionJSON;
import controllers.mappers.util.MessageFixedPoint;
import model.*;
import model.service.DiscussionService;
import model.service.NotificationService;
import model.service.SeminarService;
import model.user_util.CurrentUser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;


/**
 * Discussion controller
 */
@Controller
public class DiscussionController {

    @Autowired
    private SeminarService seminarService;

    @Autowired
    private DiscussionService discussionService;

    @Autowired
    private NotificationService notificationService;

    @RequestMapping(value = "/discussion/{id}", method = RequestMethod.GET)
    String seminar(Authentication authentication, @PathVariable("id") int id, Model model) {
        Discussion discussion;
        Seminar seminar;
        try {
            discussion = discussionService.getDiscussionById(id, true);
            if (discussion == null) {
                return "404";
            }
            seminar = seminarService.getSeminarById(discussion.getSeminar().getId(), true, true, false);
            if (seminar == null) {
                return "404";
            }
            discussion.setSeminar(seminar);
        }
        catch (Exception e) {
            return "404";
        }

        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "login";
        }
        if (! user.isActivated()) {
            return "notActivated";
        }
        SeminarParticipation participation = seminar.getUserParticipation(user.getUser());
        if (participation == null) {
            return "403";
        }

        try {
            notificationService.setNotificationsForDiscussionRead(discussion, user.getUser());
        }
        catch (Exception e) {
            // no need of actions here
        }

        boolean isDisMaster = discussion.getMaster() != null && discussion.getMaster().getId() == user.getId();

        String positions = null;
        try {
            SavedGraph savedGraph = discussionService.getSavedGraph(discussion, user.getUser());
            positions = savedGraph == null ? null : savedGraph.getPositions();
        }
        catch (Exception e) {
            // no need of actions here
        }

        List<MessageUnion> unions = null;
        if (discussion.getUnions() != null) {
            unions = new ArrayList<MessageUnion>(discussion.getUnions().keySet());
        }

        model.addAttribute("discussion", discussion);
        model.addAttribute("searchData", new MessageSearchDataJSON());
        model.addAttribute("messagesJSON", discussion.getMessagesJSON(positions));
        model.addAttribute("curParticip", participation);
        model.addAttribute("isDisMaster", isDisMaster);
        model.addAttribute("unions", unions);
        return "discussion";
    }

    @RequestMapping(value = "/discussion/{id}/fix", method = RequestMethod.POST)
    @ResponseBody
    public String fixGraph(Authentication authentication, @PathVariable("id") int id, @RequestBody String data) {

        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "error: user not found";
        }
        if (! user.isActivated()) {
            return "error: user not activated";
        }
        ObjectMapper mapper = new ObjectMapper();

        try {
            JavaType typeMap = mapper.getTypeFactory().constructMapType(Map.class, Integer.class, MessageFixedPoint.class);
            Map<Integer, MessageFixedPoint> positions = mapper.readValue(data, typeMap);
            return validateAndProcessMessagePositions(id, user.getUser(), positions);
        }
        catch (IOException e) {
            return "error: " + e.getMessage();
        }
        catch (Exception e1) {
            return "error: " + e1.getMessage();
        }
    }

    private String validateAndProcessMessagePositions(int disId, User user, Map<Integer, MessageFixedPoint> positions) {
        Discussion discussion;
        try {
            discussion = discussionService.getDiscussionById(disId, true);
        }
        catch (Exception e) {
            return "error: " + e.getMessage();
        }
        if (discussion == null) {
            return "error: discussion not found";
        }

        if (! Validation.isValidGraphPositions(positions)) {
            return "error: invalid positions";
        }

        SavedGraph savedGraph;
        try {
            savedGraph = discussionService.getSavedGraph(discussion, user);
        }
        catch (Exception e) {
            return "error: could not fetch saved graph";
        }
        if (savedGraph != null) {
            JSONObject jsonObject1 = new JSONObject(savedGraph.getPositions());
            ObjectMapper mapper = new ObjectMapper();
            JavaType typeMap = mapper.getTypeFactory().constructMapType(Map.class, Integer.class, MessageFixedPoint.class);
            Map<Integer, MessageFixedPoint> oldPos;
            try {
                oldPos = mapper.readValue(jsonObject1.optString("data"), typeMap);
            }
            catch (IOException e) {
                return "error: error in parsing old positions";
            }
            for (Map.Entry<Integer, MessageFixedPoint> entry : positions.entrySet()) {
                oldPos.put(entry.getKey(), entry.getValue());
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("data", oldPos);
            savedGraph.setPositions(jsonObject.toString());
        }
        else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("data", positions);
            savedGraph = new SavedGraph(discussion, user, jsonObject.toString());
        }
        try {
            discussionService.updateSavedGraph(savedGraph);
            return "success";
        }
        catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @RequestMapping(value = "/discussion/{id}/union", method = RequestMethod.POST)
    @ResponseBody
    public String unionCRUD(Authentication authentication, @PathVariable("id") int id, @RequestBody String data) {

        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "error: user not found";
        }
        if (! user.isActivated()) {
            return "error: user not activated";
        }
        ObjectMapper mapper = new ObjectMapper();

        try {
            UnionJSON unionJSON = mapper.readValue(data, UnionJSON.class);
            return validateAndProcessUnionCRUD(user.getUser(), id, unionJSON);
        }
        catch (IOException e) {
            return "error: " + e.getMessage();
        }
        catch (Exception e1) {
            return "error: " + e1.getMessage();
        }
    }

    private String validateAndProcessUnionCRUD(User user, int disId, UnionJSON unionJSON) {
        Discussion discussion;
        try {
            discussion = discussionService.getDiscussionById(disId, true);
        }
        catch (Exception e) {
            return "error: " + e.getMessage();
        }
        if (discussion == null) {
            return "error: discussion not found";
        }

        if (discussion.getMaster().getId() != user.getId()) {
            return "error: user is not a discussion master";
        }
        List<Message> messages = discussion.getMessages();
        if (messages == null) {
            return "error: discussion has no messages";
        }

        String mode = unionJSON.getMode();
        if (! Validation.isModeValid(mode)) {
            return "error: mode is not valid";
        }

        if (mode.equals("create") || mode.equals("update")) {

            MessageUnion union = null;
            if (mode.equals("create")) {
                union = new MessageUnion();
                union.setDiscussion(discussion);
            }
            else {
                int unId = unionJSON.getId();
                if (discussion.getUnions() != null) {
                    for (MessageUnion un : discussion.getUnions().keySet()) {
                        if (un.getId() == unId) {
                            union = un;
                        }
                    }
                }
                if (union == null) {
                    return "error: union not found";
                }
            }

            String name = unionJSON.getName();
            if (! Validation.isUnionNameValid(name)) {
                return Validation.UNION_NAME_ERROR_MESSAGE;
            }
            union.setName(name);

            if (mode.equals("create")) {

                List<Message> freeMessages = new ArrayList<Message>();
                Map<MessageUnion, List<Message>> unionMessages = new HashMap<MessageUnion, List<Message>>();
                int count = 0;

                List<Integer> mesIds = unionJSON.getMesIds();
                List<Integer> unionIds = unionJSON.getUnionIds();
                if (! Validation.isUnionContentValid(mesIds, unionIds)) {
                    return "error: invalid union content";
                }

                if (mesIds != null && ! mesIds.isEmpty()) {
                    Map<Integer, Message> map = new HashMap<Integer, Message>();
                    for (Message message : messages) {
                        map.put(message.getId(), message);
                    }
                    for (Integer mesId : mesIds) {
                        Message message = map.get(mesId);
                        if (message == null) {
                            return "error: not found message";
                        }
                        freeMessages.add(message);
                        ++ count;
                    }
                }
                if (unionIds != null && ! unionIds.isEmpty()) {
                    Map<MessageUnion, List<Message>> unions = discussion.getUnions();
                    if (unions == null) {
                        return "error: no unions in discussion";
                    }
                    Map<Integer, MessageUnion> map = new HashMap<Integer, MessageUnion>();
                    for (Map.Entry<MessageUnion, List<Message>> entry : unions.entrySet()) {
                        map.put(entry.getKey().getId(), entry.getKey());
                    }
                    for (Integer unionId : unionIds) {
                        MessageUnion u = map.get(unionId);
                        if (u == null) {
                            return "error: not found union";
                        }
                        List<Message> ms = unions.get(u);
                        if (ms == null) {
                            return "error: null messages in union";
                        }
                        count += ms.size();
                        unionMessages.put(u, ms);
                    }
                }

                if (count == messages.size()) {
                    return Validation.GLOBAL_UNION_ERROR_MESSAGE;
                }

                try {
                    discussionService.saveUnion(union, freeMessages, unionMessages);
                    return "success";
                }
                catch (Exception e) {
                    return "error: " + e.getMessage();
                }
            }
            else {
                try {
                    discussionService.updateUnion(union);
                    return "success";
                }
                catch (Exception e) {
                    return "error: " + e.getMessage();
                }
            }
        }
        else {
            MessageUnion union = null;
            int unId = unionJSON.getId();
            if (discussion.getUnions() != null) {
                for (MessageUnion un : discussion.getUnions().keySet()) {
                    if (un.getId() == unId) {
                        union = un;
                    }
                }
            }
            if (union == null) {
                return "error: union not found";
            }
            try {
                discussionService.deleteUnion(discussion.getUnions().get(union));
                return "success";
            }
            catch (Exception e) {
                return "error: " + e.getMessage();
            }
        }
    }

    @RequestMapping(value = "/discussion/{id}/thesaurus", method = RequestMethod.POST)
    @ResponseBody
    public String thesaurusCRUD(Authentication authentication, @PathVariable("id") int id, @RequestBody String data) {

        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "error: user not found";
        }
        if (! user.isActivated()) {
            return "error: user not activated";
        }
        ObjectMapper mapper = new ObjectMapper();

        try {
            ThesaurusUnitJSON thesaurusUnitJSON = mapper.readValue(data, ThesaurusUnitJSON.class);
            return validateAndProcessThesaurusCRUD(user.getUser(), id, thesaurusUnitJSON);
        }
        catch (IOException e) {
            return "error: " + e.getMessage();
        }
        catch (Exception e1) {
            return "error: " + e1.getMessage();
        }
    }

    private String validateAndProcessThesaurusCRUD(User user, int disId, ThesaurusUnitJSON thesaurusUnitJSON) {
        Discussion discussion;
        try {
            discussion = discussionService.getDiscussionById(disId, true);
        }
        catch (Exception e) {
            return "error: " + e.getMessage();
        }
        if (discussion == null) {
            return "error: discussion not found";
        }

        if (discussion.getMaster().getId() != user.getId()) {
            return "error: user is not a discussion master";
        }

        String mode = thesaurusUnitJSON.getMode();
        if (! Validation.isModeValid(mode)) {
            return "error: mode is not valid";
        }

        if (mode.equals("create") || mode.equals("update")) {

            ThesaurusUnit unit = null;
            if (mode.equals("create")) {
                unit = new ThesaurusUnit();
                unit.setDiscussion(discussion);
            }
            else {
                List<ThesaurusUnit> thesaurus = discussion.getThesaurus();
                if (thesaurus == null) {
                    return "error: unit not found";
                }
                for (ThesaurusUnit u : thesaurus) {
                    if (u.getId() == thesaurusUnitJSON.getId()) {
                        unit = u;
                    }
                }
                if (unit == null) {
                    return "error: unit not found";
                }
            }

            String name = thesaurusUnitJSON.getName();
            if (! Validation.isThesaurusNameValid(name)) {
                return Validation.THESAURUS_NAME_ERROR_MESSAGE;
            }
            unit.setName(name);

            String content = thesaurusUnitJSON.getContent();
            if (! Validation.isThesaurusContentValid(content)) {
                return Validation.THESAURUS_FORMAT_ERROR_MESSAGE;
            }

            String text = thesaurusUnitJSON.getText();
            if (! Validation.isThesaurusTextValid(text)) {
                return Validation.THESAURUS_TEXT_ERROR_MESSAGE;
            }

            if (content.startsWith("<p>") && content.endsWith("</p>")) {
                content = content.substring(3, content.length() - 4);
            }

            unit.setContent(content);
            unit.setText(text);

            if (mode.equals("create")) {
                try {
                    discussionService.saveThesaurusUnit(unit);
                    return "success";
                }
                catch (Exception e) {
                    return Validation.THESAURUS_FORMAT_ERROR_MESSAGE;
                }
            }
            else {
                try {
                    discussionService.updateThesaurusUnit(unit);
                    return "success";
                }
                catch (Exception e) {
                    return Validation.THESAURUS_FORMAT_ERROR_MESSAGE;
                }
            }
        }
        else {
            ThesaurusUnit unit = null;
            List<ThesaurusUnit> thesaurus = discussion.getThesaurus();
            if (thesaurus == null) {
                return "error: unit not found";
            }
            for (ThesaurusUnit u : thesaurus) {
                if (u.getId() == thesaurusUnitJSON.getId()) {
                    unit = u;
                }
            }
            if (unit == null) {
                return "error: unit not found";
            }
            try {
                discussionService.deleteThesaurusUnit(unit);
                return "success";
            }
            catch (Exception e) {
                return "error: " + e.getMessage();
            }
        }
    }
}
