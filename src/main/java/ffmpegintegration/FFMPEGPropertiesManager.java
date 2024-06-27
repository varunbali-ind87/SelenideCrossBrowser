package ffmpegintegration;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@Log4j2
public class FFMPEGPropertiesManager
{
	public static final String DEFAULT_DISPLAYFFMPEGLOGS_VALUE = "yes";
	private static final String FRAMERATE = "Framerate";
	public static final String DEFAULT_FRAMERATE_VALUE = "60";
	private static final String SETTING_MESSAGE = "Setting {} to {}";
	private static final String DISPLAYFFMPEGLOGS = "DisplayFFMPEGLogs";
	private static final String PRESET = "Preset";
	private static final String FFMPEG_PROPERTIES;
	public static final String DEFAULT_PRESET_VALUE = "medium";
	private static FFMPEGPropertiesManager ffmpegPropertiesManager;

	static
	{
		FFMPEG_PROPERTIES = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
				+ "resources" + File.separator + "ffmpeg.properties";
	}

	private PropertiesConfiguration configuration;
	private PropertiesConfigurationLayout layout;
	private Properties properties;

	private FFMPEGPropertiesManager()
	{
	}

	public static synchronized FFMPEGPropertiesManager getInstance()
	{
		if (ffmpegPropertiesManager == null)
			ffmpegPropertiesManager = new FFMPEGPropertiesManager();
		return ffmpegPropertiesManager;
	}

	public String getFramerateProperty()
	{
		return this.properties.getProperty(FRAMERATE);
	}

	public String getDisplayFFMPEGLogs()
	{
		return this.properties.getProperty(DISPLAYFFMPEGLOGS);
	}

	public void readFFMPEGProperties() throws ConfigurationException
    {
		properties = new Properties();
		boolean isFFMPEGFileRead = false;
		do
		{
			try (var reader = new FileReader(FFMPEG_PROPERTIES))
			{
				properties.load(reader);
				isFFMPEGFileRead = true;
			}
			catch (IOException e)
			{
				log.error("{} not found! Creating file from scratch...", FFMPEG_PROPERTIES);
				writeFFMPEGProperties();
			}
		}
		while (!isFFMPEGFileRead);
		log.info("ffmpeg.properties was read successfully.");
	}

	private void writeFFMPEGProperties() throws ConfigurationException
    {
		configuration = new PropertiesConfiguration();
		layout = new PropertiesConfigurationLayout();
		File file = new File(FFMPEG_PROPERTIES);
		FileHandler fileHandler = new FileHandler(configuration);
		configuration.setLayout(layout);
		layout.setHeaderComment("Properties specified in this file are used by FFMPEG process to create MP4 file.");
		layout.setComment(FRAMERATE,
				"Specifies the frame rate of the target MP4 file. Valid values should be: 24, 25, 30, 50, 60, 120. For a smooth video, the default value is 60.");
		configuration.setProperty(FRAMERATE, DEFAULT_FRAMERATE_VALUE);
		layout.setComment(PRESET, System.lineSeparator()
				+ "A preset is a collection of options that will provide a certain encoding speed to compression ratio. A slower preset will provide better compression (compression is quality per filesize). This means that, for example, if you target a certain file size or constant bit rate, you will achieve better quality with a slower preset. Similarly, for constant quality encoding, you will simply save bitrate by choosing a slower preset. available presets in descending order of speed are:\n" +
				"\tultrafast\n" +
				"\tsuperfast\n" +
				"\tveryfast\n" +
				"\tfaster\n" +
				"\tfast\n" +
				"\tmedium – default preset\n" +
				"\tslow\n" +
				"\tslower\n" +
				"\tveryslow\n" +
				"\tplacebo");
		configuration.setProperty(PRESET, DEFAULT_PRESET_VALUE);
		layout.setComment(DISPLAYFFMPEGLOGS, System.lineSeparator()
				+ "Allows the user to toggle FFMPEG encoding logs. Valid values: yes or no.");
		configuration.setProperty(DISPLAYFFMPEGLOGS, DEFAULT_DISPLAYFFMPEGLOGS_VALUE);
		fileHandler.setFile(file);
		fileHandler.save();
		log.info("{} created.", FFMPEG_PROPERTIES);
	}
}
