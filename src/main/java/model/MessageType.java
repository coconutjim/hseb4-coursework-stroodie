package model;

import javax.persistence.*;

/**
 * Ontology type
 */
@Entity
@Table(name = "message_types")
public class MessageType {

    @Id
    @GeneratedValue
    private int id;

    @OneToOne
    private Seminar seminar;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String iconName;

    @Column(nullable = false)
    private String iconValue;

    public MessageType(String name) {
        this.name = name;
    }

    public MessageType() {
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public void setIconValue(String iconValue) {
        this.iconValue = iconValue;
    }

    public int getId() {
        return id;
    }

    public Seminar getSeminar() {
        return seminar;
    }

    public String getName() {
        return name;
    }

    public void setSeminar(Seminar seminar) {
        this.seminar = seminar;
    }

    public String getIconName() {
        return iconName;
    }

    public String getIconValue() {
        return iconValue;
    }
}
