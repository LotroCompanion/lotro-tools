package delta.games.lotro.tools.dat.characters;

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
    setup("BALANCE_OF_MAN",1879073534); // Balance of Man
    setup("MAN_OF_THE_FOURTH_AGE",1879073523); // Man of the Fourth Age
    // Dwarf
    setup("SHIELD_BRAWLER",1879076358); // Shield Brawler
    setup("FATEFUL_DWARF",1879073603); // Fateful Dwarf
    // Elf
    setup("FRIEND_OF_MAN",1879073565); // Friend Of Man
    // Beorning
    setup("EMISSARY",1879316867); // Emissary
    // Hobbit
    setup("HOBBIT_STATURE",1879073477); // Hobbit-stature
    // High-elf
    setup("THOSE_WHO_REMAIN",1879346315); // Those Who Remain

    // Captain
    setup("ARTERIAL_STRIKES",1879269756); // Arterial Strikes
    setup("MARTIAL_PROWESS",1879269739); // Martial Prowess
    setup("STEELED_RESOLVE",1879269738); // Steeled Resolve
    // Champion
    setup("CRIT_CHANCE_INCREASE",1879270411);
    setup("MIGHT_INCREASE",1879270409);
    setup("FINESSE_INCREASE",1879270418);
    // Guardian
    setup("OVERPOWER",1879272892); // Valorous Strength
    setup("HEAVY_BLOWS",1879272894); // Heavy Blows
    setup("SKILLED_DEFLECTION",1879271351); // Skilled Deflection
    // Hunter
    setup("CRITICAL_EYE",1879259548);
    setup("IMPACT_ARROWS",1879264321); // Impact Arrows
    // Lore-master
    setup("TACTICAL_DAMAGE",1879269802); // Tactical Damage
    // Minstrel
    setup("ENDURING_MORALE",1879270745); // Enduring Morale
    setup("FINESSE_MINSTREL",1879270760);
    setup("CRITICAL_STRIKES",1879270774); // Critical Strikes
    // Rune-keeper
    setup("EXACTING_WARDS",1879270438); // Exacting Wards
    setup("FORTUNE_SMILES",1879270465); // Fortune Smiles
    setup("CUTTING_REMARKS",1879270469); // Cutting Remarks
    setup("DETERMINATION",1879270470);
    setup("LIGHT_ON_ONES_FEET",1879270461); // Light on One's Feet
    setup("DELIBERATE_ADDRESS",1879270449); // Deliberate Address
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
}
