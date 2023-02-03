package com.shop.controller;
import com.shop.dto.CartItemDto;
import com.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

import com.shop.dto.CartDetailDto;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.DeleteMapping;

import com.shop.dto.CartOrderDto;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping(value = "/cart")
    public @ResponseBody ResponseEntity order(@RequestBody @Valid CartItemDto cartItemDto, BindingResult bindingResult, Principal principal){

        if(bindingResult.hasErrors()){
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }

            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        String email = principal.getName();
        Long cartItemId;

        try {
        	// 1번 controller -> 2번 service 이메일과 장바구니에 담긴 상품의 객체
        	// 장바구니에 처음 상품을 담을 경우, 이미 상품이 담아져 있다면 처리하는 로직.(2번 service에 설명 있음)
            cartItemId = cartService.addCart(cartItemDto, email);
        } catch(Exception e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        // cartItemId와 Http상태코드 200번을 뷰에 리턴함.
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @GetMapping(value = "/cart")
    public String orderHist(Principal principal, Model model){
        List<CartDetailDto> cartDetailList = cartService.getCartList(principal.getName());
        model.addAttribute("cartItems", cartDetailList);
        return "cart/cartList";
    }

    // 1-1번 장바구니에서 수량만 업데이트 하는 부분
    @PatchMapping(value = "/cartItem/{cartItemId}")
    // 뷰(cartList.html)에서 cartItemId 에 따라서 cartItemId, count, principal 받아옴. 뷰에서 changeCount(this)로 계속 바꾸면 비동기식으로 정보를 받아온다.
    public @ResponseBody ResponseEntity updateCartItem(@PathVariable("cartItemId") Long cartItemId, int count, Principal principal){

        if(count <= 0){
            return new ResponseEntity<String>("최소 1개 이상 담아주세요", HttpStatus.BAD_REQUEST);
        } else if(!cartService.validateCartItem(cartItemId, principal.getName())){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        // 1번 controller -> 2번 service
        cartService.updateCartItemCount(cartItemId, count);
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    // 2-1번 장바구니에서 상품 취소하는 부분
    @DeleteMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity deleteCartItem(@PathVariable("cartItemId") Long cartItemId, Principal principal){

        if(!cartService.validateCartItem(cartItemId, principal.getName())){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        // 1번 controller -> 2번 service 삭제하고 끝, 서비스 기능에서 리턴 없음
        cartService.deleteCartItem(cartItemId);

        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    // 3-1번 장바구니에 담긴 상품 주문하기
    // @ResponseBody :@RestController와 방식이 같다.
    // 응답을 자바 객체 -> JSON 형태로 변환해서 브라우저에게 전달함.
    // orderId를 전달하면서 동시에 서버 상태코드도 전달하는 용도(HttpStatus.OK = 200)
    @PostMapping(value = "/cart/orders")
    public @ResponseBody ResponseEntity orderCartItem(@RequestBody CartOrderDto cartOrderDto, Principal principal){
    	// 브라우저(뷰)에서 장바구니에 담긴 상품들을 배열에 담아서 전달했고\
    	// 서버에서는 List<CartOrderDto>형으로 CartOrderDto객체로 매핑 리스트(cartOrderDtoList)로 받아서 매핑이 된 객체를 게터로 가지고 왔다(getCartOrderDtoList();)
        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();

        if(cartOrderDtoList == null || cartOrderDtoList.size() == 0){
            return new ResponseEntity<String>("주문할 상품을 선택해주세요", HttpStatus.FORBIDDEN);
        }

        for (CartOrderDto cartOrder : cartOrderDtoList) {
            if(!cartService.validateCartItem(cartOrder.getCartItemId(), principal.getName())){
                return new ResponseEntity<String>("주문 권한이 없습니다.", HttpStatus.FORBIDDEN);
            }
        }

        // 상품 등록 절차, 1번 controller -> 2번 service 주문 처리가 완료된 아이디 반환해서 받아옴.
        Long orderId = cartService.orderCartItem(cartOrderDtoList, principal.getName());
        // orderId를 전달하면서 동시에 서버 상태코드도 전달함(HttpStatus.OK = 200)
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }

}