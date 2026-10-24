package ch.itpraktika.api.merkliste;

import ch.itpraktika.api.internship.Internship;
import ch.itpraktika.api.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "merkliste_entries", indexes = {
        @Index(name = "idx_merkliste_user_created", columnList = "user_id, created_at")
})
public class MerklisteEintrag {

    @Id
    @Column(length = 36, nullable = false, columnDefinition = "char(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected MerklisteEintrag() {
    }

    public MerklisteEintrag(User user, Internship internship) {
        this.id = UUID.randomUUID().toString();
        this.user = user;
        this.internship = internship;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Internship getInternship() {
        return internship;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void updateInternship(Internship internship) {
        this.internship = internship;
    }
}
