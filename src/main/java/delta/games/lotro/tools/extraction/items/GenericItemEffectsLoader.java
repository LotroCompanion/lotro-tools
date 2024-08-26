package delta.games.lotro.tools.extraction.items;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.enums.EquipmentCategory;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.items.effects.GenericItemEffects;
import delta.games.lotro.lore.items.effects.io.xml.GenericItemEffectsXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.extraction.effects.EffectLoader;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

/**
 * Loader for generic item effects.
 * @author DAM
 */
public class GenericItemEffectsLoader
{
  private DataFacade _facade;
  private EffectLoader _effectsLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param effectsLoader Effects loader.
   */
  public GenericItemEffectsLoader(DataFacade facade, EffectLoader effectsLoader)
  {
    _facade=facade;
    _effectsLoader=effectsLoader;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    List<GenericItemEffects> allEffects=new ArrayList<GenericItemEffects>();
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"InventoryControl");
    // Equipper effects
    handleEquipperEffects(props,allEffects);
    // TODO Handle target effects
    //handleTargetEffects(props,allEffects);
    // Save
    save(allEffects);
  }

  private void handleEquipperEffects(PropertiesSet props, List<GenericItemEffects> allEffects)
  {
    Object[] propsArray=(Object[])props.getProperty("InventoryControl_DefaultEquipperEffectList");
    for(Object propsEntry : propsArray)
    {
      PropertiesSet entryProps=(PropertiesSet)propsEntry;
      GenericItemEffects effects=handleEquipperEntry(entryProps);
      allEffects.add(effects);
    }
  }

  private GenericItemEffects handleEquipperEntry(PropertiesSet props)
  {
/*
  #1: InventoryControl_DefaultEquipperEffectData
    EffectGenerator_EquipperEffectList:
      #1: EffectGenerator_EffectStruct
        EffectGenerator_EffectID: 1879049208
    InventoryControl_EquipmentCategory: 8192 (Crossbow[E])
 */
    // Category
    long categoryBits=((Long)props.getProperty("InventoryControl_EquipmentCategory")).longValue();
    BitSet bitset=BitSetUtils.getBitSetFromFlags(categoryBits);
    int index=bitset.nextSetBit(0);
    LotroEnum<EquipmentCategory> categoryEnum=LotroEnumsRegistry.getInstance().get(EquipmentCategory.class);
    EquipmentCategory category=categoryEnum.getEntry(index+1);
    GenericItemEffects ret=new GenericItemEffects(category);
    // Effects
    Object[] effectsList=(Object[])props.getProperty("EffectGenerator_EquipperEffectList");
    for(Object effectEntry : effectsList)
    {
      PropertiesSet effectProps=(PropertiesSet)effectEntry;
      int effectID=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
      Effect effect=_effectsLoader.getEffect(effectID);
      ret.addEffect(effect);
    }
    return ret;
  }

  void handleTargetEffects(PropertiesSet props, List<GenericItemEffects> allEffects)
  {
    Object[] propsArray=(Object[])props.getProperty("InventoryControl_DefaultTargetEffectList");
    for(Object propsEntry : propsArray)
    {
      PropertiesSet entryProps=(PropertiesSet)propsEntry;
      GenericItemEffects effects=handleTargetEntry(entryProps);
      allEffects.add(effects);
    }
  }

  private GenericItemEffects handleTargetEntry(PropertiesSet props)
  {
/*
  #1: InventoryControl_DefaultTargetEffectData
    EffectGenerator_TargetEffectList:
      #1: EffectGenerator_EffectStruct
        EffectGenerator_EffectID: 1879049208
    InventoryControl_EquipmentCategory: 8192 (Crossbow[E])
 */
    // Category
    long categoryBits=((Long)props.getProperty("InventoryControl_EquipmentCategory")).longValue();
    BitSet bitset=BitSetUtils.getBitSetFromFlags(categoryBits);
    int index=bitset.nextSetBit(0);
    LotroEnum<EquipmentCategory> categoryEnum=LotroEnumsRegistry.getInstance().get(EquipmentCategory.class);
    EquipmentCategory category=categoryEnum.getEntry(index+1);
    GenericItemEffects ret=new GenericItemEffects(category);
    // Effects
    Object[] effectsList=(Object[])props.getProperty("EffectGenerator_TargetEffectList");
    for(Object effectEntry : effectsList)
    {
      PropertiesSet effectProps=(PropertiesSet)effectEntry;
      int effectID=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
      Effect effect=_effectsLoader.getEffect(effectID);
      ret.addEffect(effect);
    }
    return ret;
  }

  private void save(List<GenericItemEffects> allEffects)
  {
    GenericItemEffectsXMLWriter.write(GeneratedFiles.GENERIC_ITEM_EFFECTS,allEffects);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    PlacesLoader placesLoader=new PlacesLoader(facade);
    EffectLoader loader=new EffectLoader(facade,placesLoader);
    new GenericItemEffectsLoader(facade,loader).doIt();
  }
}
