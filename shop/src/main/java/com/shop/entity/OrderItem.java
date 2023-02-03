package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

@Entity
@Getter @Setter
public class OrderItem extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    // 방향 설정이 됨, 외래키 설정이 됨.(주) @JoinColumn(name = "item_id")
    // 임의로 연관관계에서 표현하기를 : 주
    @ManyToOne(fetch = FetchType.LAZY)
//    @ManyToOne()
    @JoinColumn(name = "item_id")
    private Item item;

    //@ManyToOne 다대일 매핑, (fetch = FetchType.LAZY) 지연로딩 사용, order_id를 외래키로 씀 
    @ManyToOne(fetch = FetchType.LAZY)
//    @ManyToOne()
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; //주문가격

    private int count; //수량

    public static OrderItem createOrderItem(Item item, int count){
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setCount(count);
        orderItem.setOrderPrice(item.getPrice());
        // 주문 후 재고 수량을 주문 수량만큼 빼는 작업
        item.removeStock(count);
        return orderItem;
    }

    public int getTotalPrice(){
        return orderPrice*count;
    }
    // Order에서 호출해서 왔음.
    // orderItem.getItem -> Item에 이는 addstock(수량);
    public void cancel() {
        this.getItem().addStock(count);
    }

}