package delta.games.lotro.tools.dat.misc;

import java.io.File;

import org.apache.log4j.Logger;

import delta.common.utils.files.archives.DirectoryArchiver;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Manager for misc. icons.
 * @author DAM
 */
public class MiscIconsManager
{
  private static final Logger LOGGER=Logger.getLogger(MiscIconsManager.class);

  /**
   * Directory for misc icons.
   */
  public static final File MISC_ICONS_DIR=new File("data\\misc\\tmp").getAbsoluteFile();

  /**
   * Write misc icons file.
   */
  public static void writeMiscIconsFile()
  {
    // Icons
    DirectoryArchiver archiver=new DirectoryArchiver();
    boolean ok=archiver.go(GeneratedFiles.MISC_ICONS,MISC_ICONS_DIR);
    if (ok)
    {
      LOGGER.info("Wrote misc icons archive: "+GeneratedFiles.MISC_ICONS);
    }
  }
}
