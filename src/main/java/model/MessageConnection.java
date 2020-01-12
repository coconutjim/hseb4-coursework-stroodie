package model;

import javax.persistence.*;

/**
 * Ontology connection
 */
@Entity
@Table(name = "message_connections")
public class MessageConnection {

    @Id
    @GeneratedValue
    private int id;

    @OneToOne
    private Seminar seminar;

    @OneToOne
    private MessageType typeFrom;

    @OneToOne
    private MessageType typeTo;

    public MessageConnection(MessageType typeFrom, MessageType typeTo) {
        this.typeFrom = typeFrom;
        this.typeTo = typeTo;
    }

    public MessageConnection() {
    }

    public int getId() {
        return id;
    }

    public Seminar getSeminar() {
        return seminar;
    }

    public MessageType getTypeFrom() {
        return typeFrom;
    }

    public MessageType getTypeTo() {
        return typeTo;
    }

    public void setSeminar(Seminar seminar) {
        this.seminar = seminar;
    }

    public void setTypeFrom(MessageType typeFrom) {
        this.typeFrom = typeFrom;
    }

    public void setTypeTo(MessageType typeTo) {
        this.typeTo = typeTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageConnection that = (MessageConnection) o;

        if (id != that.id) return false;
        if (seminar != null ? !seminar.equals(that.seminar) : that.seminar != null) return false;
        if (typeFrom != null ? !typeFrom.equals(that.typeFrom) : that.typeFrom != null) return false;
        return !(typeTo != null ? !typeTo.equals(that.typeTo) : that.typeTo != null);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (seminar != null ? seminar.hashCode() : 0);
        result = 31 * result + (typeFrom != null ? typeFrom.hashCode() : 0);
        result = 31 * result + (typeTo != null ? typeTo.hashCode() : 0);
        return result;
    }
}
