/**
 * 写入文件
 */
public void fileWrite(String path){
	try {
		FileOutputStream fos = new FileOutputStream(path);
		try {
			fos.write(path[4].getBytes());
			fos.close();
		} catch (IOException e) {
			System.out.println("写入文件失败！");
			e.printStackTrace();
			return;
		}
	} catch (FileNotFoundException e) {
		System.out.println("没有找到文件！");
		e.printStackTrace();
		return;
	}
}

/**
* 该方法取消使用；以行为单位读取文件，常用于读面向行的格式化文件
*/
public static String readFileByLines(String fileName) {
	File file = new File(fileName);
	BufferedReader reader = null;
	String tempString = null;
	if (file.exists()) {
		try {
			reader = new BufferedReader(new FileReader(file));
			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 显示行号
				line++;
				return tempString;
			}
			reader.close();
		} catch (IOException e) {
			System.out.println("读取文件失败！");
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
* 清空文件；已经取消使用；
* @param fileName
*/
public void clearFile(String fileName){
	File f = new File(fileName);
	FileWriter fw = null;
	try {
		fw = new FileWriter(f);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		fw.write("");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		fw.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
