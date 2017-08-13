package com.yunzhuo.video;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.core.JdbcTemplate;

public class WorkThread extends Thread implements DisposableBean {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String ffmpegPath;

	private String inputAbslouteFileName;

	private int capacity = 10;

	private int fileId;

	private ArrayBlockingQueue<String> cacheQueue;

	private JdbcTemplate jdbcTemplate;

	private final AtomicBoolean shutdown = new AtomicBoolean(false);

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public ArrayBlockingQueue<String> getCacheQueue() {
		return cacheQueue;
	}

	public void setCacheQueue(ArrayBlockingQueue<String> cacheQueue) {
		this.cacheQueue = cacheQueue;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public String getInputAbslouteFileName() {
		return inputAbslouteFileName;
	}

	public void setInputAbslouteFileName(String inputAbslouteFileName) {
		this.inputAbslouteFileName = inputAbslouteFileName;
	}

	public WorkThread(String name) {
		super(name);
	}

	public void setFfmpegPath(String ffmpegPath) {
		this.ffmpegPath = ffmpegPath;
	}

	// messge = "12345||\\home\\user\\dd.avi"
	public void doWork(String message) {
		String[] arrMessage = message.split("||");
		fileId = Integer.valueOf(arrMessage[0]);
		ConvertVideo cv = new ConvertVideo();
		cv.setInputAbslouteFileName(arrMessage[1]);
		cv.setFfmpegPath(ffmpegPath);
		cv.parser();
		if (!cv.checkfile(arrMessage[1])) {
			System.out.println(arrMessage[1] + " is not file");
			return;
		}
		if (cv.process()) {
			System.out.println("ok");
			// 根据FileID 更新 文件状态
		}
	}

	private boolean isContinue() {
		if (!shutdown.get()) {
			return true;
		}
		if (cacheQueue.size() > 0) {
			logger.info("线程已中断, 处理缓存数据.");
			return true;
		}
		return false;
	}

	public void run() {
		MDC.put("worker-name", this.getName());
		logger.info("Worker线程启动...");
		while (isContinue()) {
			try {
				String message = cacheQueue.take();
				if (message == null || message.split("||").length != 2) {
					logger.info("中断激活消息.");
				} else {
					logger.info("Worker handle: {}.", message.toString());
					long ss = System.nanoTime();
					doWork(message);
					long ee = System.nanoTime();
					logger.info("处理总用时: {}纳秒.", (ee - ss));
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		logger.info("Worker线程正常退出...");

	}
}
