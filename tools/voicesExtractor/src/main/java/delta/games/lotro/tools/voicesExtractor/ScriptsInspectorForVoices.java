package delta.games.lotro.tools.voicesExtractor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.dat.data.script.Script;
import delta.games.lotro.dat.data.script.ScriptNode;
import delta.games.lotro.dat.data.script.ScriptNodeData;
import delta.games.lotro.dat.data.script.ScriptNodePort;
import delta.games.lotro.dat.data.script.ScriptNodeType;
import delta.games.lotro.dat.data.script.ScriptPortType;
import delta.games.lotro.dat.data.script.ScriptsTable;
import delta.games.lotro.dat.data.script.port.SwitchPortData;
import delta.games.lotro.dat.loaders.script.ScriptTableLoader;
import delta.games.lotro.tools.voicesExtractor.npc.NpcVoices;

/**
 * Inspects scripts to find sounds with their associated quest and script key.
 * @author DAM
 */
public class ScriptsInspectorForVoices
{
  private static final Logger LOGGER=LoggerFactory.getLogger(ScriptsInspectorForVoices.class);

  // Data facade
  private DataFacade _facade;
  // Context
  private int _key;
  private int _questID;
  private NpcVoices _storage;
  // Cache for scripts
  private Map<Integer,ScriptsTable> _scripts;

  /**
   * Constructor.
   * @param facade Date facade.
   */
  public ScriptsInspectorForVoices(DataFacade facade)
  {
    _facade=facade;
    _scripts=new HashMap<Integer,ScriptsTable>();
  }

  /**
   * Inspect a scripts table.
   * @param table Data to inspect.
   */
  public void inspect(ScriptsTable table)
  {
    for(Script script : table.getScripts())
    {
      _key=(int)script.getKey();
      for(ScriptNode root : script.getRoots())
      {
        inspectScriptNode(root);
      }
    }
  }

  private void inspectScriptNode(ScriptNode node)
  {
    ScriptNodeType nodeType=node.getNodeType();
    if (nodeType==ScriptNodeType.SOUND)
    {
      handleSoundNode(node);
    }
    for(ScriptNodePort port : node.getPorts())
    {
      inspectNodePort(node,port);
    }
  }

  private void inspectNodePort(ScriptNode parent, ScriptNodePort port)
  {
    ScriptNodeType nodeType=parent.getNodeType();
    ScriptPortType portType=port.getPortType();
    if ((nodeType==ScriptNodeType.SWITCH) && (portType==ScriptPortType.SWITCH_VALUE))
    {
      SwitchPortData data=(SwitchPortData)port.getData();
      List<PropertyValue> propertyValues=data.getProperties();
      handleContext(propertyValues);
    }
    // Context is active during child nodes inspection
    for(ScriptNode childNode : port.getNodes())
    {
      inspectScriptNode(childNode);
    }
  }

  private void handleContext(List<PropertyValue> propertyValues)
  {
    for(PropertyValue propertyValue : propertyValues)
    {
      PropertyDefinition def=propertyValue.getDefinition();
      if (def.getPropertyId()==268439417) // Quest_VODataID
      {
        _questID=((Integer)propertyValue.getValue()).intValue();
      }
    }
  }

  private void handleSoundNode(ScriptNode node)
  {
    ScriptNodeData data=node.getData();
    if (data!=null)
    {
      PropertiesSet props=data.getProperties();
      Integer soundID=(Integer)props.getProperty("SoundScript_SoundID");
      if ((soundID!=null) && (soundID.intValue()!=0))
      {
        _storage.addSound(_questID,_key,soundID.intValue());
      }
    }
  }

  /**
   * Handle a single NPC.
   * @param npcID NPC identifier.
   * @return storage for the voices found.
   */
  public NpcVoices handleNPC(int npcID)
  {
    _storage=new NpcVoices();
    PropertiesSet props=_facade.loadProperties(npcID+DATConstants.DBPROPERTIES_OFFSET);
    if (props!=null)
    {
      Object[] idsArray=(Object[])props.getProperty("Entity_AdditionalScriptTables");
      if (idsArray!=null)
      {
        for(Object idEntry : idsArray)
        {
          int id=((Integer)idEntry).intValue();
          if (id!=0)
          {
            ScriptsTable table=getScriptTable(id);
            if (table!=null)
            {
              inspect(table);
            }
          }
        }
      }
    }
    NpcVoices ret=_storage;
    _storage=null;
    return ret;
  }

  private ScriptsTable getScriptTable(int id)
  {
    ScriptsTable ret=null;
    Integer key=Integer.valueOf(id);
    if (_scripts.containsKey(key))
    {
      ret=_scripts.get(key);
    }
    else
    {
      ret=loadScriptsTable(id);
      _scripts.put(key,ret);
    }
    return ret;
  }

  private ScriptsTable loadScriptsTable(int id)
  {
    byte[] scriptTableData=_facade.loadData(id);
    ScriptsTable ret=null;
    ScriptTableLoader loader=new ScriptTableLoader(_facade);
    try
    {
      ret=loader.decode(scriptTableData);
    }
    catch(Exception e)
    {
      LOGGER.warn("Decoding error for script ID="+id,e);
    }
    return ret;
  }
}
