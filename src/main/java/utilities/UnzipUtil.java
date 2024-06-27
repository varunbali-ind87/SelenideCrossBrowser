package utilities;

import java.io.*;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

public class UnzipUtil
{
	private static final int BUFFER_SIZE = 4096;
	private static final Logger LOGGER = LogManager.getLogger(UnzipUtil.class);

    private UnzipUtil()
    {
    }

	/**
	 * Unzips the file in the destination directory specified.
	 *
	 * @param zipFilePath   the zip file path
	 * @param destDirectory the dest directory
	 * @throws IOException the io exception
	 */
	public static void unzip(File zipFilePath, String destDirectory) throws IOException
	{
		File destDir = new File(destDirectory);
		if (!destDir.exists())
			FileUtils.forceMkdir(destDir);
		try (var zipIn = new ZipInputStream(new FileInputStream(zipFilePath)))
		{
			var entry = zipIn.getNextEntry();
			// iterates over entries in the zip file
			while (entry != null)
			{
				String filePath = destDirectory + File.separator + entry.getName();
				if (!entry.isDirectory())
				{
					// if the entry is a file, extracts it
					try (var bos = new BufferedOutputStream(new FileOutputStream(filePath)))
					{
						byte[] bytesIn = new byte[BUFFER_SIZE];
						int read;
						while ((read = zipIn.read(bytesIn)) != -1)
						{
							bos.write(bytesIn, 0, read);
						}
					}
				}
				else
				{
					// if the entry is a directory, make the directory
					File dir = new File(filePath);
					var isDirectoryCreated = dir.mkdirs();
					if (!isDirectoryCreated)
						Assert.fail(zipFilePath.getName() + " failed! No directory was created.");
				}
				LOGGER.info("Extracted: {}", entry.getName());
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
		}
	}
}
