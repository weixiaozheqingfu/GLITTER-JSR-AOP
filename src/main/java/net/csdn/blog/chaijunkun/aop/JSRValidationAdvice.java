package net.csdn.blog.chaijunkun.aop;

import javax.validation.constraints.NotNull;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import net.csdn.blog.chaijunkun.entity.Resp;

/**
 * JSR303验证框架统一处理
 * @author limengjun
 * @since 2015年4月1日
 */
/**
 * 分析代码中aop的所有通知方法后，不同的通知方法各有特色，何时运用，如何运用，运用哪个通知方法更合适，需要根据项目情况灵活分析运用，万不可一概而论。
 * 下面是总结各种通知方法的特点，帮助运用时做出最合适的选择。
 * 
 * 1.明确一个认识或概念，织入或者切入点包括目标方法执行之前、目标方法执行后、目标方法执行过程中抛出异常之前这三个地方，在这三个地方都可以配置相应的通知方法进行织入。
 * 
 * 2.任何通知方法都可以获取到目标方法的上下文信息，around是通过ProceedingJoinPoint，其他通知方法是通过JoinPoint，当然如果不需要，这些形式参数在通知方法中都可以不定义。
 * 
 * 3.任何通知方法的返回值都可以是任意类型，但是除了around通知方法根据需要定义返回值类型外，其他种通知方法定义返回值没有意义，因为Spring对其他种通知方法的织入只是调用执行一下这个通知方法，
 *   无论返回什么类型都置之不理的，不予采用的。所以干脆返回void是最好的。即便是around通知方法绝大部分也都是返回Object类型，除非有特殊需求特殊限制才会定义返回特定的数据类型来满足需求。
 *   
 * 4.任何使用了around通知方法而在这个通知方法中又不调用proceedingJoinPoint.proceed()代码的行为，我们都认为是耍流氓，不如直接用before通知方法，所以所有的分析都不考虑这一流氓行为。
 * 
 * 5.before通知方法的切入时机是目标方法执行之前。
 * 
 * 6.after-throwing，改通知的切入时机是目标方法抛出异常前夕或者是before、around或者after通知方法抛出异常前夕，after-throwing通知方法执行完毕后，目标方法的异常抛出。有两点情况需要说明：
 *   (1)对于before通知方法中如果throw一个异常，那么代码会直接流转到after通知方法(如果目标方法有这个通知的话)，代码会流转到after-throwing中，注意不会执行目标方法，也不会走around通知，
 *      在after-throwing如果有形参接收抛出的异常对象就是before中抛出的这个异常对象，当然，如果在after中也抛出了一个异常，则after-throwing中接收到的就是after抛出的异常了，before抛出的异常就接收不到了，有点被覆盖的感觉。
 *   (2)对于before中如果没有抛出异常，则代码流转到around"前置块"中，如果在around"前置块"中抛出异常，则后续执行过程同before抛出异常后的执行过程。
 *   (3)对于before中如果没有抛出异常，则代码流转到around"前置块"中，如果在around"前置块"中没有抛出异常，则通过调用proceedingJoinPoint.proceed()代码进入到目标方法中，如果在目标方法执行过程中有异常抛出，
 *      后续执行过程同before抛出异常过程。
 *   (4)对于before中如果没有抛出异常，则代码流转到around"前置块"中，如果在around"前置块"中没有抛出异常，则通过调用proceedingJoinPoint.proceed()代码进入到目标方法中，如果在目标方法执行过程中没有异常抛出，
 *      则代码流入到around"后置块"中，如果在around"后置块"执行过程中有异常抛出，则后续执行过程同before抛出异常过程。
 *   (5)对于before中如果没有抛出异常，则代码流转到around"前置块"中，如果在around"前置块"中没有抛出异常，则通过调用proceedingJoinPoint.proceed()代码进入到目标方法中，如果在目标方法执行过程中没有异常抛出，
 *      则代码流入到around"后置块"中，如果在around"后置块"执行过程中没有异常抛出，则代码流转到after通知方法，如果after中有异常抛出，则代码流转到after-throwing通知方法中处理异常。
 *   (6)对于before中如果没有抛出异常，则代码流转到around"前置块"中，如果在around"前置块"中没有抛出异常，则通过调用proceedingJoinPoint.proceed()代码进入到目标方法中，如果在目标方法执行过程中没有异常抛出，
 *      则代码流入到around"后置块"中，如果在around"后置块"执行过程中没有异常抛出，则代码流转到after通知方法，如果after中没有异常抛出，则代码流转到after-returning通知方法中，如果after-returning执行过程中有异常抛出，
 *      则代码流转到after-throwing通知方法处理异常。
 *   (7)对于before中如果没有抛出异常，则代码流转到around"前置块"中，如果在around"前置块"中没有抛出异常，则通过调用proceedingJoinPoint.proceed()代码进入到目标方法中，如果在目标方法执行过程中没有异常抛出，
 *      则代码流入到around"后置块"中，如果在around"后置块"执行过程中没有异常抛出，则代码流转到after通知方法，如果after中没有异常抛出，则代码流转到after-returning通知方法中，如果after-returning执行过程中没有异常抛出，
 *      则代码执行完毕。
 *   (8)如果代码执行过程中有异常抛出，则不管最后抛出异常的是哪块的代码，抛出的异常都是抛给调用目标方法的那个方法。举例，比如代码在after-returning通知方法中抛出了异常，这是最后一个抛出异常的地方，在after-throwing通知方法中
 *      没有再抛出异常，那么在代码执行完毕after-throwing代码块的代码后，会将after-returning通知方法的异常抛给目标方法的调用者。
 *     	不管最后抛出异常的是哪块的代码，目标方法都不会有返回值给调用者了，而是抛出异常给调用者，调用者可以捕获活继续向上抛。如果是aop通知方法抛出的异常，建议可以定义一个AopExceptionn的自定义异常类来封装异常信息。
 *   (9)从上面可以看出，代码执行流转的顺序在有异常和没有异常的情况下是有区别的，怎么流转规律就是上面总结的，使用aop切面的时候根据需要根据执行规律顺序结合需求灵活掌握。虽然上面总结了这么多在aop通知方法中抛出异常时代码的流转情况总结，
 *      但是最佳实践还是建议在aop的通知方法中最好不要抛出异常，有异常直接在方法内部try.catch处理掉，本来我们aop的目的就是在通用的很多目标方法上来切的，这里再抛出异常不合适，并且还有可能覆盖掉目标方法抛出的异常，得不偿失。
 *      所以建议aop通知方法内部保证不抛出异常，after-throwing只是在目标方法抛出异常后执行这里的代码，可以记录目标方法的异常信息等，或做一些其他操作。
 *   
 * 7.after-returning通知方法的切入时机是目标方法正常执行完毕后，执行该通知方法，after-returning还有以下几点总结：
 *   (1).如果在目标方法执行过程中抛出异常，则after-returning通知方法不会执行，这种情况可以和after-throwing通知方法配合使用，同时切入到同一类目标方法上即可。
 *   (2).可以通过xml中returning属性，及after-returning方法中定义相同名称的形参名称，这个形参即Spring帮忙注入的目标方法的返回值。
 *   (3).虽然可以通过形参得到目标方法的返回值，可以对返回值的数据进行运用分析，但不会影响到这个返回值，即便是对这个形参对象进行了修改。Spring执行完毕after-returning通知方法后，返回的结果仍然是目标方法执行完毕时的结果。
 * 
 * 8.after通知方法的切入时机是目标方法正常执行完毕或目标方法执行过程中抛出异常前夕执行该方法。after通知方法，不能得到目标方法的返回值，更不可能影响返回值。
 * 	 after如果在目标方法执行过程中抛出异常，分为两种情况:
 *   (1).目标方法上没有after-throwing通知，此时在目标方法抛出异常之前先执行after通知方法，然后目标方法抛出异常。
 *   (2).目标方法上有after-throwing通知，此时在目标方法抛出异常之前先执行after-throwing通知方法，after-throwing通知方法执行完毕后执行after通知方法，然后目标方法抛出异常。
 * 
 * 9.around通知方法的切入时机是目标方法之前切入，切入到around方法中后，执行过程可以通过joinPoint.proceed()代码调用目标方法的执行，目标方法执行完毕后，会进行返回到around方法中，如果around中后续还有代码则会继续执行。这就是所谓的环绕。
 *   around还有以下几点总结：
 *   (1).如果在目标方法执行过程中抛出异常，是不会执行"后环绕"代码部分的，这种情况可以和after-throwing通知方法配合使用，同时切入到同一目标方法上即可。
 *   (2).可以通过proceedingJoinPoint.proceed()调用目标方法并得到目标方法的返回值。
 *   (3).假如切入点表达式expression中指定了固定的返回值类型，比如本例中的net.csdn.blog.chaijunkun.entity.Resp，那么around通知方法在调用完毕目标方法并得到目标方法的返回值后，
 *       如果需求有需要，是可以对这个返回值数据进行修改的，修改完毕后，再进行返回，因为执行这个通知方法时返回值类型是提前配置定义好的，是知道的，那么我们拿大目标方法的返回值后，可以根据需求对返回值数据进行判断加工然后返回。
 *       这一点只有around通知方法可以做到，要特别注意。
 *   (4).该通知方法的返回值类型建议不论什么情况都定义为Object类型的，这样即使目标方法是void类型的，也可以正常执行完毕该通知方法。
 *   (5).特别不建议该通知方法的返回值类型为void类型，因为一旦定义为void类型，该通知方法执行完毕后就不会有返回值，这样对于调用目标方法者来说，就相当于目标方法没有返回值了。
 *   (6).在around方法中如果要try/catch捕获异常的话，Object o = joinPoint.proceed();这一行代码不要在try/catch中，因为如果在的话，调用目标方法异常就会在这里被捕获，那么after-throwing方法就捕获不到了。当然了，反正是这样的，根据需要灵活使用吧。
 *   (7).不管在proceedingJoinPoint.proceed()执行完，代码继续执行到return这一行时，是否还有after-returning通知执行，return的返回值不受after-returning通知任何影响。
 *   	Object o = joinPoint.proceed(); 
 *		logger.info("aop执行完毕，环绕后打印日志....");
 *		return o;    // 执行这一行前时可能会执行after-returning通知(如果配置有after-returning通知的话会执行)，而执行after-returning通知时，其内部可能会修改返回值的数据(无聊，修改也没有用，也没有人这么做，但我们要说明这种情况)
 *		             // 不管after-returning内部怎么修改其形参目标方法返回值，等after-returning执行完毕后也不管其返回数据类型是什么，代码会继续流转到around通知方法的return o;这一行进行返回，此时返回的o，还是Object o = joinPoint.proceed(); 时候的数据，没有任何变化。
 *      // 所以即使对返回值进行干预也只有around能做到，那就是9(3)中描述的那种情况。代码如下：
 *      Object o = joinPoint.proceed();
 *		logger.info("aop执行完毕，环绕后打印日志....");
 *		return Resp.fail("其他错误"); // 此时返回的就是Resp.fail("其他错误")而不是o了，但是Resp.fail("其他错误")和o的类型都是在切入点表达式中定义好的固定类型即net.csdn.blog.chaijunkun.entity.Resp类型。
 *
 */

// TODO 如果aop切面方法中有操作数据库的代码，怎么和目标方法的代码控制在一个事务里，这是需要考虑的一个问题。

public class JSRValidationAdvice {

	Logger logger = LoggerFactory.getLogger(JSRValidationAdvice.class);

	/**
	 * 判断验证错误代码是否属于字段为空的情况
	 * @param code 验证错误代码
	 */
	private boolean isMissingParamsError(String code){
		if (code.equals(NotNull.class.getSimpleName()) || code.equals(NotBlank.class.getSimpleName()) || code.equals(NotEmpty.class.getSimpleName())){
			return true;
		}else{
			return false;
		}
	}
	
	public void before(JoinPoint joinPoint) throws Throwable {
		System.out.println("before，大家好，我是aop的before方法，请多多关照，before..........................................");
//		throw new RuntimeException("before，抛出异常..........................................");
	}
	
	/**
	 * 切点处理
	 * @param joinPoint
	 * @return
	 * @throws Throwable
	 */
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		logger.info("around前置块，大家好，我是aop的around前置块代码，请多多关照，around前置块..........................................");
		BindingResult result = null;
		Object[] args = joinPoint.getArgs();
		if (args != null && args.length != 0){
			for (Object object : args) {
				if (object instanceof BindingResult){
					result = (BindingResult)object;
					break;
				}
			}
		}
		// 不在这个aop，再定义一个跟业务相关的新的aop，里面通过判断先移除不对的校验。再增加上对的校验即可。
		// ...
		// result.getAllErrors().remove(i);
		// ...new ObjectError(...)
		// result.getAllErrors().add(new ObjectError(...));
		if (result != null && result.hasErrors()){
			FieldError fieldError = result.getFieldError();
			String targetName = joinPoint.getTarget().getClass().getSimpleName();
			String method = joinPoint.getSignature().getName();
			logger.info("验证失败.控制器:{}, 方法:{}, 参数:{}, 属性:{}, 错误:{}, 消息:{}", targetName, method, fieldError.getObjectName(), fieldError.getField(), fieldError.getCode(), fieldError.getDefaultMessage());
			String firstCode = fieldError.getCode();
			if (isMissingParamsError(firstCode)){
				// 返回值类型必须与目标方法的返回值类型一致，如果在执行around"前置块"中就返回值了，那么
				// 其实也就不会调用目标方法了，而这个返回值就是最后的返回值了，可以形象理解"织入"的概念，代码走到这就返回了，不往下继续了。
				return Resp.fail("必选参数丢失");
			}else{
				return Resp.fail("其他错误");
			}
		}
		
//		if(true){
//			throw new RuntimeException("around前置块，抛出异常..........................................");
//		}
		// 执行目标方法并接收目标方法执行完毕后的返回值，很灵活了，我还可以对执行完目标方法后的返回值进一步加工处理再返回，根据需要灵活掌握即可。
		Object o = joinPoint.proceed();
		logger.info("around后置块，大家好，我是aop的around后置块代码，请多多关照，around后置块..........................................");
//		if(true){
//			throw new RuntimeException("around后置块，抛出异常..........................................");
//		}
		// 执行完毕后，必须返回目标方法的返回值，返回值类型不能出错。
		return o;
	}
	
	public void after(JoinPoint joinPoint) throws Throwable {
		System.out.println("after，大家好，我是aop的after方法，请多多关照，after..........................................");
//		throw new RuntimeException("after，抛出异常..........................................");
	}
	
	public void afterReturning(JoinPoint joinPoint,Object retv) throws Throwable {
//		Object[] os = joinPoint.getArgs();
		System.out.println("afterReturning，大家好，我是aop的afterReturning方法，请多多关照，afterReturning..........................................");
//		if(true){
//			throw new RuntimeException("afterReturning，抛出异常..........................................");
//		}
	}
	
	public void afterThorwing(JoinPoint joinPoint,Exception ex) throws Throwable {
		System.out.println("afterThorwing，大家好，我是aop的afterThorwing方法，请多多关照，afterThorwing..........................................");
//		logger.info("捕获到"+joinPoint.getTarget().getClass().getSimpleName()+"类的"+joinPoint.getSignature().getName()+"方法的异常信息："+ex.getMessage());
//		throw new RuntimeException("afterThorwing，抛出异常..........................................");
//		throw ex;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
