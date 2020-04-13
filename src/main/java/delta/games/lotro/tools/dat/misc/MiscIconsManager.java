package delta.games.lotro.tools.dat.misc;

import java.io.File;

import delta.common.utils.files.archives.DirectoryArchiver;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Manager for misc. icons.
 * @author DAM
 */
public class MiscIconsManager
{
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
      System.out.println("Wrote misc icons archive: "+GeneratedFiles.MISC_ICONS);
    }
  }
}
