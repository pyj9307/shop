package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
// 이 클래스는 엔티티로 지정한다
@Entity
// 디비에 테이블 이름을 item_img로 설정한다. 없으면 jpa가 클래스 이름으로 알아서 만든다.
@Table(name="item_img")
// 메모리에 getter, setter를 자동으로 등록한다
@Getter @Setter
// 종합선물세트
// get, set, equal, toString, hash -> @Data
public class ItemImg extends BaseEntity{
	//BaseEntity 추산 클래스, 공통 기능을 묶었다.
	// 시간에 관련된 멤버들, 등록일, 수정일 등
	// 스프링 시스템상에 등록이 되어있고 리스너로 동작하고 있고,
	// 해당 name 필드로 확인하고 있다.
	// 로그인 시, 멤버 정보는 시큐리티가 정보를 가지고 있다.

	// @Id 기본키 pk = not null + unique
	// 참고로 자동번호 증가 부분은 PK에서만 가능하다.
	@Id
	// 해당 컬럼의 이름을 수동으로 지정(item_img_id). 수동 지정이 없으면, 자동으로 설정이 됨.
    @Column(name="item_img_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String imgName; //이미지 파일명

    private String oriImgName; //원본 이미지 파일명

    private String imgUrl; //이미지 조회 경로

    private String repimgYn; //대표 이미지 여부

	// @ManyToOne(fetch = FetchType.LAZY)
    // 지연로딩, 조회시 연관된 테이블 모두 조회 안하기 위해서.
    // 기본값은 즉시로딩임.
    @ManyToOne(fetch = FetchType.LAZY)
	// 외래키 설정 부분(item_id).
    @JoinColumn(name = "item_id")
    private Item item;

    // 엔티티 클래스 부분
    public void updateItemImg(String oriImgName, String imgName, String imgUrl){
        this.oriImgName = oriImgName;
        this.imgName = imgName;
        this.imgUrl = imgUrl;
    }

}