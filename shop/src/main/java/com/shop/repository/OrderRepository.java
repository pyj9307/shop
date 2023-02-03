package com.shop.repository;

import com.shop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

	
	// 3번 OrderRepository
	// 구매자가 구매한 상품들을 가지고 와야 하는데, 조건절에서, where email을 이용해서 구매자의 email을 보고 구매자가 구매한 상품들을 가지고 옴
	// 정렬은 최신순 : desc
	// 기본 쿼리 메소드는 문법이 findBy(해당컬럼)문법 조건이 있음.
	// 여기서는 문법이 기존 문법이 아니고, 사용자가 정의를 한 메서드 명이고
	// 그래서 따로 sql문법이 필요한데, jpa JPQL = sql 비슷한 문법의 형식임.
	// :email 이 부분에 동적 파라미터 값이 할당이 됨.
	// 결론, 결과값 : 파라미터로 넘어온 해당 이메일의 구매자가 구매한 상품들을 리스트로 반환함.
    @Query("select o from Order o " +
            "where o.member.email = :email " +
            "order by o.orderDate desc"
    )
    List<Order> findOrders(@Param("email") String email, Pageable pageable);

    // 구매한 상품의 총갯수. 반환함.
    @Query("select count(o) from Order o " +
            "where o.member.email = :email"
    )
    Long countOrder(@Param("email") String email);
}