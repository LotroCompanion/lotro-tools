package delta.games.lotro.tools.extraction.utils;

import java.io.File;
import java.io.FileFilter;

import org.apache.log4j.Logger;

import delta.common.utils.files.FilesDeleter;

/**
 * Utility methods to cleanup files.
 * @author DAM
 */
public class CleanupUtils
{
  private static final Logger LOGGER=Logger.getLogger(CleanupUtils.class);

  /**
   * Delete a file.
   * @param toDelete File to delete.
   */
  public static void deleteFile(File toDelete)
  {
    if (toDelete==null)
    {
      LOGGER.warn("Cannot delete null file!");
      return;
    }
    if (toDelete.exists())
    {
      boolean ok=toDelete.delete();
      if (!ok)
      {
        LOGGER.warn("Could not delete file: "+toDelete);
      }
    }
  }

  /**
   * Delete a directory (recursively).
   * @param toDelete Directory to clean (directory is kept).
   */
  public static void deleteDirectory(File toDelete)
  {
    deleteDirectory(toDelete,null);
  }

  /**
   * Delete the files in a directory that pass the given filter.
   * @param toDelete Directory to clean (directory is kept).
   * @param filter Filter to select the files to remove (<code>null</code> to remove all).
   */
  public static void deleteDirectory(File toDelete, FileFilter filter)
  {
    if (toDelete==null)
    {
      LOGGER.warn("Cannot delete null directory!");
      return;
    }
    FilesDeleter deleter=new FilesDeleter(toDelete,filter,false);
    deleter.doIt();
  }
}
