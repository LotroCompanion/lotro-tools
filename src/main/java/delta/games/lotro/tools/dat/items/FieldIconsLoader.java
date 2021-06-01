package delta.games.lotro.tools.dat.items;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Get icons for field items.
 * @author DAM
 */
public class FieldIconsLoader
{
  private static final Logger LOGGER=Logger.getLogger(FieldIconsLoader.class);

  private DataFacade _facade;
  private HashMap<Integer,Item> _mapById;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param mapById Map of item, by ID.
   */
  public FieldIconsLoader(DataFacade facade, HashMap<Integer,Item> mapById)
  {
    _facade=facade;
    _mapById=mapById;
  }

  private void handleRecipe(int indexDataId)
  {
    int dbPropertiesId=indexDataId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      // Results
      handleVersion(properties);
      // Multiple output results
      Object[] multiOutput=(Object[])properties.getProperty("CraftRecipe_MultiOutputArray");
      if (multiOutput!=null)
      {
        for(Object output : multiOutput)
        {
          PropertiesSet outputProps=(PropertiesSet)output;

          // Patch
          // - result
          Integer resultId=(Integer)outputProps.getProperty("CraftRecipe_ResultItem");
          if ((resultId!=null) && (resultId.intValue()>0))
          {
            Integer iconId=(Integer)properties.getProperty("CraftRecipe_Field_ResultIcon");
            handleIcon(resultId.intValue(),iconId);
          }
          // - critical result
          Integer critResultId=(Integer)outputProps.getProperty("CraftRecipe_CriticalResultItem");
          if ((critResultId!=null) && (critResultId.intValue()>0))
          {
            Integer iconId=(Integer)properties.getProperty("CraftRecipe_Field_CritResultIcon");
            handleIcon(critResultId.intValue(),iconId);
          }
        }
      }
    }
    else
    {
      LOGGER.warn("Could not handle recipe ID="+indexDataId);
    }
  }

  private void handleVersion(PropertiesSet properties)
  {
    // Regular result
    {
      Integer resultId=(Integer)properties.getProperty("CraftRecipe_ResultItem");
      if (resultId!=null)
      {
        Integer iconId=(Integer)properties.getProperty("CraftRecipe_Field_ResultIcon");
        handleIcon(resultId.intValue(),iconId);
      }
    }
    // Critical result
    Integer criticalResultId=(Integer)properties.getProperty("CraftRecipe_CriticalResultItem");
    if ((criticalResultId!=null) && (criticalResultId.intValue()>0))
    {
      Integer iconId=(Integer)properties.getProperty("CraftRecipe_Field_CritResultIcon");
      handleIcon(criticalResultId.intValue(),iconId);
    }
  }

  private void resolveIcon(int iconId)
  {
    File iconFile=new File(GeneratedFiles.ITEM_ICONS_DIR,iconId+".png").getAbsoluteFile();
    if (!iconFile.exists())
    {
      DatIconsUtils.buildImageFile(_facade,iconId,iconFile);
    }
  }

  /**
   * Iterate on recipes.
   */
  public void doIt()
  {
    for(int i=0x70000000;i<=0x77FFFFFF;i++)
    {
      byte[] data=_facade.loadData(i);
      if (data!=null)
      {
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        if (classDefIndex==1024)
        {
          handleRecipe(i);
        }
      }
    }
  }

  private void handleIcon(int itemId, Integer iconId)
  {
    if ((iconId!=null) && (iconId.intValue()>0))
    {
      resolveIcon(iconId.intValue());
      Item item=_mapById.get(Integer.valueOf(itemId));
      if ((item!=null) && (item.getIcon()==null))
      {
        item.setIcon(String.valueOf(iconId));
      }
    }
  }
}
