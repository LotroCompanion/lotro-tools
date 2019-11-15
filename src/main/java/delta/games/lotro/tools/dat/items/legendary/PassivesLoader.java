package delta.games.lotro.tools.dat.items.legendary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.io.xml.EffectXMLWriter;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.legendary.passives.PassivesGroup;
import delta.games.lotro.lore.items.legendary.passives.io.xml.PassivesGroupsXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;

/**
 * Loader for legendary passives.
 * @author DAM
 */
public class PassivesLoader
{
  private DataFacade _facade;
  private Map<Integer,PassivesGroup> _loadedGroups;
  private Map<Integer,Effect> _parsedEffects;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public PassivesLoader(DataFacade facade)
  {
    _facade=facade;
    _loadedGroups=new HashMap<Integer,PassivesGroup>();
    _parsedEffects=new HashMap<Integer,Effect>();
  }

  /**
   * Handle a passives table.
   * @param itemId Identifier of the source item.
   * @param staticEffectGroupId Table identifier.
   */
  public void handleTable(int itemId, int staticEffectGroupId)
  {
    Integer tableKey=Integer.valueOf(staticEffectGroupId);
    PassivesGroup group=_loadedGroups.get(tableKey);
    if (group==null)
    {
      group=new PassivesGroup();
      _loadedGroups.put(Integer.valueOf(staticEffectGroupId),group);

      //System.out.println("Handle table: "+id);
      PropertiesSet tableProps=_facade.loadProperties(staticEffectGroupId+DATConstants.DBPROPERTIES_OFFSET);
      Object[] listsArray=(Object[])tableProps.getProperty("ItemAdvancement_ProgressionListArray");
      for(Object listObj : listsArray)
      {
        PropertiesSet listProps=(PropertiesSet)listObj;
        int effectsListId=((Integer)listProps.getProperty("ItemAdvancement_ProgressionList")).intValue();
        handleList(staticEffectGroupId, effectsListId);
      }
    }
    group.addItem(itemId);
  }

  private void handleList(int staticEffectGroupId, int effectsListId)
  {
    //System.out.println("Handle list: "+effectsListId);
    PropertiesSet effectsProps=_facade.loadProperties(effectsListId+DATConstants.DBPROPERTIES_OFFSET);
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
          // Remove name: it is not interesting for passives
          effect.setName(null);
          // Remove icon: it is not interesting for passives
          effect.setIconId(null);
          _parsedEffects.put(effectId,effect);
          //System.out.println("\tLoaded effect: "+effect);
        }
        PassivesGroup group=_loadedGroups.get(Integer.valueOf(staticEffectGroupId));
        group.addPassive(effectId.intValue());
      }
    }
  }

  /**
   * Save the loaded passives to a file.
   */
  public void savePassives()
  {
    // Passives
    List<Effect> effects=new ArrayList<Effect>(_parsedEffects.values());
    Collections.sort(effects,new IdentifiableComparator<Effect>());
    EffectXMLWriter.write(GeneratedFiles.PASSIVES,effects);
    // Passives usage
    List<Integer> groupIds=new ArrayList<Integer>(_loadedGroups.keySet());
    Collections.sort(groupIds);
    List<PassivesGroup> groups=new ArrayList<PassivesGroup>();
    for(Integer groupId : groupIds)
    {
      groups.add(_loadedGroups.get(groupId));
    }
    PassivesGroupsXMLWriter.write(GeneratedFiles.PASSIVES_USAGE,groups);
  }
}
