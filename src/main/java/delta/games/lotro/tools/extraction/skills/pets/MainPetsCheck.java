package delta.games.lotro.tools.extraction.skills.pets;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.collections.pets.CosmeticPetDescription;
import delta.games.lotro.lore.collections.pets.CosmeticPetsManager;

/**
 * Check pets.
 * @author DAM
 */
public class MainPetsCheck
{
  private Set<Integer> loadPetIDs()
  {
    Set<Integer> ret=new HashSet<Integer>();
    DataFacade facade=new DataFacade();
    PropertiesSet cosmeticEntitiesDirectoryProps=facade.loadProperties(0x7004832E+DATConstants.DBPROPERTIES_OFFSET);
    Object[] cosmeticEntitySkillsList=(Object[])cosmeticEntitiesDirectoryProps.getProperty("CosmeticEntity_SkillList");
    for(Object cosmeticEntitySkillObj : cosmeticEntitySkillsList)
    {
      Integer cosmeticEntitySkillId=(Integer)cosmeticEntitySkillObj;
      ret.add(cosmeticEntitySkillId);
    }
    return ret;
  }

  private Set<Integer> loadPetSkillsIDs()
  {
    Set<Integer> ret=new HashSet<Integer>();
    CosmeticPetsManager mgr=CosmeticPetsManager.getInstance();
    for(CosmeticPetDescription pet : mgr.getAll())
    {
      int id=pet.getIdentifier();
      ret.add(Integer.valueOf(id));
    }
    return ret;
  }

  private void doIt(PrintStream out)
  {
    Set<Integer> petIDs=loadPetIDs();
    out.println("Size petIDs="+petIDs.size());
    Set<Integer> petSkillIDs=loadPetSkillsIDs();
    out.println("Size petSkillIDs="+petSkillIDs.size());
    petSkillIDs.removeAll(petIDs);
    out.println("Orphan skill(s): "+petSkillIDs);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainPetsCheck().doIt(System.out);
  }
}
