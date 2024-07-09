package ffmpegintegration;

import io.restassured.RestAssured;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import utilities.UnzipUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static io.restassured.RestAssured.given;

@Log4j2
public class FFMPEGMacManager implements FFMPEGDownloadManager
{
    private static final String FFMPEG_GETRELEASE_ZIP = "https://evermeet.cx/ffmpeg/getrelease/zip";
    private static final String FFMPEG_INFO_FFMPEG_RELEASE = "https://evermeet.cx/ffmpeg/info/ffmpeg/release";
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    public File getFFMPEGBinary() throws IOException, URISyntaxException
    {
        File downloadedFile;
        downloadedFile = new File(TEMP_DIR + File.separator + FFMPEGSetup.FFMPEG_NAME);
        if (!downloadedFile.exists())
        {
            log.info(FFMPEGSetup.MESSAGE);
            downloadedFile = extractCompressedFile();
        }
        return downloadedFile;
    }

    @Override
    public File getTempDirectoryFile()
    {
        return new File(TEMP_DIR);
    }

    /**
     * Downloads a compressed file from the server and saves it to the temporary directory.
     *
     * @return the file object representing the downloaded compressed file
     * @throws IOException if there is an error while downloading or saving the file
     */
    public File downloadCompressedFile() throws IOException, URISyntaxException
    {
        var response = given().redirects().follow(false).get(FFMPEG_GETRELEASE_ZIP);
        final var disposition = response.getHeader("content-disposition");
        var filename = StringUtils.substringAfter(disposition, "filename=");
        filename = StringUtils.strip(filename, "\"");
        var file = new File(TEMP_DIR + File.separator + filename);
        log.info("Now downloading FFMPEG binary from the server.");
        FileUtils.copyURLToFile(new URI(FFMPEG_GETRELEASE_ZIP).toURL(), file);
        return file;
    }

    /**
     * Checks if an update for the FFMPEG OSX binary is available.
     *
     * This method reads the ffmpegbuild.properties file and checks if the build version is present. If the build version is present,
     * it compares it with the latest version obtained from the getLatestVersion() method. If the build version is different
     * from the latest version, it logs a message indicating that an update is available and returns true. If the build version
     * is the same as the latest version, it logs a message indicating that no update is available and returns false.
     *
     * If the properties file does not contain any build version, it logs an error message and returns true to force the
     * compressed file download from the server.
     *
     * @return true if an update is available, false otherwise
     */
    public synchronized boolean isUpdatePresent()
    {
        FFMPEGVersionManager.getInstance().readProperties();
        // If the ffmpegbuild.properties file contains build version then compare
        if (StringUtils.isNotBlank(FFMPEGVersionManager.getInstance().getProperty(FFMPEGVersionManager.OSX_LAST_BUILD)))
        {
            log.info("Checking for FFMPEG OSX binary update...");
            FFMPEGSetup.latestBuildVersion = getLatestVersion();
            if(!FFMPEGVersionManager.getInstance().getProperty(FFMPEGVersionManager.OSX_LAST_BUILD).equalsIgnoreCase(FFMPEGSetup.latestBuildVersion))
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
        // If the properties file does not contain any build version then return true to force compressed file download from server
        else
        {
            log.error("OOPS! Looks like there is no value stored in the ffmpegbuild.properties file for {}", FFMPEGVersionManager.OSX_LAST_BUILD);
            return true;
        }
    }

    /**
     * Retrieves the latest version of FFMPEG by sending a GET request to the specified URL.
     *
     * @return the latest version of FFMPEG
     */
    public String getLatestVersion()
    {
        var response = RestAssured.given().get(FFMPEG_INFO_FFMPEG_RELEASE);
        return response.body().jsonPath().getMap("").get("version").toString();
    }

    /**
     * Extracts the compressed file by downloading it, unzipping it, and converting it to an executable.
     *
     * @return the extracted File if successfully converted to an executable
     */
    public File extractCompressedFile() throws IOException, URISyntaxException
    {
        var downloadedZip = downloadCompressedFile();
        UnzipUtil.unzip(downloadedZip, TEMP_DIR);
        var extractedFile = new File(TEMP_DIR + File.separator + "ffmpeg");
        if(extractedFile.setExecutable(true))
            return extractedFile;
        else
            throw new IllegalStateException(extractedFile.getName() + " could not be converted to an executable!");
    }
}
