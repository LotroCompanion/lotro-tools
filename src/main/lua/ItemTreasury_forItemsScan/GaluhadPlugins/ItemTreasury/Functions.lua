
function RegisterEditEvent(control)	 -- Filters numbers only
	if control == nil then return end;

	control.TextChanged = function ()
		control:SetText(string.match(control:GetText(),'%d*%,*%d*%.*%d*')); 	-- eg 1,000 or 102.5 or 1,263.87
	end

	control.FocusGained = function()
		control:SetWantsKeyEvents(true);
	end

	control.FocusLost = function()
		control:SetWantsKeyEvents(false);
	end
end


_replaceStr={[128]="A",[129]="A",[130]="A",[131]="A",[132]="A",[133]="A",[134]="AE",[135]="C",[136]="E",[137]="E",[138]="E",[139]="E",[140]="I",[141]="I",[142]="I",[143]="I",[144]="",[145]="N",[146]="O",[147]="O",[148]="O",[149]="O",[150]="O",[151]="",[152]="O",[153]="U",[154]="U",[155]="U",[156]="U",[157]="Y",[158]="Y",[159]="sz",[160]="a",[161]="a",[162]="a",[163]="a",[164]="a",[165]="a",[166]="ae",[167]="c",[168]="e",[169]="e",[170]="e",[171]="e",[172]="i",[173]="i",[174]="i",[175]="i",[176]="o",[177]="n",[178]="o",[179]="o",[180]="o",[181]="o",[182]="o",[183]="",[184]="o",[185]="u",[186]="u",[187]="u",[188]="u",[189]="y",[190]="y",[191]="y"};

function StripAccent(str)	-- Function by Garan
	local ret = "";
	local replace=false
	for i,v in ipairs({str:byte(1,-1)}) do
		if replace then
			replace=false
			if _replaceStr[v]~=nil then
				ret=ret.._replaceStr[v]
			end
		else
			if (v==195) then
				replace=true
			else
				ret = ret .. string.char(v);
			end
		end
	end
	return ret;
end


function NewPagenateButton()
	local btn = Turbine.UI.Lotro.Button();
	btn:SetSize(22,20);
	return btn;
end


function NewItemInfo(itemID)
	if itemID == nil then return end;
	local itemHex = Utils.TO_HEX(itemID);

	local cItemInspect = Turbine.UI.Lotro.Quickslot();
	cItemInspect:SetSize(1,1);
	cItemInspect:SetPosition(0,0);
	cItemInspect:SetVisible(false);

	local cItemInfo = Turbine.UI.Lotro.ItemInfoControl();
	cItemInfo:SetSize(36,36);
	cItemInfo:SetAllowDrop(false);
	cItemInfo:SetVisible(true);

	local function SetInspectIcon() 	-- PCALL THIS incase item does not exist
		cItemInspect:SetShortcut(Turbine.UI.Lotro.Shortcut(Turbine.UI.Lotro.ShortcutType.Item, "0x0,0x" .. itemHex));
	end

	if pcall(SetInspectIcon) then
		cItemInspect:SetShortcut(Turbine.UI.Lotro.Shortcut(Turbine.UI.Lotro.ShortcutType.Item, "0x0,0x" .. itemHex));
		local itemInfo = cItemInspect:GetShortcut():GetItem():GetItemInfo();
		cItemInfo:SetItemInfo(itemInfo);
		cItemInfo:SetQuantity(1);
	end

	cItemInspect = nil;
	return cItemInfo;
end


function NewWindowTextBox(parent,width,height,left,top,text)
	textbox = Turbine.UI.Lotro.TextBox();
	if width == nil then width = 120 end;
	if height == nil then height = 22 end;
	if left == nil then left = 0 end;
	if top == nil then top = 0 end;
	if text == nil then text = "" end;
	if parent ~= nil then textbox:SetParent(parent) end;
	textbox:SetPosition(left,top);
	textbox:SetSize(width,height);
	textbox:SetSelectable(true);
	textbox:SetMultiline(false);
	textbox:SetForeColor(Turbine.UI.Color.Ivory);
	textbox:SetFont(Turbine.UI.Lotro.Font.Verdana14);
	textbox:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	textbox:SetText(text);
	return textbox;
end


function NewItemContainer(width,height)
	if width == nil then width = 100 end;
	if height == nil then height = 12 end;
	local newContainer = Turbine.UI.Control();
	newContainer:SetSize(width,height);
	return newContainer;
end


function NewScrollBar(control,orientation,parent)
	if control == nil then return end;
	if orientation == nil then orientation = "vertical" end;
	scrollBar = Turbine.UI.Lotro.ScrollBar();
	if parent ~= nil then scrollBar:SetParent(parent) end;
	if orientation == "horizontal" then
		scrollBar:SetSize(control:GetWidth(),11);
		scrollBar:SetPosition(control:GetLeft(),control:GetTop()+control:GetHeight()+1);
		scrollBar:SetOrientation(Turbine.UI.Orientation.Horizontal);
		control:SetHorizontalScrollBar(scrollBar);
	else	-- vertical
		scrollBar:SetSize(11,control:GetHeight());
		scrollBar:SetPosition(control:GetLeft()+control:GetWidth()+1,control:GetTop());
		scrollBar:SetOrientation(Turbine.UI.Orientation.Vertical);
		control:SetVerticalScrollBar(scrollBar);
	end
	scrollBar:SetVisible(false);
	return scrollBar;
end


function NewWindowLabel(parent,width,height,left,top,text)
	label = Turbine.UI.Label();
	if width == nil then width = 150 end;
	if height == nil then height = 18 end;
	if left == nil then left = 0 end;
	if top == nil then top = 0 end;
	if text == nil then text = "" end;
	if parent ~= nil then label:SetParent(parent) end;
	label:SetSize(width,height);
	label:SetPosition(left,top);
	label:SetFont(Turbine.UI.Lotro.Font.Verdana14);
	label:SetForeColor(Turbine.UI.Color.Khaki);
	label:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	label:SetText(text);
	return label;
end
