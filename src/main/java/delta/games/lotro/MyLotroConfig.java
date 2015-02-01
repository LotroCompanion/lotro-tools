package delta.games.lotro;

/**
 * Configuration for MyLotro.
 * @author DAM
 */
public class MyLotroConfig
{
  private static MyLotroConfig _instance=new MyLotroConfig();
  
  private String _myLotroRootURL;

  /**
   * Get the sole instance of this class.
   * @return the sole instance of this class.
   */
  public static MyLotroConfig getInstance()
  {
    return _instance;
  }

  /**
   * Private constructor.
   */
  private MyLotroConfig()
  {
    _myLotroRootURL="http://my.lotro.com/";
  }

  /**
   * Get the URL for a toon.
   * @param serverName Server of toon.
   * @param toonName Name of toon.
   * @return An URL.
   */
  public String getCharacterURL(String serverName, String toonName)
  {
    String ret=_myLotroRootURL+"home/character/"+serverName.toLowerCase()+"/"+toonName.toLowerCase();
    return ret;
  }
}
