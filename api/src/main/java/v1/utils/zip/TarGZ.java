package v1.utils.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

public class TarGZ {

	static final int BUFFER = 2048;

	public static void compress(String srcDir, String destDir) {
		FileOutputStream fOut = null;
		BufferedOutputStream bOut = null;
		GzipCompressorOutputStream gzOut = null;
		TarArchiveOutputStream tOut = null;
		try {
			fOut = new FileOutputStream(new File(destDir));
			bOut = new BufferedOutputStream(fOut);
			gzOut = new GzipCompressorOutputStream(bOut);
			tOut = new TarArchiveOutputStream(gzOut);
			File source = new File(srcDir);
			if (!source.exists()) {
				System.out.println("Input directory does not exist..");
				System.exit(0);
			}
			File files[] = source.listFiles();
			for (File file : files) {
				System.out.println("Adding File: " + source.getParentFile().toURI().relativize(file.toURI()).getPath());
				TarArchiveEntry entry = new TarArchiveEntry(file, source.getParentFile().toURI().relativize(file.toURI()).getPath());
				tOut.putArchiveEntry(entry);
				FileInputStream fi = new FileInputStream(file);
				BufferedInputStream sourceStream = new BufferedInputStream(fi, BUFFER);
				int count;
				byte data[] = new byte[BUFFER];
				while ((count = sourceStream.read(data, 0, BUFFER)) != -1) {
					tOut.write(data, 0, count);
				}
				sourceStream.close();
				tOut.closeArchiveEntry();
			}
			tOut.close();
			System.out.println("tar.gz file created successfully!!");
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public static void compressWithoutFolder(String srcDir, String destDir, String tmpDir) {
		FileOutputStream fOut = null;
		BufferedOutputStream bOut = null;
		GzipCompressorOutputStream gzOut = null;
		TarArchiveOutputStream tOut = null;
		try {
			fOut = new FileOutputStream(new File(destDir));
			bOut = new BufferedOutputStream(fOut);
			gzOut = new GzipCompressorOutputStream(bOut);
			tOut = new TarArchiveOutputStream(gzOut);
			File source = new File(srcDir);
			if (!source.exists()) {
				System.out.println("Input directory does not exist..");
				System.exit(0);
			}
			File files[] = source.listFiles();
			for (File file : files) {
				String wf = source.getParentFile().toURI().relativize(file.toURI()).getPath().replace(tmpDir + "/", "");
				//System.out.println("Adding File: " + wf);
				TarArchiveEntry entry = new TarArchiveEntry(file, wf);
				tOut.putArchiveEntry(entry);
				FileInputStream fi = new FileInputStream(file);
				BufferedInputStream sourceStream = new BufferedInputStream(fi, BUFFER);
				int count;
				byte data[] = new byte[BUFFER];
				while ((count = sourceStream.read(data, 0, BUFFER)) != -1) {
					tOut.write(data, 0, count);
				}
				sourceStream.close();
				tOut.closeArchiveEntry();
			}
			tOut.close();
			//System.out.println("tar.gz file created successfully!!");
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

}
