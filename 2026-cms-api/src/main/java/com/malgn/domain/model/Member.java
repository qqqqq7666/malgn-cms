package com.malgn.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50, unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(length = 50, nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Builder
    public Member(Long id, String username, String password, String name, MemberRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public static Member createMember(String username, String encodedPassword, String name) {
        return Member.builder()
                .username(username)
                .password(encodedPassword)
                .name(name)
                .role(MemberRole.MEMBER)
                .build();
    }

    public boolean isAdmin() {
        return this.role.equals(MemberRole.ADMIN);
    }

    public void updateRole(MemberRole role) {
        this.role = role;
    }
}
