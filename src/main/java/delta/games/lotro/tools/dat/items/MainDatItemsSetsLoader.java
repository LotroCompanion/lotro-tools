package delta.games.lotro.tools.dat.items;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.legendary2.TraceriesManager;
import delta.games.lotro.lore.items.legendary2.Tracery;
import delta.games.lotro.lore.items.sets.ItemsSet;
import delta.games.lotro.lore.items.sets.ItemsSet.SetType;
import delta.games.lotro.lore.items.sets.SetBonus;
import delta.games.lotro.lore.items.sets.io.xml.ItemsSetXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.effects.EffectLoader;
import delta.games.lotro.tools.dat.effects.ItemsSetEffectsLoader;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.ProgressionUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Get items sets definitions from DAT files.
 * @author DAM
 */
public class MainDatItemsSetsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatItemsSetsLoader.class);

  private DataFacade _facade;
  private ItemsSetEffectsLoader _effectsLoader;
  private DatStatUtils _statUtils;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param effectsLoader Effects loader. 
   */
  public MainDatItemsSetsLoader(DataFacade facade, EffectLoader effectsLoader)
  {
    _facade=facade;
    _effectsLoader=new ItemsSetEffectsLoader(effectsLoader);
    _i18n=new I18nUtils("itemsSets",facade.getGlobalStringsManager());
    _statUtils=new DatStatUtils(facade,_i18n);
  }

  /*
Sample items set properties:
************* 1879087805 *****************
Set_ActiveCountDataList: 
  #1: 
    Mod_Array: 
      #1: 
        Mod_Modified: 268438584 (Vital_HealthCombatRegenAddMod)
        Mod_Op: 7 (Add)
        Mod_Progression: 1879212451
    Set_ActiveCount: 2
    ...
Set_Description: 
  #1: Crafted by dwarf-masters of finely-tooled leather and hardened steel from the depths of the great mines beneath Ered Luin.
Set_Icon: 1090519170
Set_Level: 30
Set_LevelRequired: 1
Set_MemberList: 
  #1: 1879087723
  #2: 1879087724
  #3: 1879087725
  #4: 1879087726
  #5: 1879087727
  #6: 1879087728
Set_Name: 
  #1: Armour of the White Flame
   */

  private ItemsSet load(int setId)
  {
    long dbPropertiesId=setId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties==null)
    {
      LOGGER.warn("Properties not found: "+setId);
      LOGGER.warn("Could not handle items set ID="+setId);
      return null;
    }
    // Name
    String name=_i18n.getNameStringProperty(properties,"Set_Name",setId,0);
    if (name==null)
    {
      name="";
    }
    boolean useIt=useIt(name);
    if (!useIt)
    {
      return null;
    }
    ItemsSet set=new ItemsSet();
    set.setIdentifier(setId);
    set.setName(name);
    // Class
    Integer classCode=(Integer)properties.getProperty("Set_TraitSet_Class");
    if ((classCode!=null) && (classCode.intValue()==213))
    {
      // PVP set
      return null;
    }
    // Level
    int level=((Integer)properties.getProperty("Set_Level")).intValue();
    set.setSetLevel(level);
    Integer useAverageItemLevel=(Integer)properties.getProperty("ItemGameSet_UseAverageItemLevelForSetLevel");
    if ((useAverageItemLevel!=null) && (useAverageItemLevel.intValue()==1))
    {
      set.setUseAverageItemLevelForSetLevel(true);
    }
    // Required level
    Integer requiredLevelInt=(Integer)properties.getProperty("Set_LevelRequired");
    int requiredLevel=(requiredLevelInt!=null)?requiredLevelInt.intValue():1;
    set.setRequiredMinLevel(requiredLevel);
    // Max character level
    Integer maxCharacterLevel=(Integer)properties.getProperty("Set_MaxPlayerLevel");
    if ((maxCharacterLevel!=null) && (maxCharacterLevel.intValue()>=1))
    {
      set.setRequiredMaxLevel(maxCharacterLevel);
    }
    // Description
    String description=_i18n.getStringProperty(properties,"Set_Description");
    set.setDescription(description);
    // Members
    Object[] membersArray=(Object[])properties.getProperty("Set_MemberList");
    if (membersArray!=null)
    {
      for(Object memberObj : membersArray)
      {
        int memberId=((Integer)memberObj).intValue();
        Item member=ItemsManager.getInstance().getItem(memberId);
        if (member!=null)
        {
          set.addMember(member);
        }
        else
        {
          LOGGER.warn("Member not found: "+memberId+" in set "+name);
        }
      }
    }
    // Bonus
    Object[] bonusesArray=(Object[])properties.getProperty("Set_ActiveCountDataList");
    if (bonusesArray!=null)
    {
      for(Object bonusObj : bonusesArray)
      {
        SetBonus bonus=loadBonus((PropertiesSet)bonusObj);
        set.addBonus(bonus);
      }
    }
    // Effects
    _effectsLoader.handleSetEffects(set,properties);
    return set;
  }

  private boolean useIt(String name)
  {
    if (name.contains("TBD")) return false;
    return true;
  }

  private SetBonus loadBonus(PropertiesSet properties)
  {
/*
    Mod_Array: 
      #1: 
        Mod_Modified: 268438584 (Vital_HealthCombatRegenAddMod)
        Mod_Op: 7 (Add)
        Mod_Progression: 1879212451
    Set_ActiveCount: 2
 */
    int count=((Integer)properties.getProperty("Set_ActiveCount")).intValue();
    if (count==0)
    {
      return null;
    }
    StatsProvider provider=_statUtils.buildStatProviders(properties);
    if (provider==null)
    {
      return null;
    }
    SetBonus bonus=new SetBonus(count);
    bonus.setStatsProvider(provider);
    Object[] effectsArray=(Object[])properties.getProperty("Set_EffectDataList");
    if (effectsArray!=null)
    {
      for(Object effectObj : effectsArray)
      {
        PropertiesSet effectProps=(PropertiesSet)effectObj;
        // EffectGenerator_EffectSpellcraft
        int effectId=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
        Effect effect=DatEffectUtils.loadEffect(_statUtils,effectId);
        StatsProvider effectStats=effect.getStatsProvider();
        int nbEffectStats=effectStats.getNumberOfStatProviders();
        for(int i=0;i<nbEffectStats;i++)
        {
          provider.addStatProvider(effectStats.getStatProvider(i));
        }
      }
    }
    return bonus;
  }

  private void handleTraceriesSets(List<ItemsSet> sets)
  {
    TraceriesManager traceriesMgr=TraceriesManager.getInstance();
    List<Tracery> traceries=traceriesMgr.getAll();
    Collections.sort(traceries,new IdentifiableComparator<Tracery>());
    for(Tracery tracery : traceries)
    {
      int setId=tracery.getSetId();
      if (setId!=0)
      {
        ItemsSet set=findSet(sets,setId);
        if (set!=null)
        {
          set.addMember(tracery.getItem());
          set.setSetType(SetType.TRACERIES);
        }
      }
    }
  }

  private ItemsSet findSet(List<ItemsSet> sets, int setId)
  {
    for(ItemsSet set : sets)
    {
      if (set.getIdentifier()==setId)
      {
        return set;
      }
    }
    return null;
  }

  /**
   * Load item sets.
   */
  public void doIt()
  {
    List<ItemsSet> sets=new ArrayList<ItemsSet>();

    PropertiesSet props=_facade.loadProperties(0x79009869); // GameSetDirectory
    Object[] setIdArray=(Object[])props.getProperty("Set_Directory_GameSetList");
    for(Object setIdObj : setIdArray)
    {
      int id=((Integer)setIdObj).intValue();
      ItemsSet set=load(id);
      if (set!=null)
      {
        sets.add(set);
      }
    }
    // Add traceries members
    handleTraceriesSets(sets);
    // Save sets
    int nbSets=sets.size();
    LOGGER.info("Writing "+nbSets+" sets");
    File to=GeneratedFiles.SETS;
    boolean ok=ItemsSetXMLWriter.writeSetsFile(to,sets);
    if (ok)
    {
      LOGGER.info("Wrote sets file: "+to);
    }
    // Save progressions
    ProgressionUtils.PROGRESSIONS_MGR.writeToFile(GeneratedFiles.PROGRESSIONS_ITEMS_SETS);
    // Save labels
    _i18n.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    EffectLoader effectsLoader=new EffectLoader(facade);
    new MainDatItemsSetsLoader(facade,effectsLoader).doIt();
    facade.dispose();
  }
}
