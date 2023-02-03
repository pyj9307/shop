package com.shop.entity;

import com.shop.constant.ItemSellStatus;
import com.shop.repository.ItemRepository;
import com.shop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderItemRepository;

@SpringBootTest
@TestPropertySource(locations="classpath:application-test.properties")
//@TestPropertySource(locations="classpath:application.properties")
//@Transactional
class OrderTest {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ItemRepository itemRepository;

    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;
//
    @Autowired
    OrderItemRepository orderItemRepository;

    public Item createItem(){
        Item item = new Item();
        item.setItemNm("테스트 상품11");
        item.setPrice(10000);
        item.setItemDetail("상세설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        item.setRegTime(LocalDateTime.now());

        item.setUpdateTime(LocalDateTime.now());
        System.out.println("테스트 상품 하나 만들기");
        return item;
    }
//
    @Test
    @DisplayName("영속성 전이 테스트")
    public void cascadeTest() {

        Order order = new Order();

        for(int i=0;i<3;i++){
            Item item = this.createItem();
            itemRepository.save(item);
            
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(10);
            orderItem.setOrderPrice(1000);
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        orderRepository.save(order);
//        em.clear();


        System.out.println("order.getOrderItems().size() : "+order.getOrderItems().size());
//        System.out.println("order.getId() 호출 전 : "+order.getId());
//        Order savedOrder = orderRepository.findById(order.getId())
//                .orElseThrow(EntityNotFoundException::new);
//        System.out.println("order.getId() 호출 후 : "+savedOrder.getOrderItems().size());
//        System.out.println("savedOrder.getOrderItems().size() : "+savedOrder.getOrderItems().size());
//        assertEquals(3, savedOrder.getOrderItems().size());
        System.out.println("영속성 전이 테스트 확인");
    }

    public Order createOrder(){
        Order order = new Order();
        for(int i=0;i<3;i++){
            Item item = createItem();
            itemRepository.save(item);
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(10);
            orderItem.setOrderPrice(1000);
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }
        
        Member member = new Member();
        memberRepository.save(member);
        
        order.setMember(member);
        orderRepository.save(order);
        return order;
    }

    @Test
    @DisplayName("고아객체 제거 테스트")
    public void orphanRemovalTest(){
        Order order = this.createOrder();
        System.out.println("고아객체 제거 전 테스트1 : "+order.getOrderItems().size());
        order.getOrderItems().remove(0);
        System.out.println("고아객체 제거 후 테스트2 : "+order.getOrderItems().size());
        // 메모리 상에 리스트에서 제거만 하고, 디비상에 반영을 안해서, 동작을 제대로 못했음
        // 그래서 아래에 디비 반영 추가하기.
        orderRepository.save(order);
        
        // 수동으로 디비에 반영하는 명령어인데
        // 트랜잭션을 사용하지 않아서 동작 안함
//        em.flush();
    }

    @Test
    @DisplayName("지연 로딩 테스트")
    public void lazyLoadingTest(){
        Order order = this.createOrder();
        Long orderItemId = order.getOrderItems().get(0).getId();
        
        // 수동으로 디비에 반영하는 명령어인데
        // 트랜잭션을 사용하지 않아서 동작 안함
//        em.flush();
//        em.clear();
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(EntityNotFoundException::new);
        System.out.println("Order class : " + orderItem.getOrder().getClass());
        System.out.println("===========================");
//        orderItem.getOrder().getOrderDate();
//        System.out.println("===========================");
    }

}