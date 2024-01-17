package delta.games.lotro.tools.dat.misc;

import java.io.File;
import java.util.Locale;

import org.apache.log4j.Logger;

import delta.common.utils.i18n.MultilocalesTranslator;
import delta.games.lotro.character.gear.GearSlot;
import delta.games.lotro.common.CharacterSex;
import delta.games.lotro.common.enums.AgentClass;
import delta.games.lotro.common.enums.Alignment;
import delta.games.lotro.common.enums.AllegianceGroup;
import delta.games.lotro.common.enums.BillingGroup;
import delta.games.lotro.common.enums.ClassificationFilter;
import delta.games.lotro.common.enums.CollectionCategory;
import delta.games.lotro.common.enums.CombatState;
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
import delta.games.lotro.common.enums.ResistCategory;
import delta.games.lotro.common.enums.RunicTier;
import delta.games.lotro.common.enums.SkillCategory;
import delta.games.lotro.common.enums.SkillCharacteristicSubCategory;
import delta.games.lotro.common.enums.SkillType;
import delta.games.lotro.common.enums.SocketType;
import delta.games.lotro.common.enums.Species;
import delta.games.lotro.common.enums.SubSpecies;
import delta.games.lotro.common.enums.TraitNature;
import delta.games.lotro.common.enums.TraitSubCategory;
import delta.games.lotro.common.enums.TraitTreeBranchType;
import delta.games.lotro.common.enums.TraitTreeType;
import delta.games.lotro.common.enums.TravelLink;
import delta.games.lotro.common.enums.WJEncounterCategory;
import delta.games.lotro.common.enums.WJEncounterType;
import delta.games.lotro.common.enums.WJInstanceGroup;
import delta.games.lotro.common.enums.io.xml.EnumXMLWriter;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.lore.deeds.DeedType;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.ItemBinding;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.ItemSturdiness;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.legendary.relics.RelicType;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DataFacadeBuilder;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;
import delta.games.lotro.tools.dat.utils.i18n.TranslationUtils;

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
    String bundleName=getClass().getPackage().getName()+".enum";
    _translator=TranslationUtils.buildMultilocalesTranslator(bundleName);
  }

  /**
   * Load enums.
   */
  public void doIt()
  {
    boolean live=LotroCoreConfig.isLive();
    if (live)
    {
      loadEnum(587203292,"SkirmishDifficultyTier",Difficulty.class); // 0x230002DC
      loadEnum(587203290,"SkirmishGroupSize",GroupSize.class); // 0x230002DA
    }
    loadEnum(587202570,"GenusType",Genus.class); // 0x2300000A
    loadEnum(587202571,"Agent_Species",Species.class); // 0x2300000B
    loadEnum(587202572,"SubspeciesType",SubSpecies.class); // 0x2300000C
    loadEnum(587202573,"AlignmentType",Alignment.class); // 0x2300000D
    loadEnum(587202574,"CharacterClassType",AgentClass.class); // 0x2300000E
    loadEnum(587202575,"ClassificationFilterType",ClassificationFilter.class);  // 0x2300000F
    loadEnum(587202672,"ExaminationModStatType",MobType.class); // 0x23000070
    loadEnum(587202586,"SkillCharacteristicCategory",SkillCategory.class); // 0x2300001A
    loadEnum(587202647,"TraitNature",TraitNature.class); // 0x23000057
    loadEnum(587203308,"TraitSubCategory",TraitSubCategory.class); // 0x230002EC
    loadEnum(587203634,"ItemSocketType",SocketType.class); // 0x23000432
    loadEnum(587202614,"ItemClass",ItemClass.class); // 0x23000036
    loadEnum(587202568,"TravelLink",TravelLink.class); // 0x23000008
    loadEnum(587202756,"BillingGroup",BillingGroup.class); // 0x230000C4
    if (live)
    {
      loadEnum(587203550,"CollectionCategory",CollectionCategory.class); // 0x230003DE
    }
    loadEnum(587202636,"EquipmentCategory",EquipmentCategory.class); // 0x2300004C
    if (live)
    {
      loadEnum(587203638,"AllegianceGroup",AllegianceGroup.class); // 0x23000436
      loadEnum(587203337,"WJEncounterType",WJEncounterType.class); // 0x23000309
      loadEnum(587203408,"WJEncounterCategory",WJEncounterCategory.class); // 0x23000350
      loadEnum(587203537,"WJInstanceGroup",WJInstanceGroup.class); // 0x230003D1
      loadEnum(587203267,"IATitleCategory",LegendaryTitleCategory.class); // 0x230002C3
      loadEnum(587203238,"IATitleTier",LegendaryTitleTier.class); // 0x230002A6
      loadEnum(587203350,"PaperItemCategory",PaperItemCategory.class); // 0x23000316
      loadEnum(587203433,"PointBasedTraitTree",TraitTreeType.class); // 0x23000369
      loadEnum(587203489,"PointBasedTraitTreeBranch",TraitTreeBranchType.class); // 0x230003A1
    }
    loadEnum(587203643,"ItemUniquenessChannel",ItemUniquenessChannel.class); // 0x2300043B
    loadEnum(587202585,"QuestCategory",QuestCategory.class); // 0x23000019
    loadEnum(587202588,"AccomplishmentUITab",DeedCategory.class); // 0x2300001C
    loadEnum(587202659,"CraftTier",CraftTier.class); // 0x23000063
    {
      // Filled for live, empty for SoA, 
      loadEnum(587203200,"MountType",MountType.class); // 0x23000280
      loadEnum(587203478,"SkillCharacteristicSubCategory",SkillCharacteristicSubCategory.class); // 0x23000396
    }
    loadEnum(587202661,"CraftUICategory",CraftingUICategory.class); // 0x23000065
    loadEnum(587202600,"DamageType",DamageType.class); // 0x23000028
    loadEnum(587202810,"ItemSturdiness",ItemSturdiness.class); // 0x230000FA
    loadEnum(587202663,"ItemQualities",ItemQuality.class); // 0x23000067
    if (live)
    {
      loadEnum(587203232,"RunicTier",RunicTier.class); // 0x230002A0
      loadEnum(587203222,"RunicType",RelicType.class); // 0x23000296
    }
    loadEnum(587203492,"SkillType",SkillType.class); // 0x230003A4
    loadEnum(587202640,"CombatState",CombatState.class); // 0x23000050
    loadEnum(587202660,"ResistCategory",ResistCategory.class); // 0x23000064

    // Derivated enums
    // From EquipmentCategory:
    // - ArmourType
    {
      int[] sourceCodes=new int[] {
          // Armour types
          10, 9, 18,
          // Shield types
          40, 11, 17
      };
      int[] codes=new int[] {
          2,1,0,5,4,3
      };
      String[] keys={
          "HEAVY", "MEDIUM", "LIGHT",
          "WARDEN_SHIELD", "HEAVY_SHIELD", "SHIELD"
      };
      buildSubEnum(587202636,"ArmourType",ArmourType.class,sourceCodes,codes,keys);
    }
    // - WeaponType
    {
      int[] sourceCodes=new int[] {
          28, 3, 24, 6, 12, 15, 26, 4, 27, 5, 20, 22, 16, 8, 14, 41, 13, 39, 48
      };
      String[] keys={
          "ONE_HANDED_SWORD", "TWO_HANDED_SWORD", "ONE_HANDED_AXE", "TWO_HANDED_AXE",
          "ONE_HANDED_HAMMER", "TWO_HANDED_HAMMER", "ONE_HANDED_CLUB", "TWO_HANDED_CLUB",
          "ONE_HANDED_MACE", "TWO_HANDED_MACE",
          "DAGGER", "STAFF", "HALBERD", "BOW", "CROSSBOW", "JAVELIN", "SPEAR", "RUNE_STONE","BATTLE_GAUNTLETS"
      };
      buildSubEnum(587202636,"WeaponType",WeaponType.class,sourceCodes,null,keys);
    }
    // From ContainerSlot
    // - GearSlot
    {
      int[] sourceCodes=new int[] {
          2,3,4,5,6,7,8,
          9,10,11,12,13,14,15,
          16,17,18,19,20,21,23,24,25
      };
      String[] keys={
          "HEAD", "BREAST", "LEGS", "HANDS", "FEET", "SHOULDER", "BACK",
          "LEFT_WRIST", "RIGHT_WRIST", "NECK", "LEFT_FINGER", "RIGHT_FINGER", "LEFT_EAR", "RIGHT_EAR",
          "POCKET", "MAIN_MELEE", "OTHER_MELEE", "RANGED", "TOOL", "CLASS_ITEM",
          "MAIN_HAND_AURA", "OFF_HAND_AURA", "RANGED_AURA"
      };
      buildSubEnum(587202798,"GearSlot",GearSlot.class,sourceCodes,null,keys);
    }

    // Custom enums
    buildGenderEnum();
    buildDeedTypeEnum();
    buildBindingEnum();
    buildEquipmentSlotEnum();
  }

  private <T extends LotroEnumEntry> void loadEnum(int enumId, String name, Class<T> implClass)
  {
    LotroEnum<T> lotroEnum=new LotroEnum<T>(enumId,name,implClass);
    String labelsSetName="enum-"+implClass.getSimpleName();
    I18nUtils i18n=new I18nUtils(labelsSetName,_facade.getGlobalStringsManager());
    EnumMapper enumMapper=_facade.getEnumsManager().getEnumMapper(enumId);
    if (enumMapper!=null)
    {
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
    }
    else
    {
      LOGGER.warn("Could not load enum: "+name);
    }
    saveEnumFile(lotroEnum,implClass,i18n);
  }

  private <T extends LotroEnumEntry> void buildSubEnum(int sourceEnumId, String name, Class<T> implClass,
      int[] sourceCodes, int[] codes, String[] keys)
  {
    LotroEnum<T> lotroEnum=new LotroEnum<T>(0,name,implClass);
    EnumMapper enumMapper=_facade.getEnumsManager().getEnumMapper(sourceEnumId);
    if (enumMapper!=null)
    {
      String labelsSetName="enum-"+implClass.getSimpleName();
      I18nUtils i18n=new I18nUtils(labelsSetName,_facade.getGlobalStringsManager());
      int nbCodes=sourceCodes.length;
      for(int i=0;i<nbCodes;i++)
      {
        int sourceCode=sourceCodes[i];
        int code=(codes!=null)?codes[i]:sourceCodes[i];
        String key=keys[i];
        String label=i18n.getEnumValue(enumMapper,sourceCode,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
        T entry=lotroEnum.buildEntryInstance(code,key,label);
        lotroEnum.registerEntry(entry);
      }
      saveEnumFile(lotroEnum,implClass,i18n);
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
      handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getBoxOfEssenceCode(),"BoxOfEssences");
      // Enhancement runes
      handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getEnhancementRuneCode(),"EnhancementRune");
      // Heraldric Traceries
      handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getHeraldicTraceryCode(),"HeraldricTracery");
      // Words of Power
      handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getWordOfPowerCode(),"WoPower");
      // Words of Mastery
      handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getWordOfMasteryCode(),"WoMastery");
      // Words of Craft
      handleCustomEntry(lotroEnum,i18n,ItemClassUtils.getWordOfCraftCode(),"WoCraft");
    }
    else if (enumId==0x23000350) // WJEncounterCategory
    {
      handleCustomEntry(lotroEnum,i18n,0,"OtherInstanceCategory");
    }
    else if (enumId==0x23000063) // CraftTier
    {
      handleCustomEntry(lotroEnum,i18n,0,"Beginner");
    }
  }

  private <T extends LotroEnumEntry> T handleCustomEntry(LotroEnum<T> lotroEnum, I18nUtils i18n, int code, String baseKey)
  {
    return handleCustomEntry(lotroEnum,i18n,code,baseKey,null);
  }

  private <T extends LotroEnumEntry> T handleCustomEntry(LotroEnum<T> lotroEnum, I18nUtils i18n, int code, String baseKey, String key)
  {
    // Define localized labels
    String i18nKey=baseKey;
    for(Locale locale : _translator.getLocales())
    {
      String value=_translator.translate(baseKey,null,locale);
      i18n.defineLabel(locale.getLanguage(),i18nKey,value);
    }
    // Define entry
    T entry=lotroEnum.buildEntryInstance(code,key,i18nKey);
    lotroEnum.registerEntry(entry);
    return entry;
  }

  private String getKey(Class<? extends LotroEnumEntry> implClass, int code)
  {
    if (implClass==GroupSize.class)
    {
      return getGroupSizeKey(code);
    }
    else if (implClass==DamageType.class)
    {
      return getDamageTypeKey(code);
    }
    else if (implClass==ItemSturdiness.class)
    {
      return getItemSturdinessKey(code);
    }
    else if (implClass==ItemQuality.class)
    {
      return getItemQualityKey(code);
    }
    else if (implClass==RelicType.class)
    {
      return getRelicTypeKey(code);
    }
    else if (implClass==SocketType.class)
    {
      return getSocketTypeKey(code);
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

  private String getDamageTypeKey(int code)
  {
    if (code==1) return "COMMON";
    if (code==2) return "WESTERNESSE";
    if (code==4) return "ANCIENT_DWARF";
    if (code==8) return "BELERIAND";
    if (code==16) return "FIRE";
    if (code==32) return "SHADOW";
    if (code==64) return "LIGHT";
    if (code==512) return "LIGHTNING";
    if (code==256) return "FROST";
    return null;
  }

  private String getItemSturdinessKey(int code)
  {
    if (code==1) return "SUBSTANTIAL";
    if (code==2) return "BRITTLE";
    if (code==3) return "NORMAL";
    if (code==4) return "TOUGH";
    if (code==5) return "FLIMSY";
    if (code==7) return "WEAK";
    return null;
  }

  private String getItemQualityKey(int code)
  {
    if (code==1) return "LEGENDARY";
    if (code==2) return "RARE";
    if (code==3) return "INCOMPARABLE";
    if (code==4) return "UNCOMMON";
    if (code==5) return "COMMON";
    return null;
  }

  private String getRelicTypeKey(int code)
  {
    if (code==1) return "RUNE";
    if (code==2) return "SETTING";
    if (code==3) return "GEM";
    if (code==4) return "CRAFTED_RELIC";
    return null;
  }

  private String getSocketTypeKey(int code)
  {
    if (code==1) return "S"; // Classic/standard
    if (code==18) return "W"; // Essence of War
    if (code==19) return "C"; // Cloak
    if (code==20) return "N"; // Necklace
    if (code==22) return "P"; // Primary
    if (code==23) return "V"; // Vital
    return null;
  }

  private void buildGenderEnum()
  {
    Class<CharacterSex> implClass=CharacterSex.class;
    LotroEnum<CharacterSex> lotroEnum=new LotroEnum<CharacterSex>(0,"Gender",implClass);
    String labelsSetName="enum-"+implClass.getSimpleName();
    I18nUtils i18n=new I18nUtils(labelsSetName,_facade.getGlobalStringsManager());
    handleCustomEntry(lotroEnum,i18n,100,"MALE","MALE");
    handleCustomEntry(lotroEnum,i18n,101,"FEMALE","FEMALE");
    saveEnumFile(lotroEnum,implClass,i18n);
  }

  private void buildDeedTypeEnum()
  {
    Class<DeedType> implClass=DeedType.class;
    String enumName="DeedType";
    LotroEnum<DeedType> lotroEnum=new LotroEnum<DeedType>(0,enumName,implClass);
    String labelsSetName="enum-"+implClass.getSimpleName();
    I18nUtils i18n=new I18nUtils(labelsSetName,_facade.getGlobalStringsManager());
    handleCustomEntry(lotroEnum,i18n,100,enumName+".CLASS","CLASS");
    handleCustomEntry(lotroEnum,i18n,101,enumName+".RACE","RACE");
    handleCustomEntry(lotroEnum,i18n,102,enumName+".EVENT","EVENT");
    handleCustomEntry(lotroEnum,i18n,103,enumName+".EXPLORER","EXPLORER");
    handleCustomEntry(lotroEnum,i18n,104,enumName+".LORE","LORE");
    handleCustomEntry(lotroEnum,i18n,105,enumName+".REPUTATION","REPUTATION");
    handleCustomEntry(lotroEnum,i18n,106,enumName+".SLAYER","SLAYER");
    saveEnumFile(lotroEnum,implClass,i18n);
  }

  private void buildBindingEnum()
  {
    Class<ItemBinding> implClass=ItemBinding.class;
    String enumName="ItemBinding";
    LotroEnum<ItemBinding> lotroEnum=new LotroEnum<ItemBinding>(0,enumName,implClass);
    String labelsSetName="enum-"+implClass.getSimpleName();
    I18nUtils i18n=new I18nUtils(labelsSetName,_facade.getGlobalStringsManager());
    handleCustomEntry(lotroEnum,i18n,100,enumName+".BIND_ON_EQUIP","BIND_ON_EQUIP");
    handleCustomEntry(lotroEnum,i18n,101,enumName+".BIND_ON_ACQUIRE","BIND_ON_ACQUIRE");
    handleCustomEntry(lotroEnum,i18n,102,enumName+".BOUND_TO_ACCOUNT_ON_ACQUIRE","BOUND_TO_ACCOUNT_ON_ACQUIRE");
    handleCustomEntry(lotroEnum,i18n,103,enumName+".NONE","NONE");
    saveEnumFile(lotroEnum,implClass,i18n);
  }

  private void buildEquipmentSlotEnum()
  {
    Class<EquipmentLocation> implClass=EquipmentLocation.class;
    String enumName="EquipmentLocation";
    LotroEnum<EquipmentLocation> lotroEnum=new LotroEnum<EquipmentLocation>(0,enumName,implClass);
    String labelsSetName="enum-"+implClass.getSimpleName();
    I18nUtils i18n=new I18nUtils(labelsSetName,_facade.getGlobalStringsManager());
    handleCustomEntry(lotroEnum,i18n,0,enumName+".HEAD","HEAD");
    handleCustomEntry(lotroEnum,i18n,1,enumName+".SHOULDER","SHOULDER");
    handleCustomEntry(lotroEnum,i18n,2,enumName+".BACK","BACK");
    handleCustomEntry(lotroEnum,i18n,3,enumName+".CHEST","CHEST");
    handleCustomEntry(lotroEnum,i18n,4,enumName+".HAND","HAND");
    handleCustomEntry(lotroEnum,i18n,5,enumName+".LEGS","LEGS");
    handleCustomEntry(lotroEnum,i18n,6,enumName+".FEET","FEET");
    handleCustomEntry(lotroEnum,i18n,7,enumName+".EAR","EAR");
    handleCustomEntry(lotroEnum,i18n,8,enumName+".NECK","NECK");
    handleCustomEntry(lotroEnum,i18n,9,enumName+".WRIST","WRIST");
    handleCustomEntry(lotroEnum,i18n,10,enumName+".FINGER","FINGER");
    handleCustomEntry(lotroEnum,i18n,11,enumName+".POCKET","POCKET");
    handleCustomEntry(lotroEnum,i18n,12,enumName+".MAIN_HAND","MAIN_HAND");
    handleCustomEntry(lotroEnum,i18n,13,enumName+".OFF_HAND","OFF_HAND");
    handleCustomEntry(lotroEnum,i18n,14,enumName+".RANGED_ITEM","RANGED_ITEM");
    handleCustomEntry(lotroEnum,i18n,15,enumName+".TOOL","TOOL");
    handleCustomEntry(lotroEnum,i18n,16,enumName+".CLASS_SLOT","CLASS_SLOT");
    handleCustomEntry(lotroEnum,i18n,17,enumName+".BRIDLE","BRIDLE");
    handleCustomEntry(lotroEnum,i18n,18,enumName+".MAIN_HAND_AURA","MAIN_HAND_AURA");
    handleCustomEntry(lotroEnum,i18n,19,enumName+".OFF_HAND_AURA","OFF_HAND_AURA");
    handleCustomEntry(lotroEnum,i18n,20,enumName+".RANGED_AURA","RANGED_AURA");
    saveEnumFile(lotroEnum,implClass,i18n);
  }

  private <T extends LotroEnumEntry> void saveEnumFile(LotroEnum<T> lotroEnum, Class<T> implClass, I18nUtils i18n)
  {
    File enumsDir=GeneratedFiles.ENUMS_DIR;
    String fileName=implClass.getSimpleName()+".xml";
    File enumFile=new File(enumsDir,fileName);
    new EnumXMLWriter<T>().writeEnum(enumFile,lotroEnum);
    i18n.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    Locale.setDefault(Locale.ENGLISH);
    new MainDatEnumsLoader(facade).doIt();
    facade.dispose();
  }
}
