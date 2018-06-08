package delta.games.lotro.tools.utils;

import java.io.File;

/**
 * Temporary files manager.
 * @author DAM
 */
public class TmpFiles
{
  private File _rootDir;

  /**
   * Constructor.
   * @param rootDir Root directory for temporary files.
   */
  public TmpFiles(File rootDir)
  {
    _rootDir=rootDir;
  }

  /**
   * Get the file to use for a given file name.
   * @param name Name of the file.
   * @return A file.
   */
  public File getFile(String name)
  {
    return new File(_rootDir,name);
  }
}
