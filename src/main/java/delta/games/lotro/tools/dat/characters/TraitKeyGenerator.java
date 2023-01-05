package delta.games.lotro.tools.dat.characters;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;

/**
 * Insert legacy keys into traits.
 * @author DAM
 */
public class TraitKeyGenerator
{
  private static final Logger LOGGER=Logger.getLogger(TraitKeyGenerator.class);

  private TraitsManager _traitsManager;

  /**
   * Constructor.
   * @param traitsManager Traits manager.
   */
  public TraitKeyGenerator(TraitsManager traitsManager)
  {
    _traitsManager=traitsManager;
  }

  /**
   * Setup trait keys.
   */
  public void setup()
  {
    // Man
    setup("BALANCE_OF_MAN","Balance of Man");
    setup("MAN_OF_THE_FOURTH_AGE","Man of the Fourth Age");
    // Dwarf
    setup("SHIELD_BRAWLER","Shield Brawler");
    setup("FATEFUL_DWARF","Fateful Dwarf");
    // Elf
    setup("FRIEND_OF_MAN","Friend Of Man");
    // Beorning
    setup("EMISSARY","Emissary");
    // Hobbit
    setup("HOBBIT_STATURE","Hobbit-stature");
    // High-elf
    setup("THOSE_WHO_REMAIN","Those Who Remain");

    // Captain
    setup("ARTERIAL_STRIKES","Arterial Strikes");
    setup("MARTIAL_PROWESS","Martial Prowess");
    setup("STEELED_RESOLVE","Steeled Resolve");
    // Champion
    setup("CRIT_CHANCE_INCREASE",1879270411);
    setup("MIGHT_INCREASE",1879270409);
    setup("FINESSE_INCREASE",1879270418);
    // Guardian
    setup("OVERPOWER","Valorous Strength");
    setup("HEAVY_BLOWS","Heavy Blows");
    setup("SKILLED_DEFLECTION","Skilled Deflection");
    // Hunter
    setup("CRITICAL_EYE",1879259548);
    setup("IMPACT_ARROWS","Impact Arrows");
    // Lore-master
    setup("TACTICAL_DAMAGE","Tactical Damage");
    // Minstrel
    setup("ENDURING_MORALE","Enduring Morale");
    setup("FINESSE_MINSTREL",1879270760);
    setup("CRITICAL_STRIKES","Critical Strikes");
    // Rune-keeper
    setup("EXACTING_WARDS","Exacting Wards");
    setup("FORTUNE_SMILES","Fortune Smiles");
    setup("CUTTING_REMARKS","Cutting Remarks");
    setup("DETERMINATION",1879270470);
    setup("LIGHT_ON_ONES_FEET","Light on One's Feet");
    setup("DELIBERATE_ADDRESS","Deliberate Address");
  }

  private void setup(String key, int identifier)
  {
    TraitDescription trait=_traitsManager.getTrait(identifier);
    if (trait!=null)
    {
      trait.setKey(key);
    }
    else
    {
      LOGGER.warn("Trait not found: ID="+identifier);
    }
  }

  private void setup(String key, String name)
  {
    List<TraitDescription> traits=getTraitByName(name);
    if (traits.size()==1)
    {
      traits.get(0).setKey(key);
    }
    else
    {
      if (traits.size()==0)
      {
        LOGGER.warn("Trait not found: "+name);
      }
      else
      {
        LOGGER.warn("Trait is ambiguous: "+name+" (found "+traits.size()+" items)");
      }
    }
  }

  private List<TraitDescription> getTraitByName(String name)
  {
    List<TraitDescription> traits=new ArrayList<TraitDescription>();
    for(TraitDescription description : _traitsManager.getAll())
    {
      if (name.equals(description.getName()))
      {
        traits.add(description);
      }
    }
    return traits;
  }
}
