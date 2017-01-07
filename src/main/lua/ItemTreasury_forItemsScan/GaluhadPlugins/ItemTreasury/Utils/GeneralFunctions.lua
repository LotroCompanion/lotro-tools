
-- Converts an RGB color (0 - 255) number to hex. ---------------------------------
function TO_HEX(IN)
	local B,K,OUT,I,D=16,"0123456789ABCDEF","",0,0;

		if IN == 0 or IN == "0" then
			return "00";
		end

		while IN>0 do
		I=I+1
		IN,D=math.floor(IN/B),math.mod(IN,B)+1
		OUT=string.sub(K,D,D)..OUT
	end
	if string.len(OUT) == 1 then OUT = "0" .. OUT end;
	return OUT
end

-- This function makes sure the object is positioned on the screen and not off it
function Onscreen(OBJECT)

	local DISPLAYWIDTH = Turbine.UI.Display.GetWidth();
	local DISPLAYHEIGHT = Turbine.UI.Display.GetHeight();

	local objWidth = OBJECT:GetWidth();
	local objHeight = OBJECT:GetHeight();
	local objLeft = OBJECT:GetLeft();
	local objTop = OBJECT:GetTop();
	local objRight = objLeft + objWidth;
	local objBottom = objTop + objHeight;

	if objRight > DISPLAYWIDTH then objLeft = DISPLAYWIDTH - objWidth end;
	if objLeft < 0 then objLeft = 0 end;
	if objTop < 0 then objTop = 0 end;
	if objBottom > DISPLAYHEIGHT then objTop = DISPLAYHEIGHT - objHeight end;

	OBJECT:SetPosition(objLeft,objTop);

end


function GetIDFromItem(item)	-- doesn't work because can't set shortcut from item
	if item==nil then return end;

	local cItemInspect = Turbine.UI.Lotro.Quickslot();
	cItemInspect:SetSize(36,36);
	cItemInspect:SetParent(Windows.wBuildWin);
	cItemInspect:SetPosition(math.random()*(Windows.wBuildWin:GetWidth()-36),math.random()*(Windows.wBuildWin:GetHeight()-36));
	cItemInspect:SetVisible(true);

	cItemInspect:SetShortcut(Turbine.UI.Lotro.Shortcut(item));

	local scData = cItemInspect:GetShortcut():GetData();
	local output = "";

	for i=1, string.len(scData) do
		output = output .. string.sub(scData,i,i+1) .. " ";
	end

	cItemInspect = nil;

	--return scData;
	return output;

end


-- This function takes an item ID value (eg. 1879230113) and returns an instance of the item
function GetItemFromID(itemID)
	if itemID == nil then return end;
	local function GetHex(IN)
		local B,K,OUT,I,D=16,"0123456789ABCDEF","",0,0;
		if IN == 0 or IN == "0" then return "00" end;
		while IN>0 do
			I=I+1
			IN,D=math.floor(IN/B),math.mod(IN,B)+1
			OUT=string.sub(K,D,D)..OUT
		end
		if string.len(OUT)==1 then OUT="0"..OUT end;
		return OUT
	end
	local itemHex = GetHex(itemID);
	local cItemInspect = Turbine.UI.Lotro.Quickslot();
	cItemInspect:SetSize(1,1);
	cItemInspect:SetVisible(false);
	local function SetItemShortcut() 	-- PCALL THIS incase item does not exist
		cItemInspect:SetShortcut(Turbine.UI.Lotro.Shortcut(Turbine.UI.Lotro.ShortcutType.Item, "0x0,0x" .. itemHex));
	end
	if pcall(SetItemShortcut) then
		SetItemShortcut();
		local item = cItemInspect:GetShortcut():GetItem();
		cItemInspect = nil;
		return item;
	end
	cItemInspect = nil;
end

function GetAllItemFromID(i1, i2)

	for i=i1, i2 do
		local wholeTable = {};
		local _table = {};
		local itemInfo = GetItemFromID(i);
		if (itemInfo ~= nil) then
		local item = itemInfo:GetItemInfo();
		if (item ~= nil) then
			-- _table["ID"] = i;
			local name = item:GetName();
			_table[1] = name;
			local description = item:GetDescription();
			_table[2] = description;
			local category = item:GetCategory();
			_table[3] = category;
			local quality = item:GetQuality();
			_table[4] = quality;
			local durability = item:GetDurability();
			_table[5] = durability;
			local magic = item:IsMagic();
			_table[6] = magic;
			local unique = item:IsUnique();
			_table[7] = unique;
			local iconId = item:GetIconImageID();
			_table[8] = "0x" .. TO_HEX(iconId);
			local backgroundIconId = item:GetBackgroundImageID();
			_table[9] = "0x" .. TO_HEX(backgroundIconId);
			
			-- local maxStack = item:GetMaxStackSize();
			-- _table["MaxStackSize"] = maxStack;
			-- local maxQuantity = item:GetMaxQuantity();
			-- _table["MaxQuantity"] = maxQuantity;
			item = nil;
			wholeTable[i]=_table;
		end;
		itemInfo = nil;
		local dump = DumpItemInfo(wholeTable);
		Turbine.Shell.WriteLine(dump);
		end;
	end
end

function DumpItemInfo(_table)
	local sReturn = "{";
	for k,v in pairs (_table) do
		sReturn = sReturn .. "[" .. k .. "]=";
		if type(v) == 'table' then
			sReturn = sReturn .. DumpItemInfo(v);
		else
			if type(v) == 'number' then
				sReturn = sReturn .. v .. ";";
			else
				if type(v) == 'boolean' then
					sReturn = sReturn .. tostring(v) .. ";";
				else
					sReturn = sReturn .. "\"" .. v .. "\";";
				end
			end
		end
	end
	sReturn = sReturn .. "};";
	return sReturn;
end

Turbine.Shell.WriteLine(GetItemFromID(1879230113):GetItemInfo():GetName()); -- Crumpet
-- 19.2.2: 1879049233->1879342508
--GetAllItemFromID(1879049233,1879049233+50000);


-- This function gets the client language and returns the string value, as used in string tables.
function GetClientLanguage()

	local LANGUAGE = 1;

	if Turbine.Engine.GetLanguage() == Turbine.Language.French then
		LANGUAGE = 2;
	elseif Turbine.Engine.GetLanguage() == Turbine.Language.German then
		LANGUAGE = 3;
	elseif Turbine.Engine.GetLanguage() == Turbine.Language.Russian then
		LANGUAGE = 4;
	end

	return LANGUAGE; -- 1=English	2=French	3=German	4=Russian
end


-- This function formats a number with commas
function comma_value(amount)
  local formatted = amount
  while true do
    formatted, k = string.gsub(formatted, "^(-?%d+)(%d%d%d)", '%1,%2')
    if (k==0) then
      break
    end
  end
  return formatted
end


function AutoHeight(SCROLLABLECONTROL)

	-- This function sets the height of a scrollable control (label, listbox, treeview)

	local vScroll = Turbine.UI.Lotro.ScrollBar();
	vScroll:SetOrientation(Turbine.UI.Orientation.Vertical);
	vScroll:SetSize(1,1);
	vScroll:SetParent(SCROLLABLECONTROL);
	SCROLLABLECONTROL:SetVerticalScrollBar(vScroll);

	if vScroll:IsVisible() == true then
		local iCount = 1;

		while vScroll:IsVisible() == true and iCount < 5000 do
			SCROLLABLECONTROL:SetHeight(SCROLLABLECONTROL:GetHeight() + 2);
			vScroll:SetSize(1,1);
			iCount = iCount + 1;
		end
	end

	SCROLLABLECONTROL:SetVerticalScrollBar(nil);

end


function AutoWidth(SCROLLABLECONTROL)

	-- This function sets the width of a scrollable control (textbox, listbox, treeview)

	SCROLLABLECONTROL:SetWidth(1);

	local hScroll = Turbine.UI.Lotro.ScrollBar();
	hScroll:SetOrientation(Turbine.UI.Orientation.Horizontal);
	hScroll:SetSize(1,1);
	hScroll:SetParent(SCROLLABLECONTROL);
	SCROLLABLECONTROL:SetHorizontalScrollBar(hScroll);

	if hScroll:IsVisible() == true then
		local iCount = 1;

		while hScroll:IsVisible() == true and iCount < 5000 do
			SCROLLABLECONTROL:SetWidth(SCROLLABLECONTROL:GetWidth() + 2);
			iCount = iCount + 1;
		end
	end

	SCROLLABLECONTROL:SetHorizontalScrollBar(nil);
end


-- This function takes a table with values of R,G,B and returns a Turbine colour
function GetTurbineColor(RGB)
	return Turbine.UI.Color((RGB.R/255),(RGB.G/255),(RGB.B/255));
end


function RegisterMouseMoveEvent(window)
	if window == nil then return end;

	blMinDragging = false;
	minRelX = 0;
	minRelY = 0;

	window.MouseDown = function (sender, args)
		blMinDragging = true;
		minRelX = args.X;
		minRelY = args.Y;
	end

	window.MouseUp = function (sender, args)
		blMinDragging = false;
	end

	window.MouseMove = function (sender, args)
		if blMinDragging == true then
			local curX = Turbine.UI.Display.GetMouseX();
			local curY = Turbine.UI.Display.GetMouseY();
			local scWidth = Turbine.UI.Display.GetWidth();
			local scHeight = Turbine.UI.Display.GetHeight();

			local curLeft = curX - minRelX;
			local curTop = curY - minRelY;

			if curLeft < 0 then curLeft = 0 end;
			if curLeft > (scWidth-window:GetWidth()) then curLeft = scWidth-window:GetWidth() end;
			if curTop < 0 then curTop = 0 end;
			if curTop > (scHeight-window:GetHeight()) then curTop = scHeight-window:GetHeight() end;

			window:SetLeft(curLeft);
			window:SetTop(curTop);
		end
	end
end


-- This takes a control and places it in the centre of the screen.
function CentreScreen(CONTROL)

	if CONTROL == nil then return end;

	CONTROL:SetLeft((Turbine.UI.Display.GetWidth()/2)-(CONTROL:GetWidth()/2));
	CONTROL:SetTop((Turbine.UI.Display.GetHeight()/2)-(CONTROL:GetHeight()/2));

end


-- This function gets the location of the centre of an object
-- returns a table with values .x  .y  .screen_x  .screen_y
function GetObjectCentre(CONTROL)

	local coords = {["x"]=0;["y"]=0;["screen_x"]=0;["screen_y"]=0;};

	if CONTROL == nil then return coords end;

	coords.x = CONTROL:GetWidth()/2;
	coords.y = CONTROL:GetHeight()/2;
	coords.screen_x,coords.screen_y = CONTROL:PointToScreen(coords.x,coords.y);

	return coords;

end


function RoundNumber(NUMBER,DECPLACES)

	-- This function takes a number and the decimal place to round to.
	-- Then returns the rounded number.

	if NUMBER == nil then return 0 end;
	if DECPLACES == nil then DECPLACES = 0 end;

	local ABSNUMBER = 0;
	local REMAINDER = 0;

	local MULTIPLIER = math.pow(10,DECPLACES);

	ABSNUMBER,REMAINDER = math.modf(NUMBER*MULTIPLIER);

	if REMAINDER >= 0.5 then
		ABSNUMBER = ABSNUMBER + 1;
	end

	local NUMTORETURN = ABSNUMBER * (math.pow(10,-(DECPLACES)));

	return NUMTORETURN;

end


function ConvertTime(PASSEDSECONDS)

	-- This function takes a seconds value and returns a table with the value
	-- broken down into days, hours, minutes and seconds.

	if PASSEDSECONDS == nil then return nil end;

	local _TIME =
	{
	["DAYS"] = 0;
	["HOURS"] = 0;
	["MINUTES"] = 0;
	["SECONDS"] = 0;
	};

	_TIME.DAYS,_TIME.HOURS = math.modf(PASSEDSECONDS/86400);

	_TIME.HOURS,_TIME.MINUTES = math.modf(_TIME.HOURS*24);

	_TIME.MINUTES,_TIME.SECONDS = math.modf(_TIME.MINUTES*60);

	_TIME.SECONDS = math.floor(_TIME.SECONDS * 60);

	return _TIME;

end


function GetEndTime(EXPIRES)

	if EXPIRES == nil then return nil end;

	local ENDSTRING = "";

	local _REMAINING = ConvertTime(EXPIRES - Turbine.Engine.GetGameTime());
	local _CURDATE = Turbine.Engine.GetDate();

	local endSECOND = _CURDATE.Second + _REMAINING.SECONDS;
	local endMINUTE = _CURDATE.Minute + _REMAINING.MINUTES;
	local endHOUR = _CURDATE.Hour + _REMAINING.HOURS;
	local endDAY = _CURDATE.Day + _REMAINING.DAYS;
	local endMONTH = _CURDATE.Month;
	local endYEAR = _CURDATE.Year;

	if endSECOND >= 60 then
		endSECOND = endSECOND - 60;
		endMINUTE = endMINUTE + 1;
	end

	if endMINUTE >= 60 then
		endMINUTE = endMINUTE - 60;
		endHOUR = endHOUR + 1;
	end

	if endHOUR >= 24 then
		endHOUR = endHOUR - 24;
		endDAY = endDAY + 1;
	end

	if endDAY > GetDaysInMonth(endMONTH) then
		endDAY = endDAY - GetDaysInMonth(endMONTH);
		endMONTH = endMONTH + 1;
	end

	if endMONTH > 12 then
		endMONTH = endMONTH - 12;
		endYEAR = endYEAR + 1;
	end


	_ENDTIME =
		{
		["SECOND"] = endSECOND;
		["MINUTE"] = endMINUTE;
		["HOUR"] = endHOUR;
		["DAY"] = endDAY;
		["MONTH"] = endMONTH;
		["YEAR"] = endYEAR;
		};


	return _ENDTIME;

end


function GetDaysInMonth(MONTH)

	if MONTH == nil then return nil end;
	if MONTH > 12 or MONTH < 1 then return nil end;


	if MONTH == 1 then return 31 end; 	-- January
	if MONTH == 2 then return 28 end; 	-- February
	if MONTH == 3 then return 31 end; 	-- March
	if MONTH == 4 then return 30 end;	-- April
	if MONTH == 5 then return 31 end;	-- May
	if MONTH == 6 then return 30 end;	-- June
	if MONTH == 7 then return 31 end;	-- July
	if MONTH == 8 then return 31 end;	-- August
	if MONTH == 9 then return 30 end; 	-- September
	if MONTH == 10 then return 31 end; 	-- October
	if MONTH == 11 then return 30 end; 	-- November
	if MONTH == 12 then return 31 end; 	-- December

end



-- Converts a hex value to dec.
function HEXtoDEC(IN)
	if IN == nil then return nil end;

	local OUT = tonumber(IN, 16);
	return OUT;
end


-- This function takes a turbine color and returns the appropriate hex value for that color to be used in RGB tags
function HexTBColor(TURBINECOLOR)
	local function HEXRGB(IN)
	local B,K,OUT,I,D=16,"0123456789ABCDEF","",0
		if IN == 0 then return "00" end;
		while IN>0 do
		I=I+1
		IN,D=math.floor(IN/B),math.mod(IN,B)+1
		OUT=string.sub(K,D,D)..OUT
	end
	if string.len(OUT) == 1 then OUT = "0" .. OUT end;
		return OUT
	end
	return HEXRGB(math.ceil(TURBINECOLOR.R*255)) .. HEXRGB(math.ceil(TURBINECOLOR.G*255)) .. HEXRGB(math.ceil(TURBINECOLOR.B*255));
end


-- Dump table function for checking tables on the fly ---------------
function dump(o)
    if type(o) == 'table' then
        local s = '{\n'
        for k,v in pairs(o) do
                if type(k) ~= 'number' then k = '"'..k..'"' end
                s = s .. '['..k..'] = ' .. dump(v) .. '\n'
        end
        return s .. '}\n'
    else
        return ( tostring(o) )
    end
end


--This function returns a deep copy of a given table ---------------
function deepcopy(object)
    local lookup_table = {}
    local function _copy(object)
        if type(object) ~= "table" then
            return object
        elseif lookup_table[object] then
            return lookup_table[object]
        end
        local new_table = {}
        lookup_table[object] = new_table
        for index, value in pairs(object) do
            new_table[_copy(index)] = _copy(value)
        end
        return setmetatable(new_table, getmetatable(object))
    end
    return _copy(object)
end


-- This function merges table two into table one, overwriting any matching entries.
function mergeTables(t1, t2)
	for k, v in pairs(t2) do
		if (type(v) == "table") and (type(t1[k] or false) == "table") then
			mergeTables(t1[k], t2[k])
		else
			t1[k] = v
		end
	end
	return t1
end


-- This function deletes a control and it's children from memory.
function DestroyControl(CONTROL)

	local ChildControlList = CONTROL:GetControls();

	for i=1, ChildControlList:GetCount() do
		DestroyControl(ChildControlList:Get(i));
	end

	CONTROL = nil;
end


-- This function empties a table out
function ClearTable(TABLE)
	if type(TABLE) ~= "table" then return end;
	for k,v in pairs(TABLE) do
		TABLE[k] = nil;
	end
end
