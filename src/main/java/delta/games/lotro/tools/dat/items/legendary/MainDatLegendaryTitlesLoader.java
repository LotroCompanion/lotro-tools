package delta.games.lotro.tools.dat.items.legendary;

import org.apache.log4j.Logger;

import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get legendary titles definitions from DAT files.
 * @author DAM
 */
public class MainDatLegendaryTitlesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatLegendaryTitlesLoader.class);

  private DataFacade _facade;
  private EnumMapper _category;
  private EnumMapper _tier;
  private EnumMapper _damageType;
  private EnumMapper _genus;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatLegendaryTitlesLoader(DataFacade facade)
  {
    _facade=facade;
    _category=_facade.getEnumsManager().getEnumMapper(587203267);
    _tier=_facade.getEnumsManager().getEnumMapper(587203238);
    _damageType=_facade.getEnumsManager().getEnumMapper(587202600);
    _genus=_facade.getEnumsManager().getEnumMapper(587202570);
  }

  /*
Sample properties:
******** Properties: 1879232157
EffectGenerator_RunicEffectList: 
  #1: 
    EffectGenerator_EffectID: 1879227042
    EffectGenerator_EffectSpellcraft: 95.0
ItemAdvancement_IATitle_Category: 90
ItemAdvancement_IATitle_Tier: 2
ItemAdvancement_Title_DamageType: 8
ItemAdvancement_Title_SlayerGenusType: 0
Name: 
  #1: Will of Eldar Days II

******** Properties: 1879227042
Effect_ApplicationProbabilityVariance: 0.0
Effect_Applied_Description: 
Effect_ClassPriority: 1
Effect_ConstantApplicationProbability: 1.0
Effect_Debuff: 0
Effect_Definition_Description: 
Effect_Duration_Permanent: 1
Effect_EquivalenceClass: 0
Effect_Harmful: 0
Effect_Icon: 1090519170
Effect_Name: 
  #1: ModificationEffect
Effect_RemoveOnAwaken: 0
Effect_RemoveOnDefeat: 0
Effect_SentToClient: 1
Effect_UIVisible: 0
Mod_Array: 
  #1: 
    Mod_Modified: 268435981
    Mod_Op: 7
    Mod_Progression: 1879211576
  */

  private void load(int indexDataId)
  {
    int dbPropertiesId=indexDataId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      System.out.println("************* "+indexDataId+" *****************");
      //System.out.println(properties.dump());

      // Name
      String name=DatUtils.getStringProperty(properties,"Name");
      System.out.println("Name: "+name);
      int category=((Integer)properties.getProperty("ItemAdvancement_IATitle_Category")).intValue();
      String categoryName=_category.getString(category);
      System.out.println("\tCategory: "+categoryName);
      int tier=((Integer)properties.getProperty("ItemAdvancement_IATitle_Tier")).intValue();
      String tierName=_tier.getString(tier);
      System.out.println("\tTier: "+tierName);
      int damageType=((Integer)properties.getProperty("ItemAdvancement_Title_DamageType")).intValue();
      String damageTypeName=_damageType.getString(damageType);
      System.out.println("\tDamage type: "+damageTypeName);
      Integer slayerGenusType=(Integer)properties.getProperty("ItemAdvancement_Title_SlayerGenusType");
      if ((slayerGenusType!=null) && (slayerGenusType.intValue()!=0))
      {
        int genusType=slayerGenusType.intValue();
        int indexGenus=getPower(genusType);
        String genus=_genus.getString(indexGenus+1);
        System.out.println("\tSlayer: "+genus);
      }

      Object[] effects=(Object[])properties.getProperty("EffectGenerator_RunicEffectList");
      if (effects!=null)
      {
        for(Object effectObj : effects)
        {
          PropertiesSet effectProps=(PropertiesSet)effectObj;
          int effectId=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
          Float spellCraft=(Float)effectProps.getProperty("EffectGenerator_EffectSpellcraft");
          System.out.println("Spellcraft: "+spellCraft);
          StatsProvider statsProvider=DatEffectUtils.loadEffect(_facade,effectId);
          int level=spellCraft.intValue();
          System.out.println("Stats: "+statsProvider.getStats(1,level));
        }
      }
    }
    else
    {
      LOGGER.warn("Could not handle legendary title ID="+indexDataId);
    }
  }

  private static int getPower(int value)
  {
    int test=1;
    for(int n=0;n<15;n++)
    {
      if (value==test) return n;
      test*=2;
    }
    return -1;
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      //int did=BufferUtils.getDoubleWordAt(data,0);
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      //System.out.println(classDefIndex);
      return (classDefIndex==2338); // Add constant: WStateClass.LEGENDARY_TITLE
    }
    return false;
  }

  private void doIt()
  {
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        load(id);
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatLegendaryTitlesLoader(facade).doIt();
    facade.dispose();
  }
}
