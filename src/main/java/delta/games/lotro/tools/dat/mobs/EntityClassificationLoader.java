package delta.games.lotro.tools.dat.mobs;

import java.util.BitSet;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.misc.EntityClassification;

/**
 * EntityClassification loader.
 * @author DAM
 */
public class EntityClassificationLoader
{
  // TODO Add alignment and class
  //private EnumMapper _alignment;
  //private EnumMapper _class;
  private EnumMapper _genus;
  private EnumMapper _species;
  private EnumMapper _subSpecies;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public EntityClassificationLoader(DataFacade facade)
  {
    //_alignment=facade.getEnumsManager().getEnumMapper(587202573);
    //_class=facade.getEnumsManager().getEnumMapper(587202574);
    _genus=facade.getEnumsManager().getEnumMapper(587202570);
    _species=facade.getEnumsManager().getEnumMapper(587202571);
    _subSpecies=facade.getEnumsManager().getEnumMapper(587202572);
  }

  /**
   * Load classification from properties.
   * @param props Properties to use.
   * @param storage Storage for loaded data.
   */
  public void loadSpecification(PropertiesSet props, EntityClassification storage)
  {
    // Alignment
    /*
    int alignmentCode=((Integer)cosmeticEntityProps.getProperty("Agent_Alignment")).intValue();
    String alignment=_alignment.getString(alignmentCode);
    System.out.println("Alignment: "+alignment);
    */
    // Class
    /*
    int classCode=((Integer)cosmeticEntityProps.getProperty("Agent_Class")).intValue();
    String className=_class.getString(classCode);
    System.out.println("Class: "+className);
    */
    // Genus
    int genusCode=((Integer)props.getProperty("Agent_Genus")).intValue();
    BitSet genusBitSet=BitSetUtils.getBitSetFromFlags(genusCode);
    String genuses=BitSetUtils.getStringFromBitSet(genusBitSet,_genus,"/");
    storage.setGenus(genuses);
    // Species
    int speciesCode=((Integer)props.getProperty("Agent_Species")).intValue();
    String species=_species.getString(speciesCode);
    storage.setSpecies(species);
    // Sub-species
    Integer showSubSpecies=(Integer)props.getProperty("Agent_ShowSubspecies");
    if ((showSubSpecies!=null) && (showSubSpecies.intValue()!=0))
    {
      int subSpeciesCode=((Integer)props.getProperty("Agent_Subspecies")).intValue();
      String subSpecies=_subSpecies.getString(subSpeciesCode);
      storage.setSubSpecies(subSpecies);
    }
  }
}
