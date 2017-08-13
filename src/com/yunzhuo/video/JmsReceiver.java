package com.yunzhuo.video;

/**
 * 
 */

import javax.jms.ConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.jms.JmsException;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.beans.factory.DisposableBean;

public class JmsReceiver implements BeanNameAware, DisposableBean {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String beanName;

	private JmsTemplate jmsTemplate;

	public JmsReceiver() {
		super();
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public String takeObject() throws JmsException {
		Object object = null;
		try {
			object = jmsTemplate.receiveAndConvert();
		} catch (JmsException e) {
			closeConnection();
			logger.error("[{}]接收异常: {}.", getBeanName(), e);
		}
		if (object == null) {
			logger.info("[{}]接收超时, 返回Null.", getBeanName());
			return null;
		}
		return (String)object;
	}

	protected void closeConnection() {
		ConnectionFactory connectionFactory = jmsTemplate.getConnectionFactory();
		if (connectionFactory instanceof SingleConnectionFactory) {
			logger.info("关闭Jms SingleConnectionFactory连接.");
			((SingleConnectionFactory) connectionFactory).resetConnection();
		} else {
			logger.info("关闭Jms连接无操作.");
		}
	}

	@Override
	public void setBeanName(String beanName) {
		// TODO Auto-generated method stub
		this.beanName = beanName;
	}

	public String getBeanName() {
		return beanName;
	}

	@Override
	public void destroy() throws Exception {
		Object cf = jmsTemplate.getConnectionFactory();
		if (cf instanceof DisposableBean) {
			logger.info("关闭Jms连接.");
			DisposableBean disposable = (DisposableBean) cf;
			try {
				disposable.destroy();
			} catch (IllegalStateException e) {
				logger.info("调用destroy方法异常." + e.getLocalizedMessage());
			} catch (Exception e) {
				logger.info("调用destroy方法异常." + e.getLocalizedMessage());
			}
		}
	}

}