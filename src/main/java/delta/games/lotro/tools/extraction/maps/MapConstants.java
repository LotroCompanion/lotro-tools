package delta.games.lotro.tools.extraction.maps;

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
}
