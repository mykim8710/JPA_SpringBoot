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
    -<b style="color:red;"> 이를 잘 활용해 모아둔 쿼리문을 DB에 한번에 전달해서 성능을 최적화 할수 있음</b>

    3. 엔티티 수정
    









