package org.apache.shiro.biz.spring.security.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.authz.aop.AuthenticatedAnnotationMethodInterceptor;
import org.apache.shiro.authz.aop.AuthorizingAnnotationMethodInterceptor;
import org.apache.shiro.authz.aop.GuestAnnotationMethodInterceptor;
import org.apache.shiro.authz.aop.PermissionAnnotationMethodInterceptor;
import org.apache.shiro.authz.aop.RoleAnnotationMethodInterceptor;
import org.apache.shiro.authz.aop.UserAnnotationMethodInterceptor;
import org.apache.shiro.biz.authz.aop.RoleAllowsAnnotationMethodInterceptor;
import org.apache.shiro.spring.aop.SpringAnnotationResolver;
import org.apache.shiro.spring.security.interceptor.AopAllianceAnnotationsAuthorizingMethodInterceptor;

public class ExtendAnnotationsAuthorizingMethodInterceptor extends AopAllianceAnnotationsAuthorizingMethodInterceptor {

	public ExtendAnnotationsAuthorizingMethodInterceptor() {
		
		List<AuthorizingAnnotationMethodInterceptor> interceptors = new ArrayList<AuthorizingAnnotationMethodInterceptor>(5);

		//use a Spring-specific Annotation resolver - Spring's AnnotationUtils is nicer than the raw JDK resolution process.  
		AnnotationResolver resolver = new SpringAnnotationResolver();
		// we can re-use the same resolver instance - it does not retain state:
		interceptors.add(new RoleAnnotationMethodInterceptor(resolver));
		interceptors.add(new PermissionAnnotationMethodInterceptor(resolver));
		interceptors.add(new AuthenticatedAnnotationMethodInterceptor(resolver));
		interceptors.add(new UserAnnotationMethodInterceptor(resolver));
		interceptors.add(new GuestAnnotationMethodInterceptor(resolver));
		// 自定义
		interceptors.add(new RoleAllowsAnnotationMethodInterceptor());

		super.setMethodInterceptors(interceptors);
		
	}

}