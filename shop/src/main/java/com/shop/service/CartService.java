package com.shop.service;

import com.shop.dto.CartItemDto;
import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.repository.CartItemRepository;
import com.shop.repository.CartRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityNotFoundException;

import com.shop.dto.CartDetailDto;
import java.util.ArrayList;
import java.util.List;

import org.thymeleaf.util.StringUtils;
import com.shop.dto.CartOrderDto;
import com.shop.dto.OrderDto;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    public Long addCart(CartItemDto cartItemDto, String email){

    	// 2번 service -> 3번 dto = repository
    	// 장바구니에 담긴 상품의 객체 가지고 오기.
        Item item = itemRepository.findById(cartItemDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);
        // 장바구니를 추가한 유저
        Member member = memberRepository.findByEmail(email);

        // 장바구니를 추가한 유저(member.getId())의 장바구니 널 체크
        Cart cart = cartRepository.findByMemberId(member.getId());
        if(cart == null){
        	// 장바구니에 상품을 처음 담을 때, 장바구니 없는 경우(if(cart == null))
        	// 장바구니 새로 만들기(createCart(member))
            cart = Cart.createCart(member);
            // cart 테이블에 반영
            cartRepository.save(cart);
        }

        // 장바구니가 있다면
        // 장바구니에 담긴 상품의 카트 아이디와 아이템 아이디를 조회(findByCartIdAndItemId(cart.getId(), item.getId()))하여 장바구니에 담긴 아이템 가져오기
        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        // 장바구니에 있는 상품을 추가시(if(savedCartItem != null)), 수량을 증가시키기(cartItemDto.getCount())
        if(savedCartItem != null){
            savedCartItem.addCount(cartItemDto.getCount());
            // 디비에 저장
            return savedCartItem.getId();
        } else {
        	// 장바구니에 없는 상품을 추가시 카트id와 아이템id와 수량을 담아(createCartItem(cart, item, cartItemDto.getCount()))
            CartItem cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());
            // cartItem 테이블에 반영
            cartItemRepository.save(cartItem);
            return cartItem.getId();
        }
    }

    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String email){

        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

        Member member = memberRepository.findByEmail(email);
        Cart cart = cartRepository.findByMemberId(member.getId());
        if(cart == null){
            return cartDetailDtoList;
        }

        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());
        return cartDetailDtoList;
    }

    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String email){
        Member curMember = memberRepository.findByEmail(email);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);
        Member savedMember = cartItem.getCart().getMember();

        if(!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())){
            return false;
        }

        return true;
    }

    public void updateCartItemCount(Long cartItemId, int count){
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);
        // 뷰에서 바꾼 count를 업데이트 시킴
        cartItem.updateCount(count);
    }

    public void deleteCartItem(Long cartItemId) {
    	// 삭제 시 요청한 장바구니_상품 아이디로 검색해서, 디비에 있는지 조회
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);
        // 실제 삭제 로직. (delete) 기본으로 제공하는 로직
        cartItemRepository.delete(cartItem);
    }

    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email){
        List<OrderDto> orderDtoList = new ArrayList<>();

        //cartOrderDtoList 에 들어 있는 데이터를 하나씩 CartOrderDto형으로 꺼내서 cartOrderDto에다가 담음
        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
        	// 장바구니 아이템이 있는지 여부 조회
        	// 2 service -> 3 dto(repository)
            CartItem cartItem = cartItemRepository
                            .findById(cartOrderDto.getCartItemId())
                            .orElseThrow(EntityNotFoundException::new);

            // 장바구니에 담긴 상품을 다시 디비에서 조회 후
            // 이것을 주문을 하기 위한 OrderDto 타입으로 다시 재할당.
            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());
            // 리스트에 추가하는 작업
            orderDtoList.add(orderDto);
        }

        // orderService의 orders메서드 수행
        // 주문 전용 DTO에 다 담은 후, 주문 로직 처리.
        Long orderId = orderService.orders(orderDtoList, email);
        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
        	// 2 service -> 3 DTO(repository)
            CartItem cartItem = cartItemRepository
                            .findById(cartOrderDto.getCartItemId())
                            .orElseThrow(EntityNotFoundException::new);
            cartItemRepository.delete(cartItem);
        }

        return orderId;
    }

}