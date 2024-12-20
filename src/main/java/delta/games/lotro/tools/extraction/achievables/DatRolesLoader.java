package delta.games.lotro.tools.extraction.achievables;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.Interactable;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.instances.PrivateEncounter;
import delta.games.lotro.lore.instances.PrivateEncountersManager;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.dialogs.DialogElement;
import delta.games.lotro.lore.quests.dialogs.QuestCompletionComment;
import delta.games.lotro.lore.quests.objectives.Objective;
import delta.games.lotro.lore.quests.objectives.ObjectivesManager;
import delta.games.lotro.lore.utils.InteractableUtils;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Loader for roles data from DAT files.
 * @author DAM
 */
public class DatRolesLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(DatRolesLoader.class);

  private static final String QUEST_DISPENSER_NPC="QuestDispenser_NPC";

  private I18nUtils _i18n;
  private EnumMapper _questRoleAction;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param i18n I18n support.
   */
  public DatRolesLoader(DataFacade facade, I18nUtils i18n)
  {
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
    //handleGlobalRoles(quest,properties)
    Object[] roles=(Object[])properties.getProperty("Quest_RoleArray");
    if (roles!=null)
    {
      handleRoles(quest,roles);
    }
    else
    {
      LOGGER.warn("No role array");
    }
    handleCompletionComments(quest,properties);
  }

  void handleGlobalRoles(PropertiesSet properties)
  {
    // Quest_GlobalRoles
    Object[] roles=(Object[])properties.getProperty("Quest_GlobalRoles");
    if (roles!=null)
    {
      LOGGER.debug("Roles (global):");
      int index=0;
      for(Object roleObj : roles)
      {
        LOGGER.debug("Index: {}",Integer.valueOf(index));
        PropertiesSet roleProps=(PropertiesSet)roleObj;
        Integer dispenserAction=(Integer)roleProps.getProperty("QuestDispenser_Action");
        if (dispenserAction!=null)
        {
          String action=_questRoleAction.getString(dispenserAction.intValue());
          LOGGER.debug("\tdispenserAction: {}",action); // LeaveInstance
        }
        Integer npcId=(Integer)roleProps.getProperty(QUEST_DISPENSER_NPC);
        if (npcId!=null)
        {
          Interactable npc=InteractableUtils.findInteractable(npcId.intValue());
          String npcName=npc.getName();
          LOGGER.debug("\tNPC: {}",npcName);
        }
        String dispenserRoleName=(String)roleProps.getProperty("QuestDispenser_RoleName");
        if (dispenserRoleName!=null)
        {
          LOGGER.debug("\tdispenserRolename: {}",dispenserRoleName);
        }
        String dispenserRoleConstraint=(String)roleProps.getProperty("QuestDispenser_RoleConstraint");
        if (dispenserRoleConstraint!=null)
        {
          LOGGER.debug("\tdispenserRole Constraint: {}",dispenserRoleConstraint);
        }
        String successText=_i18n.getStringProperty(roleProps,"QuestDispenser_RoleSuccessText");
        LOGGER.debug("\tSuccess text: {}",successText);
        index++;
      }
    }
  }

  private void handleRoles(QuestDescription quest, Object[] roles)
  {
    for(Object roleObj : roles)
    {
      PropertiesSet roleProps=(PropertiesSet)roleObj;
      int dispenserAction=((Integer)roleProps.getProperty("QuestDispenser_Action")).intValue();
      String action=_questRoleAction.getString(dispenserAction);
      Integer objectiveIndex=(Integer)roleProps.getProperty("Quest_ObjectiveIndex");
      String dispenserRoleName=(String)roleProps.getProperty("QuestDispenser_RoleName");
      Integer npcId=(Integer)roleProps.getProperty(QUEST_DISPENSER_NPC);
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
          LOGGER.warn("Found out of range objective index: {}. Max: {}",objectiveIndex,Integer.valueOf(objectives.size()));
        }
        if (dispenserAction==3) // Teleport
        {
          Integer peId=(Integer)roleProps.getProperty("QuestDispenser_PrivateEncounterTemplate");
          if (peId!=null)
          {
            PrivateEncounter pe=PrivateEncountersManager.getInstance().getPrivateEncounterById(peId.intValue());
            if (pe!=null)
            {
              LOGGER.info("Found PE: {} ({}) in quest {}",peId,pe.getName(),quest.getName());
            }
            else
            {
              LOGGER.warn("PE (ID={}) is null in quest {}",peId,quest.getName());
            }
          }
          else
          {
            LOGGER.warn("PE ID is null"+" in quest {}",quest.getName());
          }
        }
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
        LOGGER.warn("Role not used! Dispenser action={}, objectiveIndex={}, dispenserRolename={}, NPC ID={}, text={}, failure text={}",
            action,objectiveIndex,dispenserRoleName,npcId,successText,failureText);
      }
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
    ArrayPropertyValue textArrayValue=(ArrayPropertyValue)properties.getPropertyValueByName("QuestDispenser_TextArray");
    if (textArrayValue==null)
    {
      return null;
    }
    QuestCompletionComment ret=new QuestCompletionComment();
    for(Object npcObj : npcArray)
    {
      if (npcObj instanceof String)
      {
        String npcStr=(String)npcObj;
        if (QUEST_DISPENSER_NPC.equals(npcStr))
        {
          Interactable npc=getQuestBestower(quest);
          if (npc!=null)
          {
            ret.addWho(npc);
          }
          else
          {
            LOGGER.warn("No bestower found for quest: {}",quest);
          }
        }
        else if (("".equals(npcStr)) || ("QuestDispenser_TextArray".equals(npcStr)))
        {
          // Ignored
        }
        else
        {
          // Sometimes we find keys here, like: npc_eastangmar_golodir,color_commenter_buckland_partygoer_granny
          LOGGER.warn("Unexpected string value: {}",npcStr);
        }
      }
      else
      {
        int npcId=((Integer)npcObj).intValue();
        Interactable npc=InteractableUtils.findInteractable(npcId);
        if (npc!=null)
        {
          ret.addWho(npc);
        }
      }
    }

    for(PropertyValue textValue : textArrayValue.getValues())
    {
      String comment=_i18n.getStringProperty(textValue,0);
      ret.addWhat(comment);
    }
    return ret;
  }

  private Interactable getQuestBestower(QuestDescription quest)
  {
    List<DialogElement> bestowers=quest.getBestowers();
    if (bestowers.size()==1)
    {
      return bestowers.get(0).getWho();
    }
    LOGGER.warn("Bad bestowers size: {}",Integer.valueOf(bestowers.size()));
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
        Interactable npc=InteractableUtils.findInteractable(npcId.intValue());
        ret.setWho(npc);
      }
      ret.setWhat(successText);
    }
    return ret;
  }
}
