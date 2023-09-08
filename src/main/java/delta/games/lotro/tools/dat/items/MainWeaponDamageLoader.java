package delta.games.lotro.tools.dat.items;

import java.util.BitSet;
import java.util.List;

import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.weapons.WeaponDamageManager;
import delta.games.lotro.lore.items.weapons.io.xml.WeaponDamageXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DataFacadeBuilder;
import delta.games.lotro.tools.dat.utils.WeenieContentDirectory;

/**
 * Loader for weapon damage data.
 * @author DAM
 */
public class MainWeaponDamageLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainWeaponDamageLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    WeaponDamageManager mgr=load();
    WeaponDamageXMLWriter.writeWeaponDamageFile(GeneratedFiles.WEAPON_DAMAGE,mgr);
  }

  private WeaponDamageManager load()
  {
    WeaponDamageManager ret=new WeaponDamageManager();
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"WeaponDamageVarianceTable");
    Object[] entries=(Object[])props.getProperty("Item_WeaponVarianceTable");
    for(Object entryObj : entries)
    {
      PropertiesSet entryProps=(PropertiesSet)entryObj;
      // Weapon types
      Long equipmentCategoryValue=(Long)entryProps.getProperty("Item_WeaponVarianceEquipmentCategory");
      BitSet bitset=BitSetUtils.getBitSetFromFlags(equipmentCategoryValue.longValue());
      LotroEnum<WeaponType> weaponTypesEnum=LotroEnumsRegistry.getInstance().get(WeaponType.class);
      List<WeaponType> weaponTypes=weaponTypesEnum.getFromBitSet(bitset);
      // Variance
      Float variance=(Float)entryProps.getProperty("Item_WeaponVarianceValue");
      if (variance==null)
      {
        continue;
      }
      for(WeaponType type : weaponTypes)
      {
        ret.setVariance(type,variance.floatValue());
      }
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
    new MainWeaponDamageLoader(facade).doIt();
    facade.dispose();
  }
}
