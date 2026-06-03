package delta.games.lotro.tools.extraction.utils;

import java.util.BitSet;
import java.util.List;

import delta.games.lotro.common.enums.AgentClass;
import delta.games.lotro.common.enums.Alignment;
import delta.games.lotro.common.enums.Genus;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.Species;
import delta.games.lotro.common.enums.SubSpecies;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.agents.AgentClassification;
import delta.games.lotro.lore.agents.EntityClassification;

/**
 * Utility methods related to mobs.
 * @author DAM
 */
public class MobUtils
{
  private LotroEnum<AgentClass> _agentClassEnum;
  private LotroEnum<Alignment> _alignmentEnum;
  private LotroEnum<Genus> _genusEnum;
  private LotroEnum<Species> _speciesEnum;
  private LotroEnum<SubSpecies> _subSpeciesEnum;
  /**
   * Constructor.
   */
  public MobUtils()
  {
    _agentClassEnum=LotroEnumsRegistry.getInstance().get(AgentClass.class);
    _alignmentEnum=LotroEnumsRegistry.getInstance().get(Alignment.class);
    _genusEnum=LotroEnumsRegistry.getInstance().get(Genus.class);
    _speciesEnum=LotroEnumsRegistry.getInstance().get(Species.class);
    _subSpeciesEnum=LotroEnumsRegistry.getInstance().get(SubSpecies.class);
  }

  /**
   * Build a mob classification from raw properties.
   * @param mobProps Input properties.
   * @return the mob classification.
   */
  public AgentClassification parseClassification(PropertiesSet mobProps)
  {
    AgentClassification ret=new AgentClassification();
    // - Alignment
    Integer alignmentCode=(Integer)mobProps.getProperty("Quest_MonsterAlignment");
    if (alignmentCode!=null)
    {
      Alignment alignment=_alignmentEnum.getEntry(alignmentCode.intValue());
      ret.setAlignment(alignment);
    }
    // Class
    Integer classCode=(Integer)mobProps.getProperty("Quest_MonsterClass");
    if (classCode!=null)
    {
      AgentClass agentClass=_agentClassEnum.getEntry(classCode.intValue());
      ret.setAgentClass(agentClass);
    }
    loadEntityClassification(mobProps,ret.getEntityClassification());
    return ret;
  }

  /**
   * Load a classification from the given properties.
   * @param mobProps Properties.
   * @param classification Storage.
   */
  private void loadEntityClassification(PropertiesSet mobProps, EntityClassification classification)
  {
    // - Genus
    Integer genusCode=(Integer)mobProps.getProperty("Quest_MonsterGenus");
    if (genusCode!=null)
    {
      BitSet bitset=BitSetUtils.getBitSetFromFlags(genusCode.intValue());
      List<Genus> genus=_genusEnum.getFromBitSet(bitset);
      classification.setGenus(genus);
    }
    // - Species
    Integer speciesCode=(Integer)mobProps.getProperty("Quest_MonsterSpecies");
    if (speciesCode!=null)
    {
      Species species=_speciesEnum.getEntry(speciesCode.intValue());
      classification.setSpecies(species);
    }
    // Sub-species
    Integer subSpeciesCode=(Integer)mobProps.getProperty("Quest_MonsterSubspecies");
    if (subSpeciesCode!=null)
    {
      SubSpecies subSpecies=_subSpeciesEnum.getEntry(subSpeciesCode.intValue());
      classification.setSubSpecies(subSpecies);
    }
  }
}
