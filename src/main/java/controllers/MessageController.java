package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.mappers.MessageJSON;
import controllers.mappers.MessageSearchDataJSON;
import model.*;
import model.service.DiscussionService;
import model.service.MessageService;
import model.service.SeminarService;
import model.user_util.CurrentUser;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Message controller
 */
@Controller
public class MessageController {

    @Autowired
    private SeminarService seminarService;

    @Autowired
    private DiscussionService discussionService;

    @Autowired
    private MessageService messageService;

    @RequestMapping(value = "/discussion/{id}/message", method = RequestMethod.POST)
    @ResponseBody
    public String processMessageCRUD(Authentication authentication, @PathVariable("id") int id, @RequestBody String data) {

        ObjectMapper mapper = new ObjectMapper();

        try {
            MessageJSON messageJSON = mapper.readValue(data, MessageJSON.class);
            return validateAndProcessMessageCRUD(authentication, id, messageJSON);
        }
        catch (IOException e) {
            return "error: " + e.getMessage();
        }
        catch (Exception e1) {
            return "error: " + e1.getMessage();
        }
    }

    private String validateAndProcessMessageCRUD(Authentication authentication, int disId, MessageJSON messageJSON) {

        String mode = messageJSON.getMode();
        if (! Validation.isModeValid(mode)) {
            return "error: mode is not valid";
        }

        Discussion discussion;
        try {
            discussion = discussionService.getDiscussionById(disId, true);
        }
        catch (Exception e) {
            return "error: " + e.getMessage();
        }
        if (discussion == null || discussion.getId() != disId) {
            return "error: discussion not found";
        }

        Seminar seminar;
        try {
            seminar = seminarService.getSeminarById(discussion.getSeminar().getId(), true, true, false);
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

        boolean isMaster = discussion.getMaster() != null && discussion.getMaster().getId() == user.getId();

        SeminarParticipation participation = seminar.getUserParticipation(user.getUser());
        if (participation == null || (participation.getSeminarRole() == SeminarRole.SPECTATOR && ! isMaster)) {
            return "error: forbidden";
        }

        if (mode.equals("create") || mode.equals("update")) {

            Message message;
            if (mode.equals("create")) {
                message = new Message();
            }
            else {
                try {
                    message = messageService.getMessageById(messageJSON.getId());
                }
                catch (Exception e) {
                    return "error: " + e.getMessage();
                }
                if (message == null) {
                    return "error: message not found";
                }
            }

            if (mode.equals("update")) {
                if (! message.canBeUpdated(participation)) {
                    return "error: could not change message with next ones, only discussion master can";
                }
            }

            if (mode.equals("create")) {
                message.setDiscussion(discussion);
                message.setAuthor(participation.getUser());
                message.setColor(participation.getUserColor().getColor());

                if (messageJSON.isFirst()) {

                    // first message
                    if (discussion.getMessages() != null && ! discussion.getMessages().isEmpty()) {
                        return "error: discussion already has messages";
                    }
                    if (! isMaster) {
                        return "error: only master can initiate discussion";
                    }
                    message.setPrevious(null);
                    message.setType(null);
                    message.setFirst(true);
                }
                else {
                    Message prev = messageService.getMessageById(messageJSON.getPrev());
                    if (prev == null || prev.getDiscussion().getId() != discussion.getId()) {
                        return "error: previous message not found";
                    }
                    MessageType type = seminar.getTypeById(messageJSON.getType());
                    if (type == null) {
                        return "error: type not found";
                    }
                    if (! prev.isFirst() && ! seminar.isConnectionValid(prev.getType(), type)) {
                        return "error: connection is invalid";
                    }
                    message.setPrevious(prev);
                    message.setType(type);
                    message.setFirst(false);
                }
            }

            String content = messageJSON.getContent();
            if (! Validation.isMessageContentValid(content)) {
                return Validation.MESSAGE_FORMAT_ERROR_MESSAGE;
            }

            String text = messageJSON.getText();
            if (! Validation.isMessageTextValid(text)) {
                return Validation.MESSAGE_TEXT_ERROR_MESSAGE;
            }

            message.setContent(content);
            message.setText(text);


            if (mode.equals("create")) {
                try {
                    message.setCreated(new DateTime());
                    messageService.saveMessage(seminar, message);
                    return "success";
                }
                catch (Exception e) {
                    return Validation.MESSAGE_FORMAT_ERROR_MESSAGE;
                }
            }
            else {
                try {
                    message.setUpdated(new DateTime());
                    message.setUpdater(user.getUser());
                    messageService.updateMessage(message);
                    return "success";
                }
                catch (Exception e) {
                    return Validation.MESSAGE_FORMAT_ERROR_MESSAGE;
                }
            }
        }
        else {
            try {
                Message message = messageService.getMessageById(messageJSON.getId());
                if (message == null) {
                    return "error: message not found";
                }
                if (! message.canBeDeleted(participation)) {
                    return "error: could not delete message with next ones";
                }
                messageService.deleteMessage(message);
                return "success";
            }
            catch (Exception e) {
                return "error: " + e.getMessage();
            }
        }
    }

    @RequestMapping(value = "/discussion/{id}/search", method = RequestMethod.POST)
    @ResponseBody
    public String messageSearch(Authentication authentication, @PathVariable("id") int id, @RequestBody String data) {

        CurrentUser user = authentication == null ? null : (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "error: user not found";
        }
        if (! user.isActivated()) {
            return "error: user not activated";
        }

        ObjectMapper mapper = new ObjectMapper();

        try {
            MessageSearchDataJSON messageSearchDataJSON = mapper.readValue(data, MessageSearchDataJSON.class);
            return validateAndProcessMessageSearch(id, messageSearchDataJSON);
        }
        catch (IOException e) {
            return "error: " + e.getMessage();
        }
        catch (Exception e1) {
            return "error: " + e1.getMessage();
        }
    }

    private String validateAndProcessMessageSearch(int disId, MessageSearchDataJSON messageSearchDataJSON) {

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

        String type = messageSearchDataJSON.getType();

        if (! Validation.isSearchTypeValid(type)) {
            return "error: invalid type";
        }

        String text = "";
        DateTime start = new DateTime();
        DateTime end = new DateTime();
        if (type.equals("text")) {
            text = messageSearchDataJSON.getText();
            if (! Validation.isSearchTextValid(text)) {
                return "error: invalid text";
            }
        }
        if (type.equals("date")) {
            start = Validation.validateAndParseSearchDate(messageSearchDataJSON.getDateStart());
            if (start == null) {
                return "error: invalid start date";
            }
            end = Validation.validateAndParseSearchDate(messageSearchDataJSON.getDateEnd());
            if (end == null) {
                return "error: invalid end date";
            }
        }

        List<Integer> result = new ArrayList<Integer>();
        List<Message> messages = discussion.getMessages();
        if (messages != null) {
            if (type.equals("text")) {
                result = getMessagesIdByText(messages, text);
            } else {
                if (type.equals("user")) {
                    result = getMessagesIdByUser(messages, messageSearchDataJSON.getId());
                } else {
                    if (type.equals("type")) {
                        result = getMessagesIdByType(messages, messageSearchDataJSON.getId());
                    } else {
                        if (type.equals("date")) {
                            result = getMessagesIdByDate(messages, start, end);
                        }
                        else {
                            return "error: invalid type";
                        }
                    }
                }
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", result);
        return jsonObject.toString();
    }

    private List<Integer> getMessagesIdByText(List<Message> messages, String text) {
        List<Integer> result = new ArrayList<Integer>();
        for (Message message : messages) {
            String content = message.getText();
            if (content.toLowerCase().contains(text.toLowerCase())) {
                result.add(message.getUid());
            }
        }
        return result;
    }

    private List<Integer> getMessagesIdByUser(List<Message> messages, int userId) {
        List<Integer> result = new ArrayList<Integer>();
        for (Message message : messages) {
            if (message.getAuthor().getId() == userId) {
                result.add(message.getUid());
            }
        }
        return result;
    }

    private List<Integer> getMessagesIdByType(List<Message> messages, int typeId) {
        List<Integer> result = new ArrayList<Integer>();
        for (Message message : messages) {
            if (message.isFirst()) {
                continue;
            }
            if (message.getType().getId() == typeId) {
                result.add(message.getUid());
            }
        }
        return result;
    }

    private List<Integer> getMessagesIdByDate(List<Message> messages, DateTime start, DateTime end) {
        List<Integer> result = new ArrayList<Integer>();
        for (Message message : messages) {
            DateTime created = message.getCreated();
            if (created.isAfter(start) && created.isBefore(end)) {
                result.add(message.getUid());
            }
        }
        return result;
    }
}
