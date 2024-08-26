package delta.games.lotro.tools.tools;

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
  private final SimpleDateFormat _sdf=new SimpleDateFormat("yyyy-MM-dd-HHmmss");

  private void doRename(File f)
  {
    long time=f.lastModified();
    File to=null;
    int index=0;
    do
    {
      String name=null;
      if (index==0)
      {
        name=_sdf.format(new Date(time))+".jpg";
      }
      else
      {
        name=_sdf.format(new Date(time))+"-"+index+".jpg";
      }
      to=new File(f.getParentFile(),name);
      index++;
    } while (to.exists());
    if(!to.exists())
    {
      System.out.println("Rename to: "+to.getName()); // NOSONAR
      boolean ok=f.renameTo(to);
      if (!ok)
      {
        System.err.println("Rename failed!"); // NOSONAR
      }
    }
  }

  private void doIt(File rootDir)
  {
    final FileFilter f=new FileFilter()
    {
      @Override
      public boolean accept(File pathname)
      {
        return pathname.getName().startsWith("ScreenShot");
      }
    };
    FileIteratorCallback c=new FileIteratorCallback()
    {
      @Override
      public void leaveDirectory(File absolute, File relative)
      {
        // Nothing to do!
      }
      @Override
      public void handleFile(File absolute, File relative)
      {
        if (f.accept(absolute))
        {
          doRename(absolute);
        }
      }
      @Override
      public void handleDirectory(File absolute, File relative)
      {
        // Nothing to do!
      }
      @Override
      public boolean enterDirectory(File absolute, File relative)
      {
        return true;
      }
    };

    FileIterator it=new FileIterator(rootDir,true,c);
    it.run();
  }

  /**
   * Main method of this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    // X:\\damien\\docs\\jeux\\lotro\\screenshots
    File rootDir=new File(args[0]);
    new ScreenshotsRename().doIt(rootDir);
  }
}
