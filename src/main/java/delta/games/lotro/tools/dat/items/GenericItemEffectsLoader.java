package delta.games.lotro.tools.dat.items;

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
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.DataFacadeBuilder;
import delta.games.lotro.tools.dat.utils.WeenieContentDirectory;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Loader for generic item effects.
 * @author DAM
 */
public class GenericItemEffectsLoader
{
  private DataFacade _facade;
  private DatStatUtils _statUtils;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public GenericItemEffectsLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("genericItemEffects",facade.getGlobalStringsManager());
    _statUtils=new DatStatUtils(facade,_i18n);
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    List<GenericItemEffects> allEffects=new ArrayList<GenericItemEffects>();
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"InventoryControl");
    Object[] propsArray=(Object[])props.getProperty("InventoryControl_DefaultEquipperEffectList");
    for(Object propsEntry : propsArray)
    {
      PropertiesSet entryProps=(PropertiesSet)propsEntry;
      GenericItemEffects effects=handleEntry(entryProps);
      allEffects.add(effects);
    }
    // Save
    GenericItemEffectsXMLWriter.write(GeneratedFiles.GENERIC_ITEM_EFFECTS,allEffects);
    _i18n.save();
  }

  private GenericItemEffects handleEntry(PropertiesSet props)
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
    //System.out.println("Category: "+category);
    GenericItemEffects ret=new GenericItemEffects(category);
    // Effects
    Object[] effectsList=(Object[])props.getProperty("EffectGenerator_EquipperEffectList");
    for(Object effectEntry : effectsList)
    {
      PropertiesSet effectProps=(PropertiesSet)effectEntry;
      int effectID=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
      Effect effect=DatEffectUtils.loadEffect(_statUtils,effectID,_i18n);
      ret.addEffect(effect);
    }
    return ret;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    new GenericItemEffectsLoader(facade).doIt();
  }
}
