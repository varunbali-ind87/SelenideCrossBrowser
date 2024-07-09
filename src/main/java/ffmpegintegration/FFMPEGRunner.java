package ffmpegintegration;

import browsersetup.SelenideBase;
import com.google.common.util.concurrent.Uninterruptibles;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import utilities.DateUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Log4j2
public class FFMPEGRunner
{
	//private static final String COMMANDPROMPT = "C:" + File.separator + "Windows" + File.separator + "System32" + File.separator + "cmd.exe";
	private static Process process;
	private static String outputFile;
	private static String ffmpegBinary;

	private static final String UNSHARPPARAMETER = "unsharp=luma_msize_x=3:luma_msize_y=3:luma_amount=1.0";

	private FFMPEGRunner()
	{
	}

	private static String getOutputFilename(String browser) throws IOException
	{
		// Create capture directory to store captures if it doesn't exist
		var outputFile = new File(String.valueOf(Paths.get(System.getProperty("user.dir"), "Captures")));
		if (!outputFile.exists() || !outputFile.isDirectory())
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
		String outputFilename;
		if (!builder.isEmpty())
			outputFilename = browser + "_" + builder + "_" + DateUtil.getCurrentTimestamp() + ".mkv";
		else
			outputFilename = browser + "_" + DateUtil.getCurrentTimestamp() + ".mkv";

		return String.valueOf(Paths.get(System.getProperty("user.dir"), "Captures", outputFilename));
	}

	private static void killExistingFFMPEGProcesses() throws IOException, InterruptedException
	{
		log.info("Attempting to terminate FFMPEG forcefully...");
		var process = Runtime.getRuntime().exec(new String[] {"tasklist"});
		var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null)
		{
			if (line.contains(ffmpegBinary))
			{
				String pid = line.split("\\s+")[1];
				Runtime.getRuntime().exec(new String[] {"taskkill", "/F ", "/T ", "/PID ", pid});
			}
		}
	}

	/**
	 * Sets up the various commands required based on the OS for the video capture & initializes it. Before initializing it checks if there
	 * are any existing FFMPEG processes already running and if there are any then it terminates them.
	 *
	 * @param browser the browser
	 * @throws IOException the io exception
	 */
	public static void startVideoCapture(final String browser) throws IOException, InterruptedException, ConfigurationException
    {
		if (SystemUtils.IS_OS_WINDOWS)
		{
			ffmpegBinary = FFMPEGSetup.getDownloadedFile().getName();
			killExistingFFMPEGProcesses();
		}
		FFMPEGPropertiesManager.getInstance().readFFMPEGProperties();
		final var processBuilder = new ProcessBuilder();
		outputFile = getOutputFilename(browser);
		processBuilder.directory(new File(System.getProperty("user.dir")));
		String ffmpegBinaryPath = FFMPEGSetup.getDownloadedFile().getCanonicalPath();
		String captureDevice = null;
		String captureVideoInput = null;
		if (SystemUtils.IS_OS_WINDOWS)
		{
			captureDevice = "gdigrab";
			captureVideoInput = "desktop";
		}
		else if (SystemUtils.IS_OS_MAC)
		{
			captureDevice = "avfoundation";
			captureVideoInput = "1";
		}
		processBuilder.command(ffmpegBinaryPath, "-f", captureDevice, "-i", captureVideoInput, "-c:v", "libx264", "-r", FFMPEGPropertiesManager
						.getInstance().getFramerateProperty(), "-preset", "ultrafast", "-filter:v", UNSHARPPARAMETER, "-y", outputFile);
		processBuilder.redirectErrorStream(true);
		process = processBuilder.start();
		log.info("Screen capture initialized.");
		readFFMPEGLogs();
	}

	private static void readFFMPEGLogs()
	{
		if (StringUtils.containsIgnoreCase(FFMPEGPropertiesManager.getInstance().getDisplayFFMPEGLogs(), "yes"))
		{
			log.info("FFMPEG Logs enabled! Be prepared to be spammed!");
			new Thread(() ->
			{
				try (var bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream())))
				{
					String line;
					while ((line = bufferedReader.readLine()) != null)
						log.debug(line);
				}
				catch (IOException e)
				{
					log.error("Error reading I/O from FFMPEG!");
					log.error(e);
				}
			}).start();
		}
	}

	/**
	 * Attempts to stop video capture by sending the 'q' input to the FFMPEG process to end it normally. If the process continues to run
	 * even after that then forceful termination is attempted.
	 *
	 * @throws InterruptedException the interrupted exception
	 * @throws IOException          the io exception
	 */
	public static void stopVideoCapture() throws InterruptedException, IOException
	{
		log.info("Is FFMPEG process alive? {}. Attempting to terminate it gracefully.", process.isAlive());
		if (SystemUtils.IS_OS_WINDOWS)
		{
			stopFFMPEGProcess();
			if (process.isAlive())
			{
				log.error("Graceful termination of FFMPEG failed.");
				killExistingFFMPEGProcesses();
			}
			else
				log.info("FFMPEG terminated successfully.");
		}
		else if (SystemUtils.IS_OS_MAC)
		{
			stopFFMPEGProcess();
			if (process.isAlive())
			{
				log.info("FFMPEG did not end. Terminating it forcefully..");
				process.destroy();
				process.waitFor();
			}
			else
				log.info("FFMPEG terminated successfully.");
		}
		log.info("Screen capture stopped.");
		var file = new File(outputFile);
		if (file.exists() && file.isFile())
			log.info("Video capture saved: {}", outputFile);
		else
			log.error("Something went wrong! Screen capture was not found in Captures directory.");
	}

	private static void stopFFMPEGProcess() throws InterruptedException
	{
		int attempts = 0;
		while (process.isAlive() && attempts++ < 2)
		{
			try
			{
				var outputStream = process.getOutputStream();
				outputStream.write("q".getBytes());
				outputStream.write(System.lineSeparator().getBytes());
				Uninterruptibles.sleepUninterruptibly(Duration.ofSeconds(1));
				outputStream.flush();
				process.waitFor(3, TimeUnit.SECONDS);
			}
			catch (IOException e)
			{
				if (!e.getMessage().contains("Stream closed"))
				{
					log.error("FFMPEG still running", e);
				}
			}
		}
	}
}
