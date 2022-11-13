package delta.games.lotro.tools.dat.misc;

import java.io.File;

import org.apache.log4j.Logger;

import delta.games.lotro.common.enums.AgentClass;
import delta.games.lotro.common.enums.Alignment;
import delta.games.lotro.common.enums.BillingGroup;
import delta.games.lotro.common.enums.ClassificationFilter;
import delta.games.lotro.common.enums.CollectionCategory;
import delta.games.lotro.common.enums.Difficulty;
import delta.games.lotro.common.enums.EquipmentCategory;
import delta.games.lotro.common.enums.Genus;
import delta.games.lotro.common.enums.GroupSize;
import delta.games.lotro.common.enums.ItemClass;
import delta.games.lotro.common.enums.ItemClassUtils;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumEntry;
import delta.games.lotro.common.enums.MobType;
import delta.games.lotro.common.enums.SkillCategory;
import delta.games.lotro.common.enums.SocketType;
import delta.games.lotro.common.enums.Species;
import delta.games.lotro.common.enums.SubSpecies;
import delta.games.lotro.common.enums.TraitNature;
import delta.games.lotro.common.enums.TravelLink;
import delta.games.lotro.common.enums.io.xml.EnumXMLWriter;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.utils.StringUtils;

/**
 * Get enums from DAT files.
 * @author DAM
 */
public class MainDatEnumsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatEnumsLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatEnumsLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load enums.
   */
  public void doIt()
  {
    loadEnum(587203292,"SkirmishDifficultyTier",Difficulty.class);
    loadEnum(587203290,"SkirmishGroupSize",GroupSize.class);
    loadEnum(587202570,"GenusType",Genus.class);
    loadEnum(587202571,"Agent_Species",Species.class);
    loadEnum(587202572,"SubspeciesType",SubSpecies.class);
    loadEnum(587202573,"AlignmentType",Alignment.class);
    loadEnum(587202574,"CharacterClassType",AgentClass.class);
    loadEnum(587202575,"ClassificationFilterType",ClassificationFilter.class);
    loadEnum(587202672,"ExaminationModStatType",MobType.class);
    loadEnum(587202586,"SkillCharacteristicCategory",SkillCategory.class); // 0x2300001A
    loadEnum(587202647,"TraitNature",TraitNature.class);
    loadEnum(587203634,"ItemSocketType",SocketType.class);
    loadEnum(587202614,"ItemClass",ItemClass.class); // 0x23000036
    loadEnum(587202568,"TravelLink",TravelLink.class); // 0x23000008
    loadEnum(587202756,"BillingGroup",BillingGroup.class); // 0x230000C4
    loadEnum(587203550,"CollectionCategory",CollectionCategory.class); // 0x230003DE
    loadEnum(587202636,"EquipmentCategory",EquipmentCategory.class); // 0x2300004C
  }

  private <T extends LotroEnumEntry> void loadEnum(int enumId, String name, Class<T> implClass)
  {
    LotroEnum<T> lotroEnum=new LotroEnum<T>(enumId,name,implClass);
    EnumMapper enumMapper=_facade.getEnumsManager().getEnumMapper(enumId);
    if (enumMapper!=null)
    {
      for(Integer code : enumMapper.getTokens())
      {
        if ((code.intValue()==0) && (!useZero(enumId)))
        {
          continue;
        }
        String label=enumMapper.getLabel(code.intValue());
        label=StringUtils.fixName(label);
        String key=getKey(implClass,code.intValue());
        T entry=lotroEnum.buildEntryInstance(code.intValue(),key,label);
        lotroEnum.registerEntry(entry);
      }
      handleAdditionalEntries(enumId,lotroEnum,implClass);
      File enumsDir=GeneratedFiles.ENUMS_DIR;
      String fileName=implClass.getSimpleName()+".xml";
      File enumFile=new File(enumsDir,fileName);
      new EnumXMLWriter<T>().writeEnum(enumFile,lotroEnum);
    }
    else
    {
      LOGGER.warn("Could not load enum");
    }
  }

  private boolean useZero(int enumId)
  {
    return (enumId==0x23000036);
  }

  private <T extends LotroEnumEntry> void handleAdditionalEntries(int enumId, LotroEnum<T> lotroEnum, Class<T> implClass)
  {
    if (enumId==0x23000036) // ItemClass
    {
      // Box of Essences
      int code=ItemClassUtils.getBoxOfEssenceCode();
      T entry=lotroEnum.buildEntryInstance(code,null,"Box of Essences");
      lotroEnum.registerEntry(entry);
      for(int tier=1;tier<=14;tier++)
      {
        // Essences
        entry=lotroEnum.buildEntryInstance(ItemClassUtils.getEssenceCode(tier),null,"Essence:Tier"+tier);
        lotroEnum.registerEntry(entry);
        // Enhancement runes
        entry=lotroEnum.buildEntryInstance(ItemClassUtils.getEnhancementRuneCode(tier),null,"Enhancement Rune:Tier"+tier);
        lotroEnum.registerEntry(entry);
        // Heraldric Traceries
        entry=lotroEnum.buildEntryInstance(ItemClassUtils.getHeraldicTraceryCode(tier),null,"Heraldric Tracery:Tier"+tier);
        lotroEnum.registerEntry(entry);
        // Words of Power
        entry=lotroEnum.buildEntryInstance(ItemClassUtils.getWordOfPowerCode(tier),null,"Word of Power:Tier"+tier);
        lotroEnum.registerEntry(entry);
        // Words of Mastery
        entry=lotroEnum.buildEntryInstance(ItemClassUtils.getWordOfMasteryCode(tier),null,"Word of Mastery:Tier"+tier);
        lotroEnum.registerEntry(entry);
        // Words of Craft
        entry=lotroEnum.buildEntryInstance(ItemClassUtils.getWordOfCraftCode(tier),null,"Word of Craft:Tier"+tier);
        lotroEnum.registerEntry(entry);
        // Essences of War
        entry=lotroEnum.buildEntryInstance(ItemClassUtils.getEssenceOfWarCode(tier),null,"Essence of War:Tier"+tier);
        lotroEnum.registerEntry(entry);
      }
    }
  }

  private String getKey(Class<? extends LotroEnumEntry> implClass, int code)
  {
    if (implClass==GroupSize.class)
    {
      return getGroupSizeKey(code);
    }
    return null;
  }

  private String getGroupSizeKey(int code)
  {
    if (code==1) return "SOLO";
    if (code==2) return "DUO";
    if (code==3) return "SMALL_FELLOWSHIP";
    if (code==6) return "FELLOWSHIP";
    if (code==12) return "RAID12";
    if (code==24) return "RAID24";
    return "";
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatEnumsLoader(facade).doIt();
    facade.dispose();
  }
}
