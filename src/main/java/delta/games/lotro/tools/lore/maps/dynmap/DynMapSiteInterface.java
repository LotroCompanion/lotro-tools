package delta.games.lotro.tools.lore.maps.dynmap;

import java.io.File;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import delta.common.utils.files.FileCopy;
import delta.games.lotro.tools.utils.TmpFiles;

/**
 * Interface with the dynmap site.
 * @author DAM
 */
public class DynMapSiteInterface
{
  private static final Logger _logger=Logger.getLogger(DynMapSiteInterface.class);

  private TmpFiles _tmpFiles;
  private CloseableHttpClient _client;

  /**
   * Constructor.
   */
  public DynMapSiteInterface()
  {
    RequestConfig globalConfig=RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
    _client=HttpClients.custom().setDefaultRequestConfig(globalConfig).build();
    File dataDir=new File("data");
    File mapsDir=new File(dataDir,"maps");
    File inputDir=new File(mapsDir,"input");
    _tmpFiles=new TmpFiles(inputDir);
    download(DynMapConstants.BASE_URL,"root.html");
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
      HttpGet get=new HttpGet(url.toString());
      CloseableHttpResponse response=_client.execute(get);
      try
      {
        HttpEntity entity=response.getEntity();
        if (entity!=null)
        {
          InputStream instream=entity.getContent();
          try
          {
            FileCopy.copy(instream,to);
          }
          finally
          {
            instream.close();
          }
        }
      }
      finally
      {
        response.close();
      }
    }
    catch(Exception e)
    {
      _logger.error("Error when downloading from URL ["+url+"] to file ["+to+']', e);
    }
  }
}
