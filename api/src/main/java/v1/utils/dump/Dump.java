package v1.utils.dump;

import v1.utils.config.ConfigProperties;
import v1.utils.zip.TarGZ;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.FileUtils;

public class Dump {
	
	public static String writeFile(String repo) {
		try {
			String tmpDirString = "tmpDir";
			String tmpDirPath = ConfigProperties.getPropertyParam("dump_server") + tmpDirString + "/";
			String fileDir = ConfigProperties.getPropertyParam("dump_server");
			// create tmp folder
			File tmpDir1 = new File(tmpDirPath.substring(0, tmpDirPath.length() - 1));
			if (!tmpDir1.exists()) {
				if (tmpDir1.mkdir()) {
				} else {
					System.out.println("Failed to create directory!");
				}
			}
			// download dump
			URL link = new URL(ConfigProperties.getPropertyParam("ts_server") + "/repositories/" + repo + "/statements?Accept=text/plain");
			InputStream in = new BufferedInputStream(link.openStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n = 0;
			while (-1 != (n = in.read(buf))) {
				out.write(buf, 0, n);
			}
			out.close();
			in.close();
			byte[] response2 = out.toByteArray();
			long currentTime = System.currentTimeMillis();
			String fileName = tmpDirPath + String.valueOf(currentTime) + ".ttl";
			FileOutputStream fos = new FileOutputStream(fileName);
			fos.write(response2);
			fos.close();
			// compress folder
			String compressedFile = fileDir + String.valueOf(currentTime) + "_" + repo + ".tar.gz";
			TarGZ.compressWithoutFolder(tmpDirPath, compressedFile, tmpDirString);
			// delete tmp folder
			FileUtils.deleteDirectory(new File(tmpDirPath));
			// return
			return String.valueOf(currentTime) + "_" + repo + ".tar.gz";
		} catch (Exception e) {
			System.out.println(e.toString());
			throw new IllegalStateException(e.toString());
		}
	}
	
}
