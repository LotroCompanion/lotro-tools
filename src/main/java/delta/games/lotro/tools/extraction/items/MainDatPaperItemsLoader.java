package delta.games.lotro.tools.extraction.items;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.PaperItemCategory;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.paper.PaperItem;
import delta.games.lotro.lore.items.paper.io.xml.PaperItemsXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;

/**
 * Get paper items details from DAT files.
 * @author DAM
 */
public class MainDatPaperItemsLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainDatPaperItemsLoader.class);

  private DataFacade _facade;
  private LotroEnum<PaperItemCategory> _paperItemCategory;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatPaperItemsLoader(DataFacade facade)
  {
    _facade=facade;
    LotroEnumsRegistry registry=LotroEnumsRegistry.getInstance();
    _paperItemCategory=registry.get(PaperItemCategory.class);
  }

  /**
   * Handle a paper item.
   * @param itemProps Paper item properties.
   * @param free Indicates if this paper item goes into the basic wallet or not.
   * @return the loaded paper item.
   */
  private PaperItem handleItem(PropertiesSet itemProps, boolean free)
  {
    Integer itemId=(Integer)itemProps.getProperty("PaperItemControl_Item");
    // Inspect
    PaperItem paperItem=inspectItem(itemId.intValue());
    if (paperItem==null)
    {
      return null;
    }
    // Free
    paperItem.setFree(free);
    // Cap (optional)
    PropertiesSet baseCapProps=(PropertiesSet)itemProps.getProperty("PaperItemControl_BaseCap");
    if (baseCapProps!=null)
    {
      Integer capDefault=(Integer)baseCapProps.getProperty("PaperItemControl_CapDefault");
      if (capDefault!=null)
      {
        paperItem.setCap(capDefault);
        @SuppressWarnings("unused")
        Integer hardCap=(Integer)itemProps.getProperty("PaperItemControl_TreatBaseCapAsHardCap");
      }
    }
    return paperItem;
  }

  private PaperItem inspectItem(int itemId)
  {
    Item item=ItemsManager.getInstance().getItem(itemId);
    if (item==null)
    {
      LOGGER.warn("Unknown item: {}",Integer.valueOf(itemId));
      return null;
    }
    PaperItem ret=new PaperItem(item);
    PropertiesSet itemProps=_facade.loadProperties(itemId+DATConstants.DBPROPERTIES_OFFSET);
    Integer isBarter=(Integer)itemProps.getProperty("Barter_IsBarterItem");
    if ((isBarter==null) || (isBarter.intValue()!=1))
    {
      LOGGER.warn("Is barter is: {} for item={}",isBarter,item);
    }
    Integer paperItemCategoryCode=(Integer)itemProps.getProperty("PaperItem_Category");
    PaperItemCategory paperItemCategory=_paperItemCategory.getEntry(paperItemCategoryCode.intValue());
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
    if (LOGGER.isDebugEnabled())
    {
      String name=ret.getName();
      String itemClassName=ret.getItemClass().getLabel();
      LOGGER.debug("ID={}, name={}, class={}, category={}, iconID={}, shared={}",
          Integer.valueOf(itemId),name,itemClassName,paperItemCategory,smallIconId,Boolean.valueOf(accountShared));
    }
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
        if (paperItem!=null)
        {
          paperItems.put(Integer.valueOf(paperItem.getIdentifier()),paperItem);
        }
      }
    }
    // "Free" paper items
    {
      Object[] array=(Object[])props.getProperty("PaperItemControl_Free_PaperItemArray");
      for(Object arrayItem : array)
      {
        PropertiesSet itemProps=(PropertiesSet)arrayItem;
        PaperItem paperItem=handleItem(itemProps,true);
        if (paperItem!=null)
        {
          paperItems.put(Integer.valueOf(paperItem.getIdentifier()),paperItem);
        }
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
            LOGGER.warn("Old item not found: item ID={}",oldItemId);
          }
        }
      }
    }

    LOGGER.info("Writing {} paperItems",Integer.valueOf(paperItems.size()));
    File to=GeneratedFiles.PAPER_ITEMS;
    List<PaperItem> sortedItems=new ArrayList<PaperItem>(paperItems.values());
    Collections.sort(sortedItems,new IdentifiableComparator<PaperItem>());
    boolean ok=PaperItemsXMLWriter.write(to,sortedItems);
    if (ok)
    {
      LOGGER.info("Wrote paper items file: {}",to);
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
