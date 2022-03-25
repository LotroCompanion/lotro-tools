package delta.games.lotro.tools;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.DIDMapper;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.ui.UIElement;
import delta.games.lotro.dat.data.ui.UILayout;
import delta.games.lotro.dat.data.ui.UILayoutLoader;
import delta.games.lotro.dat.loaders.DataIdMapLoader;
import delta.games.lotro.dat.utils.DatIconsUtils;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.*;

public class UISkinLoader {

    private final DataFacade facade;
    private final UILayoutLoader uiLoader;
    private final EnumMapper artAssetMapper;
    private final EnumMapper uiElementMapper;
    private final Map<Integer, UIElement> elementLookup;
    private final Map<Integer, UILayout> layoutLookup;
    private final Map<String, UIElement> skinPanels;
    private final Map<Integer, String> images;
    private final File outputFolder;

    public UISkinLoader(DataFacade facade, File outputFolder) {
        this.facade = facade;
        this.uiLoader = new UILayoutLoader(facade);
        this.artAssetMapper = facade.getEnumsManager().getEnumMapper(587202796);
        this.uiElementMapper = facade.getEnumsManager().getEnumMapper(587202769);
        this.elementLookup = new HashMap<>();
        this.layoutLookup = new HashMap<>();
        this.skinPanels = new HashMap<>();
        this.images = new HashMap<>();
        this.outputFolder = outputFolder;
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("USAGE:\n\t" + UISkinLoader.class.getName() + " <out folder>\n");
            System.exit(0);
        }
        File outputFolder = new File(args[0]);
        if (!outputFolder.exists()) outputFolder.mkdirs();

        UISkinLoader skinning = new UISkinLoader(new DataFacade(), outputFolder);
        skinning.doIt();

    }

    public void doIt() {
        buildImages();
        buildUILayout();
    }

    void buildImages() {
        try {

            PropertiesSet props = facade.loadProperties(0x78000011);
            Object[] array = (Object[]) props.getProperty("UISkin_MappingArray");

            PrintWriter fw = new PrintWriter(new FileWriter(new File(outputFolder, "SkinDefinitions.xml")));
            fw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            fw.println("<opt>");
            fw.println("\t<SkinName Name=\"UIArtPackTemplate\"></SkinName>");
            List<String> mappings = new LinkedList<>();
            for (Object entry : array) {
                PropertiesSet entryProps = (PropertiesSet) entry;
                int dataId = (Integer) entryProps.getProperty("UISkin_DataID");
                int assetID = (Integer) entryProps.getProperty("UISkin_UIArtAssetID");
                String assetName = artAssetMapper.getLabel(assetID);
                images.put(dataId, assetName);
                File to = new File(outputFolder, assetName + ".tga");
                if (!to.exists()) {
                    // skins save images in tga format
                    boolean ret = DatIconsUtils.buildImageFile(facade, dataId, "tga", to);
                    if (!ret) {
                        System.out.println(to + " failed to save.");
                    }
                }
                mappings.add(String.format("\t<Mapping ArtAssetID=\"%s\" FileName=\"%s\"></Mapping>", assetName, to.getName()));
            }
            mappings.sort(Comparator.comparing(String::toString));
            mappings.forEach(fw::println);
            fw.println("</opt>");
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildUILayout() {

byte[] data = facade.loadData(0x28000000);
        int uiId = DataIdMapLoader.decodeDataIdMap(data).getDataIdForLabel("UILAYOUT");
        DIDMapper map = DataIdMapLoader.decodeDataIdMap(facade.loadData(uiId));
        map.getLabels().stream()
                .map(map::getDataIdForLabel)
                .filter(id -> id > 0)
                .map(id -> {
                    try {
                        return uiLoader.loadUiLayout(id);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(this::buildLookup);

        // These two Elements are missing from the UI Layout tables.  I had to
        // Hand map these with their child elements.. :(
        /*
        268436807 ->
         "ProgressUIElementTest_Label", "Progress_BaseBox", "ProgressUIElement_Meter", "BaseBox_Empty"
         */
        UIElement el = new UIElement(268436807);
        el.addChild(elementLookup.get(268442554));
        el.addChild(elementLookup.get(268436808));
        el.addChild(elementLookup.get(268436809));
        el.addChild(elementLookup.get(268436810));
        elementLookup.put(268436807, el);

        /*
        268441701 ->
        "BaseBox_Empty_mounted","Progress_BaseBox","ProgressUIElement_Meter","ProgressUIElement_Label"
        */
        el = new UIElement(268441701);
        el.addChild(elementLookup.get(268441703));
        el.addChild(elementLookup.get(268436808));
        el.addChild(elementLookup.get(268436809));
        el.addChild(elementLookup.get(268436811));
        elementLookup.put(268441701, el);

        /*
        // Tried scanning ranges to find layouts that were not located
        // in the DID Mappings.. this ended up not being needed
        // if you followed the "base Layout DID" of UIElements
        for (int i = 0x20000000; i < 0x2FFFFFFF; i++) {
            try {
                if (layoutLookup.containsKey(i)) continue;

                UILayout layout = uiLoader.loadUiLayout(i);
                if (layout != null && !layout.getChildElements().isEmpty()) { // && uiElements.getLabel(layout.getChildElements().get(0).getIdentifier()) != null) {
                    System.out.println("Found: " + i);
                }
            } catch (Exception e) {}
        }
        */

        try {
            // print it all out
            PrintWriter fw = new PrintWriter(new FileWriter(new File(outputFolder, "SkinDictionary.txt")));
            for (Map.Entry<String, UIElement> panel : skinPanels.entrySet()) {
                fw.println(String.format("<PanelFile ID=\"%s\">", panel.getKey()));
                printChildren(fw, panel.getValue(), "\t");
                fw.println("</PanelFile>");
            }
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void buildLookup(UILayout l) {
        layoutLookup.put(l.getIdentifier(), l);
        for (UIElement child : l.getChildElements()) {
            buildLookup(child);
        }
    }

    private void buildLookup(UIElement ui) {
        int id = ui.getIdentifier();
        elementLookup.put(id, ui);
        int bld = ui.getBaseLayoutDID();
        if (bld > 0 && !layoutLookup.containsKey(bld)) {
            try {
                buildLookup(uiLoader.loadUiLayout(bld));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String skin = (String) ui.getProperties().getProperty("UISkin_UserElementID");
        if (skin != null) {
            skinPanels.put(skin, ui);
        }
        for (UIElement child : ui.getChildElements()) {
            buildLookup(child);
        }
    }

    private void printChildren(PrintWriter pw, UIElement ui, String prefix) {

        int id = ui.getIdentifier();
        String name = uiElementMapper.getString(id);

        String artAssetID = null;
        /*
        // if you want to see which UIElements are tied to skinning images
        for (UIData d : ui.getData()) {
            if (d instanceof UIImage) {
                int imageId = ((UIImage) d)._imageDID;
                if (imageId > 0) {
                    artAssetID = images.get(imageId);
                    if (artAssetID == null) {
                        System.out.println("Image not found: " + imageId);
                    }
                }
            }
        }
        */

        List<UIElement> children = new LinkedList<>();
        int base = ui.getBaseElementId();
        int baseLayout = ui.getBaseLayoutDID();
        String notFound = null;
        if (base > 0) {
            try {
                UIElement baseEl = elementLookup.get(base);
                if (baseEl != null) {
                    Object templates = baseEl.getProperties().getProperty("UICore_ListBox_entry_templates");
                    if (templates != null) {
                        for (Object cur : (Object[]) templates) {
                            PropertiesSet ps = (PropertiesSet) cur;
                            int childId = (Integer) ps.getProperty("UICore_ListBox_entry_template_element");
                            UIElement child = elementLookup.get(childId);
                            if (child != null) {
                                children.add(child);
                            } else {
                                System.out.println("Couldn't find child! : " + childId);
                            }
                        }
                    }
                    if (!baseEl.getChildElements().isEmpty()) {
                        children.addAll(baseEl.getChildElements());
                    }
                } else {
                    // THIS SHOULD NOT HAPPEN!
                    // This is where the two elements manually defined
                    // above would reach. :(
                    String baseName = uiElementMapper.getLabel(base);
                    notFound = String.format("%s (%s)", baseName, base);
                    if (baseLayout > 0) {
                        // the layouts should have already been found at this point. no reason to have to look it up
                        UILayout layout = uiLoader.loadUiLayout(baseLayout);
                        baseEl = getUIElementById(uiElementMapper, base, layout.getChildElements(), "");
                        System.out.println(baseEl);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        children.addAll(ui.getChildElements());

        Rectangle r = ui.getRelativeBounds();
        if (artAssetID != null) {
            pw.println(prefix + String.format("<!-- ArtAssetID=\"%s\" -->", name, artAssetID));
        }
        String openingTag = String.format(prefix + "<Element ID=\"%s\" X=\"%s\" Y=\"%s\" Width=\"%s\" Height=\"%s\">", name, (int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
        if (children.isEmpty() && notFound == null) {
            pw.println(openingTag + " </Element>");
            return;
        }
        pw.println(openingTag);
        for (UIElement child : children) {
            printChildren(pw, child, prefix + "\t");
        }
        if (notFound != null) {
            pw.println(prefix + "\t<!-- NOT FOUND: " + notFound + " -->");
        }
        pw.println(prefix + "</Element>");
    }

    private UIElement getUIElementById(EnumMapper uiElements, int id, List<UIElement> elements, String prefix) {
        for (UIElement ui : elements) {
            int uiElementId = ui.getIdentifier();
            String name = uiElements.getString(uiElementId);
            System.out.println(String.format("%s%s (%s) <==> %s", prefix, name, uiElementId, id));
            if (uiElementId == id) {
                return ui;
            }
            UIElement foundElement = getUIElementById(uiElements, id, ui.getChildElements(), prefix + "\t");
            if (foundElement != null) {
                return foundElement;
            }
        }
        return null;
    }
}
