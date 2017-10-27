package delta.games.lotro.tools.lore.quests;

import java.io.File;

import org.apache.log4j.Logger;

import delta.common.utils.environment.FileSystem;
import delta.games.lotro.utils.LotroLoggers;

/**
 * Quests database generator.
 * @author DAM
 */
public class QuestsDatabaseGenerator
{
  private static final Logger _logger=LotroLoggers.getLotroLogger();

  private File _workDir;
  private File _questsDir;

  /**
   * Constructor.
   */
  public QuestsDatabaseGenerator()
  {
    _workDir=FileSystem.getTmpDir();
    _questsDir=new File(_workDir,"quests");
  }

  /**
   * Get the managed quests directory.
   * @return A directory.
   */
  public File getQuestsDir()
  {
    return _questsDir;
  }

  /**
   * Write a quests database from the files in the managed quests directory.
   * @return <code>true</code> if it was successful, <code>false</code> otherwise.
   */
  public boolean writeDatabase()
  {
    // 3 - build quest index
    File questsIndexFile=new File(_workDir,"questsIndex.xml");
    QuestsIndexBuilder questsIndexBuilder=new QuestsIndexBuilder(_questsDir,questsIndexFile);
    boolean indexOK=questsIndexBuilder.doIt();
    if (!indexOK)
    {
      _logger.error("Cannot build quests index! Stopping.");
      return false;
    }
    // 4 - archive quests
    File archiveFile=new File(_workDir,"quests.zip");
    QuestsArchiveBuilder archiveBuilder=new QuestsArchiveBuilder(_questsDir,archiveFile);
    boolean archiveOK=archiveBuilder.doIt();
    if (!archiveOK)
    {
      _logger.error("Cannot build quests archive! Stopping.");
      return false;
    }
    return true;
  }

  /**
   * Perform quests database generation. 
   */
  public void doIt()
  {
    // 1 - get quests index
    File tmpQuestsIndexFile=new File(_workDir,"tmpQuestsIndex.xml");
    QuestsIndexLoader indexLoader=new QuestsIndexLoader();
    boolean indexLoadingOK=indexLoader.doIt(tmpQuestsIndexFile);
    if (!indexLoadingOK)
    {
      _logger.error("Cannot get quests index! Stopping.");
      return;
    }
    // 2 - load quests
    QuestsLoader questsLoader=new QuestsLoader(_questsDir,tmpQuestsIndexFile);
    boolean questsLoadingOK=questsLoader.doIt();
    if (!questsLoadingOK)
    {
      _logger.error("Cannot load quests! Stopping.");
      return;
    }
    writeDatabase();
  }

  /**
   * Main method for quests database generator.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    QuestsDatabaseGenerator generator=new QuestsDatabaseGenerator();
    generator.doIt();
  }
}
