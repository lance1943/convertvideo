package com.yunzhuo.video;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ConvertVideo {

	private String ffmpegPath = "";

	private String inputAbslouteFileName = "";
	private String inputFileName = "";
	private String outputPath = "";
	private String outputFileName = "";


	public String getInputAbslouteFileName() {
		return inputAbslouteFileName;
	}

	public void setInputAbslouteFileName(String inputAbslouteFileName) {
		this.inputAbslouteFileName = inputAbslouteFileName;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setFfmpegPath(String ffmpegPath) {
		this.ffmpegPath = ffmpegPath;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public String getFfmpegPath() {
		return ffmpegPath;
	}

	// public static void main(String args[]) throws IOException {
	// ConvertVideo cv = new ConvertVideo();
	//
	// cv.init();
	//
	// if (!cv.checkfile(cv.getInputPath())) {
	// System.out.println(cv.getInputPath() + " is not file");
	// return;
	// }
	// if (cv.process()) {
	// System.out.println("ok");
	// }
	// }
	// \home\test\123.mp4
	public void parser() { // 先获取当前项目路径，在获得源文件、目标文件、转换器的路径
		String[] inputArr = inputAbslouteFileName.split("\\");
		inputFileName = inputArr[inputArr.length - 1];// a.avi
		String fliePath = inputAbslouteFileName.substring(0, inputAbslouteFileName.lastIndexOf("\\") - 1);

		outputPath = fliePath + "\\temp\\";
		System.out.println("inputAbslouteFileName:" + inputAbslouteFileName);
		System.out.println("outputPath:" + outputPath);

	}

	// protected void getPath() { // 先获取当前项目路径，在获得源文件、目标文件、转换器的路径
	// File diretory = new File("");
	// try {
	// String currPath = diretory.getAbsolutePath();
	// inputPath = currPath + "\\input\\" + inputFileName;
	// outputPath = currPath + "\\output\\";
	// ffmpegPath = currPath + "\\ffmpeg\\bin\\";
	// System.out.println(currPath);
	// } catch (Exception e) {
	// System.out.println("getPath出错");
	// }
	// }

	protected boolean process() {
		int type = checkContentType();
		boolean status = false;
		if (type == 0) {
			System.out.println("直接转成flv格式");
			status = processMp4(inputAbslouteFileName);// 直接转成flv格式
		} else if (type == 1) {
			String avifilepath = processAVI(type);
			if (avifilepath == null)
				return false;// 没有得到avi格式
			status = processMp4(avifilepath);// 将avi转成flv格式
		}
		return status;
	}

	private int checkContentType() {
		String type = inputAbslouteFileName
				.substring(inputAbslouteFileName.lastIndexOf(".") + 1, inputAbslouteFileName.length()).toLowerCase();
		// ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
		if (type.equals("avi")) {
			return 0;
		} else if (type.equals("mpg")) {
			return 0;
		} else if (type.equals("wmv")) {
			return 0;
		} else if (type.equals("3gp")) {
			return 0;
		} else if (type.equals("mov")) {
			return 0;
		} else if (type.equals("mp4")) {
			return 0;
		} else if (type.equals("asf")) {
			return 0;
		} else if (type.equals("asx")) {
			return 0;
		} else if (type.equals("flv")) {
			return 0;
		}
		// 对ffmpeg无法解析的文件格式(wmv9，rm，rmvb等),
		// 可以先用别的工具（mencoder）转换为avi(ffmpeg能解析的)格式.
		else if (type.equals("wmv9")) {
			return 1;
		} else if (type.equals("rm")) {
			return 1;
		} else if (type.equals("rmvb")) {
			return 1;
		}
		return 9;
	}

	protected boolean checkfile(String path) {
		File file = new File(path);
		if (!file.isFile()) {
			return false;
		}
		return true;
	}

	// 对ffmpeg无法解析的文件格式(wmv9，rm，rmvb等), 可以先用别的工具（mencoder）转换为avi(ffmpeg能解析的)格式.
	private String processAVI(int type) {
		List<String> commend = new ArrayList<String>();
		String outputAVIFile = outputPath + inputFileName.substring(0, inputFileName.lastIndexOf(".") - 1) + ".avi";
		commend.add(ffmpegPath + "mencoder");
		commend.add(inputAbslouteFileName);
		commend.add("-oac");
		commend.add("lavc");
		commend.add("-lavcopts");
		commend.add("acodec=mp3:abitrate=64");
		commend.add("-ovc");
		commend.add("xvid");
		commend.add("-xvidencopts");
		commend.add("bitrate=600");
		commend.add("-of");
		commend.add("avi");
		commend.add("-o");
		commend.add(outputAVIFile);
		try {
			ProcessBuilder builder = new ProcessBuilder();
			Process process = builder.command(commend).redirectErrorStream(true).start();
			new PrintStream(process.getInputStream());
			new PrintStream(process.getErrorStream());
			process.waitFor();
			return outputAVIFile;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
	// private boolean processFLV(String oldfilepath) {
	//
	// if (!checkfile(inputPath)) {
	// System.out.println(oldfilepath + " is not file");
	// return false;
	// }
	//
	// List<String> command = new ArrayList<String>();
	// command.add(ffmpegPath + "ffmpeg");
	// command.add("-i");
	// command.add(oldfilepath);
	// command.add("-ab");
	// command.add("56");
	// command.add("-ar");
	// command.add("22050");
	// command.add("-qscale");
	// command.add("8");
	// command.add("-r");
	// command.add("15");
	// command.add("-s");
	// command.add("600x500");
	// command.add(outputPath + "a.flv");
	//
	// try {
	//
	// Process videoProcess = new
	// ProcessBuilder(command).redirectErrorStream(true).start();
	//
	// new PrintStream(videoProcess.getErrorStream()).start();
	//
	// new PrintStream(videoProcess.getInputStream()).start();
	//
	// videoProcess.waitFor();
	//
	// return true;
	// } catch (Exception e) {
	// e.printStackTrace();
	// return false;
	// }
	// }

	private boolean processMp4(String oldfilepath) {

		if (!checkfile(inputAbslouteFileName)) {
			System.out.println(oldfilepath + " is not file");
			return false;
		}

		List<String> command = new ArrayList<String>();
		command.add(ffmpegPath + "ffmpeg");
		command.add("-y");
		command.add("-i");
		command.add(oldfilepath);
		command.add("-ar");
		command.add("44100");
		command.add("-vcodec");
		command.add("libx264");

		command.add(outputPath + inputFileName.substring(0, inputFileName.lastIndexOf(".") - 1) + ".mp4");

		try {

			Process videoProcess = new ProcessBuilder(command).redirectErrorStream(true).start();

			new PrintStream(videoProcess.getErrorStream()).start();

			new PrintStream(videoProcess.getInputStream()).start();

			videoProcess.waitFor();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}

class PrintStream extends Thread {
	java.io.InputStream __is = null;

	public PrintStream(java.io.InputStream is) {
		__is = is;
	}

	public void run() {
		try {
			while (this != null) {
				int _ch = __is.read();
				if (_ch != -1)
					System.out.print((char) _ch);
				else
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}