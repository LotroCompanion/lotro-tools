package delta.games.lotro.tools.extraction.sounds;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.script.Script;
import delta.games.lotro.dat.data.script.ScriptNode;
import delta.games.lotro.dat.data.script.ScriptNodeData;
import delta.games.lotro.dat.data.script.ScriptNodePort;
import delta.games.lotro.dat.data.script.ScriptNodeType;
import delta.games.lotro.dat.data.script.ScriptPortType;
import delta.games.lotro.dat.data.script.ScriptsTable;
import delta.games.lotro.dat.data.script.port.SwitchPortData;

/**
 * Inspects scripts to find sounds with their context (area, house item...).
 * @author DAM
 */
public class ScriptsInspectorForSounds
{
  private static final Logger LOGGER=LoggerFactory.getLogger(ScriptsInspectorForSounds.class);

  // Context
  private Deque<List<PropertyValue>> _context;
  // Data aggregator
  private SoundsDataAggregator _aggregator;

  private EnumMapper _channel;

  /**
   * Constructor.
   * @param facade Date facade.
   */
  public ScriptsInspectorForSounds(DataFacade facade)
  {
    _channel=facade.getEnumsManager().getEnumMapper(587203405);
    _context=new LinkedList<List<PropertyValue>>();
    _aggregator=new SoundsDataAggregator(facade);
  }

  /**
   * Get the sounds data aggregator.
   * @return the sounds data aggregator.
   */
  public SoundsDataAggregator getAggregator()
  {
    return _aggregator;
  }

  /**
   * Inspect a scripts table.
   * @param table Data to inspect.
   */
  public void inspect(ScriptsTable table)
  {
    for(Script script : table.getScripts())
    {
      inspectScript(script);
    }
  }

  private void inspectScript(Script script)
  {
    for(ScriptNode root : script.getRoots())
    {
      inspectScriptNode(root);
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
    boolean doContext=false;
    if ((nodeType==ScriptNodeType.SWITCH) && (portType==ScriptPortType.SWITCH_VALUE))
    {
      SwitchPortData data=(SwitchPortData)port.getData();
      List<PropertyValue> propertyValues=data.getProperties();
      addContext(propertyValues);
      doContext=true;
    }
    // Context is active during child nodes inspection
    for(ScriptNode childNode : port.getNodes())
    {
      inspectScriptNode(childNode);
    }
    if (doContext)
    {
      // Clear context
      removeContext();
    }
  }

  private void addContext(List<PropertyValue> propertyValues)
  {
    _context.add(propertyValues);
  }

  private void removeContext()
  {
    _context.pop();
  }

  private void showContext()
  {
    int nb=getContextPropertiesCount();
    if (nb>0)
    {
      LOGGER.debug("With context:");
      for(List<PropertyValue> values : _context)
      {
        LOGGER.debug("#");
        for(PropertyValue value : values)
        {
          LOGGER.debug("\t{}",value);
        }
      }
    }
    else
    {
      LOGGER.debug("No context");
    }
  }

  private int getContextPropertiesCount()
  {
    int nb=0;
    for(List<PropertyValue> values : _context)
    {
      nb+=values.size();
    }
    return nb;
  }

  private void handleSoundNode(ScriptNode node)
  {
    ScriptNodeData data=node.getData();
    if (data!=null)
    {
      PropertiesSet props=data.getProperties();
      Integer soundID=(Integer)props.getProperty("SoundScript_SoundID");
      int soundChannelID=((Integer)props.getProperty("SoundScript_Channel")).intValue();
      if (LOGGER.isDebugEnabled())
      {
        String soundChannel=_channel.getLabel(soundChannelID);
        LOGGER.debug("Found sound ID: {} ({})",soundID,soundChannel);
        showContext();
      }
      _aggregator.handleSound(soundID.intValue(),soundChannelID,_context);
    }
  }
}
