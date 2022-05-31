package delta.games.lotro.tools.dat.misc;

import delta.common.utils.files.FilesDeleter;
import delta.games.lotro.dat.data.*;
import delta.games.lotro.dat.data.enums.DIDMapper;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.ui.*;
import delta.games.lotro.dat.loaders.DataIdMapLoader;
import delta.games.lotro.dat.utils.DatIconsUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.*;

public class UISkinLoader {

    private final DataFacade facade;
    private final UILayoutLoader uiLoader;
    private final EnumMapper artAssetMapper;
    private final EnumMapper uiElementMapper;
    private final EnumMapper uiElementTypeMapper;
    private final Map<Integer, UIElement> elementLookup;
    private final Map<Integer, UILayout> layoutLookup;
    private final Map<String, List<UIElement>> skinPanels;
    private final Map<Integer, String> images;
    private final File outputFolder;
    private final JSONObject jsonImages;
    private final JSONObject jsonPanels;
    private final Set<Integer> baseLayouts;
    private final Set<Integer> baseElements;
    private final  boolean dumpAssocations;

    public UISkinLoader(DataFacade facade, File outputFolder, boolean dumpAssocations) {
        this.facade = facade;
        this.uiLoader = new UILayoutLoader(facade);
        this.artAssetMapper = facade.getEnumsManager().getEnumMapper(587202796);
        this.uiElementMapper = facade.getEnumsManager().getEnumMapper(587202769);
        this.uiElementTypeMapper = facade.getEnumsManager().getEnumMapper(587202763);
        this.elementLookup = new HashMap<>();
        this.layoutLookup = new HashMap<>();
        this.skinPanels = new TreeMap<>();
        this.images = new HashMap<>();
        this.baseLayouts = new HashSet<>();
        this.baseElements = new HashSet<>();
        this.outputFolder = outputFolder;
        this.dumpAssocations = dumpAssocations;
        this.jsonImages = new JSONObject();
        this.jsonPanels = new JSONObject();
    }

    public void doIt() {

        FilesDeleter deleter=new FilesDeleter(outputFolder,null,true);
        deleter.doIt();
        outputFolder.mkdirs();
        buildImages();
        buildUILayout();

        if (this.dumpAssocations) {
            try (PrintWriter fw = new PrintWriter(new FileWriter(new File(outputFolder, "SkinData.json")))) {
                JSONObject data = new JSONObject();
                data.put("panels", this.jsonPanels);
                data.put("assets", this.jsonImages);
                data.write(fw);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void buildImages() {
        try {
            int[] maps = new int[]{ 0x78000011, 0x78000009 };
            List<String> mappings = new LinkedList<>();
            Map<String, Object> availableAssets = new HashMap<>();
            for (int assetId : artAssetMapper.getTokens()) {
                availableAssets.put(artAssetMapper.getLabel(assetId), assetId);
            }
            for (int mapId : maps) {
                PropertiesSet props = facade.loadProperties(mapId);
                Object[] array = (Object[]) props.getProperty("UISkin_MappingArray");

                for (Object entry : array) {
                    PropertiesSet entryProps = (PropertiesSet) entry;
                    int dataId = (Integer) entryProps.getProperty("UISkin_DataID");
                    int assetID = (Integer) entryProps.getProperty("UISkin_UIArtAssetID");
                    String assetName = artAssetMapper.getLabel(assetID);
                    availableAssets.remove(assetName);
                    File to = writeImageToDisk(dataId, mapId == 0x78000009 ? "monsterplay" : null, assetName);
                    mappings.add(String.format("\t<Mapping ArtAssetID=\"%s\" FileName=\"%s\"></Mapping>", assetName, to.getName()));
                }
            }
            PrintWriter fw = new PrintWriter(new FileWriter(new File(outputFolder, "SkinDefinitions.xml")));
            fw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            fw.println("<opt>");
            fw.println("\t<SkinName Name=\"UIArtPackTemplate\"></SkinName>");
            Collections.sort(mappings);
            for (String mapping : mappings) {
                fw.println(mapping);
            }
            fw.println("</opt>");
            fw.close();

            if (dumpAssocations) {
                try (PrintWriter sfw = new PrintWriter(new FileWriter(new File(outputFolder, "UnusedAssetIDs.json")))) {
                    JSONObject data = new JSONObject(availableAssets);
                    data.write(sfw);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // these images aren't in skinning but are useful for plugins
            Map<Integer, String> cursors = new HashMap<Integer, String>(){{
                put(0x410000DD, "cursor_move");
                put(0x41007DC7, "cursor_pointer");
                put(0x41007E20, "cursor_resize_diagonal_backward");
                put(0x410081BF, "cursor_resize_horizontal");
                put(0x410081C0, "cursor_resize_vertical");
                put(0x410081C5, "cursor_busy");
                put(0x4101973F, "cursor_resize_diagonal_forward");;
            }};
            for (Map.Entry<Integer, String> e : cursors.entrySet()) {
                writeImageToDisk(e.getKey(), null, e.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File writeImageToDisk(int dataId, String subFolder, String assetName) throws JSONException, IOException {
        JSONObject rec = new JSONObject();
        File to = null;
        File base = subFolder == null ? outputFolder : new File(outputFolder, subFolder);
        File toTga = new File(base, assetName + ".tga");
        File toJpg = new File(base, assetName + ".jpg");
        this.images.put(dataId, assetName);
        boolean exists;
        if (exists = toTga.exists()) {
            to = toTga;
        } else if (exists = toJpg.exists()) {
            to = toJpg;
        }
        if (!exists) {
            // skins save images in tga format
            //boolean ret = DatIconsUtils.buildImageFile(facade, dataId, "tga", to);
            DecodedImage decoded = DatIconsUtils.loadDecodedImage(facade, dataId);
            BufferedImage image = decoded.getImage();
            SurfaceHeader header = decoded.getSourceHeader();
            rec.put("w", image.getWidth());
            rec.put("h", image.getHeight());
            String type = "tga";
            to = toTga; //new File(base, assetName + ".png");
            if (header.getFormat() == ImageFormat.FORMAT_JPEG) {
                to = toJpg;
                type = "jpg";
            }
            to.getAbsoluteFile().getParentFile().mkdirs();
            boolean ret= ImageIO.write(image, type, to);
            if (!ret) {
                System.out.println(to + " failed to save.");
            }
        }
        String imageIdHex = String.format("0x%08X", dataId);
        //rec.put("id", imageIdHex);
        rec.put("n", assetName);
        if (this.dumpAssocations) jsonImages.put(imageIdHex, rec);
        return to;
    }

    private String capitalize(String phrase, boolean lowerFirst) {
        String[] words = phrase.split("[\\W_]+");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i == 0 && lowerFirst) {
                word = word.isEmpty() ? word : word.toLowerCase();
            } else {
                word = word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
            }
            builder.append(word);
        }
        return builder.toString();
    }

    private void buildUILayout() {

        byte[] data = facade.loadData(0x28000000);
        int uiId = DataIdMapLoader.decodeDataIdMap(data).getDataIdForLabel("UILAYOUT");
        DIDMapper map = DataIdMapLoader.decodeDataIdMap(facade.loadData(uiId));
        Map<Integer, String> possibleHiddenSkins = new HashMap<>();
        for (String label : map.getLabels()) {
            int dataId = map.getDataIdForLabel(label);
            if (dataId == 0) continue;
            try {
                UILayout layout = uiLoader.loadUiLayout(dataId);
                if (layout == null) continue;

                int skinCount = skinPanels.size();
                buildLookup(layout);
                if (skinPanels.size() == skinCount) {
                    possibleHiddenSkins.put(dataId, label);
                }
            } catch (Exception e) {
            }
        }
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

        Map<String, List<UIElement>> hiddenSkinPanels = new TreeMap<>();
        for (Map.Entry<Integer, String> e : possibleHiddenSkins.entrySet()) {
            UILayout layout = layoutLookup.get(e.getKey());
            String label = e.getValue();
            // skip if layout is a base
            if (this.baseLayouts.contains(e.getKey())) continue;

            List<UIElement> children = new LinkedList<>();
            for (UIElement ui : layout.getChildElements()) {
                if (!baseElements.contains(ui.getIdentifier())) {
                    children.add(ui);
                }
            }
            String firstChildName = !children.isEmpty() ? uiElementMapper.getLabel(children.get(0).getIdentifier()) : "";
            String name =  capitalize(label, false);
            if (firstChildName.toLowerCase().startsWith(name.toLowerCase())) {
                name = firstChildName.substring(0, name.length());
            }
            hiddenSkinPanels.put("ID_UISkin_" + name, children);
        }

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
            fw.println("<!--");
            fw.println("\tNOTE: There are sizing issues for common base elements.  Please use this ONLY for reference.  Use the official U25 version for now.");
            fw.println("-->");
            //for (Map.Entry<String, UIElement> panel : Collections.singletonMap("ID_UISkin_AccomplishmentPanel", skinPanels.get("ID_UISkin_AccomplishmentPanel")).entrySet()) {
            for (Map.Entry<String, List<UIElement>> panel : skinPanels.entrySet()) {
                JSONArray panelRecs = new JSONArray();
                fw.println(String.format("<PanelFile ID=\"%s\">", panel.getKey()));
                for (UIElement e : panel.getValue()) {
                    JSONObject rec = new JSONObject();
                    printChildren(fw, e, rec, "\t");
                    panelRecs.put(rec);
                }
                fw.println("</PanelFile>");
                if (this.dumpAssocations) this.jsonPanels.put(panel.getKey(), panelRecs);
            }
            fw.println("<!--");
            fw.println("\tNOTE: The following are not official PanelFiles for skinning but can be used to modify some elements in the UI.\nLayout and sizing are not guaranteed to work.");
            fw.println("-->");
            for (Map.Entry<String, List<UIElement>> panel : hiddenSkinPanels.entrySet()) {
                JSONArray panelRecs = new JSONArray();
                fw.println("<!--");
                fw.println("\tUnofficial PanelFile: " + panel.getKey());
                fw.println("-->");
                fw.println(String.format("<PanelFile ID=\"%s\">", panel.getKey()));
                for (UIElement e : panel.getValue()) {
                    JSONObject rec = new JSONObject();
                    printChildren(fw, e, rec, "\t");
                    panelRecs.put(rec);
                }
                fw.println("</PanelFile>");
                if (this.dumpAssocations) this.jsonPanels.put(panel.getKey(), panelRecs);
            }

            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void buildLookup(UILayout l) {
        layoutLookup.put(l.getIdentifier(), l);
        for (UIElement child : l.getChildElements()) {
            buildLookup(l.getIdentifier(), child);
        }
    }

    private void buildLookup(int layoutId, UIElement ui) {
        int id = ui.getIdentifier();
        String name = uiElementMapper.getLabel(id);
        elementLookup.put(id, ui);
        int bld = ui.getBaseLayoutDID();
        if (bld > 0) {
            if (bld != layoutId) {
                this.baseLayouts.add(bld);
            }
            if(!layoutLookup.containsKey(bld)) {
                try {
                    buildLookup(uiLoader.loadUiLayout(bld));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        int blid = ui.getBaseElementId();
        if (blid > 0) {
            this.baseElements.add(blid);
        }
        Object templates = ui.getProperties().getProperty("UICore_ListBox_entry_templates");
        if (templates != null) {
            for (Object cur : (Object[]) templates) {
                PropertiesSet ps = (PropertiesSet) cur;
                int childId = (Integer) ps.getProperty("UICore_ListBox_entry_template_element");
                this.baseElements.add(childId);
            }
        }
        String skin = (String) ui.getProperties().getProperty("UISkin_UserElementID");
        if (skin != null) {
            skinPanels.put(skin, Collections.singletonList(ui));
        }
        for (UIElement child : ui.getChildElements()) {
           buildLookup(layoutId, child);
        }
    }

    private void printChildren(PrintWriter pw, UIElement ui, JSONObject rec, String prefix) throws JSONException {

        int id = ui.getIdentifier();
        String name = uiElementMapper.getString(id);
        Rectangle r = ui.getRelativeBounds();
        // {UICore_Element_ObjectMode=UICore_Element_ObjectMode: 1 (Stretch)} <-- Investigate
        //UICore_Element_ObjectMode -> {PropertiesSet$PropertyValue@1120} "UICore_Element_ObjectMode: 3 (CanvasResize)"
        // See which UIElements are tied to skinning images
        Map<String, String> assets = buildElementAssetMap(name, ui);

        List<UIElement> children = new LinkedList<>();
        int base = ui.getBaseElementId();
        int baseLayout = ui.getBaseLayoutDID();
        String notFound = null;
        if (base > 0) {
            try {
                UIElement baseEl = buildBaseEl(base);
                if (baseEl != null) {
                    if (!baseEl.getChildElements().isEmpty()) {
                        baseEl = this.constrainElementTo(baseEl, r);
                    }
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
                    assets.putAll(buildElementAssetMap(name, baseEl));
                    if (!baseEl.getChildElements().isEmpty()) {
                        children.addAll(baseEl.getChildElements());
                    }
                } else {
                    // THIS SHOULD NOT HAPPEN!
                    // This is where the two elements manually defined
                    // above would reach. :(
                    String baseName = uiElementMapper.getLabel(base);
                    notFound = String.format("%s (%s)", baseName, base);
                    /*
                    if (baseLayout > 0) {
                        // the layouts should have already been found at this point. no reason to have to look it up
                        UILayout layout = uiLoader.loadUiLayout(baseLayout);
                        baseEl = getUIElementById(uiElementMapper, base, layout.getChildElements(), "");
                        System.out.println(baseEl);
                    }
                    */
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        children.addAll(ui.getChildElements());

        String openingTag = String.format(prefix + "<Element ID=\"%s\" X=\"%s\" Y=\"%s\" Width=\"%s\" Height=\"%s\">", name, (int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());

        rec.put("id", name);
        if (this.dumpAssocations && !assets.isEmpty()) {
            // I want keys and values to be in same order
            // so can't use keys & values functions
            /*
            List<String> keys = new LinkedList<>();
            List<String> values = new LinkedList<>();
            for (Map.Entry<String, String> e : assets.entrySet()) {
                keys.add(e.getKey());
                values.add(e.getValue());
            }
            String artAssetID = CollectionTools.stringListAsString(keys, ",", null, null);
            String imageID = CollectionTools.stringListAsString(values, ",", null, null);
            openingTag = String.format(prefix + "<Element ID=\"%s\" ArtAssetID=\"%s\" ImageID=\"%s\" X=\"%s\" Y=\"%s\" Width=\"%s\" Height=\"%s\">", name, artAssetID, imageID, (int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
            */
            rec.put("assets", new JSONArray(new LinkedList(assets.values())));
        }
        JSONObject bounds = new JSONObject();
        bounds.put("w", (int) r.getWidth());
        bounds.put("h", (int) r.getHeight());
        bounds.put("x", (int) r.getX());
        bounds.put("y", (int) r.getY());
        rec.put("b", bounds);

        if (children.isEmpty() && notFound == null) {
            pw.println(openingTag + " </Element>");
            return;
        }
        pw.println(openingTag);
        JSONArray jsonChildren = new JSONArray();
        for (UIElement child : children) {
            JSONObject cRec = new JSONObject();
            printChildren(pw, child, cRec,prefix + "\t");
            jsonChildren.put(cRec);
        }
        rec.put("c", jsonChildren);
        if (notFound != null) {
            pw.println(prefix + "\t<!-- NOT FOUND: " + notFound + " -->");
        }
        pw.println(prefix + "</Element>");
    }

    private UIElement buildBaseEl(int base) {
        UIElement el = elementLookup.get(base);
        if (el == null) return null;
        if (el.getBaseElementId() == 0) return el;

        // some base elements have base elements of their own
        // we will collapse them into one
        el = shallowCopyEl(el, true);

        while (el.getBaseElementId() > 0) {
            UIElement baseEl = elementLookup.get(el.getBaseElementId());
            if (baseEl == null)  break;
            int newBaseId = baseEl.getBaseElementId();
            if (newBaseId == el.getBaseElementId()) {
                el.setBaseElementId(0);
                el.setBaseLayoutDID(0);
            } else {
                el.setBaseElementId(newBaseId);
                el.setBaseLayoutDID(baseEl.getBaseLayoutDID());
            }
            el.getChildElements().addAll(baseEl.getChildElements());
            PropertiesSet ps = el.getProperties();
            PropertiesSet basePs = baseEl.getProperties();
            for (String name : basePs.getPropertyNames()) {
                ps.setProperty(basePs.getPropertyValueByName(name));
            }
            for (UIData d : baseEl.getData()) el.addData(d);
        }
        return el;
    }
    private UIElement shallowCopyEl(UIElement el) { return shallowCopyEl(el, false); }
    private UIElement shallowCopyEl(UIElement el, boolean addChildren) {
        UIElement newEl = new UIElement(el.getIdentifier());
        newEl.setTypeID(el.getTypeID());
        newEl.setMargins(el.getMargins().clone());
        newEl.setBaseLayoutDID(el.getBaseLayoutDID());
        newEl.setBaseElementId(el.getBaseElementId());
        PropertiesSet oldPs = el.getProperties();
        PropertiesSet ps = newEl.getProperties();
        for (String name : oldPs.getPropertyNames()) {
            ps.setProperty(oldPs.getPropertyValueByName(name));
        }
        for (UIData d : el.getData()) newEl.addData(d);
        newEl.setRelativeBounds((Rectangle) el.getRelativeBounds().clone());
        if (addChildren) newEl.getChildElements().addAll(el.getChildElements());
        return newEl;
    }

    private Map<String, UIElement> shallowCloneChildrenToLabelMap(UIElement el, String stripLabelPrefix) {
        Map<String, UIElement> children = new LinkedHashMap<>();
        for (UIElement c : el.getChildElements()) {
            String name = uiElementMapper.getLabel(c.getIdentifier()).replaceAll(stripLabelPrefix, "").replaceAll("_", "");
            children.put(name.toLowerCase(), shallowCopyEl(c, true));
        }
        return children;
    }

    private UIElement constrainElementTo(UIElement el, Rectangle r) {
        UIElement newEl = shallowCopyEl(el);
        newEl.setRelativeBounds((Rectangle) r.clone());

        String name = uiElementMapper.getLabel(el.getIdentifier());
        switch (name) {
            case "Scrollbar2H": {
                int recWidth = (int) r.getWidth();
                Map<String, UIElement> children = shallowCloneChildrenToLabelMap(el, "(^(scroll_|widget_)|_field$)");
                Rectangle bRec = children.get("bottom").getRelativeBounds();
                Rectangle tRec = children.get("top").getRelativeBounds();
                Rectangle mRec = children.get("middle").getRelativeBounds();
                Rectangle wRec = children.get("widget").getRelativeBounds();
                mRec.setSize((int) (recWidth - tRec.getWidth() - bRec.getWidth()), (int) mRec.getHeight());
                Rectangle rbRec = children.get("rightbutton").getRelativeBounds();
                Rectangle lbRec = children.get("leftbutton").getRelativeBounds();
                rbRec.setLocation((int) (recWidth - rbRec.getWidth()), (int)rbRec.getY());
                wRec.setSize((int) (recWidth - rbRec.getWidth() - lbRec.getWidth()), (int) wRec.getHeight());
                bRec.setLocation((int) (recWidth - bRec.getWidth()), (int)bRec.getY());

                Map<String, UIElement> wc = shallowCloneChildrenToLabelMap(children.get("widget"), "(^(scroll_|widget_)|_field$)");
                bRec = wc.get("bottom").getRelativeBounds();
                tRec = wc.get("top").getRelativeBounds();
                mRec = wc.get("mid").getRelativeBounds();
                bRec.setLocation((int) (wRec.getWidth() - bRec.getWidth()), (int) bRec.getY());
                mRec.setSize((int)(wRec.getWidth() - bRec.getWidth() - tRec.getWidth()), (int) mRec.getHeight());
                children.get("widget").getChildElements().clear();
                children.get("widget").getChildElements().addAll(wc.values());

                newEl.getChildElements().addAll(children.values());
                break;
            }
            case "Scrollbar2":
            case "Scrollbar_10": {
                int recWidth = (int) r.getWidth();
                int recHeight = (int) r.getHeight();
                Map<String, UIElement> children = shallowCloneChildrenToLabelMap(el, "(^(scroll_|widget_)|_field$)");
                Rectangle bRec = children.get("bottom").getRelativeBounds();
                Rectangle tRec = children.get("top").getRelativeBounds();
                Rectangle mRec = children.get("middle").getRelativeBounds();
                Rectangle wRec = children.get("widget").getRelativeBounds();
                mRec.setSize((int) mRec.getWidth(), (int) (recHeight - tRec.getHeight() - bRec.getHeight()));
                int extraButtons = 0;
                if (children.containsKey("upbutton") && children.containsKey("downbutton")) {
                    Rectangle uRec = children.get("upbutton").getRelativeBounds();
                    Rectangle dRec = children.get("downbutton").getRelativeBounds();
                    uRec.setLocation((int) uRec.getX(), (int)(recHeight - uRec.getHeight()));
                    extraButtons = (int) (uRec.getHeight() + dRec.getHeight());
                }
                wRec.setSize((int) wRec.getWidth(), recHeight - extraButtons);
                bRec.setLocation((int) bRec.getX(), (int)(recHeight - bRec.getHeight()));
                newEl.getChildElements().addAll(children.values());

                Map<String, UIElement> wc = shallowCloneChildrenToLabelMap(children.get("widget"), "(^(scroll_|widget_)|_field$)");
                bRec = wc.get("bottom").getRelativeBounds();
                tRec = wc.get("top").getRelativeBounds();
                mRec = wc.get("mid").getRelativeBounds();
                bRec.setLocation((int) bRec.getX(), (int) (wRec.getHeight() - bRec.getHeight()));
                mRec.setSize( (int)mRec.getWidth(), (int)(wRec.getHeight() - bRec.getHeight() - tRec.getHeight()));
                children.get("widget").getChildElements().clear();
                children.get("widget").getChildElements().addAll(wc.values());
                break;
            }
            case "Button_PrettyBuyNowButton_Reference":
            case "Button_PrettyBuyNowButton": {
                int recWidth = (int) r.getWidth();
                Map<String, UIElement> children = shallowCloneChildrenToLabelMap(el, "(?i)^(Button_[A-Za-z]+)_");
                Rectangle mRec = children.get("middle").getRelativeBounds();
                Rectangle mlRec = children.get("midleft").getRelativeBounds();
                Rectangle mrRec = children.get("midright").getRelativeBounds();
                Rectangle rRec  = children.get("right").getRelativeBounds();
                rRec.setLocation(recWidth - 43, (int) rRec.getY());
                mRec.setLocation((int) (rRec.getX() / 2) + 6, (int) rRec.getY());
                mlRec.setSize((int) Math.round( 21 / 139.0 * recWidth), (int) mlRec.getHeight());
                mlRec.setLocation((int) Math.round( 34 / 139.0 * recWidth), (int) mlRec.getY());
                mrRec.setSize((int) Math.round( 18 / 139.0 * recWidth), (int) mrRec.getHeight());
                mrRec.setLocation((int) Math.round( 81 / 139.0 * recWidth), (int) mrRec.getY());
                newEl.getChildElements().addAll(children.values());
                // TODO: The label needs sized
                break;
            }
            case "Button_General_Use_Medium":
            case "Button_General_Use_Medium_BuyNow":
            case "BuyNowTextButtonReference_Gold":
            case "Chargen_Button_Small_MP":
            case "MP_Chargen_Button_Freep":
            case "ShowHideTextButton":
            case "TextButtonAdmin":
            case "TextButton_FIELD_Reference":
            case "TextButton_Gold":
            case "TextButton": {
                int recWidth = (int) r.getWidth();
                Map<String, UIElement> children = shallowCloneChildrenToLabelMap(el, "(?i)^(TextButton_Highlight|Button_Allegiance_Join|Button_Collections|Button_General_Use_New|Button_General_Use|BuyNowTextButtonReference|CharGen_Buttons_Create_LeftButton|CharGen_Buttons_Create_RightButton|CharGen_Buttons_Triskel|Chargen_Button_Generic|Chargen_Button_Large|Chargen_Button_Monster|Chargen_Button_Small|TextButtonReference|Button_[A-Za-z]+)_");
                Rectangle mRec = children.get("mid").getRelativeBounds();
                Rectangle lRec = children.get("left").getRelativeBounds();
                Rectangle rRec = children.get("right").getRelativeBounds();

                UIElement tmp = children.remove("highlight");
                if (tmp != null) children.put("r", tmp);
                tmp = children.remove("highlightl");
                if (tmp != null) children.put("l", tmp);

                mRec.setSize((int) (recWidth - lRec.getWidth() - rRec.getWidth() + 1), (int) mRec.getHeight());
                rRec.setLocation((int) (recWidth - rRec.getWidth()), (int) rRec.getY());
                if (children.containsKey("l") && children.containsKey("r")) {
                    Rectangle lRec2 = children.get("l").getRelativeBounds();
                    Rectangle rRec2 = children.get("r").getRelativeBounds();
                    rRec2.setLocation((int) (recWidth - rRec2.getWidth() - lRec2.getX() - 1), (int) rRec2.getY());
                }
                newEl.getChildElements().addAll(children.values());
                break;
            }
            case "Tab_Tier1_Left": {
                UIElement oldTab = el.getChildElements().get(0);
                UIElement newTab = shallowCopyEl(oldTab);
                int tabWidth = (int) (r.getWidth() - 1);
                newTab.getRelativeBounds().setSize(tabWidth, (int) r.getHeight());
                newEl.getChildElements().add(newTab);
                Map<String, UIElement> children = shallowCloneChildrenToLabelMap(oldTab, "^tab_front_");
                Rectangle nRec = children.get("n").getRelativeBounds();
                Rectangle wRec = children.get("w").getRelativeBounds();
                Rectangle eRec = children.get("e").getRelativeBounds();
                nRec.setSize((int) (tabWidth - wRec.getWidth() - eRec.getWidth() + 3), (int) nRec.getHeight());
                eRec.setLocation((int) (tabWidth - eRec.getWidth() + 1), (int) eRec.getY());
                newTab.getChildElements().addAll(children.values());
                break;
            }
            case "Menu_01_Background":
            case "Chargen_Silver_Border":
            case "Box_multiusage":
            case "Menu_Brown_Background":
            case "Box_Silver_Tooltip":
            case "Box_Silver_BlueGradient":
            case "quest_dialog_blue_bg":
            case "TextButton_sizeable":
            case "Box_Silver": {
                int recHeight = (int) r.getHeight();
                int recWidth = (int) r.getWidth();
                Map<String, UIElement> children = shallowCloneChildrenToLabelMap(el, "(?i)^(Base_Box_multiusage_|quest_dialog_blue_bg_|TextButtonReference_|Base_Box_Silver_BlueGradient_|Base_Box_Silver_Tooltip_|Base_Box_Silver_|Box_Multiusage_|BasePanel_|Chargen_Silver_Border_|Base_Box_)");
                Rectangle mRec = children.get("topmid").getRelativeBounds();
                Rectangle lRec = children.get("topleft").getRelativeBounds();
                Rectangle rRec = children.get("topright").getRelativeBounds();
                int middleWidth = (int) (recWidth - lRec.getWidth() - rRec.getWidth());
                int rightOffset = (int) (recWidth - rRec.getWidth());
                mRec.setSize(middleWidth, (int) mRec.getHeight());
                rRec.setLocation(rightOffset, (int) rRec.getY());
                int sideHeight = (int) lRec.getHeight();
                int midHeight = (int) mRec.getHeight();

                lRec = (children.get("midleft") == null ? children.get("leftmid") : children.get("midleft")).getRelativeBounds();
                rRec = (children.get("midright") == null ? children.get("rightmid") : children.get("midright")).getRelativeBounds();
                if (children.get("background") != null) {
                    mRec = children.get("background").getRelativeBounds();
                    mRec.setSize((int) (recWidth - lRec.getWidth() - rRec.getWidth()), (recHeight - (midHeight * 2)));
                }
                rRec.setBounds((int) (recWidth - rRec.getWidth()), (int) rRec.getY(), (int) rRec.getWidth(), (recHeight - (sideHeight * 2)));
                lRec.setSize((int) lRec.getWidth(), (int) rRec.getHeight());

                mRec = children.get("bottommid").getRelativeBounds();
                lRec = children.get("bottomleft").getRelativeBounds();
                rRec = children.get("bottomright").getRelativeBounds();
                mRec.setBounds((int) mRec.getX(), (int) (recHeight - mRec.getHeight()), middleWidth, (int) mRec.getHeight());
                rRec.setLocation(rightOffset, (int) (recHeight - rRec.getHeight()));
                lRec.setLocation((int) lRec.getX(), (int) (recHeight - rRec.getHeight()));

                UIElement co = children.get("centeroverlay");
                if (co != null) {
                    Rectangle coRec = co.getRelativeBounds();
                    coRec.setSize((int)(recWidth - (2 * coRec.getX()) + 1), (int) (recHeight - (2 * coRec.getY()) - 1));
                }
                newEl.getChildElements().addAll(children.values());
                break;
            }
            case "Base_Box_Titlebar": {
                Map<String, UIElement> children = shallowCloneChildrenToLabelMap(el, "^Base_Box_Titlebar_Top");
                Rectangle mRec = children.get("mid").getRelativeBounds();
                Rectangle lRec = children.get("left").getRelativeBounds();
                Rectangle rRec = children.get("right").getRelativeBounds();
                mRec.setSize((int) (r.getWidth() - lRec.getWidth() - rRec.getWidth()), (int) mRec.getHeight());
                rRec.setLocation((int) (r.getWidth() - rRec.getWidth()), (int) rRec.getY());
                newEl.getChildElements().addAll(children.values());
                // TODO: The label needs sized
                break;
            }
            default:
                /*List<UIElement> children = el.getChildElements().stream().filter(c -> uiElementMapper.getLabel(c.getIdentifier()).matches("(?i)^.*(Scrollbar).*")).collect(Collectors.toList());
                if (children.size() > 0) {
                    System.out.println("Buttons: " + name);
                }*/
                /*
                List<String> children = new LinkedList<>();
                for (UIElement c : el.getChildElements()) {
                    Rectangle cr = c.getRelativeBounds();
                    children.add(String.format("%s (x:%s, y:%s, w:%s, h: %s, c:[%s])", uiElementMapper.getLabel(c.getIdentifier()), cr.getX(), cr.getY(), cr.getWidth(), cr.getHeight(), c.getChildElements().size()));
                }
                */
                //System.out.println("Base: " + String.format("%s (x:%s, y:%s, w:%s, h: %s)", uiElementMapper.getLabel(el.getIdentifier()), r.getX(), r.getY(), r.getWidth(), r.getHeight()) + " => " + CollectionTools.stringListAsString(children, null, ", "));
                //System.out.println("Base: " + uiElementMapper.getLabel(el.getIdentifier()) + " => " + CollectionTools.stringListAsString(children, null, ", "));
                return el;
        }

        return newEl;
    }

    private Map<String, String> buildElementAssetMap(String elementName, UIElement ui) {
        Map<String, String> assets = new TreeMap<>();
        for (UIData d : ui.getData()) {
            UIImage img = null;
            if (d instanceof UIImage) {
                img = (UIImage) d;
            } else if (d instanceof UIStateData) {
                UIData stateData = ((UIStateData) d)._data;
                if (stateData instanceof UIImage) {
                    img = (UIImage) stateData;
                } else if (stateData instanceof UIAnimation) {
                    img = ((UIAnimation) stateData).frames.get(0);
                    //dumpAnimation(elementName, ((UIStateData) d)._stateLabel, ((UIAnimation) stateData));
                }
            } else if (d instanceof UIAnimation) {
                img = ((UIAnimation) d).frames.get(0);
            }
            if (img != null) {
                int imageId = img._imageDID;
                if (imageId > 0) {
                    String imageID = String.format("0x%08X", imageId);
                    String artAssetID = images.get(imageId);
                    if (artAssetID == null) {
                        /*artAssetID = elementName.toLowerCase();
                        System.out.println("Image not found: " + imageID + " for " + elementName);
                        File to = new File(outputFolder, artAssetID + ".tga");
                        this.images.put(imageId, artAssetID);
                        if (!to.exists()) {
                            // skins save images in tga format
                            DatIconsUtils.buildImageFile(facade, imageId, "tga", to);
                        }*/
                    }
                    if (artAssetID != null) assets.put(artAssetID, imageID);
                }
            }
        }
        return assets;
    }

    /*
    private void dumpAnimation(String elementName, String stateLabel, UIAnimation animation) {
        String prefix = String.format("%s_%s", elementName, stateLabel);
        File to = new File(outputFolder, prefix + ".apng");

        if (!to.exists()) {
            List<File> images = new LinkedList<>();
            try {
                // skins save images in tga format
                ApngBuilder builder = new ApngBuilder();
                int delay = Math.round(animation.duration * 1000 / animation.frames.size());
                for (UIImage img : animation.frames) {
                    File tmp = File.createTempFile(prefix, ".png");
                    ImageIO.write(DatIconsUtils.loadImage(facade, img._imageDID), "png", tmp);
                    images.add(tmp);
                }
                Png apng = builder.buildPng(images.toArray(new File[0]), delay);
                apng.savePng(to);
            } catch (IOException ex) {
                for (File tmp : images) {
                    try { tmp.delete(); } catch (Exception e) {}
                }
            }
        }
    }
    */

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.out.println("USAGE:\n\t" + UISkinLoader.class.getName() + " <out folder> [<dump associations> (true|false)]\n");
            System.exit(0);
        }
        File outputFolder = new File(args[0]);
        if (!outputFolder.exists()) outputFolder.mkdirs();
        DataFacade facade = new DataFacade();
        UISkinLoader skinning = new UISkinLoader(facade, outputFolder, args.length > 1 && "true".equals(args[1]));
        skinning.doIt();
        facade.dispose();
    }

}
