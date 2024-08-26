package delta.games.lotro.tools.tools;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.NumericTools;
import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.config.DataFiles;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.buffs.EffectBuff;
import delta.games.lotro.lore.buffs.io.xml.EffectBuffXMLParser;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.lore.maps.DungeonsManager;
import delta.games.lotro.tools.extraction.geo.GeoUtils;

/**
 * Tool to cleanup the images loaded from the DAT files.
 * @author DAM
 */
public class MainLotroImagesCleanup
{
  private static final Logger LOGGER=Logger.getLogger(MainLotroImagesCleanup.class);

  private DataFacade _facade;
  private File _rootDir;

  private MainLotroImagesCleanup(File rootDir)
  {
    _rootDir=rootDir;
    _facade=new DataFacade();
  }

  private void doIt()
  {
    cleanupItems();
    cleanSkills();
    cleanBasemaps();
    cleanTraits();
    cleanRadarImages();
    cleanEffects();
  }

  void cleanupItems()
  {
    ItemsManager itemsMgr=ItemsManager.getInstance();
    for(Item item : itemsMgr.getAllItems())
    {
      String iconStr=item.getIcon();
      if (iconStr!=null)
      {
        String[] iconIds=iconStr.split("-");
        for(String iconId : iconIds)
        {
          removeIcon(NumericTools.parseInt(iconId,0));
        }
      }
    }
  }

  void cleanSkills()
  {
    SkillsManager skillsMgr=SkillsManager.getInstance();
    for(SkillDescription skill : skillsMgr.getAll())
    {
      int iconId=skill.getIconId();
      removeIcon(iconId);
    }
  }

  void cleanTraits()
  {
    TraitsManager traitsMgr=TraitsManager.getInstance();
    for(TraitDescription trait : traitsMgr.getAll())
    {
      int iconId=trait.getIconId();
      removeIcon(iconId);
    }
  }

  void cleanBasemaps()
  {
    DungeonsManager dungeonsMgr=DungeonsManager.getInstance();
    for(Dungeon dungeon : dungeonsMgr.getDungeons())
    {
      int id=dungeon.getBasemapId();
      removeIcon(id);
    }
    // TODO Parchment maps
  }

  void cleanRadarImages()
  {
    int[] regions=GeoUtils.getRegions();
    for(int region : regions)
    {
      for(int blockX=0;blockX<=255;blockX++)
      {
        for(int blockY=0;blockY<=255;blockY++)
        {
          Integer radarImageId=getRadarImageId(region,blockX,blockY);
          if (radarImageId!=null)
          {
            handleRadarImageId(radarImageId.intValue());
          }
        }
      }
    }
  }

  private Integer getRadarImageId(int region, int blockX, int blockY)
  {
    long regionBaseDid=0x80400000L + region*0x10000;
    long did=regionBaseDid+(blockX<<8)+blockY;
    PropertiesSet props=_facade.loadProperties(did);
    if (props==null)
    {
      return null;
    }
    PropertiesSet gameMapProps=(PropertiesSet)props.getProperty("UI_Map_GameMap");
    if (gameMapProps==null)
    {
      return null;
    }
    int mapImageId=((Integer)gameMapProps.getProperty("UI_Map_MapImage")).intValue();
    return Integer.valueOf(mapImageId);
  }

  private void handleRadarImageId(int imageId)
  {
    File from=new File(_rootDir,imageId+".png");
    if (from.exists())
    {
      boolean ok=from.delete();
      if (!ok)
      {
        LOGGER.warn("Failed to delete file: "+from);
      }
    }
  }

  void cleanEffects()
  {
    File effectsFile=LotroCoreConfig.getInstance().getFile(DataFiles.BUFFS);
    List<EffectBuff> buffs=new EffectBuffXMLParser().parseEffectsFile(effectsFile);
    for(EffectBuff buff : buffs)
    {
      Integer iconId=buff.getEffect().getIconId();
      if (iconId!=null)
      {
        removeIcon(iconId.intValue());
      }
    }
  }

  private void removeIcon(int iconId)
  {
    File iconFile=new File(_rootDir,iconId+".png");
    if (iconFile.exists())
    {
      boolean ok=iconFile.delete();
      if (!ok)
      {
        LOGGER.warn("Failed to delete file: "+iconFile);
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    // D:\\dev\\git\\lotro-tools
    File rootDir=new File(args[0]);
    new MainLotroImagesCleanup(rootDir).doIt();
  }
}
