package ffmpegintegration;

import java.io.*;
import java.time.Duration;
import java.util.Properties;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.awaitility.Awaitility;

public class FFMPEGVersionManager
{
	public static final String OSX_LAST_BUILD = "osx_last_build";
	public static final String WINDOWS_LAST_BUILD = "windows_last_build";
	private static final String FFMPEG_BUILD_PROPERTIES;
	private static final Logger LOGGER = LogManager.getLogger(FFMPEGVersionManager.class);
	private static FFMPEGVersionManager versionManager;
	private final PropertiesConfiguration configuration = new PropertiesConfiguration();
	private final PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout();
	private Properties properties;

	static
	{
		FFMPEG_BUILD_PROPERTIES = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
				+ "resources" + File.separator + "ffmpegbuild.properties";
	}

	private FFMPEGVersionManager()
	{
	}

	public static synchronized FFMPEGVersionManager getInstance()
	{
		if (versionManager == null)
			versionManager = new FFMPEGVersionManager();
		return versionManager;
	}

	public String getProperty(String key)
	{
		return properties.getProperty(key);
	}

	public void readProperties()
	{
		properties = new Properties();
		var file = new File(FFMPEG_BUILD_PROPERTIES);
		Awaitility.waitAtMost(Duration.ofSeconds(20)).until(() ->
		{
			boolean isRead = false;
			try (var reader = new FileReader(file))
			{
				properties.load(reader);
				isRead = true;
			}
			catch (FileNotFoundException e)
			{
				writeProperties();
			}
			catch (IOException ignored)
			{
				LOGGER.error("IOException occurred while reading {}", FFMPEG_BUILD_PROPERTIES);
			}
			return isRead;
		});
	}

	public void updateProperties(final String key, final String value)
	{
		var file = new File(FFMPEG_BUILD_PROPERTIES);
		Awaitility.waitAtMost(Duration.ofSeconds(20)).until(() ->
		{
			boolean isUpdated = false;
			try (var reader = new FileReader(file))
			{
				layout.load(configuration, reader);
				configuration.setProperty(key, value);
				layout.save(configuration, new FileWriter(file));
				isUpdated = true;
			}
			catch (FileNotFoundException | ConfigurationException e)
			{
				writeProperties();
			}
			catch (IOException ignored)
			{
				LOGGER.error("IOException occurred while updating {}", FFMPEG_BUILD_PROPERTIES);
			}
			return isUpdated;
		});
		readProperties();
	}

	public void writeProperties()
	{
		var file = new File(FFMPEG_BUILD_PROPERTIES);
		configuration.setLayout(layout);
		layout.setHeaderComment("This file contains the ffmpeg build versions." + System.lineSeparator()
				+ "WARNING! Do not manually modify or delete this file.");
		configuration.addProperty(WINDOWS_LAST_BUILD, "0.0.0");
		configuration.addProperty(OSX_LAST_BUILD, "0.0.0");
		// Add OSX property here
		Awaitility.waitAtMost(Duration.ofSeconds(20)).until(() ->
		{
			try
			{
				layout.save(configuration, new FileWriter(file));
				return file.exists() && file.isFile();
			}
			catch (ConfigurationException | IOException e)
			{
				return false;
			}
		});
		readProperties();
	}
}
