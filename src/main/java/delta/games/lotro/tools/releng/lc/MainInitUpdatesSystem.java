package delta.games.lotro.tools.releng.lc;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import delta.updates.data.DirectoryDescription;
import delta.updates.data.SoftwareDescription;
import delta.updates.data.SoftwarePackageDescription;
import delta.updates.data.Version;
import delta.updates.tools.PackagesBuilder;
import delta.updates.tools.ToolsConfig;
import delta.updates.utils.DescriptionBuilder;

/**
 * Initializes the given directory so that it becomes manageable
 * by the updates manager.
 * @author DAM
 */
public class MainInitUpdatesSystem
{
  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    // Constants
    String baseURL="https://sourceforge.net/projects/lotrocompanion/files/16/${file}/download";
    File to=new File("d:/tmp/lc16-packages");
    ToolsConfig config=new ToolsConfig(baseURL,to);
    File from=new File("D:/shared/damien/dev/lotrocompanion/releases/16.0/LotRO Companion/app");

    // Build a description of the software directory
    DescriptionBuilder descriptionBuilder=new DescriptionBuilder();
    DirectoryDescription description=(DirectoryDescription)descriptionBuilder.build(from);
    description.removeEntry(".updates");
    description.setName("");

    // Build packages
    PackagesBuilder builder=new PackagesBuilder(from,config);
    List<SoftwarePackageDescription> packages=new ArrayList<SoftwarePackageDescription>();
    int packageID=0;
    // Data
    DirectoryDescription dataDir=(DirectoryDescription)description.getEntryByName("data");
    packages.add(builder.buildPackage(packageID,"data",dataDir));
    packageID++;
    // Lib
    DirectoryDescription libDir=(DirectoryDescription)description.getEntryByName("lib");
    packages.add(builder.buildPackage(packageID,"lib",libDir));
    packageID++;
    // Remaining
    packages.add(builder.buildPackage(packageID,"main",description));

    // Build the software description
    SoftwareDescription software=new SoftwareDescription(0);
    software.setName("Lotro Companion");
    software.setVersion(new Version(1600,"16.0.30.0.1"));
    software.setContentsDescription("Version 16 of the famous tool for Lotro");
    software.setDate(new Date().getTime());
    builder.updateSoftware(software,packages);
  }
}
