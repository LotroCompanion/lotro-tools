package delta.games.lotro.tools.dat.maps;

import java.io.File;

import delta.games.lotro.config.LotroCoreConfig;

/**
 * Constants related to maps management.
 * @author DAM
 */
public class MapConstants
{
  /**
   * Get the root directory for maps data.
   * @return A directory.
   */
  public static File getRootDir()
  {
    return LotroCoreConfig.getInstance().getFile("maps");
  }

  /**
   * Get the maps directory.
   * @return the maps directory.
   */
  public static File getMapsDir()
  {
    return new File(getRootDir(),"maps");
  }
}
