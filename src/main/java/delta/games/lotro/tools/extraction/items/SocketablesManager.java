package delta.games.lotro.tools.extraction.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.classes.WellKnownCharacterClassKeys;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.enums.ItemClass;
import delta.games.lotro.common.enums.ItemClassUtils;
import delta.games.lotro.common.enums.ItemUniquenessChannel;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SocketType;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.essences.Essence;
import delta.games.lotro.lore.items.essences.EssencesSlotsSetup;
import delta.games.lotro.lore.items.legendary2.EnhancementRune;
import delta.games.lotro.lore.items.legendary2.Tracery;
import delta.games.lotro.lore.items.legendary2.io.xml.EnhancementRunesXMLWriter;
import delta.games.lotro.lore.items.legendary2.io.xml.TraceriesXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;

/**
 * Manages essences, traceries and enhancement runes.
 * @author DAM
 */
public class SocketablesManager
{
  private static final Logger LOGGER=LoggerFactory.getLogger(SocketablesManager.class);

  private static final String ITEM_SOCKET_GEM_MIN_LEVEL="Item_Socket_GemMinLevel";
  private static final String ITEM_SOCKET_GEM_MAX_LEVEL="Item_Socket_GemMaxLevel";
  private static final String ITEM_SOCKET_TYPE="Item_Socket_Type";

  private static final int[] OVERLAY_FOR_TIER=
  {
    1091914756, 1091914773, 1091914770, 1091914772, 1091914776, // 1-5
    1091914767, 1091914762, 1091914765, 1091914774, 1091914766, // 6-10
    1092396132, 1092396316, 1092508824, 1092694659, 1092694658, // 11-15
    1092694657 // 16
  };

  private List<Tracery> _traceries;
  private List<EnhancementRune> _enhancementRunes;
  private LotroEnum<ItemClass> _itemClassEnum;
  private LotroEnum<ItemUniquenessChannel> _uniquenessChannel;

  /**
   * Constructor.
   */
  public SocketablesManager()
  {
    _traceries=new ArrayList<Tracery>();
    _enhancementRunes=new ArrayList<EnhancementRune>();
    LotroEnumsRegistry enumsRegistry=LotroEnumsRegistry.getInstance();
    _itemClassEnum=enumsRegistry.get(ItemClass.class);
    _uniquenessChannel=enumsRegistry.get(ItemUniquenessChannel.class);
  }

  /**
   * Indicates of the given item properties are for an essence.
   * @param properties Properties to use.
   * @return <code>null</code> or a socket type if it is an essence.
   */
  public SocketType isEssence(PropertiesSet properties)
  {
    Integer itemClassCode=(Integer)properties.getProperty("Item_Class");
    if ((itemClassCode==null) || (itemClassCode.intValue()!=ItemClassUtils.ESSENCE_CODE))
    {
      return null;
    }
    Long type=(Long)properties.getProperty(ITEM_SOCKET_TYPE);
    if ((type==null) || (type.intValue()==0))
    {
      return null;
    }
    SocketType socketType=SocketUtils.getSocketType(type.intValue());
    if (socketType==null)
    {
      return null;
    }
    int socketTypeCode=socketType.getCode();
    // Essences
    if ((socketTypeCode==1) || // Classic essences
        (socketTypeCode==18) || // Essences of War (PvP)
        (socketTypeCode==19) || // Cloak essences
        (socketTypeCode==20) || // Necklace essences
        (socketTypeCode==22) || // Primary Essence
        (socketTypeCode==23)) // Vital Essence
    {
      return socketType;
    }
    return null;
  }

  /**
   * Handle a socketable.
   * @param item Source item.
   * @param properties Item properties.
   */
  public void handleSocketable(Item item, PropertiesSet properties)
  {
    int newCode=handleSocketablePrivate(item,properties);
    if (newCode>=0)
    {
      ItemClass itemClass=_itemClassEnum.getEntry(newCode);
      item.setItemClass(itemClass);
    }
  }

  private int handleSocketablePrivate(Item item, PropertiesSet properties)
  {
    // Special case
    String name=item.getName();
    if ((name!=null) && (name.contains("Mordor - Essences")))
    {
      return ItemClassUtils.getBoxOfEssenceCode();
    }
    // Use socket type...
    Long type=(Long)properties.getProperty(ITEM_SOCKET_TYPE);
    if (type==null)
    {
      LOGGER.warn("Expected a property '{}' for item: {}",ITEM_SOCKET_TYPE,item);
      return -1;
    }
    Integer tierInt=findTier(item,properties);
    item.setTier(tierInt);
    if (type.intValue()==0)
    {
      // 0 => enhancement rune
      handleEnhancementRune(item,properties);
      return ItemClassUtils.getEnhancementRuneCode();
    }
    SocketType socketType=SocketUtils.getSocketType(type.intValue());
    if (socketType==null)
    {
      LOGGER.warn("Unexpected socket type: {} for item: {}",type,item);
      return -1;
    }
    int socketTypeCode=socketType.getCode();
    // Essences
    if (item instanceof Essence)
    {
      handleEssence((Essence)item,properties);
      return -1;
    }
    // Traceries
    handleTracery(item,socketType,properties);
    // - heraldic tracery
    if (socketTypeCode==3)
    {
      return ItemClassUtils.getHeraldicTraceryCode();
    }
    // - word of power
    else if (socketTypeCode==4)
    {
      return ItemClassUtils.getWordOfPowerCode();
    }
    // - word of craft
    else if (socketTypeCode==5)
    {
      return ItemClassUtils.getWordOfCraftCode();
    }
    // - word of mastery: 6-16+21
    else if (((socketTypeCode>=6) && (socketTypeCode<=16)) || (socketTypeCode==21))
    {
      return ItemClassUtils.getWordOfMasteryCode();
    }
    else
    {
      LOGGER.warn("Unmanaged socket type {} for item: {}",socketType,item);
      return -1;
    }
  }

  private Integer findTier(Item item, PropertiesSet properties)
  {
    Integer overlay=(Integer)properties.getProperty("Icon_Layer_OverlayDID");
    if (overlay==null)
    {
      return null;
    }
    int nbOverlays=OVERLAY_FOR_TIER.length;
    for(int i=0;i<nbOverlays;i++)
    {
      if (overlay.intValue()==OVERLAY_FOR_TIER[i])
      {
        int tier=i+1;
        return Integer.valueOf(tier);
      }
    }
    LOGGER.warn("Unmanaged essence/tracery overlay: {} for {}",overlay,item);
    return null;
  }

  private void handleEssence(Essence essence, PropertiesSet props)
  {
    Integer minItemLevel=(Integer)props.getProperty(ITEM_SOCKET_GEM_MIN_LEVEL);
    int itemLevel=((Integer)props.getProperty("Item_Level")).intValue();
    if (minItemLevel!=null)
    {
      if (minItemLevel.intValue()>itemLevel)
      {
        LOGGER.warn("Fixed essence item level: {} => {}",essence,minItemLevel);
        essence.setItemLevel(minItemLevel);
      }
      Integer maxItemLevel=(Integer)props.getProperty(ITEM_SOCKET_GEM_MAX_LEVEL);
      if ((maxItemLevel!=null) && (minItemLevel.intValue()!=maxItemLevel.intValue()))
      {
        LOGGER.warn("Essence with min item level!=max item level: {} => min={}, max={}",essence,minItemLevel,maxItemLevel);
      }
    }
  }

  private void handleTracery(Item item, SocketType socketType, PropertiesSet props)
  {
    int minItemLevel=((Integer)props.getProperty(ITEM_SOCKET_GEM_MIN_LEVEL)).intValue();
    int maxItemLevel=((Integer)props.getProperty(ITEM_SOCKET_GEM_MAX_LEVEL)).intValue();
    int levelupIncrement=((Integer)props.getProperty("Item_Socket_LevelupRuneIncrement")).intValue();
    int setId=((Integer)props.getProperty("Item_PropertySet")).intValue();
    ItemUniquenessChannel uniquenessChannel=null;
    Integer uniquenessChannelCode=(Integer)props.getProperty("Item_UniquenessChannel");
    if ((uniquenessChannelCode!=null) && (uniquenessChannelCode.intValue()!=0))
    {
      uniquenessChannel=_uniquenessChannel.getEntry(uniquenessChannelCode.intValue());
    }
    Tracery tracery=new Tracery(item,socketType,minItemLevel,maxItemLevel,levelupIncrement,setId,uniquenessChannel);
    _traceries.add(tracery);
    String requiredClassKey=getRequiredClassKey(socketType);
    if (requiredClassKey!=null)
    {
      ClassDescription requiredClass=ClassesManager.getInstance().getCharacterClassByKey(requiredClassKey);
      item.setRequiredClass(requiredClass);
    }
  }

  private void handleEnhancementRune(Item item, PropertiesSet props)
  {
    int minItemLevel=((Integer)props.getProperty(ITEM_SOCKET_GEM_MIN_LEVEL)).intValue();
    int maxItemLevel=((Integer)props.getProperty(ITEM_SOCKET_GEM_MAX_LEVEL)).intValue();
    int levelupIncrement=((Integer)props.getProperty("Item_Socket_LevelupRuneIncrement")).intValue();
    EnhancementRune enhancementRune=new EnhancementRune(item,minItemLevel,maxItemLevel,levelupIncrement);
    _enhancementRunes.add(enhancementRune);
  }

  private static String getRequiredClassKey(SocketType socketType)
  {
    int code=socketType.getCode();
    if (code==6) return WellKnownCharacterClassKeys.BEORNING;
    if (code==7) return WellKnownCharacterClassKeys.BRAWLER;
    if (code==8) return WellKnownCharacterClassKeys.BURGLAR;
    if (code==9) return WellKnownCharacterClassKeys.CAPTAIN;
    if (code==10) return WellKnownCharacterClassKeys.CHAMPION;
    if (code==11) return WellKnownCharacterClassKeys.GUARDIAN;
    if (code==12) return WellKnownCharacterClassKeys.HUNTER;
    if (code==13) return WellKnownCharacterClassKeys.LORE_MASTER;
    if (code==14) return WellKnownCharacterClassKeys.MINSTREL;
    if (code==15) return WellKnownCharacterClassKeys.RUNE_KEEPER;
    if (code==16) return WellKnownCharacterClassKeys.WARDEN;
    return null;
  }

  /**
   * Load an essences setup.
   * @param essenceSlots Input data.
   * @return A setup.
   */
  public EssencesSlotsSetup loadEssenceSlotsSetup(Object[] essenceSlots)
  {
    EssencesSlotsSetup setup=new EssencesSlotsSetup();
    for(Object essenceSlot : essenceSlots)
    {
      PropertiesSet essenceSlotProps=(PropertiesSet)essenceSlot;
      int socketTypeCode=((Long)essenceSlotProps.getProperty(ITEM_SOCKET_TYPE)).intValue();
      SocketType socketType=SocketUtils.getSocketType(socketTypeCode);
      setup.addSlot(socketType);
    }
    return setup;
  }

  /**
   * Save loaded data.
   */
  public void save()
  {
    // Save traceries
    Collections.sort(_traceries,new IdentifiableComparator<Tracery>());
    TraceriesXMLWriter.write(GeneratedFiles.TRACERIES,_traceries);
    // Save enhancement runes
    Collections.sort(_enhancementRunes,new IdentifiableComparator<EnhancementRune>());
    EnhancementRunesXMLWriter.write(GeneratedFiles.ENHANCEMENT_RUNES,_enhancementRunes);
  }
}
