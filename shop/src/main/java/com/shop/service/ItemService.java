package com.shop.service;

import com.shop.dto.ItemFormDto;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import com.shop.dto.ItemImgDto;
import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;

import com.shop.dto.ItemSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.shop.dto.MainItemDto;

//@Service 스프링 시스템에 서비스임을 알려줌.
@Service
//@RequiredArgsConstructor : not null, final이 붙은 멤버도 생성자로 생성해달라. 롬복 라이브러리에 있는 에너테이션
@RequiredArgsConstructor
//@Transactional 클래스안에 메서드들 중 한개라도 작동이 안되면 전체 메서드를 롤백한다.
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;

    private final ItemImgService itemImgService;

    private final ItemImgRepository itemImgRepository;

    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception{

        // ItemFormDto에 createItem메서드를 이용해서 상품 등록
        Item item = itemFormDto.createItem();
        itemRepository.save(item);

        //이미지 등록
        for(int i=0;i<itemImgFileList.size();i++){
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);

            if(i == 0)
            	// setRepimgYn : 대표이미지 여부 설정
                itemImg.setRepimgYn("Y");
            else
                itemImg.setRepimgYn("N");

            itemImgService.saveItemImg(itemImg, itemImgFileList.get(i));
        }

        return item.getId();
    }

    // @Transactional(readOnly = true) : 트랜잭션의 읽기 전용 설정, JPA가 더티체킹(변경감지)을 수행하지 않아서 성능을 향상시킬 수 있음.
    // 메인에서 해당 상품 클릭 시, 상세화면 처리 부분
    @Transactional(readOnly = true)
    public ItemFormDto getItemDtl(Long itemId){
    	// 상품 아이디에 해당하는 이미지 불러오기.
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
        // 엔티티 클래스 타입, dto 클래스 타입으로 변환 작업.
        List<ItemImgDto> itemImgDtoList = new ArrayList<>();
        for (ItemImg itemImg : itemImgList) {
            ItemImgDto itemImgDto = ItemImgDto.of(itemImg);
            itemImgDtoList.add(itemImgDto);
        }
        // 상품 번호로, 아이템 엔티티 객체를 리턴.
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);
        // Item <---> ItemFormDto 매핑된 정보를 ItemFormDto에다 넣어줌. 형변형.
        ItemFormDto itemFormDto = ItemFormDto.of(item);
        itemFormDto.setItemImgDtoList(itemImgDtoList);
        return itemFormDto;
    }

    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception{
        //상품 수정
        Item item = itemRepository.findById(itemFormDto.getId())
                .orElseThrow(EntityNotFoundException::new);
        item.updateItem(itemFormDto);
        List<Long> itemImgIds = itemFormDto.getItemImgIds();

        //이미지 등록
        for(int i=0;i<itemImgFileList.size();i++){
            itemImgService.updateItemImg(itemImgIds.get(i),
                    itemImgFileList.get(i));
        }

        return item.getId();
    }

    // @Transactional(readOnly = true) : 단순 조회만 하면 되는데, 트랜잭션 부분을 더티 체킹을 한다면 매번 메인을 불러올 때 마다, 성능 저하 우려가 있어서, 읽기만 하기. 
    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }

}