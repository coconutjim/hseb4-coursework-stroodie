package model;

import javax.persistence.*;

/**
 * Thesaurus unit
 */
@Entity
@Table(name = "thesaurus_units")
public class ThesaurusUnit {

    @Id
    @GeneratedValue
    private int id;

    @OneToOne
    private Discussion discussion;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 6000)
    private String content;

    @Column(nullable = false, length = 3000)
    private String text;

    public int getId() {
        return id;
    }

    public void setDiscussion(Discussion discussion) {
        this.discussion = discussion;
    }

    public Discussion getDiscussion() {
        return discussion;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
