package com.rengh.zhangui.photoWallFalls;

import com.rengh.zhangui.main.FileManager;
import com.rengh.zhangui.main.MainActivity;

public class Images {
	public static String[] init() {
		int num = FileManager.numOfLine(MainActivity.myFileName);
		String[] imageUrls = new String[num > 0 ? num - 1 : 1];
		String[] collectedName = FileManager
				.readFileByLines(MainActivity.myFileName);
		if (collectedName != null) {
			for (int i = 1; i < num; i++) {
				if (collectedName[i] != null) {
					imageUrls[i - 1] = collectedName[i] + "/index.jpg";
				}
			}
		}
		return imageUrls;
	}
}
