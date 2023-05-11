package delta.games.lotro.tools.dat.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import delta.common.utils.i18n.MultilocalesTranslator;
import delta.games.lotro.common.enums.AgentClass;
import delta.games.lotro.common.enums.Alignment;
import delta.games.lotro.common.enums.AllegianceGroup;
import delta.games.lotro.common.enums.BillingGroup;
import delta.games.lotro.common.enums.ClassificationFilter;
import delta.games.lotro.common.enums.CollectionCategory;
import delta.games.lotro.common.enums.CraftTier;
import delta.games.lotro.common.enums.CraftingUICategory;
import delta.games.lotro.common.enums.DeedCategory;
import delta.games.lotro.common.enums.Difficulty;
import delta.games.lotro.common.enums.EquipmentCategory;
import delta.games.lotro.common.enums.Genus;
import delta.games.lotro.common.enums.GroupSize;
import delta.games.lotro.common.enums.ItemClass;
import delta.games.lotro.common.enums.ItemClassUtils;
import delta.games.lotro.common.enums.ItemUniquenessChannel;
import delta.games.lotro.common.enums.LegendaryTitleCategory;
import delta.games.lotro.common.enums.LegendaryTitleTier;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumEntry;
import delta.games.lotro.common.enums.MobType;
import delta.games.lotro.common.enums.MountType;
import delta.games.lotro.common.enums.PaperItemCategory;
import delta.games.lotro.common.enums.QuestCategory;
import delta.games.lotro.common.enums.SkillCategory;
import delta.games.lotro.common.enums.SkillCharacteristicSubCategory;
import delta.games.lotro.common.enums.SocketType;
import delta.games.lotro.common.enums.Species;
import delta.games.lotro.common.enums.SubSpecies;
import delta.games.lotro.common.enums.TraitNature;
import delta.games.lotro.common.enums.TraitTreeBranchType;
import delta.games.lotro.common.enums.TraitTreeType;
import delta.games.lotro.common.enums.TravelLink;
import delta.games.lotro.common.enums.WJEncounterCategory;
import delta.games.lotro.common.enums.WJEncounterType;
import delta.games.lotro.common.enums.WJInstanceGroup;
import delta.games.lotro.common.enums.io.xml.EnumXMLWriter;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DataFacadeBuilder;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Get enums from DAT files.
 * @author DAM
 */
public class MainDatEnumsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatEnumsLoader.class);

  private DataFacade _facade;
  private MultilocalesTranslator _translator;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatEnumsLoader(DataFacade facade)
  {
    _facade=facade;
    List<Locale> locales=new ArrayList<Locale>();
    locales.add(Locale.ENGLISH);
    locales.add(Locale.FRENCH);
    locales.add(Locale.GERMAN);
    _translator=new MultilocalesTranslator(getClass().getPackage().getName()+".enum",locales);
  }

  /**
   * Load enums.
   */
  public void doIt()
  {
    loadEnum(587203292,"SkirmishDifficultyTier",Difficulty.class); // 0x230002DC
    loadEnum(587203290,"SkirmishGroupSize",GroupSize.class); // 0x230002DA
    loadEnum(587202570,"GenusType",Genus.class); // 0x2300000A
    loadEnum(587202571,"Agent_Species",Species.class); // 0x2300000B
    loadEnum(587202572,"SubspeciesType",SubSpecies.class); // 0x2300000C
    loadEnum(587202573,"AlignmentType",Alignment.class); // 0x2300000D
    loadEnum(587202574,"CharacterClassType",AgentClass.class); // 0x2300000E
    loadEnum(587202575,"ClassificationFilterType",ClassificationFilter.class);  // 0x2300000F
    loadEnum(587202672,"ExaminationModStatType",MobType.class); // 0x23000070
    loadEnum(587202586,"SkillCharacteristicCategory",SkillCategory.class); // 0x2300001A
    loadEnum(587202647,"TraitNature",TraitNature.class); // 0x23000057
    loadEnum(587203634,"ItemSocketType",SocketType.class); // 0x23000432
    loadEnum(587202614,"ItemClass",ItemClass.class); // 0x23000036
    loadEnum(587202568,"TravelLink",TravelLink.class); // 0x23000008
    loadEnum(587202756,"BillingGroup",BillingGroup.class); // 0x230000C4
    loadEnum(587203550,"CollectionCategory",CollectionCategory.class); // 0x230003DE
    loadEnum(587202636,"EquipmentCategory",EquipmentCategory.class); // 0x2300004C
    loadEnum(587203638,"AllegianceGroup",AllegianceGroup.class); // 0x23000436
    loadEnum(587203337,"WJEncounterType",WJEncounterType.class); // 0x23000309
    loadEnum(587203408,"WJEncounterCategory",WJEncounterCategory.class); // 0x23000350
    loadEnum(587203537,"WJInstanceGroup",WJInstanceGroup.class); // 0x230003D1
    loadEnum(587203267,"IATitleCategory",LegendaryTitleCategory.class); // 0x230002C3
    loadEnum(587203238,"IATitleTier",LegendaryTitleTier.class); // 0x230002A6
    loadEnum(587203350,"PaperItemCategory",PaperItemCategory.class); // 0x23000316
    loadEnum(587203433,"PointBasedTraitTree",TraitTreeType.class); // 0x23000369
    loadEnum(587203489,"PointBasedTraitTreeBranch",TraitTreeBranchType.class); // 0x230003A1
    loadEnum(587203643,"ItemUniquenessChannel",ItemUniquenessChannel.class); // 0x2300043B
    loadEnum(587202585,"QuestCategory",QuestCategory.class); // 0x23000019
    loadEnum(587202588,"AccomplishmentUITab",DeedCategory.class); // 0x2300001C
    loadEnum(587202659,"CraftTier",CraftTier.class); // 0x23000063
    loadEnum(587203200,"MountType",MountType.class); // 0x23000280
    loadEnum(587203478,"SkillCharacteristicSubCategory",SkillCharacteristicSubCategory.class); // 0x23000396
    loadEnum(587202661,"CraftUICategory",CraftingUICategory.class); // 0x23000065
  }

  private <T extends LotroEnumEntry> void loadEnum(int enumId, String name, Class<T> implClass)
  {
    LotroEnum<T> lotroEnum=new LotroEnum<T>(enumId,name,implClass);
    EnumMapper enumMapper=_facade.getEnumsManager().getEnumMapper(enumId);
    if (enumMapper!=null)
    {
      String labelsSetName="enum-"+implClass.getSimpleName();
      I18nUtils i18n=new I18nUtils(labelsSetName,_facade.getGlobalStringsManager());
      for(Integer code : enumMapper.getTokens())
      {
        if ((code.intValue()==0) && (!useZero(enumId)))
        {
          continue;
        }
        String label=i18n.getEnumValue(enumMapper,code.intValue(),I18nUtils.OPTION_REMOVE_TRAILING_MARK);
        String key=getKey(implClass,code.intValue());
        T entry=lotroEnum.buildEntryInstance(code.intValue(),key,label);
        lotroEnum.registerEntry(entry);
      }
      handleAdditionalEntries(i18n,enumId,lotroEnum);
      File enumsDir=GeneratedFiles.ENUMS_DIR;
      String fileName=implClass.getSimpleName()+".xml";
      File enumFile=new File(enumsDir,fileName);
      new EnumXMLWriter<T>().writeEnum(enumFile,lotroEnum);
      i18n.save();
    }
    else
    {
      LOGGER.warn("Could not load enum: "+name);
    }
  }

  private boolean useZero(int enumId)
  {
    return (enumId==0x23000036);
  }

  private <T extends LotroEnumEntry> void handleAdditionalEntries(I18nUtils i18n, int enumId, LotroEnum<T> lotroEnum)
  {
    if (enumId==0x23000036) // ItemClass
    {
      // Box of Essences
      handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getBoxOfEssenceCode(),"BoxOfEssences",0);
      for(int tier=1;tier<=14;tier++)
      {
        // Essences
        handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getEssenceCode(tier),"Essence",tier);
        // Enhancement runes
        handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getEnhancementRuneCode(tier),"EnhancementRune",tier);
        // Heraldric Traceries
        handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getHeraldicTraceryCode(tier),"HeraldricTracery",tier);
        // Words of Power
        handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getWordOfPowerCode(tier),"WoPower",tier);
        // Words of Mastery
        handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getWordOfMasteryCode(tier),"WoMastery",tier);
        // Words of Craft
        handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getWordOfCraftCode(tier),"WoCraft",tier);
        // Essences of War
        handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getEssenceOfWarCode(tier),"EssenceWar",tier);
        // Cloak Essences
        handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getCloakEssenceCode(tier),"CloakEssence",tier);
        // Necklace Essences
        handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getNecklaceEssenceCode(tier),"NecklaceEssence",tier);
      }
    }
    else if (enumId==0x23000350) // WJEncounterCategory
    {
      T entry=lotroEnum.buildEntryInstance(0,null,"Other");
      lotroEnum.registerEntry(entry);
    }
    else if (enumId==0x23000063) // CraftTier
    {
      T entry=lotroEnum.buildEntryInstance(0,null,"Beginner");
      lotroEnum.registerEntry(entry);
    }
  }

  private <T extends LotroEnumEntry> void handleCustomEntry(LotroEnum<T> lotroEnum, I18nUtils i18n, int code, String baseKey, int tier)
  {
    // Define localized labels
    String key=baseKey+((tier>0)?("-"+tier):"");
    Object[] params=new Object[] {Integer.valueOf(tier)};
    for(Locale locale : _translator.getLocales())
    {
      String value=_translator.translate(baseKey,params,locale);
      i18n.defineLabel(locale.getLanguage(),key,value);
    }
    // Define entry
    T entry=lotroEnum.buildEntryInstance(code,null,key);
    lotroEnum.registerEntry(entry);
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
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    Locale.setDefault(Locale.ENGLISH);
    new MainDatEnumsLoader(facade).doIt();
    facade.dispose();
  }
}
