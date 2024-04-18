package delta.games.lotro.tools.reports;

import java.io.File;

import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.misc.Version;

/**
 * Constants for the reports generators.
 * @author DAM
 */
public class ReportsConstants
{
  private static final File ROOT_DIR=new File("../lotro-live-private-doc/dat");
  private static final File SOA_ROOT_DIR=new File("../lotro-legacy-private-doc/dat");
  private static final File DDO_ROOT_DIR=new File("../ddo-live-private-doc/dat");

  /**
   * Get the root directory for reports.
   * @return A root directory.
   */
  public static File getReportsRootDir()
  {
    Version version=Context.getVersion();
    if (version==Version.LIVE) return ROOT_DIR;
    else if (version==Version.SOA_11) return SOA_ROOT_DIR;
    else if (version==Version.DDO) return DDO_ROOT_DIR;
    return null;
  }
}
