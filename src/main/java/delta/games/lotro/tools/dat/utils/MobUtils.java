package delta.games.lotro.tools.dat.utils;

import java.util.BitSet;
import java.util.List;

import delta.games.lotro.common.enums.Genus;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.Species;
import delta.games.lotro.common.enums.SubSpecies;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.agents.EntityClassification;

/**
 * Utility methods related to mobs.
 * @author DAM
 */
public class MobUtils
{
  /**
   * Build a mob reference from the given properties.
   * @param mobProps Properties.
   * @return a mob reference or <code>null</code> if data is insufficient.
   */
  public static EntityClassification buildMobReference(PropertiesSet mobProps)
  {
    LotroEnumsRegistry enumsRegistry=LotroEnumsRegistry.getInstance();
    LotroEnum<Genus> genusEnum=enumsRegistry.get(Genus.class);
    LotroEnum<Species> speciesEnum=enumsRegistry.get(Species.class);
    LotroEnum<SubSpecies> subSpeciesEnum=enumsRegistry.get(SubSpecies.class);
    Integer subSpeciesId=(Integer)mobProps.getProperty("Quest_MonsterSubspecies");
    SubSpecies subSpecies=null;
    if (subSpeciesId!=null)
    {
      subSpecies=subSpeciesEnum.getEntry(subSpeciesId.intValue());
    }
    Integer speciesId=(Integer)mobProps.getProperty("Quest_MonsterSpecies");
    Species species=null;
    if (speciesId!=null)
    {
      species=speciesEnum.getEntry(speciesId.intValue());
    }
    Integer genusId=(Integer)mobProps.getProperty("Quest_MonsterGenus");
    EntityClassification mobRef=null;
    if ((genusId!=null) || (species!=null) || (subSpecies!=null))
    {
      mobRef=new EntityClassification();
      if (genusId!=null)
      {
        BitSet bitset=BitSetUtils.getBitSetFromFlags(genusId.intValue());
        List<Genus> genus=genusEnum.getFromBitSet(bitset);
        mobRef.setGenus(genus);
      }
      mobRef.setSpecies(species);
      mobRef.setSubSpecies(subSpecies);
    }
    return mobRef;
  }
}
