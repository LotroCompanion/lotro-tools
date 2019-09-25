package delta.games.lotro.tools.dat.items;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.sets.ItemsSet;
import delta.games.lotro.lore.items.sets.ItemsSetBonus;
import delta.games.lotro.lore.items.sets.io.xml.ItemsSetXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
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
    ItemsSet set=null;
    int dbPropertiesId=indexDataId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      set=new ItemsSet();
      set.setIdentifier(indexDataId);
      System.out.println("************* "+indexDataId+" *****************");
      System.out.println(properties.dump());
      // Name
      String name=DatUtils.getStringProperty(properties,"Set_Name");
      set.setName(name);
      // Level
      int level=((Integer)properties.getProperty("Set_Level")).intValue();
      set.setLevel(level);
      // Required level
      Integer requiredLevelInt=(Integer)properties.getProperty("Set_LevelRequired");
      int requiredLevel=(requiredLevelInt!=null)?requiredLevelInt.intValue():1;
      set.setRequiredLevel(requiredLevel);
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
        // Bonus
        Object[] bonusesArray=(Object[])properties.getProperty("Set_ActiveCountDataList");
        if (bonusesArray!=null)
        {
          for(Object bonusObj : bonusesArray)
          {
            ItemsSetBonus bonus=loadBonus((PropertiesSet)bonusObj);
            set.addBonus(bonus);
          }
        }
      }
    }
    else
    {
      LOGGER.warn("Could not handle items set ID="+indexDataId);
    }
    return set;
  }

  private ItemsSetBonus loadBonus(PropertiesSet properties)
  {
/*
    Mod_Array: 
      #1: 
        Mod_Modified: 268438584 (Vital_HealthCombatRegenAddMod)
        Mod_Op: 7 (Add)
        Mod_Progression: 1879212451
    Set_ActiveCount: 2
 */
    ItemsSetBonus bonus=null;
    int count=((Integer)properties.getProperty("Set_ActiveCount")).intValue();
    StatsProvider provider=DatStatUtils.buildStatProviders(_facade,properties);
    if ((count>0) && (provider!=null))
    {
      bonus=new ItemsSetBonus(count);
      bonus.setStatsProvider(provider);
    }
    return bonus;
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      //int did=BufferUtils.getDoubleWordAt(data,0);
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      return (classDefIndex==WStateClass.SET);
    }
    return false;
  }

  private void doIt()
  {
    List<ItemsSet> sets=new ArrayList<ItemsSet>();

    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        ItemsSet set=load(id);
        if (set!=null)
        {
          sets.add(set);
        }
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
