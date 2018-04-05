package delta.games.lotro.tools.lore.deeds.checks;

import java.util.List;

import delta.games.lotro.lore.deeds.DeedDescription;

/**
 * Normalize text data in deeds.
 * @author DAM
 */
public class NormalizeDeedsText
{
  /**
   * Do it!
   * @param deeds Deeds to use.
   */
  public void doIt(List<DeedDescription> deeds)
  {
    for(DeedDescription deed : deeds)
    {
      handleDeed(deed);
    }
  }

  private void handleDeed(DeedDescription deed)
  {
    // Description
    String description=deed.getDescription();
    description=normalize(description);
    deed.setDescription(description);
    // Objectives
    String objectives=deed.getObjectives();
    objectives=normalize(objectives);
    deed.setObjectives(objectives);
  }

  private String normalize(String input)
  {
    String ret=normalizeBrackets(input);
    ret=normalizeBraces(ret);
    ret=normalizeCounts(ret);
    ret=cleanBlanks(ret);
    return ret;
  }

  private String normalizeBrackets(final String input)
  {
    String ret=input;
    while(true)
    {
      int index=ret.indexOf("[[");
      if (index!=-1)
      {
        int index2=ret.indexOf("]]",index+2);
        if (index2!=-1)
        {
          String innerText=ret.substring(index+2,index2).trim();
          if (innerText.startsWith("File:"))
          {
            innerText="";
          }
          int pipeIndex=innerText.lastIndexOf('|');
          if (pipeIndex!=-1)
          {
            innerText=innerText.substring(pipeIndex+1);
          }
          ret=ret.substring(0,index)+innerText+ret.substring(index2+2);
        }
        else
        {
          break;
        }
      }
      else
      {
        break;
      }
    }
    return ret;
  }

  private String normalizeBraces(final String input)
  {
    String ret=input;
    while(true)
    {
      int index=ret.indexOf("{{");
      if (index!=-1)
      {
        int index2=ret.indexOf("}}",index+2);
        if (index2!=-1)
        {
          String innerText=ret.substring(index+2,index2).trim();
          if ((innerText.endsWith("|mode=link")) || (innerText.endsWith("|mode=imlink")))
          {
            int pipeIndex=innerText.lastIndexOf('|');
            innerText=innerText.substring(0,pipeIndex);
            pipeIndex=innerText.lastIndexOf('|');
            if (pipeIndex!=-1)
            {
              innerText=innerText.substring(pipeIndex+1);
            }
            if (innerText.startsWith(":")) innerText=innerText.substring(1);
            if (innerText.startsWith("arg="))
            {
              innerText=innerText.substring(4);
            }
            innerText=innerText.trim();
          }
          if (innerText.toLowerCase().startsWith("tooltip coords|"))
          {
            innerText=extractGeoLocation(innerText);
          }
          if (innerText.startsWith("Color|"))
          {
            int pipeIndex=innerText.lastIndexOf('|');
            innerText=innerText.substring(pipeIndex+1);
          }
          if (innerText.startsWith("Reward|"))
          {
            int pipeIndex=innerText.indexOf('|');
            innerText=innerText.substring(pipeIndex+1);
          }
          if (innerText.startsWith("Questbox"))
          {
            innerText=extractQuestbox(innerText);
          }
          //System.out.println(innerText);
          ret=ret.substring(0,index)+innerText+ret.substring(index2+2);
        }
        else
        {
          break;
        }
      }
      else
      {
        break;
      }
    }
    return ret;
  }

  private String normalizeCounts(String input)
  {
    String ret=input;
    while(true)
    {
      int index=ret.indexOf("(0/");
      if (index!=-1)
      {
        ret=ret.substring(0,index)+"(x"+ret.substring(index+3);
      }
      else
      {
        break;
      }
    }
    return ret;
  }

  /**
   * Extract a geographic point from a Lotro-Wiki annotation:
   * {{Tooltip Coords|The Great River|31.3S|52.1W}} => 31.3S/52.1W (The Great River)
   * @param input Input data.
   * @return A string or <code>null</code> if bad format.
   */
  private String extractGeoLocation(String input)
  {
    String latitude=null;
    String longitude=null;
    String mapName=null;
    String[] parts=input.split("\\|");
    if (parts.length==4)
    {
      latitude=parts[2];
      longitude=parts[3];
      mapName=parts[1];
    }
    String ret=null;
    if ((latitude!=null) && (longitude!=null))
    {
      ret=latitude+"/"+longitude;
      if (mapName!=null)
      {
        ret=ret+" ("+mapName+")";
      }
    }
    else
    {
      System.out.println("Unmanaged: ["+input+"]");
    }
    return ret;
  }

  private String extractQuestbox(String input)
  {
    String message=null;
    String contents=null;
    String[] parts=input.split("\\|");
    if (parts.length==4)
    {
      message=parts[1];
      contents=parts[3];
    }
    String ret=null;
    if ((message!=null) && (contents!=null))
    {
      ret=message.trim()+":\n"+contents.trim();
    }
    else
    {
      System.out.println("Unmanaged: ["+input+"]");
    }
    return ret;
    /*
{{Questbox|Creatures that count toward this deed|collapsed|
Donakh
...
Iron Crown Warrior
}}
     */
  }

  private String cleanBlanks(String input)
  {
    String oldInput=input;
    while(true)
    {
      input=input.replace(" \n","\n");
      input=input.replace("\n ","\n");
      input=input.replace("  "," ");
      if (oldInput.equals(input)) break;
      oldInput=input;
    }
    input=input.trim();
    return input;
  }
}
