package delta.games.lotro.tools.dat.relics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.legendary.relics.Relic;
import delta.games.lotro.lore.items.legendary.relics.RelicsManager;
import delta.games.lotro.lore.relics.melding.MeldingInput;
import delta.games.lotro.lore.relics.melding.MeldingOutput;
import delta.games.lotro.lore.relics.melding.RelicMeldingRecipe;
import delta.games.lotro.lore.relics.melding.io.xml.MeldingRecipesXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Get relic melding recipes from DAT files.
 * @author DAM
 */
public class MainDatRelicMeldingRecipesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatRelicMeldingRecipesLoader.class);

  private DataFacade _facade;
  private EnumMapper _categories;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatRelicMeldingRecipesLoader(DataFacade facade)
  {
    _facade=facade;
    _categories=_facade.getEnumsManager().getEnumMapper(587203362);
  }

  @SuppressWarnings("unchecked")
  private RelicMeldingRecipe loadRelicMeldingRecipe(int id)
  {
    PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      LOGGER.warn("Could not load properties: "+id);
      return null;
    }
    WStateDataSet wstate=_facade.loadWState(id);
    ClassInstance recipeWState=getObjectFromWState(wstate);
    if (recipeWState==null)
    {
      LOGGER.warn("Could not load recipe wstate: "+id);
      return null;
    }
    RelicMeldingRecipe ret=new RelicMeldingRecipe(id);
    // Name override
    String nameOverride=(String)properties.getProperty("Relic_Transmutation_Recipe_Output_Title_Override");
    ret.setNameOverride(nameOverride);
    // Icon override
    Integer iconId=(Integer)properties.getProperty("Relic_Transmutation_Recipe_Output_Icon_Override");
    if (iconId!=null)
    {
      ret.setIconOverride(iconId.intValue());
    }
    // Tooltip override
    String tooltipOverride=(String)properties.getProperty("Relic_Transmutation_Recipe_Output_Tooltip_Override");
    ret.setTooltipOverride(tooltipOverride);
    // Category
    Integer categoryCode=(Integer)properties.getProperty("Relic_Transmutation_Recipe_TransmutationCategory");
    if (categoryCode!=null)
    {
      String category=_categories.getLabel(categoryCode.intValue());
      ret.setCategory(category);
    }
    // Cost
    int cost=((Integer)properties.getProperty("Relic_Transmutation_Recipe_RelicCurrencyCost")).intValue();
    ret.setCost(cost);
    // Input
    MeldingInput input=ret.getInput();
    // - tiers
    Map<Integer,Integer> tiersMap=(Map<Integer,Integer>)recipeWState.getAttributeValue("198532131");
    for(Map.Entry<Integer,Integer> entry : tiersMap.entrySet())
    {
      input.setTierCount(entry.getKey().intValue(),entry.getValue().intValue());
    }
    // - specific relics
    Map<Integer,Integer> relicsMap=(Map<Integer,Integer>)recipeWState.getAttributeValue("48561763");
    for(Map.Entry<Integer,Integer> entry : relicsMap.entrySet())
    {
      int relicId=entry.getKey().intValue();
      Relic relic=RelicsManager.getInstance().getById(relicId);
      if (relic!=null)
      {
        int count=entry.getValue().intValue();
        input.addNeededRelic(relic,count);
      }
      else
      {
        LOGGER.warn("Relic not found: "+relicId);
      }
    }

    // Output
    MeldingOutput output=ret.getOutput();
    Map<Integer,Integer> resultsMap=(Map<Integer,Integer>)recipeWState.getAttributeValue("141263172");
    
    for(Map.Entry<Integer,Integer> entry : resultsMap.entrySet())
    {
      int weight=entry.getValue().intValue();
      int outputId=entry.getKey().intValue();
      Relic relic=RelicsManager.getInstance().getById(outputId);
      if (relic!=null)
      {
        output.addOutput(relic,weight);
      }
      else
      {
        Item item=ItemsManager.getInstance().getItem(outputId);
        if (item!=null)
        {
          output.addOutput(item,weight);
        }
        else
        {
          LOGGER.warn("Output ID not found: "+outputId);
        }
      }
    }
    return ret;
  }

  private ClassInstance getObjectFromWState(WStateDataSet wstate)
  {
    if (wstate==null)
    {
      return null;
    }
    Object ret=null;
    List<Integer> orphanRefs=wstate.getOrphanReferences();
    if (orphanRefs.size()==1)
    {
      ret=wstate.getValueForReference(orphanRefs.get(0).intValue());
    }
    if (ret instanceof ClassInstance)
    {
      return (ClassInstance)ret;
    }
    return null;
  }

  /**
   * Load relic melding recipes.
   */
  public void doIt()
  {
    // Relic Transmutation Directory:
    PropertiesSet props=_facade.loadProperties(1879201331+DATConstants.DBPROPERTIES_OFFSET);
    if (props==null)
    {
      LOGGER.warn("Could not load transmutation directory properties");
    }
    List<RelicMeldingRecipe> recipes=new ArrayList<RelicMeldingRecipe>();
    // Recipe IDs
    Object[] ids=(Object[])props.getProperty("RelicTransmutationDirectory_RecipeList");
    for(Object id : ids)
    {
      int recipeId=((Integer)id).intValue();
      RelicMeldingRecipe recipe=loadRelicMeldingRecipe(recipeId);
      if (recipe!=null)
      {
        recipes.add(recipe);
      }
    }
    // Write result file
    MeldingRecipesXMLWriter writer=new MeldingRecipesXMLWriter();
    boolean ok=writer.write(GeneratedFiles.RELIC_MELDING_RECIPES,recipes,EncodingNames.UTF_8);
    if (ok)
    {
      LOGGER.info("Wrote relic melding recipes file: "+GeneratedFiles.RELIC_MELDING_RECIPES);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatRelicMeldingRecipesLoader(facade).doIt();
    facade.dispose();
  }
}
