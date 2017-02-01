package net.csdn.blog.chaijunkun.controller;

import net.csdn.blog.chaijunkun.entity.Resp;
import net.csdn.blog.chaijunkun.entity.Student;
import net.csdn.blog.chaijunkun.service.StudentService;
import net.csdn.blog.chaijunkun.validation.group.StudentGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 学生控制器
 * @author chaijunkun
 * @since 2015年4月3日
 */
@Controller
@RequestMapping(value = "student")
public class StudentController {
	
	@Autowired
	private StudentService studentService;
	
	/**
	 * Limj批注：@Validated是Spring的注解，正如所分析那样，Spring的数据校验顺序是先看有没有使用Spring自己的binder.setValidator()的Spring
	 * 自身的自定义校验，如果用了，那么就不会使用Jsr303的校验了，也就是说不管使用@Validated还是jsr303的@Valid注解标记都没有作用了，
	 * 使用Spring自定义校验最终的错误信息也会绑定到BindingResult中。
	 * 
	 * 那么假如没有使用Spring自定义校验而是想使用jsr303校验呢，在Spring中，Spring对jsr303的支持是通过注解来实现的，即@Valid注解，如果入参
	 * 对象用@Valid注解标记了，同时没有Spring的自定义校验的定义，而Spring又发现有hibernate-validate等对jsr303的实现jar包（类），
	 * 那么Spring就会自动注入jsr303的实现，进行jsr303的验证。既然@Valid对于Spring来说只是一个启用jsr303的标记，那么这个标记当然也可以用别的
	 * 比如@Validated，这是Spring自定义的一个注解，被@Validated标记的入参对象与@Valid标记作用一样，也会启用对jsr303的启用，但是@Validated
	 * 注解比@Valid注解更丰富，使用@Validated注解可以进行分组信息的设定，这是@Valid注解所办不到的。所以我们推荐不管什么时候都使用@Validated
	 * 注解为最好。
	 *
	 * @param student
	 * @param result
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "add", method = {RequestMethod.GET})
	public Resp<?> add(@Validated(StudentGroup.Add.class) Student student, BindingResult result){
		System.out.println("add目标方法，大家好，我是aop的目标方法，请多多关照，add目标方法..........................................");
		Integer id = studentService.add(student);
//		if(true){
//			throw new RuntimeException("add目标方法抛出一个异常");
//		}
		if (id == null){
			return Resp.fail("添加学生信息失败");
		}else{
			return Resp.success(id);
		}
	}
	
	@SuppressWarnings("unused")
	@ResponseBody
	@RequestMapping(value = "addVoid", method = {RequestMethod.GET})
	public void addVoid(@Validated(StudentGroup.Add.class) Student student, BindingResult result){
		Integer id = studentService.add(student);
		throw new RuntimeException("测试一个异常情况");
	}
	
	@ResponseBody
	@RequestMapping(value = "del", method = {RequestMethod.GET})
	public Resp<?> del(@Validated(StudentGroup.Del.class) Student student, BindingResult result){
		if (studentService.del(student.getId())){
			return Resp.success(true);
		}else{
			return Resp.fail("删除学生信息失败");
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "get", method = {RequestMethod.GET})
	public Resp<?> get(@Validated(StudentGroup.Get.class) Student student, BindingResult result){
		Student data = studentService.get(student.getId());
		if (data == null){
			return Resp.fail("未找到指定学生");
		}else{
			return Resp.success(data);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "update", method = {RequestMethod.POST})
	public Resp<?> update(@Validated(StudentGroup.Update.class) Student student, BindingResult result){
		if (studentService.update(student)){
			return Resp.success(true);
		}else{
			return Resp.fail("更新学生信息失败");
		}
	}
	
}
