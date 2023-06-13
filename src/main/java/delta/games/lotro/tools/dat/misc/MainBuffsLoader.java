package delta.games.lotro.tools.dat.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.files.archives.DirectoryArchiver;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.buffs.EffectBuff;
import delta.games.lotro.lore.buffs.io.xml.EffectBuffXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.ProgressionUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Loader for effect-based buffs.
 * @author DAM
 */
public class MainBuffsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainBuffsLoader.class);

  /**
   * Directory for effect icons.
   */
  private static File EFFECT_ICONS_DIR=new File("data\\effects\\tmp").getAbsoluteFile();

  private DataFacade _facade;
  private DatStatUtils _statUtils;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainBuffsLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("buffs",facade.getGlobalStringsManager());
    _statUtils=new DatStatUtils(facade,_i18n);
  }

  /**
   * Load specific buffs.
   */
  public void doIt()
  {
    List<EffectBuff> buffs=loadBuffs();
    // Save
    // - data
    save(buffs);
    // - labels
    _i18n.save();
  }

  private List<EffectBuff> loadBuffs()
  {
    List<EffectBuff> buffs=new ArrayList<EffectBuff>();
    // In Defence of Middle Earth
    loadBuff(buffs,1879053032,"IN_DEFENCE_OF_MIDDLE_EARTH");
    // Motivated
    loadBuff(buffs,1879073072,"MOTIVATED");
    // Dissonance
    loadBuff(buffs,1879095617,null);
    // Resonance
    loadBuff(buffs,1879217087,null);
    // Enhanced Abilities
    loadBuff(buffs,1879145605,null);
    // Peace and Quiet
    //loadBuff(buffs,1879145606,null));
    // A Quiet Calm (I)
    loadBuff(buffs,1879145414,null);
    // A Quiet Calm (II)
    loadBuff(buffs,1879239303,null);
    // Minstrel anthems:
    // Anthem of Composure
    loadBuff(buffs,1879073122,null);
    // Anthem of Prowess
    loadBuff(buffs,1879073120,null);
    // Anthem of War
    loadBuff(buffs,1879060866,null);
    // Anthem of the Third Age - Resonance
    loadBuff(buffs,1879205019,null);
    // Anthem of the Third Age - Dissonance
    loadBuff(buffs,1879205020,null);
    // Anthem of the Third Age - Melody
    loadBuff(buffs,1879217358,null);
    // Dwarves Endurance
    loadBuff(buffs,1879073616,null);
    // Duty Bound
    loadBuff(buffs,1879084065,null);

    // Scroll of Finesse
    loadBuff(buffs,1879216017,null);

    // Veteran Fortitude
    loadBuff(buffs,1879077245,null);
    // Veteran Determination
    loadBuff(buffs,1879139404,null);

    // Guardian's Ward
    loadBuff(buffs,1879060192,null);

    return buffs;
  }

  private void loadBuff(List<EffectBuff> buffs, int id, String key)
  {
    EffectBuff buff=null;
    Effect effect=DatEffectUtils.loadEffect(_statUtils,id,_i18n);
    if (effect!=null)
    {
      buff=new EffectBuff();
      buff.setEffect(effect);
      if (key!=null)
      {
        buff.setKey(key);
      }

      // Build icon file
      Integer iconId=effect.getIconId();
      if (iconId!=null)
      {
        String iconFilename=iconId+".png";
        File to=new File(EFFECT_ICONS_DIR,"effectIcons/"+iconFilename).getAbsoluteFile();
        if (!to.exists())
        {
          boolean ok=DatIconsUtils.buildImageFile(_facade,iconId.intValue(),to);
          if (!ok)
          {
            LOGGER.warn("Could not build trait icon: "+iconFilename);
          }
        }
      }
    }
    if (buff!=null)
    {
      buffs.add(buff);
    }
  }

  private void save(List<EffectBuff> buffs)
  {
    // Buffs file
    saveBuffs(buffs);
    // Save progressions
    ProgressionUtils.PROGRESSIONS_MGR.writeToFile(GeneratedFiles.PROGRESSIONS_BUFFS);
    // Write effect icons archive
    DirectoryArchiver archiver=new DirectoryArchiver();
    boolean ok=archiver.go(GeneratedFiles.EFFECT_ICONS,EFFECT_ICONS_DIR);
    if (ok)
    {
      System.out.println("Wrote effects icons archive: "+GeneratedFiles.EFFECT_ICONS);
    }
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
    DataFacade facade=new DataFacade();
    new MainBuffsLoader(facade).doIt();
    facade.dispose();
  }
}
