package delta.games.lotro.tools.characters.dat;

import java.io.File;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.utils.dat.DatIconsUtils;
import delta.games.lotro.tools.utils.dat.DatUtils;

/**
 * Traits loader.
 * @author DAM
 */
public class TraitLoader
{
  /**
   * Load a trait.
   * @param facade Data facade.
   * @param traitId Trait identifier.
   */
  public static void loadTrait(DataFacade facade, int traitId)
  {
    PropertiesSet traitProperties=facade.loadProperties(0x9000000+traitId);
    System.out.println("*********** Trait: "+traitId+" ****************");
    String traitName=DatUtils.getStringProperty(traitProperties,"Trait_Name");
    Integer iconId=(Integer)traitProperties.getProperty("Trait_Icon");
    Integer minLevel=(Integer)traitProperties.getProperty("Trait_Minimum_Level");
    traitName=traitName.replace(":","-");
    traitName=traitName.replace("/",";");
    String traitIconFile=traitName+".png";
    File to=new File("traits/"+traitIconFile).getAbsoluteFile();
    if (!to.exists())
    {
      DatIconsUtils.buildImageFile(facade,iconId.intValue(),to);
    }
    System.out.println("Trait name: "+traitName+" (min level="+minLevel+")");
  }
}
