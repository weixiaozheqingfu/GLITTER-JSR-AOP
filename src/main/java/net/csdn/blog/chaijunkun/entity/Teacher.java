package net.csdn.blog.chaijunkun.entity;

import javax.validation.constraints.NotNull;

import net.csdn.blog.chaijunkun.validation.group.TeacherGroup.Add;
import net.csdn.blog.chaijunkun.validation.group.TeacherGroup.Del;
import net.csdn.blog.chaijunkun.validation.group.TeacherGroup.Get;
import net.csdn.blog.chaijunkun.validation.group.TeacherGroup.Update;

import org.hibernate.validator.constraints.NotBlank;


/**
 * 教师对象
 * @author chaijunkun
 * @since 2015年4月3日
 */
public class Teacher {
	
	@NotNull(groups = {Get.class, Del.class, Update.class})
	private Integer id;
	
	@NotBlank(groups = {Add.class, Update.class})
	private String name;
	
	@NotNull(groups = {Add.class, Update.class})
	private Boolean male;
	
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

}
