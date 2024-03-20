package delta.games.lotro.tools.dat.items.legendary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.legendary.passives.Passive;
import delta.games.lotro.lore.items.legendary.passives.PassivesGroup;
import delta.games.lotro.lore.items.legendary.passives.io.xml.PassivesGroupsXMLWriter;
import delta.games.lotro.lore.items.legendary.passives.io.xml.PassivesXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Loader for legendary passives.
 * @author DAM
 */
public class PassivesLoader
{
  private DataFacade _facade;
  private I18nUtils _i18n;
  private DatStatUtils _statUtils;
  private Map<Integer,PassivesGroup> _loadedGroups;
  private Map<Integer,Passive> _parsedPassives;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public PassivesLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("passives",facade.getGlobalStringsManager());
    _statUtils=new DatStatUtils(facade,_i18n);
    _loadedGroups=new HashMap<Integer,PassivesGroup>();
    _parsedPassives=new HashMap<Integer,Passive>();
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
        Passive passive=_parsedPassives.get(effectId);
        if (passive==null)
        {
          passive=DatEffectUtils.loadPassive(_statUtils,effectId.intValue());
          _parsedPassives.put(effectId,passive);
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
    List<Passive> effects=new ArrayList<Passive>(_parsedPassives.values());
    Collections.sort(effects,new IdentifiableComparator<Passive>());
    PassivesXMLWriter.write(GeneratedFiles.PASSIVES,effects);
    // Passives usage
    List<Integer> groupIds=new ArrayList<Integer>(_loadedGroups.keySet());
    Collections.sort(groupIds);
    List<PassivesGroup> groups=new ArrayList<PassivesGroup>();
    for(Integer groupId : groupIds)
    {
      groups.add(_loadedGroups.get(groupId));
    }
    PassivesGroupsXMLWriter.write(GeneratedFiles.PASSIVES_USAGE,groups);
    // Stats
    _i18n.save();
  }
}
