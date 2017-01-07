
----------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------

--[[

ToolTip.lua v.1.0 by Galuhad

This library allows you to code tooltips to your controls. Add this lua file to your project, including it prior to any constructors.
It will add two new properties to every control (and inherited types) which will allow you to set or get a tooltip which shall be
displayed on MouseEnter events.

This uses the AddCallback() function by Garan for assigning events. Please make sure you use
the same function if you need to assign any additional events.

Usage:

SomeControl:SetToolTip("Example");
SomeControl:GetToolTip();

If you need to create a modified MouseEnter/MouseLeave event, then please use AltMouseEnter/AltMouseLeave instead
e.g.

MyControl.AltMouseEnter = function (sender,args)
	-- Do something here
end

This will be called along with the normal MouseEnter/MouseLeave event, but without overwriting it. If you are not using mouse events,
then you do not need to do anything else beyond setting the tooltip.

--]]

----------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------


function Turbine.UI.Control.SetToolTip(SENDER,MESSAGE)

	-- This function creates the :SetToolTip() property for all new controls
	if MESSAGE == nil or MESSAGE == "" then
		SENDER["ToolTip"] = nil;
		return;
	end

	SENDER["ToolTip"] = MESSAGE;

	local controlEnter = function (sender, args)
		ProcessToolTip(SENDER,MESSAGE);
	end

	AddCallback(SENDER,"MouseEnter",controlEnter)

end


function Turbine.UI.Control.GetToolTip(SENDER)

	-- This function creates the :GetToolTip() property for all new controls
	if SENDER == nil or SENDER["ToolTip"] == nil then return "" end;
	return SENDER["ToolTip"];
end


function ProcessToolTip(SENDER, MESSAGE)

	-- This function processes the delay in presenting the tooltip, and handles early mouseleave events.
	ENTERTIME = Turbine.Engine.GetGameTime();
	TOOLTIPDELAY = 0.75; -- time in seconds

	local controlLeave = function ()
		if tempTimer ~= nil then
			tempTimer:SetWantsUpdates(false);
			tempTimer = nil;
		end
	end

	AddCallback(SENDER,"MouseLeave",controlLeave);

	tempTimer = Turbine.UI.Control();
	tempTimer.Update = function()
		local CURRENTTIME = Turbine.Engine.GetGameTime();
		if CURRENTTIME > (ENTERTIME + TOOLTIPDELAY) then
			DrawToolTip(SENDER,MESSAGE);
			tempTimer:SetWantsUpdates(false);
			tempTimer = nil;
		end
	end
	tempTimer:SetWantsUpdates(true);

end


function AutoSizeToolTip(SCROLLABLECONTROL)

	-- This function handles the dynamic resizing of the tooltip
	SCROLLABLECONTROL:SetWidth(1);

	local hScroll = Turbine.UI.Lotro.ScrollBar();
	hScroll:SetOrientation(Turbine.UI.Orientation.Horizontal);
	hScroll:SetSize(1,1);
	hScroll:SetParent(SCROLLABLECONTROL);
	SCROLLABLECONTROL:SetHorizontalScrollBar(hScroll);

	if hScroll:IsVisible() == true then
		local iCount = 1; -- prevents getting stuck in an infinite loop

		while hScroll:IsVisible() == true and iCount < 1000 do
			SCROLLABLECONTROL:SetWidth(SCROLLABLECONTROL:GetWidth() + 2);
			iCount = iCount + 1;
		end
	end

	SCROLLABLECONTROL:SetHorizontalScrollBar(nil);
end


function DrawToolTip(SENDER,MESSAGE)

	-- This function draws the tooltip to the screen, and controls the mouseleave event
	local ToolTipFrame = Turbine.UI.Window();
	ToolTipFrame:SetPosition(Turbine.UI.Display.GetMouseX()+15,Turbine.UI.Display.GetMouseY()+20);
	ToolTipFrame:SetSize(50,21);
	ToolTipFrame:SetBackColor(Turbine.UI.Color(0.6,0.6,0.5));
	ToolTipFrame:SetVisible(true);
	ToolTipFrame:SetMouseVisible(false);

	local ToolTipBack = Turbine.UI.Control();
	ToolTipBack:SetParent(ToolTipFrame);
	ToolTipBack:SetSize(ToolTipFrame:GetWidth()-2,ToolTipFrame:GetHeight()-2);
	ToolTipBack:SetPosition(1,1);
	ToolTipBack:SetBackColor(Turbine.UI.Color.Black);
	ToolTipBack:SetMouseVisible(false);

	local ToolTipLabel = Turbine.UI.TextBox();
	ToolTipLabel:SetParent(ToolTipBack);
	ToolTipLabel:SetSize(ToolTipBack:GetWidth()-8,ToolTipBack:GetHeight()-4);
	ToolTipLabel:SetPosition(4,2);
	ToolTipLabel:SetMultiline(false);
	ToolTipLabel:SetText(MESSAGE);
	ToolTipLabel:SetFont(Turbine.UI.Lotro.Font.TrajanPro14);
	ToolTipLabel:SetForeColor(Turbine.UI.Color.Khaki);
	ToolTipLabel:SetMouseVisible(false);

	AutoSizeToolTip(ToolTipLabel);
	ToolTipBack:SetSize(ToolTipLabel:GetWidth()+8,ToolTipLabel:GetHeight()+4);
	ToolTipFrame:SetSize(ToolTipBack:GetWidth()+2,ToolTipBack:GetHeight()+2);

	--Make sure tooltip is completely on screen
	if ( ToolTipFrame:GetLeft() + ToolTipFrame:GetWidth() ) > Turbine.UI.Display.GetWidth() then ToolTipFrame:SetLeft(Turbine.UI.Display.GetWidth() - ToolTipFrame:GetWidth() - 10) end;
	if ( ToolTipFrame:GetTop() + ToolTipFrame:GetHeight() ) > Turbine.UI.Display.GetHeight() then ToolTipFrame:SetTop(ToolTipFrame:GetTop()-35-ToolTipFrame:GetHeight()) end;

	local controlLeave = function ()
		if ToolTipFrame ~= nil then ToolTipFrame:Close() end;
	end

	AddCallback(SENDER,"MouseLeave",controlLeave);

	ToolTipFrame.Closed = function ()
		ToolTipLabel = nil;
		ToolTipBack = nil;
		ToolTipFrame = nil;
	end

end
