package delta.games.lotro.tools.dat.utils;

import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.misc.Version;

/**
 * Find the game version to use for tools.
 * @author DAM
 */
public class VersionFinder
{
  private static final String BK11_ARG="--bk11";

  /**
   * Initialize the game version.
   * @param args Command line arguments.
   */
  public static final void initVersion(String[] args)
  {
    Version version=Version.LIVE;
    for(String arg : args)
    {
      if (BK11_ARG.equals(arg))
      {
        version=Version.SOA_11;
      }
    }
    Context.setVersion(version);
  }
}
