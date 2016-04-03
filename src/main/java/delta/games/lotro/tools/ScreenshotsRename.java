package delta.games.lotro.tools;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

import delta.common.utils.files.iterator.FileIterator;
import delta.common.utils.files.iterator.FileIteratorCallback;

/**
 * Tool to rename screenshot files.
 * @author DAM
 */
public class ScreenshotsRename
{
  private static final File ROOT_DIR=new File("X:\\damien\\docs\\jeux\\lotro\\screenshots");

  private static final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd-HHmmss");
  private static void doRename(File f)
  {
    long time=f.lastModified();
    String name=sdf.format(new Date(time))+".jpg";
    File to=new File(f.getParentFile(),name);
    if(!to.exists())
    {
      System.out.println("Rename to: "+name);
      f.renameTo(to);
    }
  }

  /**
   * Main method of this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    final FileFilter f=new FileFilter()
    {
      
      public boolean accept(File pathname)
      {
        return pathname.getName().startsWith("ScreenShot");
      }
    };
    FileIteratorCallback c=new FileIteratorCallback()
    {
      public void leaveDirectory(File absolute, File relative)
      {
        // Nothing to do!
      }
      public void handleFile(File absolute, File relative)
      {
        if (f.accept(absolute))
        {
          doRename(absolute);
        }
      }
      public void handleDirectory(File absolute, File relative)
      {
        // Nothing to do!
      }
      public boolean enterDirectory(File absolute, File relative)
      {
        return true;
      }
    };

    FileIterator it=new FileIterator(ROOT_DIR,true,c);
    it.run();
  }
}
