package delta.games.lotro.tools.utils.lotrowiki;

import java.io.File;

import org.apache.log4j.Logger;

import delta.downloads.DownloadException;
import delta.downloads.Downloader;
import delta.games.lotro.tools.utils.TmpFiles;

/**
 * Interface with the lotro-wiki site.
 * @author DAM
 */
public class LotroWikiSiteInterface
{
  private static final Logger _logger=Logger.getLogger(LotroWikiSiteInterface.class);

  private TmpFiles _tmpFiles;
  private Downloader _client;

  /**
   * Constructor.
   * @param dirName Directory name.
   */
  public LotroWikiSiteInterface(String dirName)
  {
    _client=new Downloader();
    File tmpDir=new File("tmp",dirName);
    _tmpFiles=new TmpFiles(tmpDir);
  }

  /**
   * Download a page to a temporary file.
   * @param url URL to read from.
   * @param name Name of the temporary file.
   * @return The path of the result file.
   */
  public File download(String url, String name)
  {
    File file=_tmpFiles.getFile(name);
    if (!file.exists())
    {
      download(url,file);
    }
    return file;
  }

  /**
   * Download a page to a given file.
   * @param url URL to read from.
   * @param to File to write to.
   */
  public void download(String url, File to)
  {
    to.getParentFile().mkdirs();
    try
    {
      _client.downloadToFile(url,to);
    }
    catch(DownloadException downloadException)
    {
      _logger.error("Could not download URL ["+url+"]", downloadException);
    }
  }
}
