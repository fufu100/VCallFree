package ufree.call.international.phone.wifi.vcallfree.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings("ALL")
public class UnZip {
	private static final int BUFFER = 2048;

	public static void unzip(String fileName, String filePath) {
		try {
			ZipFile zipFile = new ZipFile(fileName);
			Enumeration<? extends ZipEntry> emu = zipFile.entries();
			while (emu.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) emu.nextElement();
				// 会把目录作为一个file读出一次，所以只建立目录就可以，之下的文件还会被迭代到。
				if (entry.isDirectory()) {
					new File(filePath + entry.getName()).mkdirs();
					continue;
				}
				BufferedInputStream bis = new BufferedInputStream(
						zipFile.getInputStream(entry));
				File file = new File(filePath + entry.getName().replace("\\", "/"));
				// 加入这个的原因是zipfile读取文件是随机读取的，这就造成可能先读取一个文件
				// 而这个文件所在的目录还没有出现过，所以要建出目录来。
				File parent = file.getParentFile();
				if (parent != null && (!parent.exists())) {
					parent.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER);

				int count;
				byte[] data = new byte[BUFFER];
				try{
					while ((count = bis.read(data, 0, BUFFER)) != -1) {
						bos.write(data, 0, count);
					}
				}catch(IOException e){
					e.printStackTrace();
				}
				bos.flush();
				bos.close();
				bis.close();
			}
			zipFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
