package delta.games.lotro.tools.dat.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.EffectsManager;
import delta.games.lotro.lore.buffs.EffectBuff;
import delta.games.lotro.lore.buffs.io.xml.EffectBuffXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Loader for effect-based buffs.
 * @author DAM
 */
public class MainBuffsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainBuffsLoader.class);

  /**
   * Load specific buffs.
   */
  public void doIt()
  {
    List<EffectBuff> buffs=loadBuffs();
    // Save
    save(buffs);
  }

  private List<EffectBuff> loadBuffs()
  {
    List<EffectBuff> buffs=new ArrayList<EffectBuff>();
    // In Defence of Middle Earth
    loadBuff(buffs,1879053032,"IN_DEFENCE_OF_MIDDLE_EARTH"); // EFFECT
    // Motivated
    loadBuff(buffs,1879073072,"MOTIVATED"); // EFFECT
    // Dissonance
    loadBuff(buffs,1879095617,null); // EFFECT
    // Resonance
    loadBuff(buffs,1879217087,null); // EFFECT
    // Enhanced Abilities
    loadBuff(buffs,1879145605,null); // EFFECT
    // Peace and Quiet
    //loadBuff(buffs,1879145606,null)); // EFFECT
    // A Quiet Calm (I)
    loadBuff(buffs,1879145414,null); // EFFECT
    // A Quiet Calm (II)
    loadBuff(buffs,1879239303,null); // EFFECT
    // Minstrel anthems:
    // Anthem of Composure
    loadBuff(buffs,1879073122,null); // EFFECT
    // Anthem of Prowess
    loadBuff(buffs,1879073120,null); // EFFECT
    // Anthem of War
    loadBuff(buffs,1879060866,null); // EFFECT
    // Anthem of the Third Age - Resonance
    loadBuff(buffs,1879205019,null); // EFFECT
    // Anthem of the Third Age - Dissonance
    loadBuff(buffs,1879205020,null); // EFFECT
    // Anthem of the Third Age - Melody
    loadBuff(buffs,1879217358,null); // EFFECT
    // Dwarves Endurance
    loadBuff(buffs,1879073616,null); // EFFECT
    // Duty Bound
    loadBuff(buffs,1879084065,null); // EFFECT

    // Scroll of Finesse
    loadBuff(buffs,1879216017,null); // EFFECT

    // Veteran Fortitude
    loadBuff(buffs,1879077245,null); // EFFECT
    // Veteran Determination
    loadBuff(buffs,1879139404,null); // EFFECT

    // Guardian's Ward
    loadBuff(buffs,1879060192,null); // EFFECT

    return buffs;
  }

  private void loadBuff(List<EffectBuff> buffs, int id, String key)
  {
    EffectBuff buff=null;
    Effect effect=EffectsManager.getInstance().getEffectById(id);
    if (effect!=null)
    {
      buff=new EffectBuff();
      buff.setEffect(effect);
      if (key!=null)
      {
        buff.setKey(key);
      }
      buffs.add(buff);
    }
    else
    {
      LOGGER.warn("Effect not found: "+id);
    }
  }

  private void save(List<EffectBuff> buffs)
  {
    // Buffs file
    saveBuffs(buffs);
  }

  /**
   * Save the loaded buffs to a file.
   * @param buffs Loaded buffs.
   */
  private void saveBuffs(List<EffectBuff> buffs)
  {
    Collections.sort(buffs,new IdentifiableComparator<EffectBuff>());
    EffectBuffXMLWriter.write(GeneratedFiles.BUFFS,buffs);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainBuffsLoader().doIt();
  }
}
