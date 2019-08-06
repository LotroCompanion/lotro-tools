package delta.games.lotro.tools.dat.items.legendary;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.lore.items.legendary.titles.LegendaryTitle;
import delta.games.lotro.lore.items.legendary.titles.io.xml.LegendaryTitleXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
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
  private EnumMapper _genus;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatLegendaryTitlesLoader(DataFacade facade)
  {
    _facade=facade;
    _category=_facade.getEnumsManager().getEnumMapper(587203267);
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

  private LegendaryTitle load(int indexDataId)
  {
    LegendaryTitle ret=null;
    int dbPropertiesId=indexDataId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      //System.out.println("************* "+indexDataId+" *****************");
      //System.out.println(properties.dump());
      ret=new LegendaryTitle();
      // ID
      ret.setIdentifier(indexDataId);
      // Name
      String name=DatUtils.getStringProperty(properties,"Name");
      ret.setName(name);
      // Category
      int category=((Integer)properties.getProperty("ItemAdvancement_IATitle_Category")).intValue();
      String categoryName=_category.getString(category);
      ret.setCategory(categoryName);
      // Tier
      int tier=((Integer)properties.getProperty("ItemAdvancement_IATitle_Tier")).intValue();
      ret.setTier(tier);
      // Damage type
      int damageTypeCode=((Integer)properties.getProperty("ItemAdvancement_Title_DamageType")).intValue();
      DamageType damageType=DatEnumsUtils.getDamageType(damageTypeCode);
      ret.setDamageType(damageType);
      // Slayer genus type
      Integer slayerGenusType=(Integer)properties.getProperty("ItemAdvancement_Title_SlayerGenusType");
      if ((slayerGenusType!=null) && (slayerGenusType.intValue()!=0))
      {
        BitSet flags=BitSetUtils.getBitSetFromFlags(slayerGenusType.intValue());
        String genus=BitSetUtils.getStringFromBitSet(flags,_genus,",");
        ret.setSlayerGenusType(genus);
      }
      // Stats
      // - Private stats
      {
        StatsProvider statsProvider=DatStatUtils.buildStatProviders(_facade,properties);
        if (statsProvider.getNumberOfStatProviders()>0)
        {
          // Level does not matter because it's only constant stats
          BasicStatsSet stats=statsProvider.getStats(1,100);
          ret.getStats().addStats(stats);
        }
      }
      // - Effects
      Object[] effects=(Object[])properties.getProperty("EffectGenerator_RunicEffectList");
      if (effects!=null)
      {
        for(Object effectObj : effects)
        {
          PropertiesSet effectProps=(PropertiesSet)effectObj;
          int effectId=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
          Float spellCraft=(Float)effectProps.getProperty("EffectGenerator_EffectSpellcraft");
          StatsProvider statsProvider=DatEffectUtils.loadEffectStats(_facade,effectId);
          int level=spellCraft.intValue();
          BasicStatsSet stats=statsProvider.getStats(1,level);
          ret.getStats().addStats(stats);
        }
      }
    }
    else
    {
      LOGGER.warn("Could not handle legendary title ID="+indexDataId);
    }
    return ret;
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      //int did=BufferUtils.getDoubleWordAt(data,0);
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      //System.out.println(classDefIndex);
      return (classDefIndex==WStateClass.LEGENDARY_TITLE);
    }
    return false;
  }

  private void doIt()
  {
    DatStatUtils.doFilterStats=false;
    List<LegendaryTitle> titles=new ArrayList<LegendaryTitle>();
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        LegendaryTitle title=load(id);
        if (title!=null)
        {
          titles.add(title);
        }
      }
    }
    // Save titles
    int nbTitles=titles.size();
    System.out.println("Writing "+nbTitles+" legendary titles");
    boolean ok=LegendaryTitleXMLWriter.writeLegendaryTitlesFile(GeneratedFiles.LEGENDARY_TITLES,titles);
    if (ok)
    {
      System.out.println("Wrote titles file: "+GeneratedFiles.LEGENDARY_TITLES);
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
