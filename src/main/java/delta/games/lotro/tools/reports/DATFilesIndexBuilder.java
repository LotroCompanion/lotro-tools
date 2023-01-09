package delta.games.lotro.tools.reports;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import delta.common.utils.files.FilesFinder;
import delta.common.utils.files.filter.ExtensionPredicate;
import delta.games.lotro.dat.archive.DATArchive;
import delta.games.lotro.dat.archive.DATArchiveIndexBuilder;
import delta.games.lotro.dat.utils.ClientPathUtils;

/**
 * Build for DAT files indexes.
 * @author DAM
 */
public class DATFilesIndexBuilder
{
  private static final File ROOT_DIR=new File("../lotro-companion-private-doc/dat/index");

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
    String indexName=datFile.getName().replace(".dat",".index.txt");
    File indexFile=new File(ROOT_DIR,indexName);
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
    FileFilter filter=new ExtensionPredicate(".dat");
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
    new DATFilesIndexBuilder().doIt();
  }
}
