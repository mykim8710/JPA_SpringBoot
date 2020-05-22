package io.jpaStudy.ch03.jpaStudy_ch03;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JPAmain {
	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
		EntityManager em = emf.createEntityManager();
		EntityTransaction transaction = em.getTransaction();

		//
		try {
			transaction.begin(); // 트랜젝션 시작
			logic(em); // 비지니스 로직을 실행하는 메서드
			transaction.commit(); // 커밋(트랜젝션) - 비지니스 로직이 정상작동시
		} catch (Exception e) {
			transaction.rollback(); // 롤백(트랜젝션) - 비지니스 로직에서 예외가 발생할때
		} finally {
			em.close(); // 사용을 마치면 엔티티 매니져 종료
		}
		emf.close(); // 사용을 마치면 엔티티매니져팩토리 종료

	}

	private static void logic(EntityManager em) {
		String id = "memberA";
		Member member = new Member();

		member.setId(id);
		member.setUserName("hi");
		member.setAge(10);

		em.persist(member); // 엔티티 등록

		Member memberA = em.find(Member.class, "memberA");	// 영속 엔티티 조회
		
		// 영속 엔티티 데이터 수정
		// memberA.setUserName("mykim");
		// memberA.setAge(50);
		
		em.remove(memberA);

	}
}
