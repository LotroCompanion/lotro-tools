package delta.games.lotro.tools.extraction.skills.baubles;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.collections.baubles.io.xml.BaublesXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;

/**
 * Get the baubles directory from DAT files.
 * @author DAM
 */
public class MainBaublesLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainBaublesLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainBaublesLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load data.
   */
  public void doIt()
  {
    List<SkillDescription> baubles=loadSkills();
    save(baubles);
  }

  private List<SkillDescription> loadSkills()
  {
    List<SkillDescription> ret=new ArrayList<SkillDescription>();
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"BaubleDirectory");
    if (props!=null)
    {
      SkillsManager skillsMgr=SkillsManager.getInstance();
      Object[] idsArray=(Object[])props.getProperty("Bauble_SkillList");
      for(Object entryObj : idsArray)
      {
        int id=((Integer)entryObj).intValue();
        SkillDescription skill=skillsMgr.getSkill(id);
        ret.add(skill);
      }
    }
    return ret;
  }

  private void save(List<SkillDescription> baubles)
  {
    boolean ok=BaublesXMLWriter.write(GeneratedFiles.BAUBLES,baubles);
    if (ok)
    {
      LOGGER.info("Wrote baubles file: "+GeneratedFiles.BAUBLES);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    MainBaublesLoader loader=new MainBaublesLoader(facade);
    loader.doIt();
    facade.dispose();
  }
}
