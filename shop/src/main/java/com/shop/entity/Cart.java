package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import javax.persistence.*;

@Entity
@Table(name = "cart")
@Getter @Setter
@ToString
public class Cart extends BaseEntity {

    @Id
    @Column(name = "cart_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // @OneToOne 방향성, 카트 입장에서만 설정을 했음. 이부분을 방향성으로 표현하면 단방향.
    // Cart ----------> Member : 연관관계 매핑.
    
    // (fetch = FetchType.LAZY) 옵션이 추가된 상황
    // 기본은 즉시 로딩이 기본값.
    // 테스트 하는 부분이 즉시 로딩이면, 참조되는 테이블 여러개가 조회되는 상황 - 성능 저하 되는 부분
    // 그래서 즉시 로딩 하지않는 (fetch = FetchType.LAZY) 지연로딩 옵션으로 하나만 조회하는 상황을 테스트 하는 부분.
    // @OneToOne
    @OneToOne(fetch = FetchType.LAZY)
    // Member에 PK 역할을 하는 멤버변수(member_id)를 지정했음 
    @JoinColumn(name="member_id")
    private Member member;

    public static Cart createCart(Member member){
        Cart cart = new Cart();
        cart.setMember(member);
        return cart;
    }

}