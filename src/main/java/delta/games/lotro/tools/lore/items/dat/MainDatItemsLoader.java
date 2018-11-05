package delta.games.lotro.tools.lore.items.dat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemBinding;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.ItemSturdiness;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.io.xml.ItemXMLWriter;

/**
 * Get item definitions from DAT files.
 * @author DAM
 */
public class MainDatItemsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatItemsLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatItemsLoader(DataFacade facade)
  {
    _facade=facade;
  }

  private Item load(int indexDataId)
  {
    Item item=null;
    int dbPropertiesId=indexDataId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      //FileIO.writeFile(new File(indexDataId+".props"),properties.dump().getBytes());
      //System.out.println(properties.dump());
      Integer weenieType=(Integer)properties.getProperty("WeenieType");
      item=buildItemFromWeenieType(weenieType.intValue());
      // ID
      item.setIdentifier(indexDataId);
      // Name
      String name=getStringProperty(properties,"Name");
      item.setName(name);
      // Icon
      Integer iconId=(Integer)properties.getProperty("Icon_Layer_ImageDID");
      Integer backgroundIconId=(Integer)properties.getProperty("Icon_Layer_BackgroundDID");
      item.setIcon(iconId+"-"+backgroundIconId);
      // Level
      Integer level=(Integer)properties.getProperty("Item_Level");
      item.setItemLevel(level);
      // Binding
      item.setBinding(getBinding(properties));
      // Durability
      Integer durability=(Integer)properties.getProperty("Item_MaxStructurePoints");
      item.setDurability(durability);
      // Quality
      Integer quality=(Integer)properties.getProperty("Item_Quality");
      if (quality!=null)
      {
        item.setQuality(getQuality(quality.intValue()));
      }
      // Armour value
      Integer armourValue=(Integer)properties.getProperty("Item_Armor_Value");
      if (armourValue!=null)
      {
        if (item instanceof Armour)
        {
          ((Armour)item).setArmourValue(armourValue.intValue());
        }
        else
        {
          //item.getStats().setStat(STAT.ARMOUR,armourValue.intValue());
        }
      }
      Integer durabilityEnum=(Integer)properties.getProperty("Item_DurabilityEnum");
      if (durabilityEnum!=null)
      {
        item.setSturdiness(getSturdiness(durabilityEnum.intValue()));
      }
      /*
Item_MaterialType: 7
Item_MaxStructurePoints: 30
Item_MaxStructurePointsEnum: 3
Item_RepairCostPerPointProgression: 1879086445
Item_Value: 160
Item_ValueLookupTable: 1879049295
Item_WearState: 2
       */
    }
    else
    {
      LOGGER.warn("Could not handle item ID="+indexDataId);
    }
    return item;
  }

  private Item buildItemFromWeenieType(int weenieType)
  {
    if (weenieType==0x30081) return new Armour(); // Clothing
    if (weenieType==0x40081) return new Armour(); // Armor
    if (weenieType==0x20081) return new Weapon(); // Weapon
    //System.out.println(weenieType);
    return new Item();
  }

  private ItemQuality getQuality(int qualityEnum)
  {
    if (qualityEnum==1) return ItemQuality.LEGENDARY;
    if (qualityEnum==2) return ItemQuality.RARE;
    if (qualityEnum==3) return ItemQuality.INCOMPARABLE;
    if (qualityEnum==4) return ItemQuality.UNCOMMON;
    if (qualityEnum==5) return ItemQuality.COMMON;
    return null;
  }

  private ItemSturdiness getSturdiness(int durabilityEnum)
  {
    //{0=Undef, 1=Substantial, 2=Brittle, 3=Normal, 4=Tough, 5=Flimsy, 6=, 7=Weak}
    //if (durabilityEnum==0) return ItemSturdiness.UNDEFINED;
    if (durabilityEnum==1) return ItemSturdiness.SUBSTANTIAL;
    if (durabilityEnum==2) return ItemSturdiness.BRITTLE;
    if (durabilityEnum==3) return ItemSturdiness.NORMAL;
    if (durabilityEnum==4) return ItemSturdiness.TOUGH;
    //if (durabilityEnum==5) return ItemSturdiness.FLIMSY;
    //if (durabilityEnum==6) return ???;
    if (durabilityEnum==7) return ItemSturdiness.WEAK;
    return null;
  }

  private ItemBinding getBinding(PropertiesSet properties)
  {
    Integer bindOnAcquire=(Integer)properties.getProperty("Inventory_BindOnAcquire");
    if ((bindOnAcquire!=null) && (bindOnAcquire.intValue()==1)) return ItemBinding.BIND_ON_ACQUIRE;
    Integer bindOnEquip=(Integer)properties.getProperty("Inventory_BindOnEquip");
    if ((bindOnEquip!=null) && (bindOnEquip.intValue()==1)) return ItemBinding.BIND_ON_EQUIP;
    return null;
  }

  private String getStringProperty(PropertiesSet properties, String propertyName)
  {
    String ret=null;
    Object value=properties.getProperty(propertyName);
    if (value!=null)
    {
      if (value instanceof String[])
      {
        ret=((String[])value)[0];
      }
    }
    return ret;
  }

  private void doIt()
  {
    List<Item> items=new ArrayList<Item>();
    ItemsManager itemsManager=ItemsManager.getInstance();
    List<Item> refItems=itemsManager.getAllItems();
    int nbTotal=refItems.size();
    //int[] itemIds=new int[]{1879049233,1879049248};
    for(int i=0;i<nbTotal;i++)
    //for(int i=0;i<itemIds.length;i++)
    {
      int id=refItems.get(i).getIdentifier();
      //int id=itemIds[i];
      Item newItem=load(id);
      if (newItem!=null)
      {
        items.add(newItem);
      }
    }
    // Write result files
    File toFile=new File("../lotro-companion/data/lore/items_dat.xml").getAbsoluteFile();
    ItemXMLWriter.writeItemsFile(toFile,items);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatItemsLoader(facade).doIt();
    facade.dispose();
  }
}
