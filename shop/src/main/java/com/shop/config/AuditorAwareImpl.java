package com.shop.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

	// 리턴 타입이 Optional이면 null 값 처리 가능
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = "";
        // 로그인을 했을 경우 null 아니다.
        if(authentication != null){
            userId = authentication.getName();
            System.out.println("audit 실행마다 확인용 코드");
            System.out.println("userId = authentication.getName() 호출 후 : "+userId);
        }
        return Optional.of(userId);
    }

}