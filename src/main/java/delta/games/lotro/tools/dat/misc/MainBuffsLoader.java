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

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainBuffsLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load specific buffs.
   */
  public void doIt()
  {
    List<EffectBuff> buffs=loadBuffs();
    save(buffs);
  }

  private List<EffectBuff> loadBuffs()
  {
    List<EffectBuff> buffs=new ArrayList<EffectBuff>();
    // In Defence of Middle Earth
    buffs.add(loadBuff(1879053032,"IN_DEFENCE_OF_MIDDLE_EARTH"));
    // Motivated
    buffs.add(loadBuff(1879073072,"MOTIVATED"));
    // Dissonance
    buffs.add(loadBuff(1879095617,null));
    // Resonance
    buffs.add(loadBuff(1879217087,null));
    // Enhanced Abilities
    buffs.add(loadBuff(1879145605,null));
    // Peace and Quiet
    //buffs.add(loadBuff(1879145606,null));
    // A Quiet Calm (I)
    buffs.add(loadBuff(1879145414,null));
    // A Quiet Calm (II)
    buffs.add(loadBuff(1879239303,null));
    return buffs;
  }

  private EffectBuff loadBuff(int id, String key)
  {
    EffectBuff buff=null;
    Effect effect=DatEffectUtils.loadEffect(_facade,id);
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
    return buff;
  }

  private void save(List<EffectBuff> buffs)
  {
    // Buffs file
    saveBuffs(buffs);
    // Save progressions
    DatStatUtils._progressions.writeToFile(GeneratedFiles.PROGRESSIONS_BUFFS);
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
