package delta.games.lotro.tools.dat.agents;

import java.util.BitSet;
import java.util.List;

import delta.games.lotro.common.enums.AgentClass;
import delta.games.lotro.common.enums.Alignment;
import delta.games.lotro.common.enums.ClassificationFilter;
import delta.games.lotro.common.enums.Genus;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.Species;
import delta.games.lotro.common.enums.SubSpecies;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.agents.AgentClassification;
import delta.games.lotro.lore.agents.EntityClassification;

/**
 * EntityClassification loader.
 * @author DAM
 */
public class ClassificationLoader
{
  private LotroEnum<Alignment> _alignment;
  private LotroEnum<AgentClass> _class;
  private LotroEnum<ClassificationFilter> _classFilter;
  private LotroEnum<Genus> _genus;
  private LotroEnum<Species> _species;
  private LotroEnum<SubSpecies> _subSpecies;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public ClassificationLoader(DataFacade facade)
  {
    LotroEnumsRegistry enumsRegistry=LotroEnumsRegistry.getInstance();
    _alignment=enumsRegistry.get(Alignment.class);
    _class=enumsRegistry.get(AgentClass.class);
    _classFilter=enumsRegistry.get(ClassificationFilter.class);
    _genus=enumsRegistry.get(Genus.class);
    _species=enumsRegistry.get(Species.class);
    _subSpecies=enumsRegistry.get(SubSpecies.class);
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
    Alignment alignment=_alignment.getEntry(alignmentCode);
    storage.setAlignment(alignment);
    // Class
    int classCode=((Integer)props.getProperty("Agent_Class")).intValue();
    AgentClass className=_class.getEntry(classCode);
    storage.setAgentClass(className);
    // Class filter
    int classFilterCode=((Integer)props.getProperty("Agent_ClassificationFilter")).intValue();
    ClassificationFilter classFilter=_classFilter.getEntry(classFilterCode);
    storage.setClassificationFilter(classFilter);
    // Entity classification
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
    List<Genus> genuses=_genus.getFromBitSet(genusBitSet);
    storage.setGenus(genuses);
    // Species
    int speciesCode=((Integer)props.getProperty("Agent_Species")).intValue();
    Species species=_species.getEntry(speciesCode);
    storage.setSpecies(species);
    // Sub-species
    Integer showSubSpecies=(Integer)props.getProperty("Agent_ShowSubspecies");
    if ((showSubSpecies!=null) && (showSubSpecies.intValue()!=0))
    {
      int subSpeciesCode=((Integer)props.getProperty("Agent_Subspecies")).intValue();
      SubSpecies subSpecies=_subSpecies.getEntry(subSpeciesCode);
      storage.setSubSpecies(subSpecies);
    }
  }
}
