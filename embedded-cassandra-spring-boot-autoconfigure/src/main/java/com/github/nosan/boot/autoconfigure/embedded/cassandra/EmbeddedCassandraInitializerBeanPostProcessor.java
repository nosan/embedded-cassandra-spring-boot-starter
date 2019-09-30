/*
 * Copyright 2019 the original author or authors.
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

package com.github.nosan.boot.autoconfigure.embedded.cassandra;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

import com.github.nosan.embedded.cassandra.api.Cassandra;

/**
 * {@link BeanPostProcessor} used to ensure that {@link EmbeddedCassandraInitializer} is initialized as soon as a {@link
 * Cassandra} is.
 *
 * @author Dmytro Nosan
 */
class EmbeddedCassandraInitializerBeanPostProcessor implements BeanPostProcessor, Ordered {

	private final ApplicationContext applicationContext;

	private final AtomicBoolean initialized = new AtomicBoolean();

	EmbeddedCassandraInitializerBeanPostProcessor(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Cassandra && this.initialized.compareAndSet(false, true)) {
			this.applicationContext.getBean(EmbeddedCassandraInitializer.class);
		}
		return bean;
	}

}
