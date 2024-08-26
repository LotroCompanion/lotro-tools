package delta.games.lotro.tools.extraction.achievables;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import delta.common.utils.files.TextFileWriter;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Logs achievables data to a file.
 * @author DAM
 */
public class AchievablesLogger
{
  private Set<Integer> _doneIDs;
  private TextFileWriter _writer;
  private boolean _useDeeds;
  private boolean _useQuests;

  /**
   * Constructor.
   * @param useDeeds Use deeds or not.
   * @param useQuests Use quests or not.
   * @param name File name.
   */
  public AchievablesLogger(boolean useDeeds, boolean useQuests, String name)
  {
    _useDeeds=useDeeds;
    _useQuests=useQuests;
    _doneIDs=new HashSet<Integer>();
    _writer=new TextFileWriter(new File(name));
    _writer.start();
  }

  /**
   * Handle an achievable.
   * @param achievableID Achievable identifier.
   * @param isQuest Quest or deed?
   * @param properties Description properties.
   */
  public void handleAchievable(int achievableID, boolean isQuest, PropertiesSet properties)
  {
    if (_doneIDs.contains(Integer.valueOf(achievableID)))
    {
      return;
    }
    _doneIDs.add(Integer.valueOf(achievableID));
    boolean doIt=(isQuest)?_useQuests:_useDeeds;
    if (doIt)
    {
      _writer.writeNextLine(achievableID);
      _writer.writeNextLine(properties.dump());
    }
  }

  /**
   * End of the process.
   */
  public void finish()
  {
    _writer.terminate();
  }
}
