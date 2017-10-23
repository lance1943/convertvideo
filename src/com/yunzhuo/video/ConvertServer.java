package com.yunzhuo.video;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.core.JdbcTemplate;

public class ConvertServer extends Thread implements DisposableBean {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private JmsReceiver receiver;

	private int wokerNum = 3;

	private int wokerCacheCapacity = 100;

	private WorkThread[] workers;

	private String ffmpegPath;

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setFfmpegPath(String ffmpegPath) {
		this.ffmpegPath = ffmpegPath;
	}

	private final AtomicBoolean shutdown = new AtomicBoolean(false);

	public ConvertServer() {
		super("ConvertServer");
	}

	public void setReceiver(JmsReceiver receiver) {
		this.receiver = receiver;
	}

	public void setWokerCacheCapacity(int wokerCacheCapacity) {
		this.wokerCacheCapacity = wokerCacheCapacity;
	}

	public void setWokerNum(int wokerNum) {
		this.wokerNum = wokerNum;
	}

	public void init() throws CloneNotSupportedException {
		logger.info("�����߳�����{}.", wokerNum);
		workers = new WorkThread[wokerNum];
		for (int i = 0; i < wokerNum; i++) {
			workers[i] = new WorkThread("woker-" + i);
			workers[i].setCapacity(wokerCacheCapacity);
			workers[i].init();
			workers[i].setFfmpegPath(ffmpegPath);
			workers[i].setJdbcTemplate(jdbcTemplate);
			workers[i].start();
		}
		this.start();
	}

	/**
	 * ���̲߳�����interrupt������ӦΪ��Ϣ����Worker�����жϡ� join ��ԭ����Ϊ�����offer
	 * interrupt��ϢUniinPdu��
	 */
	@Override
	public void destroy() {
		logger.info("����Server destroy.");
		this.shutdown.set(true);
		logger.info("����receiver destroy.");
		try {
			receiver.destroy();
		} catch (Exception e) {
			logger.info("����receiver destroy�����쳣.", e);
		}
		logger.info("�ȴ�Server shutdown.");
		try {
			this.join();
		} catch (InterruptedException e) {
			logger.info("����destroy join�����쳣.", e);
		}
		logger.info("���Server shutdown.");
		logger.info("����Worker destroy.");
		for (WorkThread worker : workers) {
			worker.destroy();
		}
		logger.info("����Report destroy");
//		 ProcessorReport.shutdown.set(true);
	}

	@Override
	public void run() {
		logger.info("{}�߳�����...", super.getName());
		String message = null;
		try {
			do {
				message = receiver.takeObject();

				if (message != null) {
					int i = distribute(message);
					workers[i].getCacheQueue().put(message);
					// logger.info("�澯�ַ���Worker{}.", i); �����壬�ͽ����澯���첽��
				} else {
					logger.debug("uniinPdu is null.");
				}
			} while ((!shutdown.get()) || (message != null));
		} catch (Exception ex) {
			logger.error("", ex);
		}
		logger.info("{}�߳�ֹͣ...", super.getName());
	}

	private int distribute(String message) {
		if (wokerNum == 1) {
			return 0;
		}

		int disNum = message.hashCode() % wokerNum;
		if (disNum < 0) {
			disNum = disNum * -1;
		}
		return disNum;
	}

}
