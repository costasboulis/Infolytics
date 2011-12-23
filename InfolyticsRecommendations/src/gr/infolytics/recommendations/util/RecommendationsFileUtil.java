package gr.infolytics.recommendations.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;

/**
 * A simple utility class for handling files.  At the moment all it provides is the ability
 * to extract a zip file into a specified directory.
 */
public class RecommendationsFileUtil {

	/**
	 * This method extracts data to a given directory.
	 * 
	 * @param directory the directory to extract into
	 * @param zipIn input stream pointing to the zip file
	 * @throws ArchiveException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	 
	public static void extractZipToDirectory(File directory, InputStream zipIn)
	  throws ArchiveException, IOException, FileNotFoundException {
		
		ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("zip", zipIn);
		while (true) {
			ZipArchiveEntry entry = (ZipArchiveEntry)in.getNextEntry(); 
			if (entry==null) {
				in.close();
				break;
			}
			//Skip empty files
			if (entry.getName().equals("")) {
				continue;
			}
			
			
			if (entry.isDirectory()) {
				File file = new File(directory,entry.getName());
				file.mkdirs();
			}
			else {
				File outFile = new File(directory,entry.getName());
				if (!outFile.getParentFile().exists()) {
					outFile.getParentFile().mkdirs();
				}
				OutputStream out = new FileOutputStream(outFile); 
				IOUtils.copy(in, out); 
				out.flush();
				out.close(); 
			}
		}
	}
}
