# 자바 ORM 표준 JPA 프로그래밍 - CH03
---
- JPA가 제공하는 기능 : 엔티티(객체)와 테이블을 맵핑(설계부분) + 맵핑한 객체를 실제 사용
- 엔티티 매니져 : 엔티티를 저장, 수정, 삭제, 조회 -> 즉 엔티티를 저장하는 가상의 DB

## 3.1 엔티티 매니져 팩토리와 엔티티 매니져
- persistence.xml의 정보를 바탕으로 엔티티 매니져 팩토리 객체를 생성(한번만)
```java
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("~~~.xml");
```
- 필요할때마다 엔티티 매니져 팩토리에서 엔티티 매니져를 생성
```java
    EntityManager em = emf.createEntityManager();
```
![ex_screenshot](/img/entitymanager.png)
- 엔티티 매니져 팩토리는 여러 스레드가 동시에 접근해도 안전 : 서로 다른 스레드간 공유
- 엔티티 매니져는 여러 스레드가 동시에 접근하면 동시성 문제발생, 스레드간 공유 X
- 엔티티 매니져는 보통 트렌젝션을 시작할때 커넥션을 획득


## 3.2 영속성 컨텍스트
- 영속성 컨텍스트(Persistence Context) : 엔티티를 영구 저장하는 환경
- 엔티티 매니져로 엔티티를 저장, 조회하면 엔티티 매니져는 영속성 컨텍스트에 엔티티를 보관
```java
    Member member = new Member();
    em.persist(member);
```
- 영속성 컨텍스트는 논리적인 개념(보이지않음)
- 영속성 컨텍스트는 엔티티 매니져를 생성할때 하나 생성됨 / 엔티티매니져를 통해서 접근, 관리가 가능

## 3.3 엔티티 생명주기
- 엔티티의 4가지 상태
    - 비영속(new/transient) : 영속성 컨텍스트와 전혀 관계가 없는 상태
    ```java
        Member member = new Member();   // 엔티티 객체를 생성 : 순수 객체상태, 영속성 컨텍스트에 아직 저장 X
        member.setId("id1");
        member.setName("김민영");   
    ```
    - 영속(managed) : 영속성 컨텍스트에 저장된 상태
    ```java
        em.persist(member);     // 엔티티 매니져를 통해 생성한 엔티티 객체를 영속성 컨텍스르에 저장
                                // 영속성 컨텍스트에 의해 관리된다는 뜻
    ```
    - 준영속(detached) : 영속성 컨텍스트에 저장되었다가 분리된 상태, 영속성 컨텍스트가 관리하지 않으면 준영속 상태
    ```java
        em.detach(member);    // 특정 엔티티를 준영속상태로 만들때 호출되는 메서드
        em.close();           // 영속성 컨텍스트를 닫는다 -> 준영속
        em.clear();           // 영속성 컨텍스트를 초기화 -> 준영속
    ```
    - 삭제(removed) : 삭제된 상태
    ```java
        em.removed(member); // 엔티티를 영속성 컨텍스트와 DB에서 삭제
    ```
![ex_screenshot](/img/entity_Type.png)


## 3.4 영속성 컨텍스트의 특징
- 영속성 컨텍스트와 식별자 값
    - 엔티티를 식별자값(@Id)로 구분
    - 식별자값이 없으면 예외발생
- 영속성 컨텍스트와 DB 저장
    - 트랜젝션을 커밋하는 순간 영속성 컨텍스트에 새로 저장된 엔티티를 DB에 반영 : flush
- 영속성 컨텍스트가 엔티티를 관리할때 장점
    - 1차 캐시
    - 동일성 보장
    - 트랜젝션을 지원하는 쓰기지연
    - 변경감지
    - 지연로딩
- CRUD를 통한 영속성 컨텍스트의 필요성 및 이점
    1. 엔티티 조회
    - 엔티티 컨텍스트 내부캐쉬 : 1차캐쉬, 영속 상태의 엔티티는 모두 이곳에 저장
    - 영속성 컨텍스트 내부에 Map이 있고 키는 @Id로 맵핑한 식별자, 값은 엔티티 객체
    ```java
        // 엔티티 생성 : 비영속
        Member member = new Member();
        member.setId("id1");
        member.setName("김민영");

        // 엔티티 영속 -> 1차캐쉬 내에 엔티티를 저장, 아직 DB에 저장 X
        em.persist(member);

        // 영속성 컨텍스트에 데이터를 저장하고 조회하는 모든기준은 DB의 PK
        // em.find()를 호출하면 먼저 1차캐쉬에서 엔티티를 찾고 만약 없으면 DB에서 조회
        Member findMember = em.find(Member.class, "id1");    // 1차 캐쉬에서 엔티티 조회

        // 조회하고자 하는 엔티티가 1차 캐쉬에 없다면 엔티티매니져는 DB에서 조회해서 엔티티를 생성, 1차 캐쉬에 저장하고 해당 엔티티를 반환
        Member findMember2 = em.find(Member.class, "id2");
        // 메모리상의 1차캐쉬에서 엔티티를 바로 불러와 성능상의 이점이 존재
    ```
    ![ex_screenshot](/img/entityFindDB.png)
    - 영속엔티티의 동일성 보장
    ```java
        Member a = em.find(Member.class, "id1");    // 1차 캐쉬에 있는 엔티티를 조회해서 반환
        Member b = em.find(Member.class, "id1");    // 1차 캐쉬에 있는 엔티티를 조회해서 반환

        System.out.println(a == b);     // 참(a==b), 이 둘은 같은 엔티티 객체이다
    ```
    - <b style="color:red;">즉, 영속성 컨텍스트는 성능상의 이점과 엔티티의 동일성을 보장</b>

    2. 엔티티 등록
    ```java
        EntityManager em = emf.createEntityManager();
        EntityTransection transaction = em.getTransaction();    // 엔티티 매니져는 데이터 변경시 트랜젝션을 시작해야 함
        transaction.begin();    // 트랜젝션 시작

        em.persist(memberA);    // 1차 캐쉬에 엔티티(memberA) 저장
        em.persist(memberB);    // 1차 캐쉬에 엔티티(memberB) 저장, 아직 INSERT 쿼리를 DB에 보내지 않음

        transaction.commit();   // 커밋, DB에 쿼리문을 보냄
    ```
    - 엔티티 매니져는 트랜젝션을 커밋하기 전까지 DB에 엔티티를 저장하지않고 내부저장소(1차캐쉬)에 쿼리문을 저장해둠
    - 트랜젝션을 커밋할때 모아둔 쿼리문을 DB에 보냄 : 트랜젝션을 지원하는 쓰기 지연(transaction write-behind)
    - 영속성 컨텍스트의 변경내용을 DB에 동기화한 후 실제 DB에 트랜젝션을 커밋
    - 쿼리를 그때그때마다 DB에 보내도 트랜젝션을 커밋하지 않으면 아무소용이 없음, 즉 어떻게든 커밋직전에 DB에 쿼리문을 보내면됨
    - <b style="color:red;"> 이를 잘 활용해 모아둔 쿼리문을 DB에 한번에 전달해서 성능을 최적화 할수 있음</b>

    3. 엔티티 수정
    - 수정 SQL문의 문제점
        - SQL문을 사용하여 직접 수정쿼리문을 작성할때, 요구사항이 늘어날때마다 수정 쿼리문이 추가될수 있음
        - 기존에 이름, 나이를 변경하는 쿼리문이 있는데, 회원등급을 변견하는 쿼리문을 추가할때
        - 기존 쿼리문에 합쳐서 수정하거나, 아니면 2개의 수정쿼리문을 작성
        - 즉, 수정쿼리문이 많아지며 비지니스 로직을 분석하기위해 SQL문을 계속 확인 -> SQL에 의존적이게 됨
    - 변경감지
        ```java
            public class JPAmain {
                public static void main(String[] args) {
                    EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
                    EntityManager em = emf.createEntityManager();
                    EntityTransaction transaction = em.getTransaction();

                    //
                    try {
                        transaction.begin();    // 트랜젝션 시작
                        logic(em);              // 비지니스 로직을 실행하는 메서드
                        transaction.commit();   // 커밋(트랜젝션) - 비지니스 로직이 정상작동시
                    } catch (Exception e) {
                        transaction.rollback(); // 롤백(트랜젝션) - 비지니스 로직에서 예외가 발생할때
                    } finally {
                        em.close(); // 사용을 마치면 엔티티 매니져 종료
                    }
                    emf.close();    // 사용을 마치면 엔티티매니져팩토리 종료

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
                    memberA.setUserName("mykim");
                    memberA.setAge(50);
                }
            }
        ```
        - JPA로 엔티티를 수정할때, 단순히 엔티티를 조회해서 데이터만 변경하면 됨, em.update()같은 메서드 X
        - 엔티티의 변경사항을 자동으로 감지 : <b style="color:red;">변경감지(dirty checking)</b>
        - 영속성 컨텍스트에 엔티티를 저장할 때 최초상태를 복사해서 저장(스냅샷), 플러시 시점에 스냅샷과 엔티티를 비교하여 변경사항을 찾음
        - 변경감지는 영속성 컨텍스트가 관리하는 영속상태의 엔티티에만 적용
        - JPA의 기본전략은 변경된 엔티티만 업데이트 하는것이 아니라 모든 엔티티를 업데이트
        - 필드가 많거나 저장되는 내용이 너무 크면 수정된 데이터만 사용해서 동적으로 UPDATE SQL을 생성하면 됨
    
    4. 엔티티 삭제
    - 엔티티를 삭제하려면 먼저 대상 엔티티를 조회
    ```java
        Member member = new Member();

		member.setId(id);
		member.setUserName("hi");
		member.setAge(10);

		em.persist(member); // 엔티티 등록

		Member memberA = em.find(Member.class, "memberA");	// 영속 엔티티 조회
		
		em.remove(memberA);
    ```
    - 삭제쿼리를 쓰기지연 SQL저장소에 등록, 이후 트랜젝션 commit을 플러시를 호출하면 실제 DB에 삭제쿼리전달
    - em.remove(memberA);가 호출되면 memberA 엔티티는 영속성 컨텍스트에서 제거


## 3.5 플러시
- 플러시(Flush) : <b style="color:red;">영속성 컨텍스트의 변경내용을 DB에 반영하는 행위(동기화)</b>
    - 변경감지가 동작해서 영속성 컨텍스트의 모든 엔티티를 스냅샷과 비교해서 변경사항을 찾은다음 수정 SQL를 만들어 지연 SQL저장소에 등록
    - 쓰기 지연 SQL저장소의 모든 쿼리를 DB에 전송(CRUD)

- 영속성 컨텍스트를 플러쉬하는 방법
    1. em.flush(); 호출
    2. 트랜젝션을 commit
    3. JPQL쿼리 실행(부분조회시 사용)
          
## 3.6 준영속
- 준영속 : 영속성 컨텍스트에서 관리하는 엔티티가 영속성 컨텍스트에서 분리되는 것
- 준영속 상태의 엔티티는 영속성 컨텍스트가 제공하는 기능사용 X
- 영속 -> 준영속 만드는 방법  
    1. em.detach(entity) : 특정 엔티티만 준영속상태로 만듬
    2. em.clear() : 영속성 컨텍스트를 초기화, 모든 엔티티를 준영속상태로 만드는 것
    3. em.close() : 영속성 컨텍스트를 종료
- 준영속 상태의 특징
    1. 거의 비영속상태
    2. 식별자 값을 가짐
    3. 지연로딩을 할 수 없음
- 병합(merger)
    - 준영속상태의 엔티티를 다시 영속상태로 만들때 사용
    ```java
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
                System.out.println("em2 contains member : " + em2.contains(member));    // 영속성 컨텍스트가 해당 엔티티를  관리하는지 확인하는 메서드
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
    ```
    - console창
    ```
        member : 준영속
        mergeMember : 준영속
        em2 contains member : false
        em2 contains mergeMember : true
    ```
    - DB 반영결과
    ![ex_screenshot](/img/ch3_dbResult.png)

    - member엔티티(준영속) / mergetMember(영속) 엔티티, 서로 다른 객체
    - 비영속병합 : 비영속상태의 엔티티도 병합을 통해 영속상태로 만들수 있음
    - 병합은 준영속, 비영속을 신경쓰지않음

    
    








