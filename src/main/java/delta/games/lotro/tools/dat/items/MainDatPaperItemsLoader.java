package delta.games.lotro.tools.dat.items;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.lore.items.paper.PaperItem;
import delta.games.lotro.lore.items.paper.io.xml.PaperItemsXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get paper items details from DAT files.
 * @author DAM
 */
public class MainDatPaperItemsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatPaperItemsLoader.class);

  private DataFacade _facade;
  private EnumMapper _itemClass;
  private EnumMapper _paperItemCategory;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatPaperItemsLoader(DataFacade facade)
  {
    _facade=facade;
    _itemClass=_facade.getEnumsManager().getEnumMapper(587202614);
    _paperItemCategory=_facade.getEnumsManager().getEnumMapper(587203350);
  }

  /**
   * Handle a paper item.
   * @param itemProps Paper item properties.
   * @param free Indicates if this papaer item goes into the basic wallet or not.
   * @return the loaded paper item.
   */
  private PaperItem handleItem(PropertiesSet itemProps, boolean free)
  {
    Integer itemId=(Integer)itemProps.getProperty("PaperItemControl_Item");
    //System.out.println("Paper item: "+itemId+", free="+free);
    // Inspect
    PaperItem paperItem=inspectItem(itemId.intValue());
    // Free
    paperItem.setFree(free);
    // Cap (optional)
    PropertiesSet baseCapProps=(PropertiesSet)itemProps.getProperty("PaperItemControl_BaseCap");
    if (baseCapProps!=null)
    {
      Integer capDefault=(Integer)baseCapProps.getProperty("PaperItemControl_CapDefault");
      if (capDefault!=null)
      {
        //System.out.println("Cap: "+capDefault);
        /*
        Integer hardCap=(Integer)itemProps.getProperty("PaperItemControl_TreatBaseCapAsHardCap");
        if ((hardCap!=null) && (hardCap.intValue()!=0))
        {
          System.out.println("Hard cap!");
        }
        */
        paperItem.setCap(capDefault);
      }
    }
    return paperItem;
  }

  private PaperItem inspectItem(int itemId)
  {
    PaperItem ret=new PaperItem(itemId);
    PropertiesSet itemProps=_facade.loadProperties(itemId+DATConstants.DBPROPERTIES_OFFSET);
    //System.out.println(itemProps.dump());
    String name=DatUtils.getStringProperty(itemProps,"Name");
    name=DatStringUtils.fixName(name);
    ret.setName(name);
    Integer isBarter=(Integer)itemProps.getProperty("Barter_IsBarterItem");
    if ((isBarter==null) || (isBarter.intValue()!=1))
    {
      LOGGER.warn("Is barter is: "+isBarter+" for item ID="+itemId);
    }
    Integer itemClass=(Integer)itemProps.getProperty("Item_Class");
    String itemClassName=_itemClass.getLabel(itemClass.intValue());
    ret.setItemClass(itemClassName);
    Integer paperItemCategoryCode=(Integer)itemProps.getProperty("PaperItem_Category");
    String paperItemCategory=_paperItemCategory.getLabel(paperItemCategoryCode.intValue());
    ret.setCategory(paperItemCategory);
    Integer smallIconId=(Integer)itemProps.getProperty("PaperItem_SmallIcon");
    if (smallIconId!=null)
    {
      ret.setIconId(smallIconId);
    }
    // Account shared?
    boolean accountShared=false;
    Integer boa=(Integer)itemProps.getProperty("Inventory_BindToAccount");
    if ((boa!=null) && (boa.intValue()==1))
    {
      accountShared=true;
    }
    ret.setShared(accountShared);
    //System.out.println("ID="+itemId+", name="+name+", class="+itemClassName+", category="+paperItemCategory+", iconID="+smallIconId+", shared="+accountShared);
    return ret;
  }

  /**
   * Load paper items.
   */
  public void doIt()
  {
    // PaperItemControl
    PropertiesSet props=_facade.loadProperties(0x79024867);

    Map<Integer,PaperItem> paperItems=new HashMap<Integer,PaperItem>();
    // Paper items
    {
      Object[] array=(Object[])props.getProperty("PaperItemControl_PaperItemArray");
      for(Object arrayItem : array)
      {
        PropertiesSet itemProps=(PropertiesSet)arrayItem;
        PaperItem paperItem=handleItem(itemProps,false);
        paperItems.put(Integer.valueOf(paperItem.getIdentifier()),paperItem);
      }
    }
    // "Free" paper items
    {
      Object[] array=(Object[])props.getProperty("PaperItemControl_Free_PaperItemArray");
      for(Object arrayItem : array)
      {
        PropertiesSet itemProps=(PropertiesSet)arrayItem;
        PaperItem paperItem=handleItem(itemProps,true);
        paperItems.put(Integer.valueOf(paperItem.getIdentifier()),paperItem);
      }
    }

    /*
PaperItemControl_VersionArray: 
  #1: 
    PaperItemControl_NewItem: 1879224343
    PaperItemControl_NewItem_QuantityRatio: 1.0
    PaperItemControl_OldItem: 1879155768
    PaperItemControl_VersionTutorial: 237 (Currency)
  ... (91 items)
     */
    {
      Object[] array=(Object[])props.getProperty("PaperItemControl_VersionArray");
      for(Object arrayItem : array)
      {
        PropertiesSet versionProps=(PropertiesSet)arrayItem;
        Integer oldItemId=(Integer)versionProps.getProperty("PaperItemControl_OldItem");
        if (oldItemId!=null)
        {
          PaperItem paperItem=paperItems.get(oldItemId);
          if (paperItem!=null)
          {
            paperItem.setOld(true);
          }
          else
          {
            LOGGER.warn("Old item not found: item ID="+oldItemId);
          }
        }
      }
    }

    LOGGER.info("Writing "+paperItems.size()+" paperItems");
    File to=GeneratedFiles.PAPER_ITEMS;
    List<PaperItem> sortedItems=new ArrayList<PaperItem>(paperItems.values());
    Collections.sort(sortedItems,new IdentifiableComparator<PaperItem>());
    boolean ok=PaperItemsXMLWriter.write(to,sortedItems);
    if (ok)
    {
      System.out.println("Wrote paper items file: "+to);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatPaperItemsLoader(facade).doIt();
    facade.dispose();
  }
}
