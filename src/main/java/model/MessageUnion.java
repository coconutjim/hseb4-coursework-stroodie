package model;


import javax.persistence.*;

/**
 * Message union
 */
@Entity
@Table(name = "message_unions")
public class MessageUnion {

    @Id
    @GeneratedValue
    private int id;

    @Column(nullable = false)
    private int uid;

    @OneToOne
    private Discussion discussion;

    @Column(nullable = false)
    private String name;

    public MessageUnion() {
    }

    public int getId() {
        return id;
    }

    public void setDiscussion(Discussion discussion) {
        this.discussion = discussion;
    }

    public Discussion getDiscussion() {
        return discussion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageUnion that = (MessageUnion) o;

        if (id != that.id) return false;
        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
}
