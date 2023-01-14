package delta.games.lotro.tools.dat.quests;

import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.lore.agents.npcs.NpcDescription;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.dialogs.DialogElement;
import delta.games.lotro.lore.quests.dialogs.QuestCompletionComment;
import delta.games.lotro.lore.quests.objectives.Objective;
import delta.games.lotro.lore.quests.objectives.ObjectivesManager;
import delta.games.lotro.tools.dat.utils.NpcLoader;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for roles data from DAT files.
 * @author DAM
 */
public class DatRolesLoader
{
  private static final Logger LOGGER=Logger.getLogger(DatRolesLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;
  private EnumMapper _questRoleAction;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param i18n I18n support.
   */
  public DatRolesLoader(DataFacade facade, I18nUtils i18n)
  {
    _facade=facade;
    _i18n=i18n;
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
    handleCompletionComments(quest,properties);
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
        String successText=_i18n.getStringProperty(roleProps,"QuestDispenser_RoleSuccessText");
        System.out.println("\tSuccess text: "+successText);
        index++;
      }
    }
  }

  private void handleRoles(QuestDescription quest, PropertiesSet properties)
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
        String successText=_i18n.getStringProperty(roleProps,"QuestDispenser_RoleSuccessText");
        String failureText=_i18n.getStringProperty(roleProps,"QuestDispenser_RoleFailureText");
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
          /*
          if (dispenserAction==3) // Teleport
          {
            Integer peId=(Integer)roleProps.getProperty("QuestDispenser_PrivateEncounterTemplate");
            if (peId!=null)
            {
              PrivateEncounter pe=PrivateEncountersManager.getInstance().getPrivateEncounterById(peId.intValue());
              System.out.println("Found PE: "+peId+"("+pe.getName()+") in quest "+quest.getName());
            }
            else
            {
              System.out.println("PE is null"+" in quest "+quest.getName());
            }
          }
          */
        }
        else if (dispenserAction==5) // LeaveInstance
        {
          DialogElement dialog=buildDialog(npcId,successText);
          if (dialog!=null)
          {
            quest.addEndDialog(dialog);
          }
        }
        else
        {
          LOGGER.warn("Role not used! Dispenser action="+action+", objectiveIndex="+objectiveIndex+", dispenserRolename="+dispenserRoleName+", NPC ID="+npcId+", text="+successText+", failure text="+failureText);
        }
      }
    }
    else
    {
      LOGGER.warn("No role array");
    }
  }

  private void handleCompletionComments(QuestDescription quest, PropertiesSet properties)
  {
    Object[] commentsArray=(Object[])properties.getProperty("Quest_CompletionCommentArray");
    if (commentsArray!=null)
    {
      for(Object commentsObj : commentsArray)
      {
        PropertiesSet commentsProps=(PropertiesSet)commentsObj;
        QuestCompletionComment comment=handleCompletionCommentsForNpc(quest,commentsProps);
        if (comment.isValid())
        {
          quest.addCompletionComment(comment);
        }
      }
    }
  }

  private QuestCompletionComment handleCompletionCommentsForNpc(QuestDescription quest, PropertiesSet properties)
  {
    Object[] npcArray=(Object[])properties.getProperty("QuestDispenser_NPCArray");
    if (npcArray==null)
    {
      return null;
    }
    Object[] textArray=(Object[])properties.getProperty("QuestDispenser_TextArray");
    if (textArray==null)
    {
      return null;
    }
    QuestCompletionComment ret=new QuestCompletionComment();
    for(Object npcObj : npcArray)
    {
      if (npcObj instanceof String)
      {
        String npcStr=(String)npcObj;
        if ("QuestDispenser_NPC".equals(npcStr))
        {
          Proxy<NpcDescription> npc=getQuestBestower(quest);
          if (npc!=null)
          {
            ret.addWho(npc);
          }
          else
          {
            LOGGER.warn("No bestower found for quest: "+quest);
          }
        }
        else if (("".equals(npcStr)) || ("QuestDispenser_TextArray".equals(npcStr)))
        {
          // Ignored
        }
        else
        {
          // Sometimes we find keys here, like: npc_eastangmar_golodir,color_commenter_buckland_partygoer_granny
          LOGGER.warn("Unexpected string value: "+npcStr);
        }
      }
      else
      {
        int npcId=((Integer)npcObj).intValue();
        Proxy<NpcDescription> npc=buildNpcProxy(npcId);
        if (npc.getName()!=null)
        {
          ret.addWho(npc);
        }
      }
    }

    for(Object textObj : textArray)
    {
      String comment=DatStringUtils.getString(textObj);
      ret.addWhat(comment);
    }
    return ret;
  }

  private Proxy<NpcDescription> getQuestBestower(QuestDescription quest)
  {
    List<DialogElement> bestowers=quest.getBestowers();
    if (bestowers.size()==1)
    {
      return bestowers.get(0).getWho();
    }
    LOGGER.warn("Bad bestowers size: "+bestowers.size());
    return null;
  }

  private DialogElement buildDialog(Integer npcId, String successText)
  {
    DialogElement ret=null;
    if (successText!=null)
    {
      ret=new DialogElement();
      if (npcId!=null)
      {
        Proxy<NpcDescription> npc=buildNpcProxy(npcId.intValue());
        ret.setWho(npc);
      }
      ret.setWhat(successText);
    }
    return ret;
  }

  private Proxy<NpcDescription> buildNpcProxy(int npcId)
  {
    String npcName=NpcLoader.loadNPC(_facade,npcId);
    Proxy<NpcDescription> npc=new Proxy<NpcDescription>();
    npc.setId(npcId);
    npc.setName(npcName);
    return npc;
  }
}
