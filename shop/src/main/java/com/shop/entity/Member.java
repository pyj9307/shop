package com.shop.entity;

import com.shop.constant.Role;
import com.shop.dto.MemberFormDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;

@Entity
@Table(name="member")
@Getter @Setter
@ToString
public class Member extends BaseEntity {

	// @Id : 데이터 상 Primary Key
    @Id
    @Column(name="member_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String address;

    // enum 타입을 명시하고, 타입을 문자열로 지정.
    // 컴파일러에서 문법 체크도 가능.
    @Enumerated(EnumType.STRING)
    private Role role;

    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder){
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getAddress());
        // 패스워드 인코딩 작업, 해싱하는 작업, 패스워드 일반 평문으로 특정 길이의 문자열로 변환하는 작업.
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);
        // 회원 가입시 기본 역할 지정 부분.
        member.setRole(Role.USER);
        return member;
    }

}
