/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import java.util.function.Supplier;

import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
/**
 * SingletonBeanRegistry ：定义对羊例的注册及获取
 * BeanFactory ：定义获取 bean ean 的各种属性
 * Defau I tS ingletonBeanRegi stry ：对接口 Si ngletonBeanRegistry 各函数的实现
 * 25
 * HierarchicalBeanFactory ：继承 BeanFactory ，也就是在 BeanFactory 定义的功能的基础
 * 上增加了对 parentFactory 支持
 * BeanDefinitionRegistry 定义对 BeanDefinition 的各种增删改操作
 * FactoryBeanReg stryS upport ：在 Default SingletonBeanRegist 基础上增加了对 FactoryB
 * 的特殊处理功能
 * ConfigurableBeanFactory ：提供配直 Factory 的各种方法
 * ListableBeanFactory ：根据各种条件获取 bean 的配直清单
 * AbstractBeanFactory ：综合 FactoryBeanRegistrySupport ConfigurableBeanFactory
 * 功能
 * AutowireCapableBeanFactory ：提供创 bean 、自动注入、初始化以及应用 an 的后
 * 处理器
 * AbstractAutowireCapab eBeanFactory ：综合 bstractBeanF acto1y 并对接口 Autowire Capable
 * BeanFactory 进行实现
 * ConfigurableListableBeanFactory : Beanfacto 配直清单，指定忽略类型及接口等
 * DefaultListableBeanFactory 综合上面所有功能， 要是对 bean 注册后的处理
 */

/**
 * Standalone application context, accepting annotated classes as input - in particular
 * {@link Configuration @Configuration}-annotated classes, but also plain
 * {@link org.springframework.stereotype.Component @Component} types and JSR-330 compliant
 * classes using {@code javax.inject} annotations. Allows for registering classes one by
 * one using {@link #register(Class...)} as well as for classpath scanning using
 * {@link #scan(String...)}.
 *
 * <p>In case of multiple {@code @Configuration} classes, @{@link Bean} methods defined in
 * later classes will override those defined in earlier classes. This can be leveraged to
 * deliberately override certain bean definitions via an extra {@code @Configuration}
 * class.
 *
 * <p>See @{@link Configuration}'s javadoc for usage examples.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @see #register
 * @see #scan
 * @see AnnotatedBeanDefinitionReader
 * @see ClassPathBeanDefinitionScanner
 * @see org.springframework.context.support.GenericXmlApplicationContext
 * @since 3.0
 */
public class AnnotationConfigApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {
	// 保存一个读取注解的bean定义读取器 并将其设置到容器中
	private final AnnotatedBeanDefinitionReader reader;
	// 保存一个扫描指定类路径中注解bean定义的扫描器 并将其设置到容器中
	private final ClassPathBeanDefinitionScanner scanner;


	/**
	 * Create a new AnnotationConfigApplicationContext that needs to be populated
	 * through {@link #register} calls and then manually {@linkplain #refresh refreshed}.
	 */
	public AnnotationConfigApplicationContext() {
		// super(); 父类的构造方法会完成 DefaultListableBeanFactory 的创建
		// 初始化BD的注解式读取器 和 注解的路径扫描器  需要稍后调用register和refresh完成bean的扫描 解析 注册
		this.reader = new AnnotatedBeanDefinitionReader(this);
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}

	/**
	 * Create a new AnnotationConfigApplicationContext with the given DefaultListableBeanFactory.
	 *
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context
	 */
	public AnnotationConfigApplicationContext(DefaultListableBeanFactory beanFactory) {
		super(beanFactory);
		this.reader = new AnnotatedBeanDefinitionReader(this);
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}

	/**
	 * Create a new AnnotationConfigApplicationContext, deriving bean definitions
	 * from the given annotated classes and automatically refreshing the context.
	 *
	 * @param annotatedClasses one or more annotated classes,
	 *                         e.g. {@link Configuration @Configuration} classes
	 */
	public AnnotationConfigApplicationContext(Class<?>... annotatedClasses) {
		// 创建reader scanner beanfactory 注入七个PostProcessor 包含BeanFactoryPostProcessor  BD注册PP BPP
		this();
		// 注册配置类到BeanDefinition;无其他工作 在springBoot中一般就是main方法所在的configuration等类
		register(annotatedClasses);
		// 启动容器
		refresh();
	}

	/**
	 * Create a new AnnotationConfigApplicationContext, scanning for bean definitions
	 * in the given packages and automatically refreshing the context.
	 *
	 * @param basePackages the packages to check for annotated classes
	 */
	public AnnotationConfigApplicationContext(String... basePackages) {
		this();
		scan(basePackages);
		refresh();
	}


	/**
	 * Propagates the given custom {@code Environment} to the underlying
	 * {@link AnnotatedBeanDefinitionReader} and {@link ClassPathBeanDefinitionScanner}.
	 */
	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		super.setEnvironment(environment);
		this.reader.setEnvironment(environment);
		this.scanner.setEnvironment(environment);
	}

	/**
	 * Provide a custom {@link BeanNameGenerator} for use with {@link AnnotatedBeanDefinitionReader}
	 * and/or {@link ClassPathBeanDefinitionScanner}, if any.
	 * <p>Default is {@link org.springframework.context.annotation.AnnotationBeanNameGenerator}.
	 * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
	 * and/or {@link #scan(String...)}.
	 *
	 * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator
	 * @see ClassPathBeanDefinitionScanner#setBeanNameGenerator
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		// 为容器的注解bean读取器和注解bean扫描器设置bean名称生产器
		this.reader.setBeanNameGenerator(beanNameGenerator);
		this.scanner.setBeanNameGenerator(beanNameGenerator);
		getBeanFactory().registerSingleton(
				AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
	}

	/**
	 * Set the {@link ScopeMetadataResolver} to use for detected bean classes.
	 * <p>The default is an {@link AnnotationScopeMetadataResolver}.
	 * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
	 * and/or {@link #scan(String...)}.
	 */
	public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		// 为BD-reader和scanner设置作用范围元信息解析器
		this.reader.setScopeMetadataResolver(scopeMetadataResolver);
		this.scanner.setScopeMetadataResolver(scopeMetadataResolver);
	}


	//---------------------------------------------------------------------
	// Implementation of AnnotationConfigRegistry
	//---------------------------------------------------------------------

	/**
	 * Register one or more annotated classes to be processed.
	 * <p>Note that {@link #refresh()} must be called in order for the context
	 * to fully process the new classes.
	 *
	 * @param annotatedClasses one or more annotated classes,
	 *                         e.g. {@link Configuration @Configuration} classes
	 * @see #scan(String...)
	 * @see #refresh()
	 */
	public void register(Class<?>... annotatedClasses) {
		Assert.notEmpty(annotatedClasses, "At least one annotated class must be specified");
		// 注册bd
		this.reader.register(annotatedClasses);
	}

	/**
	 * Perform a scan within the specified base packages.
	 * <p>Note that {@link #refresh()} must be called in order for the context
	 * to fully process the new classes.
	 *
	 * @param basePackages the packages to check for annotated classes
	 * @see #register(Class...)
	 * @see #refresh()
	 */
	public void scan(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		// 扫描注册bd
		this.scanner.scan(basePackages);
	}


	//---------------------------------------------------------------------
	// Convenient methods for registering individual beans
	//---------------------------------------------------------------------

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations, and optionally providing explicit constructor
	 * arguments for consideration in the autowiring process.
	 * <p>The bean name will be generated according to annotated component rules.
	 *
	 * @param annotatedClass       the class of the bean
	 * @param constructorArguments argument values to be fed into Spring's
	 *                             constructor resolution algorithm, resolving either all arguments or just
	 *                             specific ones, with the rest to be resolved through regular autowiring
	 *                             (may be {@code null} or empty)
	 * @since 5.0
	 */
	public <T> void registerBean(Class<T> annotatedClass, Object... constructorArguments) {
		registerBean(null, annotatedClass, constructorArguments);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations, and optionally providing explicit constructor
	 * arguments for consideration in the autowiring process.
	 *
	 * @param beanName             the name of the bean (may be {@code null})
	 * @param annotatedClass       the class of the bean
	 * @param constructorArguments argument values to be fed into Spring's
	 *                             constructor resolution algorithm, resolving either all arguments or just
	 *                             specific ones, with the rest to be resolved through regular autowiring
	 *                             (may be {@code null} or empty)
	 * @since 5.0
	 */
	public <T> void registerBean(@Nullable String beanName, Class<T> annotatedClass, Object... constructorArguments) {
		this.reader.doRegisterBean(annotatedClass, null, beanName, null,
				bd -> {
					for (Object arg : constructorArguments) {
						bd.getConstructorArgumentValues().addGenericArgumentValue(arg);
					}
				});
	}

	@Override
	public <T> void registerBean(@Nullable String beanName, Class<T> beanClass, @Nullable Supplier<T> supplier,
								 BeanDefinitionCustomizer... customizers) {

		this.reader.doRegisterBean(beanClass, supplier, beanName, null, customizers);
	}

}
