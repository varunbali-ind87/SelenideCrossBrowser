package ffmpegintegration;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

public class FFMPEGSetup
{
    private static final Logger LOGGER = LogManager.getLogger(FFMPEGSetup.class);
    static final String MESSAGE = "OOPS! Looks like FFMPEG binary was deleted by someone.";
    static final String WIN_FFMPEG_BINARY = "ffmpeg.exe";
    private static boolean isSetupDone = false;
    static final String FFMPEG_NAME = "ffmpeg";
    static String latestBuildVersion;
    @Getter
    private static File downloadedFile;

    private FFMPEGSetup()
    {
    }

    /**
     * Sets up the FFMPEG environment by checking for updates, deleting existing binaries, downloading the latest FFMPEG binary,
     * and updating the build version properties file.
     *
     * @throws ClassNotFoundException If the class is not found
     * @throws NoSuchMethodException If a specific method is not found
     * @throws InvocationTargetException If an invocation target exception occurs
     * @throws InstantiationException If an instantiation exception occurs
     * @throws IllegalAccessException If an illegal access exception occurs
     * @throws IOException If an IO exception occurs
     * @throws URISyntaxException If a URI syntax exception occurs
     */
    public static void setup() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException, URISyntaxException
    {
        if (Boolean.FALSE.equals(isSetupDone))
        {
            String operatingSystem = FFMPEGUtils.getOperatingSystem();

            // Determines whether to call the Windows or Mac OSX logic based on the OS
            var className = Class.forName(FFMPEGSetup.class.getPackageName() + ".FFMPEG" + operatingSystem + "Manager");
            var ffmpegDownloadInstance = (FFMPEGDownloadManager) className.getConstructor().newInstance();

            // Checks if the update is present & if there is, then it deletes all the existing FFMPEG binaries & directories. Finally, it downloads the latest FFMPEG binary
            if (ffmpegDownloadInstance.isUpdatePresent())
            {
                // Delete all pre-existing FFMPEG files
                LOGGER.info("Deleting all pre-existing FFMPEG binaries to ensure only single binary exists for future use...");
                var tempDirectoryFile = ffmpegDownloadInstance.getTempDirectoryFile();
                FileUtils.cleanDirectory(tempDirectoryFile);

                // Download the FFMPEG binary
                downloadedFile = ffmpegDownloadInstance.extractCompressedFile();

                // Update the ffmpegbuild.properties file with the latest build version
                String buildVersionKey = SystemUtils.IS_OS_WINDOWS ? FFMPEGVersionManager.WINDOWS_LAST_BUILD : FFMPEGVersionManager.OSX_LAST_BUILD;
                FFMPEGVersionManager.getInstance().updateProperties(buildVersionKey, latestBuildVersion);
            }
            else
                downloadedFile = ffmpegDownloadInstance.getFFMPEGBinary();

            isSetupDone = true; // avoid downloading binary for every scenario. It should be once every launch.
        }
    }
}
