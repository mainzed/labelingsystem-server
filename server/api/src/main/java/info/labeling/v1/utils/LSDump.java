package info.labeling.v1.utils;

import info.labeling.v1.rest.DumpResource;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FileUtils;

public class LSDump implements Runnable {

	@Override
	public void run() {
		if (DumpResource.dumping) {
			writeFile();
		}
	}

	private void writeFile() {
		try {
			// look for file number and delete
			DumpResource.listDumpFilesDeleteForMax();
			// create tmp folder
			File tmpDir1 = new File(DumpResource.tmpDirPath.substring(0, DumpResource.tmpDirPath.length() - 1));
			if (!tmpDir1.exists()) {
				if (tmpDir1.mkdir()) {
				} else {
					System.out.println("Failed to create directory!");
				}
			}
			File tmpDir2 = new File(DumpResource.tmpDirPath2.substring(0, DumpResource.tmpDirPath2.length() - 1));
			if (!tmpDir2.exists()) {
				if (tmpDir2.mkdir()) {
				} else {
					System.out.println("Failed to create directory!");
				}
			}
			// download dump
			URL link = new URL(DumpResource.downloadLink);
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
			String fileName = DumpResource.tmpDirPath + String.valueOf(currentTime) + ".ttl";
			FileOutputStream fos = new FileOutputStream(fileName);
			fos.write(response2);
			fos.close();
			// copy file
			Path sourcePath = Paths.get(fileName);
			Path destinationpath = Paths.get(fileName.replace(String.valueOf(currentTime), "labelingsystem-latest").replace(DumpResource.tmpDirString, DumpResource.tmpDirString2));
			Files.copy(sourcePath, destinationpath);
			// delete file
			File fileDelete = new File(fileName.replace(String.valueOf(currentTime), "labelingsystem-latest"));
			Files.deleteIfExists(fileDelete.toPath());
			// write readme.txt
			PrintWriter writer = new PrintWriter(DumpResource.tmpDirPath + "readme.txt", "UTF-8");
			writer.println("Labeling System Readme");
			writer.close();
			PrintWriter writer2 = new PrintWriter(DumpResource.tmpDirPath2 + "readme.txt", "UTF-8");
			writer2.println("Labeling System Readme");
			writer2.close();
			// compress folder(s)
			DumpResource.dumbNo++;
			String compressedFile = DumpResource.fileDir + "LS-" + String.valueOf(currentTime) + "-" + DumpResource.getNumberOfStatements() + "-" + DumpResource.dumbNo + ".tar.gz"; // destination tar.gz
			TarGZ.compressWithoutFolder(DumpResource.tmpDirPath, compressedFile, DumpResource.tmpDirString);
			String compressedFile2 = DumpResource.fileDir + "labelingsystem-latest.tar.gz"; // destination tar.gz
			TarGZ.compressWithoutFolder(DumpResource.tmpDirPath2, compressedFile2, DumpResource.tmpDirString2);
			// delte tmp folder(2)
			FileUtils.deleteDirectory(new File(DumpResource.tmpDirPath));
			FileUtils.deleteDirectory(new File(DumpResource.tmpDirPath2));
			// set public path
			DumpResource.lastDump = DumpResource.filePath + "LS-" + String.valueOf(currentTime) + "-" + DumpResource.getNumberOfStatements() + "-" + DumpResource.dumbNo + ".tar.gz"; // destination tar.gz
			// sleep and start function again
			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");
			Date resultdate = new Date(currentTime);
			System.out.println("downloaded at: " + sdf.format(resultdate) + " | " + compressedFile);
			DumpResource.lastDumpTime = sdf.format(resultdate);
			Thread.currentThread().sleep(DumpResource.sleepTimeInMills);
			if (DumpResource.dumping) {
				writeFile();
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			throw new IllegalStateException(e.toString());
		}
	}

}
