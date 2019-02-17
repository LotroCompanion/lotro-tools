package delta.games.lotro.tools.dat.items.legendary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import delta.games.lotro.common.Effect;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.io.xml.EffectXMLWriter;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;

/**
 * Loader for legendary passives.
 * @author DAM
 */
public class PassivesLoader
{
  private DataFacade _facade;
  private Set<Integer> _parsedTables;
  private Map<Integer,Effect> _parsedEffects;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public PassivesLoader(DataFacade facade)
  {
    _facade=facade;
    _parsedTables=new HashSet<Integer>();
    _parsedEffects=new HashMap<Integer,Effect>();
  }

  /**
   * Handle a passives table.
   * @param id Table identifier.
   */
  public void handleTable(int id)
  {
    Integer tableKey=Integer.valueOf(id);
    if (_parsedTables.contains(tableKey))
    {
      return;
    }
    System.out.println("Handle table: "+id);
    PropertiesSet tableProps=_facade.loadProperties(id+0x9000000);
    Object[] listsArray=(Object[])tableProps.getProperty("ItemAdvancement_ProgressionListArray");
    for(Object listObj : listsArray)
    {
      PropertiesSet listProps=(PropertiesSet)listObj;
      int effectsListId=((Integer)listProps.getProperty("ItemAdvancement_ProgressionList")).intValue();
      handleList(effectsListId);
    }
    _parsedTables.add(tableKey);
  }

  private void handleList(int effectsListId)
  {
    System.out.println("Handle list: "+effectsListId);
    PropertiesSet effectsProps=_facade.loadProperties(effectsListId+0x9000000);
    Object[] effectsListArray=(Object[])effectsProps.getProperty("ItemAdvancement_Effect_Array");
    for(Object effectObj : effectsListArray)
    {
      PropertiesSet effectProps=(PropertiesSet)effectObj;
      Integer effectId=(Integer)effectProps.getProperty("ItemAdvancement_Effect");
      if (effectId!=null)
      {
        Effect effect=_parsedEffects.get(effectId);
        if (effect==null)
        {
          effect=DatEffectUtils.loadEffect(_facade,effectId.intValue());
          _parsedEffects.put(effectId,effect);
          System.out.println("\tLoaded effect: "+effect);
        }
      }
    }
  }

  /**
   * Save the loaded passives to a file.
   */
  public void savePassives()
  {
    List<Effect> effects=new ArrayList<Effect>(_parsedEffects.values());
    Collections.sort(effects,new IdentifiableComparator<Effect>());
    EffectXMLWriter.write(GeneratedFiles.PASSIVES,effects);
  }
}
