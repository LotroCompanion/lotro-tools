package delta.games.lotro.tools.lore.items.icons;

import java.io.File;

import delta.common.utils.files.FileCopy;

/**
 * Moves sets of icons for a single object, to a separate folder.
 * @author DAM
 */
public class ExtractDuplicates
{
  private File _iconsDbDir=new File("d:\\tmp\\icons-db");
  private File _iconsDbDir2=new File("d:\\tmp\\icons-db2");

  private void doIt()
  {
    _iconsDbDir2.mkdirs();
    File[] files = _iconsDbDir.listFiles();
    for(File file : files)
    {
      String name=file.getName();
      int index=name.indexOf("-");
      int index2=name.indexOf("-",index+1);
      if (index2!=-1)
      {
        File to=new File(_iconsDbDir2,name);
        FileCopy.copy(file,to);
        String refName=name.substring(0,index2)+".png";
        File refFile=new File(_iconsDbDir,refName);
        if (refFile.exists())
        {
          FileCopy.copy(refFile,new File(_iconsDbDir2,refName));
        }
        file.delete();
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new ExtractDuplicates().doIt();
  }
}
