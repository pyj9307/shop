package com.shop.service;

import com.shop.entity.ItemImg;
import com.shop.repository.ItemImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;
import javax.persistence.EntityNotFoundException;

//@Service 스프링 시스템에 서비스임을 알려줌.
@Service
//@RequiredArgsConstructor : not null, final이 붙은 멤버도 생성자로 생성해달라. 롬복 라이브러리에 있는 에너테이션
@RequiredArgsConstructor
//@Transactional 클래스안에 메서드들 중 한개라도 작동이 안되면 전체 메서드를 롤백한다.
@Transactional
public class ItemImgService {

	// application.properties 에 잡힌 itemImgLocation=C:/shop/item 안에 있는 파일을 사용하겠다.
    @Value("${itemImgLocation}")
    private String itemImgLocation;

    private final ItemImgRepository itemImgRepository;

    //ItemImgService 에서 FileService를 final로 DI(주입)했음(@RequiredArgsConstructor가 있어서)
    private final FileService fileService;

    public void saveItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws Exception{
    	// oriImgName에 .getOriginalFilename()이라는 내장 메서드를 이용해서 파일 이름을 집어 넣음.
        String oriImgName = itemImgFile.getOriginalFilename();
        System.out.println("ItemImgService oriImgName : "+oriImgName);
        String imgName = "";
        String imgUrl = "";

        // 파일 업로드
        // if(!StringUtils.isEmpty(oriImgName))파일이 있다면, fileService에서 uploadFile 메서드로 (String uploadPath, String originalFileName, byte[] fileData) 해당하는 값을 넣어줌.
        if(!StringUtils.isEmpty(oriImgName)){
            imgName = fileService.uploadFile(itemImgLocation, oriImgName,
                    itemImgFile.getBytes());
            System.out.println("ItemImgService imgName : "+imgName);
            // WebMvcConfig.java 파일에 설정 부분에
            // uploadPath=file:///C:/shop/
            // registry.addResourceHandler("/images/**")
            // .addResourceLocations(uploadPath);
            imgUrl = "/images/item/" + imgName;
            System.out.println("ItemImgService imgUrl : "+imgUrl);
        }

        // 상품 이미지 정보 저장
        // 엔티티 클래스 객체에 멤버로 값을 재할당.
        itemImg.updateItemImg(oriImgName, imgName, imgUrl);
        // itemImg 이미지를 데이터베이스에 반영
        itemImgRepository.save(itemImg);
    }

    // 이름은 같지만 ItemImg 엔티티가 아닌 ItemImgService 서비스에 있는 메서드
    public void updateItemImg(Long itemImgId, MultipartFile itemImgFile) throws Exception{
        if(!itemImgFile.isEmpty()){
            ItemImg savedItemImg = itemImgRepository.findById(itemImgId)
                    .orElseThrow(EntityNotFoundException::new);

            //기존 이미지 파일 삭제
            if(!StringUtils.isEmpty(savedItemImg.getImgName())) {
                fileService.deleteFile(itemImgLocation+"/"+
                        savedItemImg.getImgName());
            }

            // 수정 시, 새로 입력된 파일의 이미지를 다시 수정하는 역할.
            String oriImgName = itemImgFile.getOriginalFilename();
            String imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
            String imgUrl = "/images/item/" + imgName;
            savedItemImg.updateItemImg(oriImgName, imgName, imgUrl);
        }
    }

}