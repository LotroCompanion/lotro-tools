package delta.games.lotro.utils;

/**
 * Escapes management methods.
 * @author DAM
 */
public class Escapes
{
  /**
   * Escape an identifier.
   * @param id Source identifier.
   * @return Escaped string.
   */
  public static String escapeIdentifier(String id)
  {
    // Escape codes may be found at: http://www.utf8-chartable.de/
    id=id.replace(":","%3A");
    id=id.replace("'","%27");
    id=id.replace("â","%C3%A2");
    id=id.replace("ä","%C3%A4");
    id=id.replace("á","%C3%A1");
    id=id.replace("Â","%C3%82");
    id=id.replace("Á","%C3%81");
    id=id.replace("É","%C3%89");
    id=id.replace("ë","%C3%AB");
    id=id.replace("é","%C3%A9");
    id=id.replace("í","%C3%AD");
    id=id.replace("ï","%C3%AF");
    id=id.replace("î","%C3%AE");
    id=id.replace("ó","%C3%B3");
    id=id.replace("ö","%C3%B6");
    id=id.replace("ô","%C3%B4");
    id=id.replace("Ö","%C3%96");
    id=id.replace("û","%C3%BB");
    id=id.replace("ú","%C3%BA");
    id=id.replace("?","%3F");
    id=id.replace(",","%2C");
    id=id.replace("\\","%5C");
    
    return id;
  }

  /**
   * Escape an URL.
   * @param input Input string.
   * @return Result string.
   */
  public static String escapeUrl(String input)
  {
    String ret=input;
    ret=ret.replace("ó","%F3");
    ret=ret.replace("ú","%FA");
    ret=ret.replace("û","%FB");
    ret=ret.replace("í","%ED");
    ret=ret.replace("á","%E1");
    ret=ret.replace("â","%E2");
    ret=ret.replace("Ú","%DA");
    return ret;
  }

  /**
   * Escape a file.
   * @param input Input string.
   * @return Result string.
   */
  public static String escapeFile(String input)
  {
    String ret=input;
    ret=ret.replace(":","_");
    return ret;
  }
}
