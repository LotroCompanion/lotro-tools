--[[
-----------------------------------------------------------------------------------------------------------------------------------------------------------

LotRO Dropdown class v.2.0 By Galuhad
http://www.lotrointerface.com/list.php?skinnerid=3762

This class creates a LOTRO style drop-down menu
This class uses the AddCallback() function by Garan for assigning events. Please make sure you use
the same function if you need to assign any additional events.

WARNING:	Plugins using version 1.x of this class will need a bit of recoding due to a number of changes to the
			constructor and methods used.


-- USAGE --
someDropDown = DropDown(list,default)	-- list is the table of strings to include, default [optional] is the string to display on creation.

-- METHODS --
DropDown:Close();							-- Forces the dropdown list to close, needed for customised lists
DropDown:GetAlignment();					-- Gets the alignment of the text
DropDown:GetListBox();						-- Returns the listbox used in the dropdown for customised lists
DropDown:GetMaxItems();						-- Gets the number of items displayed in the dropdown before a scrollbar is used
DropDown:GetText();							-- Gets the text in the dropdown
DropDown:IsEnabled();
DropDown:Reset();							-- Scrolls back to the top item in the list
DropDown:SetAlignment(ContentAlignment);	-- Sets the text alignment for the labels in the dropdown
DropDown:SetEnabled(value);
DropDown:SetParent(control);
DropDown:SetPosition(left,top);
DropDown:SetMaxItems(value);				-- Sets the number of items displayed in the dropdown before a scrollbar is used
DropDown:SetText(string);					-- Sets the text of the dropdown
DropDown:SetVisible(value);
DropDown:SetWidth(value);

-- DISABLED METHODS --
SetHeight(), SetSize()						-- Only width can be set using SetWidth(value)

-- EVENTS --
DropDown.ItemChanged(Sender,Args)			-- Event sender, Event Args (Args.Index,Args.Text)


-- EXAMPLE --
local listTable = {"First Label","Second Label","Third Label"};	-- table of strings used to create the dropdown
someDropDown = DropDown(listTable);
someDropDown:SetParent(control);
someDropDown:SetPosition(20,50);

someDropDown.ItemChanged = function (Sender,Args)
	Turbine.Shell.WriteLine("Index:"..Args.Index..",  Text:"..Args.Text);
end

-----------------------------------------------------------------------------------------------------------------------------------------------------------
--]]

DropDown = {};
_mtDropDown = {};


function DropDown.Constructor(sender,_list,defaultLabel)

	if defaultLabel==nil then
		if type(_list)=='table' and _list[1]~=nil then
			defaultLabel=_list[1]
		else
			defaultLabel=""
		end
	end

	-- Main Control --
	local ddLabelContainer = Turbine.UI.Control();
	ddLabelContainer:SetSize(159,19);
	ddLabelContainer:SetBackColor(Turbine.UI.Color(0.63,0.63,0.63));

	local ddLabelBack = Turbine.UI.Control();
	ddLabelBack:SetParent(ddLabelContainer);
	ddLabelBack:SetSize(ddLabelContainer:GetWidth()-4,ddLabelContainer:GetHeight()-4);
	ddLabelBack:SetPosition(2,2);
	ddLabelBack:SetBackColor(Turbine.UI.Color(0,0,0));
	ddLabelBack:SetMouseVisible(false);

	local lblSelected = Turbine.UI.Label();
	lblSelected:SetParent(ddLabelBack);
	lblSelected:SetSize(ddLabelBack:GetWidth()-15,ddLabelBack:GetHeight());
	lblSelected:SetForeColor(Turbine.UI.Color((229/255),(209/255),(136/255)));
	lblSelected:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleCenter);
	lblSelected:SetFont(Turbine.UI.Lotro.Font.TrajanPro14);
	lblSelected:SetMultiline(false);
	lblSelected:SetMouseVisible(false);
	lblSelected:SetText(defaultLabel);

	local arrow = Turbine.UI.Control();
	arrow:SetParent(ddLabelBack);
	arrow:SetSize(14,14);
	arrow:SetPosition((ddLabelBack:GetWidth()-15),(ddLabelBack:GetHeight()-15));
	arrow:SetBackground(0x41007e18);
	arrow:SetBlendMode(4);
	arrow:SetMouseVisible(false);

	local greyBox = Turbine.UI.Window();
	greyBox:SetParent(ddLabelContainer);
	greyBox:SetPosition(2,2);
	greyBox:SetBackColor(Turbine.UI.Color(0.65,0,0,0));
	greyBox:SetVisible(false);

	-- DropDown List --
	local ddListContainer = Turbine.UI.Window();
	ddListContainer:SetSize(ddLabelContainer:GetWidth(),0);
	ddListContainer:SetBackColor(Turbine.UI.Color(0.63,0.63,0.63));
	ddListContainer["ChildFocus"] = false;
	ddListContainer["MaxItems"] = 8;
	ddListContainer["Selected"] = 0;

	ddListContainer.FocusLost = function ()
		if ddListContainer.ChildFocus == false then ddListContainer:Close() end;
	end

	local ddListBack = Turbine.UI.Control();
	ddListBack:SetParent(ddListContainer);
	ddListBack:SetSize(ddListContainer:GetWidth()-4,0);
	ddListBack:SetPosition(2,2);
	ddListBack:SetBackColor(Turbine.UI.Color(0.9,0,0,0));

	local ddListBox = Turbine.UI.ListBox();
	ddListBox:SetParent(ddListBack);
	ddListBox:SetSize(ddListBack:GetWidth()-15,0);

	local sbList = Turbine.UI.Lotro.ScrollBar();
	sbList:SetParent(ddListBack);
	sbList:SetOrientation(Turbine.UI.Orientation.Vertical);
	sbList:SetWidth(8);
	sbList:SetPosition(ddListBox:GetLeft()+ddListBox:GetWidth()+2,0);
	sbList:SetVisible(false);
	ddListBox:SetVerticalScrollBar(sbList);

	sbList.MouseEnter = function ()
		ddListContainer.ChildFocus = true;
	end

	sbList.MouseLeave = function ()
		ddListContainer.ChildFocus = false;
		ddListContainer:Focus();
	end

	if type(_list) == 'table' then
		for k,v in ipairs(_list) do
			local cItemContainer = Turbine.UI.Control();
			cItemContainer:SetSize(ddListBox:GetWidth(),18);

			local lblItem = Turbine.UI.Label();
			lblItem:SetParent(cItemContainer);
			lblItem:SetSize(cItemContainer:GetSize());
			lblItem:SetFont(Turbine.UI.Lotro.Font.TrajanPro14);
			lblItem:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleCenter);
			lblItem:SetMultiline(false);
			lblItem:SetMouseVisible(false);
			lblItem:SetText(v);

			if tostring(v) == tostring(defaultLabel) then
				lblItem:SetForeColor(Turbine.UI.Color.Yellow);
				ddListContainer.Selected = k;
			else
				lblItem:SetForeColor(Turbine.UI.Color((229/255),(209/255),(136/255)));
			end

			cItemContainer["Label"] = lblItem;

			cItemContainer.MouseEnter = function ()
				lblItem:SetOutlineColor(Turbine.UI.Color(0.85,0.65,0));
				lblItem:SetForeColor(Turbine.UI.Color(1,1,1));
				lblItem:SetFontStyle(8);
			end

			cItemContainer.MouseLeave = function ()
				lblItem:SetOutlineColor(Turbine.UI.Color(0,0,0));
				lblItem:SetFontStyle(0);
				if ddListContainer.Selected == k then
					lblItem:SetForeColor(Turbine.UI.Color.Yellow);
				else
					lblItem:SetForeColor(Turbine.UI.Color((229/255),(209/255),(136/255)));
				end
			end

			cItemContainer.MouseDown = function ()
				lblSelected:SetText(v);
				ddListContainer:Close();
				ddLabelContainer.ResetLabelColors();
				ddListContainer.Selected = k;
				lblItem:SetForeColor(Turbine.UI.Color.Yellow);
				ddLabelContainer.ItemChanged(cItemContainer,{["Text"]=v;["Index"]=k});
			end

			ddListBox:AddItem(cItemContainer);
		end
		if ddListContainer.Selected ~= 0 then ddListBox:EnsureVisible(ddListContainer.Selected) end;
	end

	ddLabelContainer.ResetLabelColors = function ()
		for i=1, ddListBox:GetItemCount() do
			if ddListBox:GetItem(i).Label ~= nil then ddListBox:GetItem(i).Label:SetForeColor(Turbine.UI.Color((229/255),(209/255),(136/255))) end;
		end
	end

	ddLabelContainer.RescaleList = function()
		local height = 0;
		for i=1, math.min(ddListContainer.MaxItems,ddListBox:GetItemCount()) do
			height = height + ddListBox:GetItem(i):GetHeight();
		end
		ddListBox:SetHeight(height);
		ddListBack:SetHeight(ddListBox:GetHeight());
		sbList:SetHeight(ddListBack:GetHeight());
		ddListContainer:SetHeight(ddListBox:GetHeight()+4);
		for i=1, ddListBox:GetItemCount() do
			local item = ddListBox:GetItem(i);
			item:SetWidth(ddListBox:GetWidth());
			if item.Label ~=nil then item.Label:SetWidth(item:GetWidth()) end;
		end
	end

	local controlEnter = function ()
		if ddLabelContainer:IsEnabled() == true then
			arrow:SetBackground(0x41007e1b);
			lblSelected:SetOutlineColor(Turbine.UI.Color(0.85,0.65,0));
			lblSelected:SetForeColor(Turbine.UI.Color(1,1,1));
			lblSelected:SetFontStyle(8);
		end
	end
	AddCallback(ddLabelContainer,"MouseEnter",controlEnter);

	local controlLeave = function ()
		if ddLabelContainer:IsEnabled() == true then
			arrow:SetBackground(0x41007e18);
			lblSelected:SetOutlineColor(Turbine.UI.Color(0,0,0));
			lblSelected:SetForeColor(Turbine.UI.Color((229/255),(209/255),(136/255)));
			lblSelected:SetFontStyle(0);
		end
	end
	AddCallback(ddLabelContainer,"MouseLeave",controlLeave);

	local controlDown = function ()
		ddListContainer:SetPosition(ddLabelContainer:PointToScreen(0,ddLabelContainer:GetHeight()-2));
		ddLabelContainer:RescaleList();
		ddListContainer:SetVisible(true);
		ddListContainer:Activate();
		ddListContainer:Focus();
	end
	AddCallback(ddLabelContainer,"MouseDown",controlDown);


	-- MEMBERS ----------------------------------------------------------------------------------------------

	-- Disabled Methods
	ddLabelContainer.SetHeight = function () end;
	ddLabelContainer.SetSize = function () end;

	-- Diverted Methods
	ddLabelContainer.ApplyWidth = ddLabelContainer.SetWidth;

	-- New Methods
	ddLabelContainer.Close = function ()
		ddListContainer:Close();
	end

	ddLabelContainer.GetAlignment = function ()
		return lblSelected:GetTextAlignment();
	end

	ddLabelContainer.GetListBox = function ()
		return ddListBox;
	end

	ddLabelContainer.GetMaxItems = function ()
		return ddListContainer.MaxItems;
	end

	ddLabelContainer.GetText = function ()
		return lblSelected:GetText();
	end

	ddLabelContainer.IsEnabled = function ()
		return not greyBox:IsVisible();
	end

	ddLabelContainer.Reset = function ()
		ddListBox:EnsureVisible(1);
	end

	ddLabelContainer.SetAlignment = function (Sender,ContentAlignment)
		if ContentAlignment == nil then return end;
		lblSelected:SetTextAlignment(ContentAlignment);
		for i=1, ddListBox:GetItemCount() do
			if ddListBox:GetItem(i).Label ~= nil then ddListBox:GetItem(i).Label:SetTextAlignment(ContentAlignment) end;
		end
	end

	ddLabelContainer.SetEnabled = function (Sender,Value)
		if Value == false then
			greyBox:SetWidth(ddLabelContainer:GetWidth()-4);
			greyBox:SetHeight(ddLabelContainer:GetHeight()-4);
			greyBox:SetVisible(true);
		else
			greyBox:SetVisible(false);
		end
	end

	ddLabelContainer.SetMaxItems = function (Sender,Value)
		if Value == nil or type(Value) ~= 'number' then return end;
		ddListContainer.MaxItems = Value;
	end

	ddLabelContainer.SetText = function (Sender,Text)
		if Text == nil then return end;
		lblSelected:SetText(Text);
		ddLabelContainer.ResetLabelColors();
		for i=1,ddListBox:GetItemCount() do
			if ddListBox:GetItem(i).Label ~= nil and ddListBox:GetItem(i).Label:GetText() == Text then
				ddListContainer.Selected = i;
				ddListBox:GetItem(i).Label:SetForeColor(Turbine.UI.Color.Yellow);
				ddListBox:EnsureVisible(ddListContainer.Selected);
				break;
			end
		end
	end

	ddLabelContainer.SetWidth = function (Sender,Width)
		if Width == nil then return end;
		ddLabelContainer:ApplyWidth(Width);
		ddLabelBack:SetWidth(Width-4);
		lblSelected:SetWidth(Width-19);
		arrow:SetLeft(Width-19);
		ddListContainer:SetWidth(Width);
		ddListBox:SetWidth(Width-19);
		ddListBack:SetWidth(Width-4);
		sbList:SetLeft(ddListBox:GetLeft()+ddListBox:GetWidth()+2);
	end

	-- Events
	ddLabelContainer.ItemChanged = function () end;

	return ddLabelContainer;

end


function _mtDropDown.__call(...)
    return DropDown.Constructor(...);
end

setmetatable(DropDown, _mtDropDown);
