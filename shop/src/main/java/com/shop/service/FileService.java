package com.shop.service;

import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

// @Service 스프링 시스템에 서비스임을 알려줌.
@Service
@Log
public class FileService {

	// byte[] fileData :  파일 서비스에서 해당 이미지 파일을 바이트로 읽어서 업로드 함.
    public String uploadFile(String uploadPath, String originalFileName, byte[] fileData) throws Exception{
    	// 129비트의 1aad11c2-7c3c-4a7a-9fba-089ba6e0edc4.jpg 처럼 중복방지위해 이름 표기
        UUID uuid = UUID.randomUUID();
        // 확장자만 출력 되는 부분
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        System.out.println("FileService extension : "+extension);
        String savedFileName = uuid.toString() + extension;
        
        System.out.println("FileService uploadPath : "+uploadPath); 
        // 경로 콘솔에 확인해보기.
        String fileUploadFullUrl = uploadPath + "/" + savedFileName;
        System.out.println("FileService fileUploadFullUrl : "+fileUploadFullUrl);
        
        // 바이트 단위로 해당 경로 출력하기 위한 객체
        FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);
        // byte[] fileData 형의 배열에 쓰기
        fos.write(fileData);
        // 자원반납
        fos.close();
        return savedFileName;
    }

    public void deleteFile(String filePath) throws Exception{
        File deleteFile = new File(filePath);
        if(deleteFile.exists()) {
            deleteFile.delete();
            log.info("파일을 삭제하였습니다.");
        } else {
            log.info("파일이 존재하지 않습니다.");
        }
    }

}