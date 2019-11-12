package delta.games.lotro.tools.dat.utils;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.mobs.MobDescription;
import delta.games.lotro.lore.mobs.MobReference;
import delta.games.lotro.utils.Proxy;
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

  private EnumMapper _genus;
  private EnumMapper _species;
  private EnumMapper _subSpecies;

  //public static HashSet<String> propNames=new HashSet<String>();

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MobLoader(DataFacade facade)
  {
    _facade=facade;
    _genus=_facade.getEnumsManager().getEnumMapper(587202570);
    _species=_facade.getEnumsManager().getEnumMapper(587202571);
    _subSpecies=_facade.getEnumsManager().getEnumMapper(587202572);
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
  public MobReference buildMobReference(PropertiesSet mobProps)
  {
    Integer subSpeciesId=(Integer)mobProps.getProperty("Quest_MonsterSubspecies");
    String subSpecies=null;
    if (subSpeciesId!=null)
    {
      subSpecies=_subSpecies.getString(subSpeciesId.intValue());
    }
    Integer speciesId=(Integer)mobProps.getProperty("Quest_MonsterSpecies");
    String species=null;
    if (speciesId!=null)
    {
      species=_species.getString(speciesId.intValue());
    }
    Integer genusId=(Integer)mobProps.getProperty("Quest_MonsterGenus");
    String genus=null;
    if (genusId!=null)
    {
      genus=getGenus(genusId.intValue());
    }
    Proxy<MobDescription> mob=null;
    Integer mobId=(Integer)mobProps.getProperty("QuestEvent_MonsterDID");
    if (mobId!=null)
    {
      String mobName=loadMob(mobId.intValue());
      mob=new Proxy<MobDescription>();
      mob.setId(mobId.intValue());
      mob.setName(mobName);
    }
    MobReference mobRef=null;
    if ((genus!=null) || (species!=null) || (subSpecies!=null) || (mob!=null))
    {
      mobRef=new MobReference();
      mobRef.setGenus(genus);
      mobRef.setSpecies(species);
      mobRef.setSubSpecies(subSpecies);
      mobRef.setMobProxy(mob);
    }
    return mobRef;
  }

  private String getGenus(int genusId)
  {
    if (genusId==0)
    {
      return null;
    }
    // genusId is a bitset. Each bit set is to be interpreted using the GenusType enum
    // 1 = 2^(1-1) = 1: Orc-kind
    // 4 = 2^(3-1) => 3: Man
    // 8 = 2^(4-1) => 4: Spiders and Insects
    // 16 = 2^(5-1) => 5: Ancient Evil
    // 64 = 2^(7-1) => 7: Troll-kind
    // 1024 = 2^(11-1) => 11: The Dead (found in deed "Lore of the Enemy", weird. Otherwise OK, for instance in deed "Spirits Aiding Angmar")
    // 2048 = 2^(12-1) => 12: Elf
    // 8192 = 2^(14-1) => 14: Beast
    // 16384 = 2^(15-1) => 15: Dwarf
    // 32768 = 2^(16-1) => 16: Hobbit
    BitSet bitset=BitSetUtils.getBitSetFromFlags(genusId);
    String genus=BitSetUtils.getStringFromBitSet(bitset,_genus,",");
    return genus;
  }
}
