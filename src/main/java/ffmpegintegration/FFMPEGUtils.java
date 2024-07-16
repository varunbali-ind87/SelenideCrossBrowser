package ffmpegintegration;

import browsersetup.SelenideBase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import utilities.DateUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class FFMPEGUtils
{
    private static final String USERDIR = System.getProperty("user.dir");

    private FFMPEGUtils()
    {
    }

    /**
     * Generates the output filename alongwith the absolute path for the captured video based on the browser and scenario name.
     *
     * @param  browser  the browser used for capturing the video
     * @return          the final output filename for the captured video
     */
    public static String getOutputFilePath(String browser) throws IOException
    {
        // Create capture directory to store captures if it doesn't exist
        var outputFile = new File(String.valueOf(Paths.get(USERDIR, "Captures")));
        if (!FileUtils.isDirectory(outputFile))
            FileUtils.forceMkdir(outputFile);

        // Logic to deduce outputfilename by detecting the Jira ticket ID. If no ticket ID is present then replace with timestamp
        String scenarioName = SelenideBase.getScenario().getName();
        scenarioName = scenarioName.replace(" ", "").replaceAll("\\W", "").trim();
        var builder = new StringBuilder();
        char[] array = scenarioName.toCharArray();
        boolean isLastDigit = false;

        // Continue appending until the last digit is detected. At that moment, stop further processing
        for (int i = 0; i < array.length && !isLastDigit; i++)
        {
            if (!Character.isDigit(array[i]) && !builder.isEmpty() && builder.toString().matches(".*\\d.*"))
                isLastDigit = true;
            else
                builder.append(array[i]);
        }

        // Create final capture name
        String outputFilename = !builder.isEmpty() ? browser + "_" + builder + "_" + DateUtil.getCurrentTimestamp() + ".mkv"
                : browser + "_" + DateUtil.getCurrentTimestamp() + ".mkv";
        return String.valueOf(Paths.get(USERDIR, "Captures", outputFilename));
    }

    static String getOperatingSystem()
    {
        String operatingSystem = SystemUtils.OS_NAME;
        if (operatingSystem.contains(" "))
        {
            int indexOfSpace = operatingSystem.indexOf(" ");
            operatingSystem = operatingSystem.substring(0, indexOfSpace);
        }
        return operatingSystem;
    }
}
