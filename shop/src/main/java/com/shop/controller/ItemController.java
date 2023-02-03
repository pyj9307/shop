package com.shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.ui.Model;
import com.shop.dto.ItemFormDto;

import com.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import javax.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import javax.persistence.EntityNotFoundException;

import com.shop.dto.ItemSearchDto;
import com.shop.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Controller
//@RequiredArgsConstructor : not null, final이 붙은 멤버도 생성자로 생성해달라. 롬복 라이브러리에 있는 에너테이션
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 상품 등록 입력 폼
    // /admin/item/new 주소로 요청이 들어오면 그냥 ItemController로 들어오게끔 설정해둠.
    @GetMapping(value = "/admin/item/new")
    public String itemForm(Model model){
        model.addAttribute("itemFormDto", new ItemFormDto());
        return "item/itemForm";
    }
    // 상품 등록 처리 로직
    // /admin/item/new 주소에서 포스팅할 때(등록) 그냥 ItemController로 들어오게끔 설정해둠.
    @PostMapping(value = "/admin/item/new")
    // @Valid 입력 폼에서 유효성 체크 실시
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult,
    						// @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList : 
    						// @RequestParam으로 뷰에서 itemImgFile 이름으로 매핑된 인자를 가져와서 MultipartFile형식으로 리스트를 만들어 itemImgFileList이름의 객체에 집어넣음.
                          Model model, @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList){

    	// 상품 등록시 일반 데이터
        if(bindingResult.hasErrors()){
        	// 유효성 체크 에러가 생기면 다시 item/itemForm으로 돌아간다.
            return "item/itemForm";
        }

    	// 상품 등록시 파일 데이터
        if(itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값 입니다.");
            return "item/itemForm";
        }

        try {
            itemService.saveItem(itemFormDto, itemImgFileList);
        } catch (Exception e){
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }

        return "redirect:/";
    }

    // 상품관리 테이블에서 해당 상품명 클릭시, 받게되는 주소.
    @GetMapping(value = "/admin/item/{itemId}")
    public String itemDtl(@PathVariable("itemId") Long itemId, Model model){

        try {
        	// 해당 클릭시 상품 아이디를 가지고 메서드를 호출.
        	// 결과값은 한 상품의 ㅏㅇ세페이지 내용을 가지고 옴.
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            // 해당 뷰에 키 to 값 형태로 model에 등록해서 전달.
            model.addAttribute("itemFormDto", itemFormDto);
        } catch(EntityNotFoundException e){
            model.addAttribute("errorMessage", "존재하지 않는 상품 입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
            return "item/itemForm";
        }

        return "item/itemForm";
    }

    // 상품 수정시 처리하는 로직.
    @PostMapping(value = "/admin/item/{itemId}")
    public String itemUpdate(@Valid ItemFormDto itemFormDto, BindingResult bindingResult,
                             @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList, Model model){
        if(bindingResult.hasErrors()){
            return "item/itemForm";
        }

        if(itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값 입니다.");
            return "item/itemForm";
        }

        try {
        	// 다시 해당 상품 번호로 재등록함. 일반 데이터, 파일 데이터.
            itemService.updateItem(itemFormDto, itemImgFileList);
        } catch (Exception e){
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }

        return "redirect:/";
    }

    @GetMapping(value = {"/admin/items", "/admin/items/{page}"})
    public String itemManage(ItemSearchDto itemSearchDto, @PathVariable("page") Optional<Integer> page, Model model){

    	// 0 : 조회할 페이지 번호.
    	// 3 : 가지고 올 데이터 수.
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 3);
        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 5);

        return "item/itemMng";
    }

    // 상세 페이지 부분을 받는 곳.
    @GetMapping(value = "/item/{itemId}")
    public String itemDtl(Model model, @PathVariable("itemId") Long itemId){
    	
    	// 상세페이지에 필요한 정보를 담는 객체로 리턴.
        ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
        // 뷰에 item 키로, 값은 : itemFormDto
        // 상품 등록시에 필요한 정보들이 다 있음.
        model.addAttribute("item", itemFormDto);
        // item/itemDtl 상세 페이지 반환
        return "item/itemDtl";
    }

}