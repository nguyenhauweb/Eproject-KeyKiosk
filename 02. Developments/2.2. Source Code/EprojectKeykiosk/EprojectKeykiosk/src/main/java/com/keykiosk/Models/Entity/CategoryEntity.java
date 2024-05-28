package com.keykiosk.Models.Entity;

import com.keykiosk.Models.EnumType.Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Categories", indexes = {@Index(name = "idx_name", columnList = "name")})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private UserEntity account;

    @Column(columnDefinition = "NVARCHAR(255)", name = "name", nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "NVARCHAR(255)", name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<ImageEntity> images;


    @Override
    public String toString() {
        return "CategoryEntity{" +
                "categoryId=" + categoryId +
                ", account=" + account +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                // do not print 'images'
                '}';
    }

}
