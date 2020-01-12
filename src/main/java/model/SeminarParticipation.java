package model;

import javax.persistence.*;

/**
 * Seminar participation
 */
@Entity
@Table(name = "seminar_participations")
public class SeminarParticipation {

    @Id
    @GeneratedValue
    private int id;

    @OneToOne
    private Seminar seminar;

    @OneToOne
    private User user;

    @Column(nullable = false)
    private UserColor userColor;

    @Column(name = "seminar_role", nullable = false)
    private SeminarRole seminarRole;

    public SeminarParticipation(Seminar seminar, User user, SeminarRole seminarRole, UserColor userColor) {
        this.seminar = seminar;
        this.user = user;
        this.seminarRole = seminarRole;
        this.userColor = userColor;
    }

    public SeminarParticipation() {
    }

    public int getId() {
        return id;
    }

    public Seminar getSeminar() {
        return seminar;
    }

    public User getUser() {
        return user;
    }

    public SeminarRole getSeminarRole() {
        return seminarRole;
    }

    public UserColor getUserColor() {
        return userColor;
    }

    public void setSeminar(Seminar seminar) {
        this.seminar = seminar;
    }
}
