package delta.games.lotro.tools.dat.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.PerkUICategory;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.perks.PerkDescription;
import delta.games.lotro.lore.perks.io.xml.PerkDescriptionXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.effects.EffectLoader;
import delta.games.lotro.tools.dat.maps.PlacesLoader;
import delta.games.lotro.tools.dat.utils.WeenieContentDirectory;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Loader for perks data.
 * @author DAM
 */
public class MainPerksLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainPerksLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;
  private EffectLoader _effectsLoader;
  private LotroEnum<PerkUICategory> _uiCategoryEnum;
  private Set<Integer> _handledInventories;
  private Map<Integer,PerkDescription> _perks;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param effectsLoader Effects loader.
   */
  public MainPerksLoader(DataFacade facade, EffectLoader effectsLoader)
  {
    _facade=facade;
    _effectsLoader=effectsLoader;
    _i18n=new I18nUtils("perks",facade.getGlobalStringsManager());
    _uiCategoryEnum=LotroEnumsRegistry.getInstance().get(PerkUICategory.class);
    _handledInventories=new HashSet<Integer>();
    _perks=new HashMap<Integer,PerkDescription>();
  }

  /**
   * Load perks.
   */
  public void doIt()
  {
    // Free peoples
    {
      PropertiesSet mpProps=WeenieContentDirectory.loadWeenieContentProps(_facade,"LevelTableDirectory");
      Object[] idsArray=(Object[])mpProps.getProperty("AdvTable_LevelTableList");
      for(Object idObj : idsArray)
      {
        int id=((Integer)idObj).intValue();
        PropertiesSet mpClassProps=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
        Integer perkInventoryId=(Integer)mpClassProps.getProperty("AdvTable_PerkInventory");
        if (perkInventoryId!=null)
        {
          handlePerkInventory(perkInventoryId.intValue());
        }
      }
    }
    // Monster play
    {
      PropertiesSet mpProps=WeenieContentDirectory.loadWeenieContentProps(_facade,"MPLevelTableDirectory");
      String[] lists= {"AdvTable_EvilMLTList", "AdvTable_GoodMLTList", "AdvTable_NeutralMLTList"};
      for(String list : lists)
      {
        Object[] idsArray=(Object[])mpProps.getProperty(list);
        for(Object idObj : idsArray)
        {
          int id=((Integer)idObj).intValue();
          PropertiesSet mpClassProps=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
          Integer perkInventoryId=(Integer)mpClassProps.getProperty("MonsterPlay_PerkInventory");
          if (perkInventoryId!=null)
          {
            handlePerkInventory(perkInventoryId.intValue());
          }
        }
      }
    }
    List<PerkDescription> perks=new ArrayList<PerkDescription>(_perks.values());
    Collections.sort(perks,new IdentifiableComparator<PerkDescription>());
    savePerks(perks);
  }

  private void handlePerkInventory(int perkInventoryId)
  {
    Integer key=Integer.valueOf(perkInventoryId);
    if (!_handledInventories.contains(key))
    {
      List<PerkDescription> newPerks=loadPerksInventory(perkInventoryId);
      for(PerkDescription perk : newPerks)
      {
        Integer perkKey=Integer.valueOf(perk.getIdentifier());
        _perks.put(perkKey,perk);
      }
      _handledInventories.add(key);
    }
  }

  private List<PerkDescription> loadPerksInventory(int id)
  {
    List<PerkDescription> ret=new ArrayList<PerkDescription>();
    PropertiesSet props=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    Object[] idsArray=(Object[])props.getProperty("PerkVendor_InventoryList");
    for(Object idObj : idsArray)
    {
      int perkId=((Integer)idObj).intValue();
      PerkDescription perk=handlePerk(perkId);
      if (perk!=null)
      {
        ret.add(perk);
      }
    }
    return ret;
  }

  private PerkDescription handlePerk(int perkId)
  {
    /*
Perk_Channel: 11 (Perk_Perf_Mitigation)
Perk_Desc: Provides a temporary increase to the effectiveness of your armour.
Perk_Effect: 1879077245
Perk_Icon: 1090563514
Perk_MinLevel: 40
Perk_Name: Veteran Fortitude
Perk_SessionPointCost: 3000
Perk_UICategory: 7 (Armour)
     */
    PropertiesSet props=_facade.loadProperties(perkId+DATConstants.DBPROPERTIES_OFFSET);
    if (props==null)
    {
      return null;
    }
    PerkDescription ret=new PerkDescription();
    // ID
    ret.setIdentifier(perkId);
    // Name
    String name=_i18n.getNameStringProperty(props,"Perk_Name",perkId,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
    ret.setName(name);
    // Description
    String description=_i18n.getStringProperty(props,"Perk_Desc");
    ret.setDescription(description);
    // Icon
    int iconId=((Integer)props.getProperty("Perk_Icon")).intValue();
    ret.setIconId(iconId);
    // Build icon file
    String iconFilename=iconId+".png";
    File to=new File(GeneratedFiles.PERK_ICONS,iconFilename).getAbsoluteFile();
    if (!to.exists())
    {
      boolean ok=DatIconsUtils.buildImageFile(_facade,iconId,to);
      if (!ok)
      {
        LOGGER.warn("Could not build perk icon: "+iconFilename);
      }
    }
    // Effect
    Integer effectId=(Integer)props.getProperty("Perk_Effect");
    if (effectId!=null)
    {
      Effect effect=_effectsLoader.getEffect(effectId.intValue());
      ret.setEffect(effect);
    }
    // UI category
    Integer uiCategoryCode=(Integer)props.getProperty("Perk_UICategory");
    if (uiCategoryCode!=null)
    {
      PerkUICategory category=_uiCategoryEnum.getEntry(uiCategoryCode.intValue());
      ret.setUICategory(category);
    }
    // Min Level
    int minLevel=((Integer)props.getProperty("Perk_MinLevel")).intValue();
    ret.setMinLevel(minLevel);
    // Points cost
    Integer pointsCost=(Integer)props.getProperty("Perk_SessionPointCost");
    if (pointsCost!=null)
    {
      ret.setPointsCost(pointsCost.intValue());
    }
    return ret;
  }

  /**
   * Save perks to disk.
   * @param perks Data to save.
   */
  private void savePerks(List<PerkDescription> perks)
  {
    int nbPerks=perks.size();
    LOGGER.info("Writing "+nbPerks+" perks");
    // Write perks file
    boolean ok=PerkDescriptionXMLWriter.write(GeneratedFiles.PERKS,perks);
    if (ok)
    {
      LOGGER.info("Wrote perks file: "+GeneratedFiles.PERKS);
    }
    // Labels
    _i18n.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    EffectLoader effectsLoader=new EffectLoader(facade,new PlacesLoader(facade));
    new MainPerksLoader(facade,effectsLoader).doIt();
    facade.dispose();
  }
}
