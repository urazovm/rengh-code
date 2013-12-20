/**
 * д���ļ�
 */
public void fileWrite(String path){
	try {
		FileOutputStream fos = new FileOutputStream(path);
		try {
			fos.write(path[4].getBytes());
			fos.close();
		} catch (IOException e) {
			System.out.println("д���ļ�ʧ�ܣ�");
			e.printStackTrace();
			return;
		}
	} catch (FileNotFoundException e) {
		System.out.println("û���ҵ��ļ���");
		e.printStackTrace();
		return;
	}
}

/**
* �÷���ȡ��ʹ�ã�����Ϊ��λ��ȡ�ļ��������ڶ������еĸ�ʽ���ļ�
*/
public static String readFileByLines(String fileName) {
	File file = new File(fileName);
	BufferedReader reader = null;
	String tempString = null;
	if (file.exists()) {
		try {
			reader = new BufferedReader(new FileReader(file));
			int line = 1;
			// һ�ζ���һ�У�ֱ������nullΪ�ļ�����
			while ((tempString = reader.readLine()) != null) {
				// ��ʾ�к�
				line++;
				return tempString;
			}
			reader.close();
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
* ����ļ����Ѿ�ȡ��ʹ�ã�
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
