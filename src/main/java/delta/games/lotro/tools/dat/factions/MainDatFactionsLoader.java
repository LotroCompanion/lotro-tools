package delta.games.lotro.tools.dat.factions;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.Dump;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get faction definitions from DAT files.
 * @author DAM
 */
public class MainDatFactionsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatFactionsLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatFactionsLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /*
Sample properties:
************* 1879091341 *****************
Reputation_Faction_AdvancementTable: 1879090256
Reputation_Faction_CurrentTier_PropertyName: 268441507
Reputation_Faction_DefaultTier: 3
Reputation_Faction_Description: 
  #1: A Council of several races who struggle to take the war into the heart of Angmar itself. They strike from the hidden refuge of Gath Forthnír in the far northern wastes of Angmar.
Reputation_Faction_EarnedReputation_PropertyName: 268441501
Reputation_Faction_GlobalCap_PropertyName: 268441759
Reputation_Faction_Name: 
  #1: Council of the North
Reputation_Faction_TierNameProgression: 1879209346
Reputation_HighestTier: 7
Reputation_LowestTier: 1
  */

  //private Set<String> propNames=new HashSet<String>();
  private void load(int indexDataId)
  {
    int dbPropertiesId=indexDataId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      //System.out.println("************* "+indexDataId+" *****************");
      //System.out.println(properties.dump());
      //propNames.addAll(properties.getPropertyNames());
    }
    else
    {
      LOGGER.warn("Could not handle faction ID="+indexDataId);
    }
    // Name
    String name=DatUtils.getStringProperty(properties,"Reputation_Faction_Name");
    System.out.println("Name: "+name);
    // Description
    String description=DatUtils.getStringProperty(properties,"Reputation_Faction_Description");
    System.out.println("Description: "+description);
    // Reputation_Faction_AdvancementTable gives nothing
    // Tier names:
    int advTableId=((Integer)properties.getProperty("Reputation_Faction_TierNameProgression")).intValue();
    PropertiesSet advancementTable=_facade.loadProperties(advTableId+0x9000000);
    System.out.println(advTableId+" => "+advancementTable.dump());

    int lowestTier=((Integer)properties.getProperty("Reputation_LowestTier")).intValue();
    int highestTier=((Integer)properties.getProperty("Reputation_HighestTier")).intValue();
    int defaultTier=((Integer)properties.getProperty("Reputation_Faction_DefaultTier")).intValue();
    System.out.println("Tiers (lowest/default/highest): "+lowestTier+" / "+defaultTier+" / "+highestTier);

    // Property names
    PropertiesRegistry propsRegistry=_facade.getPropertiesRegistry();
    int currentTierPropId=((Integer)properties.getProperty("Reputation_Faction_CurrentTier_PropertyName")).intValue();
    System.out.println("Current tier property: "+propsRegistry.getPropertyDef(currentTierPropId));
    int earnedRepPropId=((Integer)properties.getProperty("Reputation_Faction_EarnedReputation_PropertyName")).intValue();
    System.out.println("Earned rep property: "+propsRegistry.getPropertyDef(earnedRepPropId));
    int globalCapPropId=((Integer)properties.getProperty("Reputation_Faction_GlobalCap_PropertyName")).intValue();
    System.out.println("Global cap property: "+propsRegistry.getPropertyDef(globalCapPropId));

    Object disableAcc=properties.getProperty("Reputation_Faction_DisableAcceleration");
    if (disableAcc!=null)
    {
      // Only 1 for Dol Amroth library (null otherwise)
      System.out.println("Disable acc: "+disableAcc);
    }
    Integer lowestMonetizedTier=(Integer)properties.getProperty("Reputation_Faction_LowestMonetizedTier");
    if (lowestMonetizedTier!=null)
    {
      // 4 for guilds, nothing for other factions
      System.out.println("Lowest monetized tier: "+lowestMonetizedTier);
    }
    Integer webStoreDataId=(Integer)properties.getProperty("WebStoreAccountItem_DataID");
    if (webStoreDataId!=null)
    {
      // Only for guilds
      System.out.println("Web store ID: "+webStoreDataId);
    }
  }

  private void doIt()
  {
    PropertiesSet indexProperties=_facade.loadProperties(0x7900A452);
    Object[] idsArray=(Object[])indexProperties.getProperty("Reputation_FactionTable");
    System.out.println(indexProperties.dump());
    for(Object idObj : idsArray)
    {
      int id=((Integer)idObj).intValue();
      load(id);
    }
    System.out.println("Reputation_GroupModifierTable:");
    int tableId=((Integer)indexProperties.getProperty("Reputation_GroupModifierTable")).intValue();
    //System.out.println(_facade.loadProperties(tableId+0x9000000).dump());
    System.out.println(Dump.dumpString(_facade.loadData(tableId)));

    // Faction advancement table (same value for all factions)
    // Here we find the amount of reputation points for each tier
    System.out.println("Reputation_Faction_AdvancementTable:");
    System.out.println(Dump.dumpString(_facade.loadData(1879090256)));
    /*
0x00000000  50 A4 00 70 51 03 00 00 02 00 00 00 01 80 00 00  P..pQ...........
0x00000010  00 02 00 00 00 00 01 00 00 00 01 00 51 03 01 00  ............Q...
0x00000020  EC 18 F5 0F 01 00 00 00 00 51 03 01 00 00 00 00  .........Q......
0x00000030  00 00 00 68 00 0B 00 00 00 00 00 00 00 00 00 00  ...h............
0x00000040  00 00 00 00 00 00 00 00 00 >10 27< 00 00 00 00 00  ..........'.....     >10000<
0x00000050  00 >20 4E< 00 00 00 00 00 00 >30 75< 00 00 00 00 00  . N......0u.....   >20000<, >30000<
0x00000060  00 >50 C3< 00 00 00 00 00 00 >F8 24 01< 00 00 00 00  .P........$.....   >50000<, >75000<
0x00000070  00 >28 9A 01< 00 00 00 00 00 >F0 49 02< 00 00 00 00  .(........I.....   >105000<, >150000<
0x00000080  00 >50 34 03< 00 00 00 00 00 >E0 93 04< 00 00 00 00  .P4.............   >210000<, >300000<
0x00000090  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  ................
0x000000a0  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  ................
0x000000b0  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  ................
0x000000c0  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  ................
0x000000d0  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  ................
0x000000e0  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  ................
0x000000f0  00 00 00 00 00 00 00 00                          ........
     */

    /*
    List<String> sortedPropNames=new ArrayList<String>(propNames);
    Collections.sort(sortedPropNames);
    for(String propName : sortedPropNames)
    {
      System.out.println(propName);
    }
    */
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatFactionsLoader(facade).doIt();
    facade.dispose();
  }
}
