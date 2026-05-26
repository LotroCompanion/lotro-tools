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
  /**
   * Build a mob classification from raw properties.
   * @param props Input properties.
   * @return the mob classification.
   */
  public static AgentClassification buildClassification(PropertiesSet props)
  {
    AgentClassification ret=new AgentClassification();
    LotroEnumsRegistry enumsRegistry=LotroEnumsRegistry.getInstance();
    // - class
    LotroEnum<AgentClass> classEnum=enumsRegistry.get(AgentClass.class);
    Integer classId=(Integer)props.getProperty("Quest_MonsterClass");
    if (classId!=null)
    {
      AgentClass monsterClass=classEnum.getEntry(classId.intValue());
      ret.setAgentClass(monsterClass);
    }
    // - alignment
    LotroEnum<Alignment> alignmentEnum=enumsRegistry.get(Alignment.class);
    Integer alignmentId=(Integer)props.getProperty("Quest_MonsterAlignment");
    if (alignmentId!=null)
    {
      Alignment alignment=alignmentEnum.getEntry(alignmentId.intValue());
      ret.setAlignment(alignment);
    }
    loadEntityClassification(props,ret.getEntityClassification());
    return ret;
  }

  /**
   * Load a mob reference from the given properties.
   * @param mobProps Properties.
   * @param mobRef Storage.
   */
  private static void loadEntityClassification(PropertiesSet mobProps, EntityClassification mobRef)
  {
    LotroEnumsRegistry enumsRegistry=LotroEnumsRegistry.getInstance();
    // - subspecies
    LotroEnum<SubSpecies> subSpeciesEnum=enumsRegistry.get(SubSpecies.class);
    Integer subSpeciesId=(Integer)mobProps.getProperty("Quest_MonsterSubspecies");
    SubSpecies subSpecies=null;
    if (subSpeciesId!=null)
    {
      subSpecies=subSpeciesEnum.getEntry(subSpeciesId.intValue());
      mobRef.setSubSpecies(subSpecies);
    }
    // - species
    LotroEnum<Species> speciesEnum=enumsRegistry.get(Species.class);
    Integer speciesId=(Integer)mobProps.getProperty("Quest_MonsterSpecies");
    Species species=null;
    if (speciesId!=null)
    {
      species=speciesEnum.getEntry(speciesId.intValue());
      mobRef.setSpecies(species);
    }
    // - genus
    LotroEnum<Genus> genusEnum=enumsRegistry.get(Genus.class);
    Integer genusId=(Integer)mobProps.getProperty("Quest_MonsterGenus");
    if (genusId!=null)
    {
      BitSet bitset=BitSetUtils.getBitSetFromFlags(genusId.intValue());
      List<Genus> genus=genusEnum.getFromBitSet(bitset);
      mobRef.setGenus(genus);
    }
  }
}
