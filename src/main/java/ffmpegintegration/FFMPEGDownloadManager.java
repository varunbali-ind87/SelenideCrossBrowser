package ffmpegintegration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public interface FFMPEGDownloadManager
{
    File downloadCompressedFile() throws IOException, URISyntaxException;
    boolean isUpdatePresent();

    String getLatestVersion();

    File extractCompressedFile() throws IOException, URISyntaxException;

    File getFFMPEGBinary() throws IOException, URISyntaxException;

    File getTempDirectoryFile();
}
