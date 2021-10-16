package delta.games.lotro.tools.dat.items;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.sets.ItemsSet;
import delta.games.lotro.lore.items.sets.SetBonus;
import delta.games.lotro.lore.items.sets.io.xml.ItemsSetXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.utils.Proxy;

/**
 * Get items sets definitions from DAT files.
 * @author DAM
 */
public class MainDatItemsSetsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatItemsSetsLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatItemsSetsLoader(DataFacade facade)
  {
    _facade=facade;
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

  private ItemsSet load(int indexDataId)
  {
    int dbPropertiesId=indexDataId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties==null)
    {
      LOGGER.warn("Properties not found: "+indexDataId);
      LOGGER.warn("Could not handle items set ID="+indexDataId);
      return null;
    }
    //System.out.println("************* "+indexDataId+" *****************");
    //System.out.println(properties.dump());
    // Name
    String name=DatUtils.getStringProperty(properties,"Set_Name");
    boolean useIt=useIt(name);
    if (!useIt)
    {
      return null;
    }
    ItemsSet set=new ItemsSet();
    set.setIdentifier(indexDataId);
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
    String description=DatUtils.getStringProperty(properties,"Set_Description");
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
          Proxy<Item> proxy=new Proxy<Item>();
          proxy.setId(memberId);
          proxy.setName(member.getName());
          proxy.setObject(member);
          set.addMember(proxy);
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
    SetBonus bonus=null;
    int count=((Integer)properties.getProperty("Set_ActiveCount")).intValue();
    StatsProvider provider=DatStatUtils.buildStatProviders(_facade,properties);
    if ((count>0) && (provider!=null))
    {
      bonus=new SetBonus(count);
      bonus.setStatsProvider(provider);
    }
    Object[] effectsArray=(Object[])properties.getProperty("Set_EffectDataList");
    if (effectsArray!=null)
    {
      for(Object effectObj : effectsArray)
      {
        PropertiesSet effectProps=(PropertiesSet)effectObj;
        // EffectGenerator_EffectSpellcraft
        int effectId=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
        Effect effect=DatEffectUtils.loadEffect(_facade,effectId);
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

  /**
   * Load item sets.
   */
  public void doIt()
  {
    DatStatUtils.doFilterStats=false;
    DatStatUtils._statsUsageStatistics.reset();
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
    // Save sets
    int nbSets=sets.size();
    LOGGER.info("Writing "+nbSets+" sets");
    File to=GeneratedFiles.SETS;
    boolean ok=ItemsSetXMLWriter.writeSetsFile(to,sets);
    if (ok)
    {
      System.out.println("Wrote sets file: "+to);
    }
    // Save progressions
    DatStatUtils._progressions.writeToFile(GeneratedFiles.PROGRESSIONS_ITEMS_SETS);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatItemsSetsLoader(facade).doIt();
    facade.dispose();
  }
}
