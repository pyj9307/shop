package com.shop.service;

import com.shop.dto.OrderDto;
import com.shop.entity.*;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.shop.dto.OrderHistDto;
import com.shop.dto.OrderItemDto;
import com.shop.repository.ItemImgRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.thymeleaf.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final ItemRepository itemRepository;

    private final MemberRepository memberRepository;

    private final OrderRepository orderRepository;

    private final ItemImgRepository itemImgRepository;

    public Long order(OrderDto orderDto, String email){

        Item item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);

        Member member = memberRepository.findByEmail(email);

        List<OrderItem> orderItemList = new ArrayList<>();
        OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
        orderItemList.add(orderItem);
        Order order = Order.createOrder(member, orderItemList);
        orderRepository.save(order);

        return order.getId();
    }

    @Transactional(readOnly = true)
    // Pageable 이라는 인자는 OrderController에서 가지고 왔다(페이지 구성을 하는 기능).
    public Page<OrderHistDto> getOrderList(String email, Pageable pageable) {

    	// 구매자가 구매한 물품의 주문들, 총 갯수
    	// 현재 위치 2번 서비스 -> 3번 dao = Repository가 역할 대신함
    	// 해당 구매자가 구입한 물품을 조회해서, 상품들을 가지고 옴(해당 유저를 파악하기 위해서 이메일 필요)
    	// orders : 해당 구매자가 구입한 물품들.
    	// totalCount : 구매자가 구매한 물품의 주문들, 총 갯수
        List<Order> orders = orderRepository.findOrders(email, pageable);
        Long totalCount = orderRepository.countOrder(email);

        // 리스트 orderHistDtos : 주문 기록 전달하는 DTO 객체
        List<OrderHistDto> orderHistDtos = new ArrayList<>();

        for (Order order : orders) {
        	// orders에서 꺼낸 상품의 타입은 Entity형이고
        	// 다시, OrderHistDto 타입으로 반환.
            OrderHistDto orderHistDto = new OrderHistDto(order);
            
            // getOrderItems메서드로 order객체에서 orderItems리스트에 담긴 데이터를 orderItems로 옮김.
            // 엔티티클래스 order, 멤버 중에 orderItems라는 멤버가 있고
            // 해당 멤버에서 롬복 게터에 의해서, 한번에 리스트를 가지고 옴.
            // 예) List<Order> orders에 담긴 8번의 주문 중에서 for (Order order : orders)를 이용해 하나의 주문을 선택하고, 해당 주문에 담겨있는 상품의 목록을 가지고 옴.
            List<OrderItem> orderItems = order.getOrderItems();
            // 8번의 주문 중에서 하나의 주문을 선택하고, 하나의 주문에서 여러 개 상품들을 꺼내고
            // 여러 개 상품에서 -> 하나씩 상품 꺼내기
            for (OrderItem orderItem : orderItems) {
            	
            	// findByItemIdAndRepimgYn메서드를 이용해서 itemId와 repimgYn(대표이미지)를 사용해서 itemImg에 담음 
            	// 꺼낸 하나의 상품에서 해당 이미지를 가지고 옴.
            	// 결과값 : 상품아이디 & 대표이비지 여부 Y인 이미지를 조회해서 가지고 옴.
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepimgYn
                        (orderItem.getItem().getId(), "Y");
                
                // 상품 정보와 상품 이미지 정보를 orderHistDto에 담아서
                // 컨트롤러에 반환해서 -> 뷰에 이 리스트 넘겨서 -> 뷰에서 사용 가능
                OrderItemDto orderItemDto =
                        new OrderItemDto(orderItem, itemImg.getImgUrl());
                orderHistDto.addOrderItemDto(orderItemDto);
            }

            // List<OrderHistDto> orderHistDtos = new ArrayList<>(); 리스트에 추가하는 작업.
            orderHistDtos.add(orderHistDto);
        }

        return new PageImpl<OrderHistDto>(orderHistDtos, pageable, totalCount);
    }

    // 취소 작업 부분
    @Transactional(readOnly = true)
    public boolean validateOrder(Long orderId, String email){
    	// 현위치 2번 -> 3번 memberRepository
    	// 이메일로 현재 멤버 객체 반환.
        Member curMember = memberRepository.findByEmail(email);
        // 
        // orderId 취소 하려는 주문의 아이디에 해당하는 주문의 값 불러오기
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new);
        // 주문한 멤버를 불러오기
        Member savedMember = order.getMember();

        // 두 이메일의 값이 같지 않다면 -> false
        if(!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())){
            return false;
        }

        // 두 이메일의 값이 같다면 -> true
        return true;
    }

    // void 리턴값이 없다 -> 캔슬기능만 하고 뷰페이지 반환하는건 없음.
    public void cancelOrder(Long orderId){
    	// 파라미터로 넘어온 orderId(취소할 주문 아이디) 에 해당하는 디비 내용 불러오기.
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new);
        // 주문 취소 할 주문 객체로 메소드 호출
        order.cancelOrder();
    }

    public Long orders(List<OrderDto> orderDtoList, String email){

    	// 디비에서 구매자 확인.
        Member member = memberRepository.findByEmail(email);
        // orderDtoList 리스트 -> orderItemList 재할당
        List<OrderItem> orderItemList = new ArrayList<>();

        // orderDtoList에서 하나씩 꺼내 orderDto를 이용해서 해당 상품을 조회해서
        // 상품을 다시 orderItemList에 추가
        for (OrderDto orderDto : orderDtoList) {
            Item item = itemRepository.findById(orderDto.getItemId())
                    .orElseThrow(EntityNotFoundException::new);

            OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
            // 상품을 orderItemList에 추가
            orderItemList.add(orderItem);
        }

        // 주문 상품 리스트가 만들어지고 -> 주문으로 전달.
        // 주문 처리 로직.
        Order order = Order.createOrder(member, orderItemList);
        // 주문 기록을 디비에 반영
        orderRepository.save(order);

        // 주문 아이디 반환
        return order.getId();
    }

}