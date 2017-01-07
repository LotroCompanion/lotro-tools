
function Turbine.UI.Control.SetHideF12(Sender,Value)
	if Value==nil then Value=false end;
	if _EVENT_F12==nil or type(_EVENT_F12)~='table' then _EVENT_F12 = {} end;
	local tableID = 0;
	for k,v in ipairs(_EVENT_F12) do
		if v==Sender then tableID=k break end;
	end
	if Value==true and tableID==0 then
		table.insert(_EVENT_F12,Sender);
	elseif Value==false and tableID~=0 then
		table.remove(_EVENT_F12,tableID);
	end
end


function Turbine.UI.Control.GetHideF12(Sender)
	if type(_EVENT_F12)~='table' then return false end;
	local tableID = 0;
	for k,v in ipairs(_EVENT_F12) do
		if v==Sender then tableID=k break end;
	end
	if tableID==0 then return false else return true end;
end


function Turbine.UI.Control.SetCloseEsc(Sender,Value)
	if Value==nil then Value=false end;
	if _EVENT_ESC==nil or type(_EVENT_ESC)~='table' then _EVENT_ESC = {} end;
	local tableID = 0;
	for k,v in ipairs(_EVENT_ESC) do
		if v==Sender then tableID=k break end;
	end
	if Value==true and tableID==0 then
		table.insert(_EVENT_ESC,Sender);
	elseif Value==false and tableID~=0 then
		table.remove(_EVENT_ESC,tableID);
	end
end


function Turbine.UI.Control.GetCloseEsc(Sender)
	if type(_EVENT_ESC)~='table' then return false end;
	local tableID = 0;
	for k,v in ipairs(_EVENT_ESC) do
		if v==Sender then tableID=k break end;
	end
	if tableID==0 then return false else return true end;
end


function HandleF12Event()
	if type(_EVENT_F12)~='table' then return end;
	if _OPEN_F12==nil then _OPEN_F12 = {} end;
	if AREHIDDENF12==nil then AREHIDDENF12 = false end;
	if AREHIDDENF12==true then
		for k,v in ipairs(_OPEN_F12) do v:SetVisible(true) end;
		_OPEN_F12 = {};
	else
		for k,v in ipairs(_EVENT_F12) do
			if v:IsVisible()==true then
				table.insert(_OPEN_F12,v);
				v:Close();
			end
		end
	end
	AREHIDDENF12 = not AREHIDDENF12;
end


function HandleEscEvent()
	if type(_EVENT_ESC)~='table' then return false end;
	for k,v in pairs(_EVENT_ESC) do
		v:Close();
	end
end


-- Key control --
wKeyControl = Turbine.UI.Window();
wKeyControl:SetSize(1,1);
wKeyControl:SetWantsKeyEvents(true);
wKeyControl.KeyDown = function (sender,args)
	if args.Action == 268435635 then	-- F12
		HandleF12Event();
	elseif args.Action == 145 then		-- ESC
		HandleEscEvent();
	end
end
