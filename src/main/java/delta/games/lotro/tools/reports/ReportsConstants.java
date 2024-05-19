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
  private static final File LIVE_ROOT_DIR=new File("../lotro-live-private-doc");
  private static final File SOA_ROOT_DIR=new File("../lotro-legacy-private-doc");
  private static final File DDO_ROOT_DIR=new File("../ddo-live-private-doc");

  /**
   * Get the root directory for DAT reports.
   * @return A root directory.
   */
  public static File getReportsRootDir()
  {
    return new File(getBaseReportsRootDir(),"dat");
  }

  /**
   * Get the root directory for data reports.
   * @return A root directory.
   */
  public static File getDataReportsRootDir()
  {
    return new File(getBaseReportsRootDir(),"reports");
  }

  /**
   * Get the root directory for all reports.
   * @return A root directory.
   */
  public static File getBaseReportsRootDir()
  {
    Version version=Context.getVersion();
    if (version==Version.LIVE) return LIVE_ROOT_DIR;
    else if (version==Version.SOA_11) return SOA_ROOT_DIR;
    else if (version==Version.DDO) return DDO_ROOT_DIR;
    return null;
  }
}
