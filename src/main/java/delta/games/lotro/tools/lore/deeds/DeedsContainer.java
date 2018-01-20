package delta.games.lotro.tools.lore.deeds;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLParser;

/**
 * Container for deeds.
 * @author DAM
 */
public class DeedsContainer
{
  private File _deedsFile;
  private List<DeedDescription> _deeds;

  /**
   * Constructor.
   * @param deedsFile
   */
  public DeedsContainer(File deedsFile)
  {
    _deedsFile=deedsFile;
    load();
  }

  private void load()
  {
    DeedXMLParser parser=new DeedXMLParser();
    _deeds=parser.parseXML(_deedsFile);
  }

  /**
   * Get the number of deeds in this container.
   * @return A count.
   */
  public int getCount()
  {
    return _deeds.size();
  }

  /**
   * Get a list of all deeds in this container.
   * @return A possibly empty but not <code>null</code> list of deeds.
   */
  public List<DeedDescription> getAll()
  {
    return new ArrayList<DeedDescription>(_deeds);
  }

  /**
   * Get deeds using a name.
   * @param name Name to search.
   * @return A possibly empty but not <code>null</code> list of deeds.
   */
  public List<DeedDescription> getDeedByName(String name)
  {
    List<DeedDescription> deeds=new ArrayList<DeedDescription>();
    for(DeedDescription deed : _deeds)
    {
      if (deed.getName().equals(name))
      {
        deeds.add(deed);
      }
    }
    return deeds;
  }
}
