package cn.hutool.extra.aop.interceptor;

import cn.hutool.core.reflect.ModifierUtil;
import cn.hutool.core.reflect.ReflectUtil;
import cn.hutool.extra.aop.aspects.Aspect;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * JDK实现的动态代理切面
 *
 * @author Looly
 * @author ted.L
 */
public class JdkInterceptor implements InvocationHandler, Serializable {
	private static final long serialVersionUID = 1L;

	private final Object target;
	private final Aspect aspect;

	/**
	 * 构造
	 *
	 * @param target 被代理对象
	 * @param aspect 切面实现
	 */
	public JdkInterceptor(final Object target, final Aspect aspect) {
		this.target = target;
		this.aspect = aspect;
	}

	public Object getTarget() {
		return this.target;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		final Object target = this.target;
		final Aspect aspect = this.aspect;
		Object result = null;

		// 开始前回调
		if (aspect.before(target, method, args)) {
			ReflectUtil.setAccessible(method);

			try {
				result = method.invoke(ModifierUtil.isStatic(method) ? null : target, args);
			} catch (final InvocationTargetException e) {
				// 异常回调（只捕获业务代码导致的异常，而非反射导致的异常）
				if (aspect.afterException(target, method, args, e.getTargetException())) {
					throw e;
				}
			}

			// 结束执行回调
			if (aspect.after(target, method, args, result)) {
				return result;
			}
		}

		return null;
	}

}
