package delta.games.lotro.tools.dat.utils;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.common.enums.Genus;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.Species;
import delta.games.lotro.common.enums.SubSpecies;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.agents.EntityClassification;
import delta.games.lotro.utils.StringUtils;

/**
 * Mobs loader.
 * @author DAM
 */
public class MobLoader
{
  //private static final Logger LOGGER=Logger.getLogger(MobLoader.class);

  private Map<Integer,String> _names=new HashMap<Integer,String>();

  private DataFacade _facade;
  private LotroEnum<Genus> _genus;
  private LotroEnum<Species> _species;
  private LotroEnum<SubSpecies> _subSpecies;
  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MobLoader(DataFacade facade)
  {
    _facade=facade;
    LotroEnumsRegistry enumsRegistry=LotroEnumsRegistry.getInstance();
    _genus=enumsRegistry.get(Genus.class);
    _species=enumsRegistry.get(Species.class);
    _subSpecies=enumsRegistry.get(SubSpecies.class);
  }

  /**
   * Load a mob.
   * @param mobId Mob identifier.
   * @return the mob name.
   */
  public String loadMob(int mobId)
  {
    String ret=_names.get(Integer.valueOf(mobId));
    if (ret==null)
    {
      PropertiesSet properties=_facade.loadProperties(mobId+DATConstants.DBPROPERTIES_OFFSET);
      if (properties!=null)
      {
        ret=DatUtils.getStringProperty(properties,"Name");
        ret=StringUtils.fixName(ret);
      }
    }
    return ret;
  }

  /**
   * Build a mob reference from the given properties.
   * @param mobProps Properties.
   * @return a mob reference or <code>null</code> if data is insufficient.
   */
  public EntityClassification buildMobReference(PropertiesSet mobProps)
  {
    Integer subSpeciesId=(Integer)mobProps.getProperty("Quest_MonsterSubspecies");
    SubSpecies subSpecies=null;
    if (subSpeciesId!=null)
    {
      subSpecies=_subSpecies.getEntry(subSpeciesId.intValue());
    }
    Integer speciesId=(Integer)mobProps.getProperty("Quest_MonsterSpecies");
    Species species=null;
    if (speciesId!=null)
    {
      species=_species.getEntry(speciesId.intValue());
    }
    Integer genusId=(Integer)mobProps.getProperty("Quest_MonsterGenus");
    EntityClassification mobRef=null;
    if ((genusId!=null) || (species!=null) || (subSpecies!=null))
    {
      mobRef=new EntityClassification();
      if (genusId!=null)
      {
        BitSet bitset=BitSetUtils.getBitSetFromFlags(genusId.intValue());
        List<Genus> genus=_genus.getFromBitSet(bitset);
        mobRef.setGenus(genus);
      }
      mobRef.setSpecies(species);
      mobRef.setSubSpecies(subSpecies);
    }
    return mobRef;
  }
}
