package net.csdn.blog.chaijunkun.entity;

import javax.validation.constraints.NotNull;

import net.csdn.blog.chaijunkun.validation.group.StudentGroup.Add;
import net.csdn.blog.chaijunkun.validation.group.StudentGroup.Del;
import net.csdn.blog.chaijunkun.validation.group.StudentGroup.Get;
import net.csdn.blog.chaijunkun.validation.group.StudentGroup.Update;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 学生对象
 * @author chaijunkun
 * @since 2015年4月3日
 */
public class Student {
	
	@NotNull(groups = {Get.class, Del.class, Update.class})
	private Integer id;
	
	@NotBlank(groups = {Add.class, Update.class})
	private String name;
	
	@NotNull(groups = {Add.class, Update.class})
	private Boolean male;
	
	private Integer teacherId;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getMale() {
		return male;
	}

	public void setMale(Boolean male) {
		this.male = male;
	}

	public Integer getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(Integer teacherId) {
		this.teacherId = teacherId;
	}
	
}
