package io.jpaStudy.ch03.jpaStudy_ch03;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class ExampleMerge {
	static EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");

	static Member createMember(String id, String username, int age) {
		// 영속성 컨텍스트 1 시작
		EntityManager em1 = emf.createEntityManager();
		EntityTransaction tx1 = em1.getTransaction();
		tx1.begin();

		Member member = new Member();
		member.setId(id);
		member.setUserName(username);
		member.setAge(age); // 비영속 상태

		em1.persist(member); // 영속성 등록

		tx1.commit();

		em1.close(); // 영속성 컨텍스트1 종료, member엔티티는 준영속 상태

		return member; // 준영속상태의 member엔티티 객체 반환
		// 영속성 컨텍스트 1 종료
	}

	static void mergeMember(Member member) {
		// 영속성 컨텍스트2 시작
		EntityManager em2 = emf.createEntityManager();
		EntityTransaction tx2 = em2.getTransaction();
		tx2.begin();
		Member mergeMember = em2.merge(member);
		tx2.commit();

		// 준영속상태 엔티티 member
		System.out.println("member : " + member.getUserName());

		// 영속상태 엔티티 mergeMember
		System.out.println("mergeMember : " + mergeMember.getUserName());
		System.out.println("em2 contains member : " + em2.contains(member));
		System.out.println("em2 contains mergeMember : " + em2.contains(mergeMember));

		em2.close();
		// 영속성 컨텍스트2 종료
	}

	public static void main(String[] args) {
		Member member = createMember("memberA", "영속", 32); // 준영속상태의 엔티티 반환

		member.setUserName("준영속"); // 준영속 상태에서 변경, 이때는 member를 관리하는 영속성 컨텍스트가 없으므로 수정사항을 DB에 반영 불가 

		mergeMember(member);	// merge를 통해 준영속상태의 엔티티를 영속상태의 엔티티로 다시 반환
	}

}
