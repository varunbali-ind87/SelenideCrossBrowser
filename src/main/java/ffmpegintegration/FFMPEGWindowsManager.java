package ffmpegintegration;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import utilities.UnzipUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

import static io.restassured.RestAssured.given;

@Log4j2
public class FFMPEGWindowsManager implements FFMPEGDownloadManager
{
	private static final String FFMPEG_RELEASE_ESSENTIALS_ZIP = "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip";
	public static final String GYAN_DEV_FFMPEG_BUILDS = "https://www.gyan.dev/ffmpeg/builds/";
	private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    public File getFFMPEGBinary() throws IOException, URISyntaxException
    {
        File downloadedFile;
        var subdirs = FileUtils.listFiles(new File(TEMP_DIR), new String[] {"exe"}, true);
        var optional = subdirs.stream().filter(temp -> temp.isFile() && temp.getName().equalsIgnoreCase(FFMPEGSetup.WIN_FFMPEG_BINARY))
                .findFirst();
        if (optional.isPresent())
            downloadedFile = optional.get();
        else
        {
            log.info(FFMPEGSetup.MESSAGE);
            downloadedFile = extractCompressedFile();
        }
        return downloadedFile;
    }

	@Override
	public File getTempDirectoryFile()
	{
		return new File(String.valueOf(Paths.get(TEMP_DIR, "ffmpeg")));
	}

	public File downloadCompressedFile() throws IOException, URISyntaxException
    {
		if (StringUtils.isNotBlank(FFMPEGSetup.latestBuildVersion))
            FFMPEGSetup.latestBuildVersion = getLatestVersion();
		String zipToDownload = "ffmpeg-" + FFMPEGSetup.latestBuildVersion + "-release-essentials.zip";
		Path downloadPath = Paths.get(TEMP_DIR, zipToDownload);
		log.info("Downloading from {}", FFMPEG_RELEASE_ESSENTIALS_ZIP);
		FileUtils.copyURLToFile(new URI(FFMPEG_RELEASE_ESSENTIALS_ZIP).toURL(), downloadPath.toFile());
		log.info("Download complete.");
		return downloadPath.toFile();
	}

	public synchronized boolean isUpdatePresent()
	{
		// reads the ffmpegbuild.properties file & stores the Windows & OSX build versions
		FFMPEGVersionManager.getInstance().readProperties();

		//If the ffmpegbuild.properties file contains build version then compare
		if (StringUtils.isNotBlank(FFMPEGVersionManager.getInstance().getProperty(FFMPEGVersionManager.WINDOWS_LAST_BUILD)))
		{
			log.info("Checking for latest FFMPEG Windows build...");
			FFMPEGSetup.latestBuildVersion = getLatestVersion();
			if(!FFMPEGVersionManager.getInstance().getProperty(FFMPEGVersionManager.WINDOWS_LAST_BUILD).equalsIgnoreCase(FFMPEGSetup.latestBuildVersion))
			{
				log.info("FFMPEG v{} is available.", FFMPEGSetup.latestBuildVersion);
				return true;
			}
			else
			{
				log.info("No FFMPEG update available.");
				return false;
			}
		}
		//If the properties file does not contain any build version then return true to force compressed file download from server
		else
		{
			log.error("OOPS! Looks like there is no value stored in the ffmpegbuild.properties file for {}", FFMPEGVersionManager.WINDOWS_LAST_BUILD);
			return true;
		}
	}

	/**
	 * Retrieves the latest version of FFMPEG by sending a GET request to the specified URL.
	 *
	 * @return the latest version of ffmpeg.exe
	 */
	public String getLatestVersion()
	{
		var response = given().get(GYAN_DEV_FFMPEG_BUILDS);
		var body = response.body().asString();
		var document = Jsoup.parse(body);
		var elements = document.select("span");
		var optional = elements.stream().filter(element -> element.hasAttr("id") && element.attr("id")
				.contains("release-version")).findFirst();
		return optional.orElseThrow().text();
	}

	/**
	 * Extracts a compressed file by downloading it, unzipping it, and converting it to an executable.
	 *
	 * @return the extracted File if successfully converted to an executable
	 * @throws IOException if there is an error while downloading or unzipping the file
	 * @throws URISyntaxException if there is an error while creating a URI
	 */
	public File extractCompressedFile() throws IOException, URISyntaxException
    {
		//Download the file
		var downloadedCompressedZip = downloadCompressedFile();
		log.info("{} was downloaded.", downloadedCompressedZip.getName());

		// Unzip the downloaded ffmpeg file in the OS temp folder
		UnzipUtil.unzip(downloadedCompressedZip, TEMP_DIR);

		// Get a list of all the file handles including directories in the OS temp folder
		var files = new File(TEMP_DIR).listFiles();

		// Get the parent directory starting with the ffmpeg name
		var parentDir = Arrays.stream(Objects.requireNonNull(files)).filter(file -> file.isDirectory() && file.getName().startsWith("ffmpeg")).findFirst().orElseThrow();
		log.info("Unzipped downloaded file to {}", parentDir.getCanonicalPath());

		// Get the list of all the files residing the parent directory
		var executables = FileUtils.listFiles(parentDir, new String[] {"exe"}, true);

		// Get the file handle of ffmpeg.exe
		var extractedFile = executables.stream().filter(temp -> temp.isFile() && temp.getName().equalsIgnoreCase("ffmpeg.exe"))
				.findFirst().orElseThrow();
		if(extractedFile.setExecutable(true))
			return extractedFile;
		else
			throw new IllegalStateException(extractedFile.getName() + " could not be converted to an executable!");
	}
}
