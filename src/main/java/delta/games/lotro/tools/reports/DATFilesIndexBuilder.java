package delta.games.lotro.tools.reports;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import delta.common.utils.files.FilesFinder;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.archive.DATArchive;
import delta.games.lotro.dat.archive.DATArchiveIndexBuilder;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.ClientPathUtils;

/**
 * Build for DAT files indexes.
 * @author DAM
 */
public class DATFilesIndexBuilder
{
  private File _rootDir;

  /**
   * Constructor.
   * @param rootDir Root directory for produced data.
   */
  public DATFilesIndexBuilder(File rootDir)
  {
    _rootDir=rootDir;
  }

  private File findClientDir()
  {
    File ret;
    String path=System.getenv("LOTRO_CLIENT_PATH");
    if (path!=null)
    {
      ret=new File(path);
    }
    else
    {
      ret=ClientPathUtils.findClientDir();
    }
    return ret;
  }

  private void handleDATFile(File datFile)
  {
    DATArchiveIndexBuilder builder=new DATArchiveIndexBuilder();
    DATArchive archive=new DATArchive(datFile);
    archive.open();
    String name=datFile.getName();
    name=name.substring(0,name.indexOf('.'));
    File dir=new File(_rootDir,"index");
    String indexName=name+".index.txt";
    File indexFile=new File(dir,indexName);
    indexFile.getParentFile().mkdirs();
    builder.buildIndex(archive,indexFile);
    archive.close();
  }

  /**
   * Do it!
   */
  public void doIt()
  {
    File clientDir=findClientDir();
    FilesFinder finder=new FilesFinder();
    FileFilter filter=new FileFilter()
    {
      @Override
      public boolean accept(File pathname)
      {
        String name=pathname.getName();
        if ((name.startsWith("client_")) &&
            ((name.endsWith(".dat"))||(name.endsWith(".datx"))))
        {
          return true;
        }
        return false;
      }
    };
    List<File> files=finder.find(FilesFinder.ABSOLUTE_MODE,clientDir,filter,false);
    for(File file : files)
    {
      handleDATFile(file);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    File rootDir=ReportsContants.getReportsRootDir();
    new DATFilesIndexBuilder(rootDir).doIt();
  }
}
