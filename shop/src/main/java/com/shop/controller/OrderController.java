package com.shop.controller;

import com.shop.dto.OrderDto;
import com.shop.service.OrderService;
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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.shop.dto.OrderHistDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ResponseEntity 리턴 타입에 http 상태 코드를 같이 전송하겠다는 의미.
    // @ResponseBody : 응답 객체에 전달하는 방식을 json 형식으로 전달하겠다.
    // @RequestBody : 요청 객체에서 전달된 json -> java 객체 변환.
    // spring framework, jackson lib : ObjectMapper, 자바 객체 -> json 객체(중간데이터)
    // @Valid : OrderDto에 걸려있던 @NotNull, @Min, @Max 등 유효성 검사를 실행하겠다.
    @PostMapping(value = "/order")
    public @ResponseBody ResponseEntity order(@RequestBody @Valid OrderDto orderDto
            , BindingResult bindingResult, Principal principal){
    	
    	// 에러 체크실행시 유효성 체크를 위반했을 시 동작하는 기능
        if(bindingResult.hasErrors()){
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }

            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        // 정상 동작.
        // principal : 시큐리티 로그인을 관리하는데, 설정에서 로그인의 파라미터 타입 : email
        String email = principal.getName();
        Long orderId;

        try {
            orderId = orderService.order(orderDto, email);
        } catch(Exception e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }

    @GetMapping(value = {"/orders", "/orders/{page}"})
    public String orderHist(@PathVariable("page") Optional<Integer> page, Principal principal, Model model){

    	// Pageable 페이징 처리가 필요한 부분에 대해서 사용한다. 부트에서는 인터페이스로 만들어서
    	// 잉요하게 편하게 구성을 했음.
    	// PageRequest.of(page.isPresent() ? page.get() : 0, 4); 페이지설정이 없다면 page 설정을 0, 4로 한다. 0 -> 1페이지 의미, 4 -> 한 페이지 보여줄 갯수
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 4);
        // 현재 위치 1번 -> 서비스 orderService
        // 경과값 : 구매자가 구매한 상품들과 이미지가 담겨있네.
        Page<OrderHistDto> ordersHistDtoList = orderService.getOrderList(principal.getName(), pageable);

        // 뷰에 전달하기 orders의 아이디로 지정해서 ordersHistDtoList를 전달하고, page의 아이디로 페이지 설정 전달하고, maxPage의 아이디로 페이지 최대표시 전달
        model.addAttribute("orders", ordersHistDtoList);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("maxPage", 5);

        return "order/orderHist";
    }

    // RestController 형식의 비동기식 데이터 처리 방식
    // 웹 브라우저에서 요청(비동기방식 ajax) -> 서버에서도 응딥을 데이터만 리턴 해줌.
    @PostMapping("/order/{orderId}/cancel")
    public @ResponseBody ResponseEntity cancelOrder(@PathVariable("orderId") Long orderId , Principal principal){

    	// 현재 위치 1번 -> 2번 서비스로
    	// 두 이메일이 동일하면 -> true, 앞에 !있으므로 -> false
    	System.out.println("결과 확인 해보자 orderService.validateOrder(orderId, principal.getName()) : "+orderService.validateOrder(orderId, principal.getName()));
        if(!orderService.validateOrder(orderId, principal.getName())){
            return new ResponseEntity<String>("주문 취소 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

    	// 정상적인 취소 서비스를 구현, 현재 위치 1번 -> 2번 서비스로
        // 주문의 상태도 변경, 주문상품의 수량도 원래대로 복구.
        orderService.cancelOrder(orderId);
        // 주문 취소가 다 되면, 리턴으로 주문 아이디와 정상 코드 200을 브라우저로 응답.
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }

}