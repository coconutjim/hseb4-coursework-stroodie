package model;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.mappers.util.MessageFixedPoint;
import controllers.mappers.util.MessageReprJSON;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import javax.persistence.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Discussion
 */
@Entity
@Table(name = "discussions")
public class Discussion {

    @Id
    @GeneratedValue
    private int id;

    @Column(nullable = false)
    private String name;

    private String description;

    @OneToOne
    private Seminar seminar;

    @OneToOne
    private User master;

    @Column(nullable = false)
    private DateTime created;

    @Transient
    private List<Message> messages;

    @Transient
    private Map<MessageUnion, List<Message>> unions;

    @Transient
    private List<ThesaurusUnit> thesaurus;

    public Discussion(Seminar seminar) {
        this.seminar = seminar;
    }

    public Discussion() {
    }

    public String getMessagesJSON(String savedGraphPos) {
        JSONObject jsonObject = new JSONObject();
        List<MessageReprJSON> ms = new ArrayList<MessageReprJSON>();
        List<MessageReprJSON> uns = new ArrayList<MessageReprJSON>();
        if (messages == null) {
            jsonObject.put("messages", ms);
            jsonObject.put("unions", uns);
            jsonObject.put("fixed", false);
            return jsonObject.toString();
        }

        Map<Integer, MessageFixedPoint> positions = new HashMap<Integer, MessageFixedPoint>();
        if (savedGraphPos != null && ! savedGraphPos.isEmpty()) {
            JSONObject jsonObject1 = new JSONObject(savedGraphPos);
            ObjectMapper mapper = new ObjectMapper();
            JavaType typeMap = mapper.getTypeFactory().constructMapType(Map.class, Integer.class, MessageFixedPoint.class);
            try {
                positions = mapper.readValue(jsonObject1.optString("data"), typeMap);
            }
            catch (IOException e) {
                jsonObject.put("messages", ms);
                jsonObject.put("unions", uns);
                jsonObject.put("fixed", false);
                return jsonObject.toString();
            }
        }

        int i = 1;
        for (Message message : messages) {
            Message prev = message.getPrevious();
            MessageType type = message.getType();
            MessageUnion union = message.getUnion();

            MessageFixedPoint point = positions.get(message.getUid());

            MessageReprJSON m = new MessageReprJSON(message.getId(), message.getUid(), false, "Сообщение #" + Integer.toString(i),
                    prev == null? -1 : prev.getUid(), type == null ? "Начало дискуссии" : type.getName(),
                    message.getColor(), message.isFirst(), type == null ? null : type.getIconValue(),
                    union == null ? -1 : union.getUid(),
                    point != null, point != null ? point.getX() : 0, point != null ? point.getY() : 0);
            List<Message> next = message.getNextMessages();
            if (next != null) {
                for (Message message1 : next) {
                    m.addNextMessage(message1.getUid());
                }
            }
            ms.add(m);
            ++i;
        }

        if (unions != null) {
            for (MessageUnion union : unions.keySet()) {
                MessageFixedPoint point = positions.get(union.getUid());
                MessageReprJSON m = new MessageReprJSON(union.getId(), union.getUid(), true, union.getName(),
                        -1, "", "", false, "", -1,
                        point != null, point != null ? point.getX() : 0, point != null ? point.getY() : 0);
                uns.add(m);
            }
        }

        jsonObject.put("messages", ms);
        jsonObject.put("unions", uns);
        jsonObject.put("fixed", ! positions.isEmpty());
        return jsonObject.toString();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Seminar getSeminar() {
        return seminar;
    }

    public User getMaster() {
        return master;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public Map<MessageUnion, List<Message>> getUnions() {
        return unions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaster(User master) {
        this.master = master;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void setUnions(Map<MessageUnion, List<Message>> unions) {
        this.unions = unions;
    }

    public void setSeminar(Seminar seminar) {
        this.seminar = seminar;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setThesaurus(List<ThesaurusUnit> thesaurus) {
        this.thesaurus = thesaurus;
    }

    public List<ThesaurusUnit> getThesaurus() {
        return thesaurus;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public String getParsedCreated() {
        String result = null;
        if (created != null) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
            result = formatter.print(created);
        }
        return result;
    }
}
