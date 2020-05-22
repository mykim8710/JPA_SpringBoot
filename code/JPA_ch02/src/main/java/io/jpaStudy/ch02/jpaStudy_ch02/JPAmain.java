package io.jpaStudy.ch02.jpaStudy_ch02;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;


public class JPAmain {
	public static void main(String[] args) {
		
		// 1. 엔티티 매니져 설정
		// Pesistence.xml의 설정정보를 이용해서 엔티티 매니져 팩토리 객체 생성  : <persistence-unit name="jpabook">
		// JPA를 동작시키기 위한 기반객체를 생성, 
		// 애플리케이션 전체에 딱 한번만 생성, 공유해서 사용
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");	
		
		// 엔티티 매니저 객체 생성 : JPA의 기능 대부분을 제공(CRUD)
		// 내부에 DB커넥션을 유지하면서 DB와 통신
		EntityManager em = emf.createEntityManager();
		
		// 2. 트렌젝션 관리
		// JPA를 사용하면 항상 트렌젝션안에서 데이터를 변경해야함
		// 트렌젝션 획득, 엔티티 매니져에서 트렌젝션 API를 받아옴
		EntityTransaction et = em.getTransaction();
		
		// 
		try {
			et.begin();		// 트랜젝션 시작
			logic(em);		// 비지니스 로직을 실행하는 메서드
			et.commit(); 	// 커밋(트랜젝션) - 비지니스 로직이 정상작동시
		} catch (Exception e) {
			et.rollback(); 	// 롤백(트랜젝션) - 비지니스 로직에서 예외가 발생할때
		} finally {
			em.close(); 	// 사용을 마치면 엔티티 매니져 종료
		}
		emf.close(); 		// 사용을 마치면 엔티티매니져팩토리 종료
		
	}
	
	// 3. 비지니스 로직
	// Member객체 하나를 생성하고 엔티티 매니져를 통해 DB에 CRUD
	private static void logic(EntityManager em) {
		String id = "id1";
        Member member = new Member();
        
        member.setId(id);
        member.setUserName("mykim");
        member.setAge(34);
        
        // 등록(INSERT INTO (ID, NAME, AGE) VALUES ("id1", "mykim", 34))
        em.persist(member);	
        
        // 수정(UPDATE) : 객체의 변수만 변경해주면 UPDATE SQL문을 생성해서 변경
        member.setAge(32); 	
        
        // 한건조회(SELECT WHERE = 'id1'?)
        Member findMember = em.find(Member.class, id);	
        System.out.println("findMember : " +findMember.getUserName());
        
        // 목록조회(SELECT ALL) : JPQL
        // 애플리케이션이 필요한 데이터만 DB에서 불러오려면 검색조건이 포함된 SQL문이 필요 : JPQL(Java Persistence Query Language)로 해결
        // "select m from Member m" : from Member는 회원객체임, MEMBER테이블이 아님
        
        
        
        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
        System.out.println("members.size=" + members.size());
        
        // 삭제(DELETE)
        em.remove(member);   
        
	}
}
