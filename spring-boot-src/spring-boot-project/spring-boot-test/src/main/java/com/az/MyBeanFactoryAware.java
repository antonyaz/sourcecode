package com.az;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBeanFactoryAware implements BeanFactoryAware {
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		System.out.println("beanFactory->" + beanFactory);
	}
}
