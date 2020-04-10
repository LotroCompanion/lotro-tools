package delta.games.lotro.tools.dat.characters;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Get trait definitions from DAT files.
 * @author DAM
 */
public class MainSkillDataLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainSkillDataLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load trait data.
   */
  public void doIt()
  {
    loadSkills();
  }

  private void loadSkills()
  {
    SkillsManager skillsMgr=SkillsManager.getInstance();

    for(int i=0x70000000;i<=0x77FFFFFF;i++)
    {
      byte[] data=_facade.loadData(i);
      if (data!=null)
      {
        int did=BufferUtils.getDoubleWordAt(data,0);
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        if (classDefIndex==827)
        {
          SkillDescription skill=SkillLoader.loadSkill(_facade,did);
          if (skill!=null)
          {
            skillsMgr.registerSkill(skill);
          }
        }
      }
    }
    SkillLoader.saveSkills(skillsMgr);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainSkillDataLoader(facade).doIt();
    facade.dispose();
  }
}
