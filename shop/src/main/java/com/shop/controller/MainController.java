package com.shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;

    // 메인은 무조건, 컨트롤러에 매핑 후, 메인 화면이 그려진다.
    @GetMapping(value = "/")
    public String main(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model){
    	// 0 : 조회할 페이지 번호.
    	// 6 : 가지고 올 데이터 수.
    	// 메인에 보여 줄 페이지에 보여줄 아이템 개수.
    	// 페이징 처리시, 화면에는 1이라고 보이지만, 실제 페이지는 0이라고 인식.
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 6);
        // 메인에 보여줄 상품을 반환받는 서비스입니다.
        // 정방항으로 하면 1 -> 2 -> 3번 순서로 진행.
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 5);

        return "main";
    }

}