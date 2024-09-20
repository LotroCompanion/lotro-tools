package delta.games.lotro.tools.extraction.misc.actions;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.action.ActionEntry;
import delta.games.lotro.common.action.ActionTable;
import delta.games.lotro.common.action.ActionTableEntry;
import delta.games.lotro.common.action.ActionTables;
import delta.games.lotro.common.action.ActionTablesEntry;
import delta.games.lotro.common.action.io.xml.ActionTablesXMLWriter;
import delta.games.lotro.common.enums.AICooldownChannel;
import delta.games.lotro.common.enums.AIHint;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.agents.mobs.MobDescription;
import delta.games.lotro.lore.agents.mobs.MobsManager;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;

/**
 * Loader for action tables.
 * @author DAM
 */
public class ActionTablesLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(ActionTablesLoader.class);

  private DataFacade _facade;
  private Map<Integer,ActionTable> _actionTables;
  private Map<Integer,Float> _probabilities;
  private LotroEnum<AICooldownChannel> _cooldownChannel;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public ActionTablesLoader(DataFacade facade)
  {
    _facade=facade;
    _actionTables=new HashMap<Integer,ActionTable>();
    _cooldownChannel=LotroEnumsRegistry.getInstance().get(AICooldownChannel.class);
    initProbabilities();
  }

  /**
   * Load data.
   */
  public void doIt()
  {
    doItMobs();
    save();
  }

  private void doItMobs()
  {
    for(MobDescription mob : MobsManager.getInstance().getMobs())
    {
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("Loading actions tables for mob: "+mob);
      }
      int mobID=mob.getIdentifier();
      PropertiesSet props=_facade.loadProperties(mobID+DATConstants.DBPROPERTIES_OFFSET);
      loadActionTables(props);
    }
  }

  /**
   * Load action tables.
   * @param props Mob properties.
   * @return Action tables or <code>null</code> if none.
   */
  public ActionTables loadActionTables(PropertiesSet props)
  {
    Object[] actionTableArray=(Object[])props.getProperty("AI_ActionTable_Array");
    if ((actionTableArray==null) || (actionTableArray.length==0))
    {
      return null;
    }
    ActionTables ret=new ActionTables();
    for(Object actionTableEntry : actionTableArray)
    {
      PropertiesSet entryProps=(PropertiesSet)actionTableEntry;
      int tableID=((Integer)entryProps.getProperty("AI_ActionTable")).intValue();
      if (tableID==0)
      {
        continue;
      }
      Integer minLevel=(Integer)entryProps.getProperty("AI_ActionTable_MinLevel");
      if ((minLevel!=null) && (minLevel.intValue()<=0))
      {
        minLevel=null;
      }
      Integer maxLevel=(Integer)entryProps.getProperty("AI_ActionTable_MaxLevel");
      if ((maxLevel!=null) && (maxLevel.intValue()<=0))
      {
        maxLevel=null;
      }
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("\tTable ID: "+tableID);
      }
      ActionTable table=getTable(tableID);
      if (table!=null)
      {
        ActionTablesEntry entry=new ActionTablesEntry(table);
        entry.setMinLevel(minLevel);
        entry.setMaxLevel(maxLevel);
        ret.addActionTablesEntry(entry);
      }
    }
    return ret;
  }

  private ActionTable getTable(int tableID)
  {
    Integer key=Integer.valueOf(tableID);
    ActionTable ret=_actionTables.get(key);
    if (ret==null)
    {
      ret=loadTable(tableID);
      if (ret!=null)
      {
        _actionTables.put(key,ret);
      }
    }
    return ret;
  }

  private ActionTable loadTable(int tableID)
  {
    PropertiesSet props=_facade.loadProperties(tableID+DATConstants.DBPROPERTIES_OFFSET);
    if (props==null)
    {
      return null;
    }
    Object[] actionsArray=(Object[])props.getProperty("AIActionTable_Actions");
    if ((actionsArray==null) || (actionsArray.length==0))
    {
      return null;
    }
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Loading Table ID: "+tableID);
    }
    ActionTable ret=new ActionTable(tableID);
    int index=0;
    for(Object actionEntryObj : actionsArray)
    {
      PropertiesSet entryProps=(PropertiesSet)actionEntryObj;
      ActionTableEntry entry=new ActionTableEntry();
      // Probability
      Integer probabilityCode=(Integer)entryProps.getProperty("AIAction_Probability");
      Float probability=_probabilities.get(probabilityCode);
      entry.setProbability(probability.floatValue());
      // Cooldown
      Float cooldown=(Float)entryProps.getProperty("AIAction_Cooldown");
      if ((cooldown!=null) && (cooldown.floatValue()>0))
      {
        entry.setCooldown(cooldown);
      }
      // Required hints
      List<AIHint> requiredHints=loadHints(entryProps,"AIAction_RequiredHints");
      entry.setRequiredHints(requiredHints);
      // Disallowed hints
      List<AIHint> disallowedHints=loadHints(entryProps,"AIAction_DisallowedHints");
      entry.setDisallowedHints(disallowedHints);
      // Cooldown channel
      Integer cooldownChannelCode=(Integer)entryProps.getProperty("AIAction_CooldownChannel");
      if (cooldownChannelCode!=null)
      {
        AICooldownChannel cooldownChannel=_cooldownChannel.getEntry(cooldownChannelCode.intValue());
        entry.setCooldownChannel(cooldownChannel);
      }
      // Target cooldown
      Float targetCooldown=(Float)entryProps.getProperty("AIAction_TargetCooldown");
      if ((targetCooldown!=null) && (targetCooldown.floatValue()>0))
      {
        entry.setTargetCooldown(targetCooldown);
      }
      // Target required hints
      List<AIHint> targetRequiredHints=loadHints(entryProps,"AIAction_TargetRequiredHints");
      entry.setTargetRequiredHints(targetRequiredHints);
      // Target disallowed hints
      List<AIHint> targetDisallowedHints=loadHints(entryProps,"AIAction_TargetDisallowedHints");
      entry.setTargetDisallowedHints(targetDisallowedHints);
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("Entry #"+index);
        LOGGER.debug("\tProbability: "+probability);
        LOGGER.debug("\tCooldown: "+cooldown);
        LOGGER.debug("\tRequired Hints: "+requiredHints);
        LOGGER.debug("\tDisallowed Hints: "+disallowedHints);
        LOGGER.debug("\tTarget cooldown: "+targetCooldown);
        LOGGER.debug("\tTarget required hints: "+targetRequiredHints);
        LOGGER.debug("\tTarget disallowed hints: "+targetDisallowedHints);
      }
      // Action Chain
      loadActionChain(entryProps,entry);
      index++;

      ret.addEntry(entry);
    }
    return ret;
  }

  private void loadActionChain(PropertiesSet entryProps, ActionTableEntry entry)
  {
    Object[] actionChain=(Object[])entryProps.getProperty("AIAction_Chain");
    for(Object actionChainEntry : actionChain)
    {
      PropertiesSet actionChainEntryProps=(PropertiesSet)actionChainEntry;
      int skillID=((Integer)actionChainEntryProps.getProperty("AIAction_Skill")).intValue();
      Float recovery=(Float)actionChainEntryProps.getProperty("AIAction_Recovery");
      SkillDescription skill=SkillsManager.getInstance().getSkill(skillID);
      ActionEntry actionEntry=new ActionEntry(skill);
      actionEntry.setRecovery(recovery);
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("\t\tSkill: "+skill+" => "+recovery);
      }
      entry.addAction(actionEntry);
    }
  }

  private List<AIHint> loadHints(PropertiesSet entryProps, String propertyName)
  {
    Object rawHints=entryProps.getProperty(propertyName);
    BitSet bs=null;
    if (rawHints!=null)
    {
      if (Context.isLive())
      {
        bs=(BitSet)rawHints;
      }
      else
      {
        Long flags=(Long)rawHints;
        bs=BitSetUtils.getBitSetFromFlags(flags.longValue());
      }
    }
    List<AIHint> hintsList=loadHints(bs);
    return hintsList;
  }

  private List<AIHint> loadHints(BitSet hints)
  {
    if (hints==null)
    {
      return Collections.emptyList();
    }
    LotroEnum<AIHint> aiHintsEnum=LotroEnumsRegistry.getInstance().get(AIHint.class);
    List<AIHint> hintsList=aiHintsEnum.getFromBitSet(hints);
    return hintsList;
  }

  private void initProbabilities()
  {
    _probabilities=new HashMap<Integer,Float>();
    PropertiesSet properties=WeenieContentDirectory.loadWeenieContentProps(_facade,"AIActionProbability");
    if (properties==null)
    {
      return;
    }
    Object[] tableArray=(Object[])properties.getProperty("AIActionProbability_Array");
    for(Object tableEntryObj : tableArray)
    {
      PropertiesSet entryProps=(PropertiesSet)tableEntryObj;
      float percentage=((Float)entryProps.getProperty("AIActionProbability_Value")).floatValue();
      int code=((Integer)entryProps.getProperty("AIActionProbability_Type")).intValue();
      _probabilities.put(Integer.valueOf(code),Float.valueOf(percentage));
    }
  }

  /**
   * Save loaded data.
   */
  public void save()
  {
    List<ActionTable> tables=new ArrayList<ActionTable>(_actionTables.values());
    Collections.sort(tables,new IdentifiableComparator<ActionTable>());
    ActionTablesXMLWriter.writeActionTablesFile(GeneratedFiles.ACTION_TABLES,tables);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new ActionTablesLoader(facade).doIt();
    facade.dispose();
  }
}
