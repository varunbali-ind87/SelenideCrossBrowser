package ffmpegintegration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import utilities.UnzipUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

public class FFMPEGWindowsManager implements FFMPEGDownloadManager
{
	private static final String FFMPEG_RELEASE_ESSENTIALS_ZIP = "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip";
	public static final String GYAN_DEV_FFMPEG_BUILDS = "https://www.gyan.dev/ffmpeg/builds/";
	private static final Logger LOGGER = LogManager.getLogger(FFMPEGWindowsManager.class);
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
            LOGGER.info(FFMPEGSetup.MESSAGE);
            downloadedFile = extractCompressedFile();
        }
        return downloadedFile;
    }

	@Override
	public File getTempDirectoryFile()
	{
		return new File(TEMP_DIR);
	}

	public File downloadCompressedFile() throws IOException, URISyntaxException
    {
		var response = given().redirects().follow(false).get(FFMPEG_RELEASE_ESSENTIALS_ZIP).then().assertThat().statusCode(200).extract();
		var pattern = Pattern.compile("\\b((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])");
		var matcher = pattern.matcher(response.body().asString());
        if (matcher.find())
		{
			// Obtain the download URL
			String finalURL = matcher.group();
			LOGGER.info(finalURL);

			// Extract the filename from the download URL
			String filename = StringUtils.substringAfter(finalURL, "packages/");
			var file = new File(TEMP_DIR + File.separator + filename);
			LOGGER.info("Now downloading FFMPEG binary from the server.");

			// Download the file in the OS temp folder
			FileUtils.copyURLToFile(new URI(finalURL).toURL(), file);
			return file;
		}
		else
			throw new MalformedURLException("No valid windows binary path was found in the response!");
	}

	public synchronized boolean isUpdatePresent()
	{
		// reads the ffmpegbuild.properties file & stores the Windows & OSX build versions
		FFMPEGVersionManager.getInstance().readProperties();

		//If the ffmpegbuild.properties file contains build version then compare
		if (StringUtils.isNotBlank(FFMPEGVersionManager.getInstance().getProperty(FFMPEGVersionManager.WINDOWS_LAST_BUILD)))
		{
			LOGGER.info("Checking for latest FFMPEG Windows build...");
			FFMPEGSetup.latestBuildVersion = getLatestVersion();
			if(!FFMPEGVersionManager.getInstance().getProperty(FFMPEGVersionManager.WINDOWS_LAST_BUILD).equalsIgnoreCase(FFMPEGSetup.latestBuildVersion))
			{
				LOGGER.info("FFMPEG v{} is available.", FFMPEGSetup.latestBuildVersion);
				return true;
			}
			else
			{
				LOGGER.info("No FFMPEG update available.");
				return false;
			}
		}
		//If the properties file does not contain any build version then return true to force compressed file download from server
		else
		{
			LOGGER.error("OOPS! Looks like there is no value stored in the ffmpegbuild.properties file for {}", FFMPEGVersionManager.WINDOWS_LAST_BUILD);
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
		LOGGER.info("{} was downloaded.", downloadedCompressedZip.getName());

		// Unzip the downloaded ffmpeg file in the OS temp folder
		UnzipUtil.unzip(downloadedCompressedZip, TEMP_DIR);

		// Get a list of all the file handles including directories in the OS temp folder
		var files = new File(TEMP_DIR).listFiles();

		// Get the parent directory starting with the ffmpeg name
		var parentDir = Arrays.stream(Objects.requireNonNull(files)).filter(file -> file.isDirectory() && file.getName().startsWith("ffmpeg")).findFirst().orElseThrow();
		LOGGER.info("Unzipped downloaded file to {}", parentDir.getCanonicalPath());

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
