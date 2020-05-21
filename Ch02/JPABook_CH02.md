# 자바 ORM 표준 JPA 프로그래밍 - CH02
---
## 2.1 이클립스 설치 & 프로젝트 불러오기
## 2.2 H2 데이터베이스 설치
- H2 DB : H2는 자바로 작성된 관계형 데이터베이스 관리 시스템
- 다운로드 → 압축해제 → /bin/h2.bat 실행
- 예제 테이블 생성 : MEMBER TABLE
```
CREATE TABLE MEMBER (
    ID VARCHAR(255) NOT NULL,
    NAME VARCHAR(255),
    AGE INTEGER NOT NULL,
    PRIMARY KEY (ID)
)
```

## 2.3 라이브러리와 프로젝트 구조
- 라이브러리 관리도구 : 메이븐(Maven) 이용()
- JPA 구현체로 하이버네이트 이용
- pom.xml에 라이브러리 등록

## 2.4 객체맵핑
- Member 클래스와 Member테이블 맵핑 : JPA를 사용하기 위해
- Member Class
```java
package io.jpaStudy.ch02.jpaStudy_ch02;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity 						// 맵핑정보, 해당 클래스(Member)를 테이블(MEMBER)과 맵핑하겠다고 JPA에 알려줌 
@Table(name="MEMBER")			// 맵핑정보, 엔티티 클래스에 맵핑할 테이블 정보(name, Table name속성 사용)

public class Member {
	
	@Id							// 맵핑정보, 엔티티클래스의 필드를 테이블의 pk에 맵핑, 식별자 필드
	@Column(name="ID")			// 맵핑정보, 해당되는 컬럼에 맵핑
	private String id; 			// 아이디(pk)
	
	@Column(name="NAME")		// 맵핑정보
	private String userName; 	// 이름
	
	private Integer age; 		// 나이, 맵핑정보가 없는 필드, 맵핑 어노테이션이 없다면 필드명을 사용해서 컬럼명으로 맵핑

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

}
```

## 2.5 persistence.xml 설정
- JPA 환경설정 파일, 필요한 설정정보를 관리
- JPA설정 : 영속성유닛(persistence-unit)이라는 것으로 시작, 일반적으로 연결할 DB하나당 영속성 유닛을 등록, <b style="color:red;">고유이름을 부여해야함</b>  
```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence" version="2.1">

    <persistence-unit name="jpabook"> <!-- 고유이름 -->

        <properties>

            <!-- 필수 속성 -->
            <property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>	                <!-- JDBC driver, JPA표준속성-->
            <property name="javax.persistence.jdbc.user" value="sa"/>				                <!-- DB접속 id, JPA표준속성 -->
            <property name="javax.persistence.jdbc.password" value=""/>				                <!-- DB접속 pw, JPA표준속성 -->
            <property name="javax.persistence.jdbc.url" value="jdbc:h2:tcp://localhost/~/test"/>	<!-- DB접속 url, JPA표준속성 -->
            <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect" />	        <!-- DB방언 설정, 하이버네이트 속성 -->

            <!--DB방언
            - JPA는 특정 DB에 종속적이지 않은 기술, 손쉽게 DB 교체가능
            - SQL표준을 지키지않거나 특정 DB만의 고유한 기능을 JPA에서는 방언(Dialect)이라 함  -->


            <!-- 옵션 -->
            <property name="hibernate.show_sql" value="true" />
            <property name="hibernate.format_sql" value="true" />
            <property name="hibernate.use_sql_comments" value="true" />
            <property name="hibernate.id.new_generator_mappings" value="true" />

            <!--<property name="hibernate.hbm2ddl.auto" value="create" />-->
        </properties>
    </persistence-unit>

</persistence>

```
- 개발자는 JPA가 제공하는 표준문법에 맞추어 JPA를 사용, 특정 DB에 의존적인 SQL은 DB방언이 처리 : DB가 바뀌면 DB방언만 교체해주면됨


## 2.6 애플리케이션 개발
1. 객체맵핑 : Member.java
2. JPA 설정 : persitence.xml
3. 애플리케이션 시작코드 : JPAmain.java
	- 크게 3부분으로 코드가 구성  
	a. 엔티티 매니져 설정  
	b. 트랜젝션 관리  
	c. 비지니스 로직 


```java
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
```







