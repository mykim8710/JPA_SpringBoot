package io.jpaStudy.ch03.jpaStudy_ch03;

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
