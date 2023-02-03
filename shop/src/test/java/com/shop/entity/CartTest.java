package com.shop.entity;

import com.shop.dto.MemberFormDto;
import com.shop.repository.CartRepository;
import com.shop.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

@SpringBootTest
//@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
//@TestPropertySource(locations="classpath:application.yml")

class CartTest {

	@Autowired
	CartRepository cartRepository;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@PersistenceContext
	EntityManager em;
	private Long id;

	private Member member;

	private LocalDateTime regTime;

	private LocalDateTime updateTime;
	private Cart cart;

	public Member createMember() {
		MemberFormDto memberFormDto = new MemberFormDto();
		memberFormDto.setEmail("test6@email.com");
		memberFormDto.setName("홍길동");
		memberFormDto.setAddress("서울시 마포구 합정동");
		memberFormDto.setPassword("1234");
		System.out.println("createMember 실행함.");
		return Member.createMember(memberFormDto, passwordEncoder);
	}

	@Test
	@DisplayName("장바구니 회원 엔티티 매핑 조회 테스트")
	public void findCartAndMemberTest() {
		Member member = createMember();
		memberRepository.save(member);
		
		
		Cart cart = new Cart();
		cart.setMember(member);
		cartRepository.save(cart);

		// 영속성 컨텍스트(중간저장소) -> 디비에 반영
//		em.flush();
		// 영속성 컨텍스트(중간저장소) 비우기
//		em.clear();

		Cart savedCart = cartRepository.findById(cart.getId()).orElseThrow(EntityNotFoundException::new);
		assertEquals(savedCart.getMember().getId(), member.getId());
		System.out.println("장바구니 회원 엔티티 매핑 조회 테스트 실행함.");
	}

}