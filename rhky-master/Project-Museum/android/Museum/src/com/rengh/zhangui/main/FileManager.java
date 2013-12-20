package com.rengh.zhangui.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

public class FileManager {
	/**
	 * ���ַ�������д���ļ�;
	 */
	public static Boolean appendMethod(String content, String fileName) {
		createMyCollectedFile(fileName);
		int line = numOfLine(fileName);
		String[] text = readFileByLines(fileName);
		for (int i = 0; i < line; i++) {
			if (text[i].equals(content)) {
				return false;
			}
		}
		try {
			// ��һ��д�ļ��������캯���еĵڶ�������true��ʾ��׷����ʽд�ļ�
			FileWriter writer = new FileWriter(fileName, true);
			writer.write(content + "\r\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * ͳ���ļ�����
	 * 
	 * @param fileName
	 */
	public static int numOfLine(String fileName) {
		createMyCollectedFile(fileName);
		int i = 0;
		FileReader fr = null;
		try {
			fr = new FileReader(fileName);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		try {
			while (br.readLine() != null) {
				i++;
			}
			return i;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return i;
	}

	/**
	 * ����Ϊ��λ��ȡ�ļ�;
	 */
	public static String[] readFileByLines(String fileName) {
		createMyCollectedFile(fileName);
		File file = new File(fileName);
		BufferedReader reader = null;
		String tempString = null;
		String[] savedStr = new String[24];
		if (file.exists()) {
			try {
				reader = new BufferedReader(new FileReader(file));
				int line = 1;
				// һ�ζ���һ�У�ֱ������nullΪ�ļ�����
				while ((tempString = reader.readLine()) != null) {
					savedStr[line - 1] = tempString;
					line++;
				}
				for (int i = line; i < savedStr.length; i++) {
					savedStr[i] = null;
				}
				reader.close();
				return savedStr;
			} catch (IOException e) {
				System.out.println("��ȡ�ļ�ʧ�ܣ�");
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e1) {
					}
				}
			}
		}
		return null;
	}

	/**
	 * ����ļ�;
	 * 
	 * @param fileName
	 */
	public static void clearFile(String fileName) {
		File f = new File(fileName);
		FileWriter fw = null;
		f.delete();
		createMyCollectedFile(fileName);
	}
	
	/**
	 * ���������ղ�չƷ�������ļ���
	 */
	public static void createMyCollectedFile(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) {
			try {
				// ��һ��д�ļ��������캯���еĵڶ�������true��ʾ��׷����ʽд�ļ�
				FileWriter writer = new FileWriter(fileName, true);
				writer.write("\r\n");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * ɾ���ļ���ָ����һ��
	 * @param fileName
	 */
	public static void resetFile(String fileName){
		int n = numOfLine(fileName);
		String[] bak = new String[n];
		bak = readFileByLines(fileName);
		clearFile(fileName);
		for(int i = 1; i < n; i++){
			if(!bak[i].equals(MainActivity.delName)){
				appendMethod(bak[i], fileName);
			}
		}
		n = numOfLine(fileName);
		String[] bak1 = new String[n];
		bak1 = readFileByLines(fileName);
		return;
	}
}