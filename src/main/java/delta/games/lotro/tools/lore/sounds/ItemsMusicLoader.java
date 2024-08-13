package delta.games.lotro.tools.lore.sounds;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.enums.ItemClass;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyType;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.dat.data.enums.AbstractMapper;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.enums.MapperUtils;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.tools.dat.GeneratedJukeboxFiles;
import delta.lotro.jukebox.core.model.base.SoundDescription;
import delta.lotro.jukebox.core.model.context.SoundContext;
import delta.lotro.jukebox.core.model.context.SoundContextsManager;
import delta.lotro.jukebox.core.model.context.SoundReference;
import delta.lotro.jukebox.core.model.context.SoundReferences;
import delta.lotro.jukebox.core.model.context.io.xml.SoundContextsXMLConstants;
import delta.lotro.jukebox.core.model.context.io.xml.SoundContextsXMLWriter;

/**
 * Loads music data for items.
 * @author DAM
 */
public class ItemsMusicLoader
{
  private static final Logger LOGGER=Logger.getLogger(ItemsMusicLoader.class);

  private DataFacade _facade;
  private SoundContextManager _contextMgr;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param contextMgr Context manager.
   */
  public ItemsMusicLoader(DataFacade facade, SoundContextManager contextMgr)
  {
    _facade=facade;
    _contextMgr=contextMgr;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    SoundContextsManager musicItems=doMusicItems();
    saveContextFile(GeneratedJukeboxFiles.MUSIC_ITEMS,musicItems,SoundContextsXMLConstants.MUSIC_ITEM_CONTEXT_TAG);
    SoundContextsManager instruments=doInstruments();
    saveContextFile(GeneratedJukeboxFiles.INSTRUMENTS,instruments,SoundContextsXMLConstants.INSTRUMENT_CONTEXT_TAG);
  }

  private void saveContextFile(File toFile, SoundContextsManager mgr, String contextTag)
  {
    SoundContextsXMLWriter writer=new SoundContextsXMLWriter(contextTag);
    writer.writeSoundContextsFile(toFile,mgr.getAllSoundContexts());
  }

  private SoundContextsManager doMusicItems()
  {
    SoundContextsManager ret=new SoundContextsManager();
    // 82 is: "Home: Music"
    List<Item> items=findItemsForClass(82);
    for(Item item : items)
    {
      SoundContext musicItem=handleHousingItem(item);
      ret.registerSoundContext(musicItem);
    }
    return ret;
  }

  private List<Item> findItemsForClass(int classCode)
  {
    List<Item> ret=new ArrayList<Item>();
    ItemsManager itemsMgr=ItemsManager.getInstance();
    for(Item item : itemsMgr.getAllItems())
    {
      ItemClass itemClass=item.getItemClass();
      if ((itemClass!=null) && (itemClass.getCode()==classCode))
      {
        ret.add(item);
      }
    }
    return ret;
  }

  private SoundContextsManager doInstruments()
  {
    /*
Item_Class: 11 (Instrument)
Item_EquipmentCategory: 64 (Instrument[e])
Item_ImplementType: 131072 (Instrument)
Item_Music_Channeling_State: 1879377567
Item_Music_Instrument_Type: 16 (Bassoon)
Usage_IsMusicalInstrument: 1
     */
    SoundContextsManager ret=new SoundContextsManager();
    // 11 is: "Instrument"
    List<Item> items=findItemsForClass(11);
    Map<Integer,SoundContext> contexts=new HashMap<Integer,SoundContext>();
    for(Item item : items)
    {
      SoundContext instrumentContext=handleInstrumentItem(item,contexts);
      if (instrumentContext!=null)
      {
        ret.registerSoundContext(instrumentContext);
      }
    }
    return ret;
  }


  private SoundContext handleHousingItem(Item item)
  {
    int itemId=item.getIdentifier();
    PropertiesSet itemProps=_facade.loadProperties(itemId+DATConstants.DBPROPERTIES_OFFSET);
    int propertyId=((Integer)itemProps.getProperty("Item_Decoration_PropertyHook_Name")).intValue();
    PropertyValue propertyValueValue=itemProps.getPropertyValueByName("Item_Decoration_PropertyHook_Value");
    int propertyValue=((Integer)propertyValueValue.getValue()).intValue();
    if (LOGGER.isDebugEnabled())
    {
      long category=((Long)itemProps.getProperty("Item_Decoration_Category")).longValue();
      EnumMapper enumMapper=findEnumForProperty(propertyValueValue.getDefinition());
      String valueLabel=(enumMapper!=null)?enumMapper.getLabel(propertyValue):"?";
      LOGGER.debug("Item: "+item+" => category="+category+", ID="+propertyId+", value="+propertyValue+" ("+valueLabel+")");
    }
    SoundContext ret=new SoundContext(itemId,item.getName(),item.getIcon());
    SoundReferences soundRefs=ret.getSounds();
    List<SoundDescription> sounds=findSounds(propertyId,propertyValue);
    for(SoundDescription sound : sounds)
    {
      SoundReference ref=new SoundReference(sound.getIdentifier());
      ref.setSound(sound);
      soundRefs.addSoundReference(ref);
    }
    return ret;
  }

  private SoundContext handleInstrumentItem(Item item, Map<Integer,SoundContext> contexts)
  {
    int itemId=item.getIdentifier();
    PropertiesSet itemProps=_facade.loadProperties(itemId+DATConstants.DBPROPERTIES_OFFSET);
    Integer category=(Integer)itemProps.getProperty("Item_Music_Instrument_Type");
    SoundContext ret=contexts.get(category);
    if (ret!=null)
    {
      return null;
    }
    PropertiesRegistry registry=_facade.getPropertiesRegistry();
    PropertyDefinition propertyDef=registry.getPropertyDefByName("Item_Music_Instrument_Type");
    EnumMapper enumMapper=findEnumForProperty(propertyDef);
    String valueLabel=(enumMapper!=null)?enumMapper.getLabel(category.intValue()):"?";
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Item: "+item+" => category="+category+" ("+valueLabel+")");
    }
    PropertyDefinition propertyDefForSound=registry.getPropertyDefByName("Music_Instrument_Type");
    List<SoundDescription> sounds=findSounds(propertyDefForSound.getPropertyId(),category.intValue());
    ret=new SoundContext(category.intValue(),valueLabel,null);
    SoundReferences soundRefs=ret.getSounds();
    for(SoundDescription sound : sounds)
    {
      SoundReference ref=new SoundReference(sound.getIdentifier());
      ref.setSound(sound);
      soundRefs.addSoundReference(ref);
    }
    return ret;
  }

  private List<SoundDescription> findSounds(int propertyID, int value)
  {
    PropertyDefinition propertyDef=_facade.getPropertiesRegistry().getPropertyDef(propertyID);
    PropertySoundsRegistry registry=_contextMgr.getProperty(propertyDef);
    List<SoundDescription> sounds=registry.getSoundsForValue(value);
    return sounds;
  }

  private EnumMapper findEnumForProperty(PropertyDefinition propertyDef)
  {
    if (propertyDef==null)
    {
      return null;
    }
    PropertyType type=propertyDef.getPropertyType();
    if (type!=PropertyType.ENUM_MAPPER)
    {
      return null;
    }
    int enumId=propertyDef.getData();
    AbstractMapper mapper=MapperUtils.getEnum(_facade,enumId);
    if (mapper instanceof EnumMapper)
    {
      return (EnumMapper)mapper;
    }
    return null;
  }
}
