package delta.games.lotro.tools.reports;

import java.io.File;

import delta.games.lotro.config.LotroCoreConfig;

/**
 * Constants for the reports generators.
 * @author DAM
 */
public class ReportsContants
{
  private static final File ROOT_DIR=new File("../lotro-live-private-doc/dat");
  private static final File SOA_ROOT_DIR=new File("../lotro-legacy-private-doc/dat");

  /**
   * Get the root directory for reports.
   * @return A root directory.
   */
  public static File getReportsRootDir()
  {
    boolean isLive=LotroCoreConfig.isLive();
    return (isLive)?ROOT_DIR:SOA_ROOT_DIR;
  }
}
