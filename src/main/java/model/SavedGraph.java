package model;

import javax.persistence.*;

/**
 * Saved graph positions
 */
@Entity
@Table(name = "saved_graphs")
public class SavedGraph {

    @Id
    @GeneratedValue
    private int id;

    @OneToOne
    private Discussion discussion;

    @OneToOne
    private User user;

    @Column(length = 3000, nullable = false)
    private String positions;

    public SavedGraph() {
    }

    public SavedGraph(Discussion discussion, User user, String positions) {
        this.discussion = discussion;
        this.user = user;
        this.positions = positions;
    }

    public int getId() {
        return id;
    }

    public Discussion getDiscussion() {
        return discussion;
    }

    public User getUser() {
        return user;
    }

    public String getPositions() {
        return positions;
    }

    public void setPositions(String positions) {
        this.positions = positions;
    }
}
