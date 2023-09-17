package delta.games.lotro.tools.dat.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.classes.WellKnownCharacterClassKeys;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.enums.ItemClassUtils;
import delta.games.lotro.common.enums.ItemUniquenessChannel;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SocketType;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.legendary2.EnhancementRune;
import delta.games.lotro.lore.items.legendary2.Tracery;
import delta.games.lotro.lore.items.legendary2.io.xml.EnhancementRunesXMLWriter;
import delta.games.lotro.lore.items.legendary2.io.xml.TraceriesXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Manages essences, traceries and enhancement runes.
 * @author DAM
 */
public class SocketablesManager
{
  private static final Logger LOGGER=Logger.getLogger(SocketablesManager.class);

  private static final int[] OVERLAY_FOR_TIER=
  {
    1091914756, 1091914773, 1091914770, 1091914772, 1091914776, // 1-5
    1091914767, 1091914762, 1091914765, 1091914774, 1091914766, // 6-10
    1092396132, 1092396316, 1092508824, 1092694659 // 11-14
  };

  private List<Tracery> _traceries;
  private List<EnhancementRune> _enhancementRunes;
  private LotroEnum<ItemUniquenessChannel> _uniquenessChannel;

  /**
   * Constructor.
   */
  public SocketablesManager()
  {
    _traceries=new ArrayList<Tracery>();
    _enhancementRunes=new ArrayList<EnhancementRune>();
    LotroEnumsRegistry enumsRegistry=LotroEnumsRegistry.getInstance();
    _uniquenessChannel=enumsRegistry.get(ItemUniquenessChannel.class);
  }

  /**
   * Classify a socketable.
   * @param item Source item.
   * @param properties Item properties.
   * @return A class code.
   */
  public int classifySocketable(Item item, PropertiesSet properties)
  {
    Integer overlay=(Integer)properties.getProperty("Icon_Layer_OverlayDID");
    if (overlay==null)
    {
      return ItemClassUtils.ESSENCE_CODE;
    }
    String name=item.getName();
    if ((name!=null) && (name.contains("Mordor - Essences")))
    {
      return ItemClassUtils.getBoxOfEssenceCode();
    }
    int nbOverlays=OVERLAY_FOR_TIER.length;
    int tier=0;
    for(int i=0;i<nbOverlays;i++)
    {
      if (overlay.intValue()==OVERLAY_FOR_TIER[i])
      {
        tier=i+1;
        break;
      }
    }
    if (tier==0)
    {
      LOGGER.warn("Unmanaged essence/tracery overlay: "+overlay+" for "+name);
    }
    int code=getSocketableItemClass(item,properties,tier);
    return code;
  }

  private int getSocketableItemClass(Item item, PropertiesSet properties, int tier)
  {
    Long type=(Long)properties.getProperty("Item_Socket_Type");
    if (type==null)
    {
      LOGGER.warn("Expected an Item_Socket_Type property for item: "+item);
      return 0;
    }
    if (type.intValue()==0)
    {
      handleEnhancementRune(item,properties);
      return ItemClassUtils.getEnhancementRuneCode(tier);
    }
    SocketType socketType=SocketUtils.getSocketType(type.intValue());
    if (socketType==null)
    {
      LOGGER.warn("Unexpected socket type: "+type+" for item: "+item);
      return 0;
    }
    int socketTypeCode=socketType.getCode();
    if (socketTypeCode==1)
    {
      return ItemClassUtils.getEssenceCode(tier);
    }
    if (socketTypeCode==18)
    {
      return ItemClassUtils.getEssenceOfWarCode(tier);
    }
    if (socketTypeCode==19)
    {
      return ItemClassUtils.getCloakEssenceCode(tier);
    }
    if (socketTypeCode==20)
    {
      return ItemClassUtils.getNecklaceEssenceCode(tier);
    }
    handleTracery(item,socketType,properties);
    if (socketTypeCode==3)
    {
      return ItemClassUtils.getHeraldicTraceryCode(tier);
    }
    else if (socketTypeCode==4)
    {
      return ItemClassUtils.getWordOfPowerCode(tier);
    }
    else if (socketTypeCode==5)
    {
      return ItemClassUtils.getWordOfCraftCode(tier);
    }
    // 6-16+21
    else if (((socketTypeCode>=6) && (socketTypeCode<=16)) || (socketTypeCode==21))
    {
      return ItemClassUtils.getWordOfMasteryCode(tier);
    }
    else //if (socketTypeCode==2)
    {
      LOGGER.warn("Unmanaged socket type "+socketTypeCode+" for item: "+item);
      return 0;
    }
  }

  private void handleTracery(Item item, SocketType socketType, PropertiesSet props)
  {
    int minItemLevel=((Integer)props.getProperty("Item_Socket_GemMinLevel")).intValue();
    int maxItemLevel=((Integer)props.getProperty("Item_Socket_GemMaxLevel")).intValue();
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
    int minItemLevel=((Integer)props.getProperty("Item_Socket_GemMinLevel")).intValue();
    int maxItemLevel=((Integer)props.getProperty("Item_Socket_GemMaxLevel")).intValue();
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
