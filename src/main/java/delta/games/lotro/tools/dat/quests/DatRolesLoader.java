package delta.games.lotro.tools.dat.quests;

import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.npc.NpcDescription;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.dialogs.DialogElement;
import delta.games.lotro.lore.quests.objectives.Objective;
import delta.games.lotro.lore.quests.objectives.ObjectivesManager;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.NpcLoader;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for roles data from DAT files.
 * @author DAM
 */
public class DatRolesLoader
{
  private static final Logger LOGGER=Logger.getLogger(DatRolesLoader.class);

  private DataFacade _facade;
  private EnumMapper _questRoleAction;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public DatRolesLoader(DataFacade facade)
  {
    _facade=facade;
    _questRoleAction=facade.getEnumsManager().getEnumMapper(587202589);
  }

  /**
   * Load roles.
   * @param quest Quest.
   * @param properties Input properties.
   */
  public void loadRoles(QuestDescription quest, PropertiesSet properties)
  {
    //handleGlobalRoles(quest,properties);
    handleRoles(quest,properties);
  }

  void handleGlobalRoles(QuestDescription quest, PropertiesSet properties)
  {
    // Quest_GlobalRoles
    Object[] roles=(Object[])properties.getProperty("Quest_GlobalRoles");
    if (roles!=null)
    {
      System.out.println("Roles (global):");
      int index=0;
      for(Object roleObj : roles)
      {
        System.out.println("Index: "+index);
        PropertiesSet roleProps=(PropertiesSet)roleObj;
        Integer dispenserAction=(Integer)roleProps.getProperty("QuestDispenser_Action");
        if (dispenserAction!=null)
        {
          String action=_questRoleAction.getString(dispenserAction.intValue());
          System.out.println("\tdispenserAction: " +action); // LeaveInstance
        }
        Integer npcId=(Integer)roleProps.getProperty("QuestDispenser_NPC");
        if (npcId!=null)
        {
          String npcName=NpcLoader.loadNPC(_facade,npcId.intValue());
          System.out.println("\tNPC: "+npcName);
        }
        String dispenserRoleName=(String)roleProps.getProperty("QuestDispenser_RoleName");
        if (dispenserRoleName!=null)
        {
          System.out.println("\tdispenserRolename: " +dispenserRoleName);
        }
        String dispenserRoleConstraint=(String)roleProps.getProperty("QuestDispenser_RoleConstraint");
        if (dispenserRoleConstraint!=null)
        {
          System.out.println("\tdispenserRole Constraint: " +dispenserRoleConstraint);
        }
        String successText=DatUtils.getFullStringProperty(roleProps,"QuestDispenser_RoleSuccessText","{***}");
        System.out.println("\tSuccess text: "+successText);
        index++;
      }
    }
  }

  void handleRoles(QuestDescription quest, PropertiesSet properties)
  {
    // Quest_RoleArray
    Object[] roles=(Object[])properties.getProperty("Quest_RoleArray");
    if (roles!=null)
    {
      for(Object roleObj : roles)
      {
        PropertiesSet roleProps=(PropertiesSet)roleObj;
        int dispenserAction=((Integer)roleProps.getProperty("QuestDispenser_Action")).intValue();
        String action=_questRoleAction.getString(dispenserAction);
        Integer objectiveIndex=(Integer)roleProps.getProperty("Quest_ObjectiveIndex");
        String dispenserRoleName=(String)roleProps.getProperty("QuestDispenser_RoleName");
        Integer npcId=(Integer)roleProps.getProperty("QuestDispenser_NPC");
        String npcName=null;
        if (npcId!=null)
        {
          npcName=NpcLoader.loadNPC(_facade,npcId.intValue());
        }
        String successText=DatUtils.getFullStringProperty(roleProps,"QuestDispenser_RoleSuccessText",Markers.CHARACTER);
        String failureText=DatUtils.getFullStringProperty(roleProps,"QuestDispenser_RoleFailureText",Markers.CHARACTER);
        if ((objectiveIndex!=null) && (objectiveIndex.intValue()==0) && (dispenserAction==6)) // Bestow
        {
          DialogElement dialog=buildDialog(npcId,successText);
          if (dialog!=null)
          {
            quest.addBestower(dialog);
          }
        }
        else if (objectiveIndex!=null)
        {
          ObjectivesManager objectivesMgr=quest.getObjectives();
          List<Objective> objectives=objectivesMgr.getObjectives();
          if ((objectiveIndex.intValue()>0) && (objectiveIndex.intValue()<=objectives.size()))
          {
            Objective objective=objectives.get(objectiveIndex.intValue()-1);
            DialogElement dialog=buildDialog(npcId,successText);
            if (dialog!=null)
            {
              objective.addDialog(dialog);
            }
          }
          else
          {
            LOGGER.warn("Found out of range objective index: "+objectiveIndex+". Max: "+objectives.size());
          }
        }
        else
        {
          System.out.println("\tdispenserAction: " +action);
          System.out.println("\tobjectiveIndex: " +objectiveIndex);
          System.out.println("\tdispenserRolename: " +dispenserRoleName);
          if (npcId!=null)
          {
            System.out.println("\tNPC: id="+npcId+", name="+npcName);
          }
          System.out.println("\tSuccess text: "+successText);
          if (failureText!=null)
          {
            System.out.println("\tFailure text: "+failureText);
          }
        }
      }
    }
  }

  private DialogElement buildDialog(Integer npcId, String successText)
  {
    DialogElement ret=null;
    if (successText!=null)
    {
      ret=new DialogElement();
      if (npcId!=null)
      {
        String npcName=NpcLoader.loadNPC(_facade,npcId.intValue());
        Proxy<NpcDescription> npc=new Proxy<NpcDescription>();
        npc.setId(npcId.intValue());
        npc.setName(npcName);
        ret.setWho(npc);
      }
      ret.setWhat(successText);
    }
    return ret;
  }
}
