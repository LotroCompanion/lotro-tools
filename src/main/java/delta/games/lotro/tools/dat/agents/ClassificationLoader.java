package delta.games.lotro.tools.dat.agents;

import java.util.BitSet;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.agents.AgentClassification;
import delta.games.lotro.lore.agents.EntityClassification;
import delta.games.lotro.utils.StringUtils;

/**
 * EntityClassification loader.
 * @author DAM
 */
public class ClassificationLoader
{
  private EnumMapper _alignment;
  private EnumMapper _class;
  private EnumMapper _classFilter;
  private EnumMapper _genus;
  private EnumMapper _species;
  private EnumMapper _subSpecies;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public ClassificationLoader(DataFacade facade)
  {
    _alignment=facade.getEnumsManager().getEnumMapper(587202573);
    _class=facade.getEnumsManager().getEnumMapper(587202574);
    _classFilter=facade.getEnumsManager().getEnumMapper(587202575);
    _genus=facade.getEnumsManager().getEnumMapper(587202570);
    _species=facade.getEnumsManager().getEnumMapper(587202571);
    _subSpecies=facade.getEnumsManager().getEnumMapper(587202572);
  }

  /**
   * Load classification from properties.
   * @param props Properties to use.
   * @param storage Storage for loaded data.
   */
  public void loadClassification(PropertiesSet props, AgentClassification storage)
  {
    // Alignment
    int alignmentCode=((Integer)props.getProperty("Agent_Alignment")).intValue();
    String alignment=_alignment.getString(alignmentCode);
    storage.setAlignment(alignment);
    // Class
    int classCode=((Integer)props.getProperty("Agent_Class")).intValue();
    String className=_class.getString(classCode);
    storage.setAgentClass(className);
    // Class filter
    int classFilterCode=((Integer)props.getProperty("Agent_ClassificationFilter")).intValue();
    String classFilter=_classFilter.getString(classFilterCode);
    storage.setClassificationFilter(classFilter);
    loadSpecification(props,storage.getEntityClassification());
  }

  /**
   * Load classification from properties.
   * @param props Properties to use.
   * @param storage Storage for loaded data.
   */
  public void loadSpecification(PropertiesSet props, EntityClassification storage)
  {
    // Genus
    int genusCode=((Integer)props.getProperty("Agent_Genus")).intValue();
    BitSet genusBitSet=BitSetUtils.getBitSetFromFlags(genusCode);
    String genuses=BitSetUtils.getStringFromBitSet(genusBitSet,_genus,"/");
    genuses=StringUtils.fixName(genuses);
    storage.setGenus(genuses);
    // Species
    int speciesCode=((Integer)props.getProperty("Agent_Species")).intValue();
    String species=_species.getString(speciesCode);
    species=StringUtils.fixName(species);
    storage.setSpecies(species);
    // Sub-species
    Integer showSubSpecies=(Integer)props.getProperty("Agent_ShowSubspecies");
    if ((showSubSpecies!=null) && (showSubSpecies.intValue()!=0))
    {
      int subSpeciesCode=((Integer)props.getProperty("Agent_Subspecies")).intValue();
      String subSpecies=_subSpecies.getString(subSpeciesCode);
      subSpecies=StringUtils.fixName(subSpecies);
      storage.setSubSpecies(subSpecies);
    }
  }
}
