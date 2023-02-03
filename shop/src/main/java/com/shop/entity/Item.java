package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.Data;

import javax.persistence.*;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemFormDto;
import com.shop.exception.OutOfStockException;
// @Entity 이 클래스는 테이블로 만들예정
@Entity
// @Table(name="item") 테이블 이름을 자동이 아닌 수동으로 설정.
@Table(name="item")
// lombok이라는 라이브러리를 이용해서 @Getter, @Setter, @ToString 이것을 메모리상에 올려준다.
@Getter
@Setter
@ToString
public class Item extends BaseEntity {

	// @Id 기본키 pk = not null + unique
    @Id
    // 해당 컬럼의 이름을 수동으로 지정(item_id). 수동 지정이 없으면, 자동으로 설정이 됨.
    @Column(name="item_id")
    // 해당 데이터베이스에서 테이블마다 식별하기 위한 번호를 자동 생성해주는 전략.
    // 보통은 AUTO로 합니다. Mysql : Autoincrement, 오라클 : 시퀀스 테이블 만들어서 사용함.
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;       //상품 코드

    // nullable = false(not null), length = 50
    @Column(nullable = false, length = 50)
    private String itemNm; //상품명

    //해당 컬럼 이름 price 수동 지정, not null
    @Column(name="price", nullable = false)
    private int price; //가격

    @Column(nullable = false)
    private int stockNumber; //재고수량

    // 큰 데이터 타입을 지정시 사용함. Lob : Large Object, CLOB(문자), BLOB(바이너리, 영상, 음성, 이미지 등)
    @Lob
    @Column(nullable = false)
    private String itemDetail; //상품 상세 설명

    // 상수들을 모아둔 클래스이고, 지정된 타입으로 컴파일러 체크를 해주어서 좋다.
    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus; //상품 판매 상태

    // itemFormDto에 들어있는 정보를 Item으로 재할당하는 메서드
    public void updateItem(ItemFormDto itemFormDto){
        this.itemNm = itemFormDto.getItemNm();
        this.price = itemFormDto.getPrice();
        this.stockNumber = itemFormDto.getStockNumber();
        this.itemDetail = itemFormDto.getItemDetail();
        this.itemSellStatus = itemFormDto.getItemSellStatus();
    }

    // 주문 동작시, 재고 갯수 주문 갯수만큼 줄이기
    public void removeStock(int stockNumber){
        int restStock = this.stockNumber - stockNumber;
        if(restStock<0){
            throw new OutOfStockException("상품의 재고가 부족 합니다. (현재 재고 수량: " + this.stockNumber + ")");
        }
        this.stockNumber = restStock;
    }

    // 주문 취소시, 다시 원래 재고 수량만큼 더할 때 사용함.
    public void addStock(int stockNumber){
        this.stockNumber += stockNumber;
    }

}