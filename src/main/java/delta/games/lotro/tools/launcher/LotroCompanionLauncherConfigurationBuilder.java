package delta.games.lotro.tools.launcher;

import java.io.File;

import delta.launcher.data.Classpath;
import delta.launcher.data.LauncherConfiguration;
import delta.launcher.data.io.xml.LauncherConfigurationXmlIO;

/**
 * Builds a launcher configuration for LotroCompanion.
 * @author DAM
 */
public class LotroCompanionLauncherConfigurationBuilder
{
  private static final File ROOT_DIR=new File("lib");
  private static final String[] FILENAMES={
    "patches",
    "delta-lotro-companion-14.0.jar",
    "delta-lotro-interceptor-1.1.jar",
    "delta-lotro-dat-utils-5.0.jar",
    "delta-json-1.1.1.jar",
    "delta-lotro-core-14.0.jar",
    "delta-lotro-maps-3.0.jar",
    "delta-common-1.6.2.jar",
    "delta-common-ui-1.12.jar",
    // Packets capture
    "pcap4j-core-1.8.2.jar",
    "pcap4j-packetfactory-static-1.8.2.jar",
    "jna-5.3.1.jar",
    // JFreeChart
    "jcommon-1.0.16.jar",
    "jfreechart-1.0.13.jar",
    // Logging
    "log4j-over-slf4j-1.7.28.jar",
    "slf4j-api-1.7.28.jar",
    "logback-classic-1.2.3.jar",
    "logback-core-1.2.3.jar",
    // Icons
    "effectIcons.zip",
    "emoteIcons.zip",
    "icons.zip",
    "legaciesIcons.zip",
    "relicIcons.zip",
    "skillIcons.zip",
    "titleIcons.zip",
    "traitIcons.zip",
    "miscIcons.zip"
  };

  private Classpath buildClasspath()
  {
    Classpath ret=new Classpath();
    for(String filename : FILENAMES)
    {
      ret.addElement(new File(filename));
    }
    return ret;
  }

  private LauncherConfiguration buildLauncherConfiguration()
  {
    LauncherConfiguration config=new LauncherConfiguration();
    config.setMainClass("delta.games.lotro.Main");
    config.setRootDir(ROOT_DIR);
    Classpath classpath=buildClasspath();
    config.setClasspath(classpath);
    return config;
  }

  private void doIt()
  {
    File configFile=new File("launcher.xml");
    LauncherConfiguration config=buildLauncherConfiguration();
    LauncherConfigurationXmlIO.writeFile(configFile,config);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new LotroCompanionLauncherConfigurationBuilder().doIt();
  }
}
