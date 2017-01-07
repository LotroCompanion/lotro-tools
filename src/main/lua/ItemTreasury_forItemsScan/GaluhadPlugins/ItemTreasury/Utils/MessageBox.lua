
----------------------------------------------------------------------------------------------------------------------
--[[

MESSAGEBOX V1.3 BY GALUHAD

This class creates a LOTRO style message box.
It has two parameters, Message and Button.
Message is the message string you would like to display.
Button is the style of button(s) you would like..

v1.2 updated to support different languages
v1.3 removes need to use .Create when calling for a new popup.. Now you can just use MessageBox(Message,ButtonStyle)

Button options:
		MBOK 		-- OK only (default if no option entered)
		MBCANCEL 	-- Cancel only
		MBYESNO 	-- Yes / No Buttons

The function returns the button pressed; either:
		MBOK		-- OK button pressed
		MBCANCEL	-- Cancel button pressed
		MBYES		-- Yes button pressed
		MBNO		-- No button pressed

You can use these values in your logic to determine how to process the players response.
E.G

tempMsg = MessageBox("Are you sure?","MBYESNO");
tempMsg.ButtonClick = function (sender,args) -- Event executed when a button is pressed.. args returns a table with the value of the button pressed
	if args.Button == "MBYES" then
		--<do something here>
	end
end
--]]

MSGBOXBACKIMAGE = RESOURCEDIR.."MessageBox.tga"; -- location where you stored the background image in your plugin.

-----------------------------------------------------------------------------------------------------------------------

_MSGBOXSTRINGS =
{
["MBOK"] = 		{["ENGLISH"] = "Ok";		["FRENCH"] = "Ok";			["GERMAN"] = "Ok";			["RUSSIAN"] = "Ok";};
["MBYES"] = 	{["ENGLISH"] = "Yes";		["FRENCH"] = "Oui";			["GERMAN"] = "Ja";			["RUSSIAN"] = "Yes";};
["MBNO"] = 		{["ENGLISH"] = "No";		["FRENCH"] = "Non";			["GERMAN"] = "Nein";		["RUSSIAN"] = "No";};
["MBCANCEL"] = 	{["ENGLISH"] = "Cancel";	["FRENCH"] = "Résilier";	["GERMAN"] = "Kündigen";	["RUSSIAN"] = "Cancel";};
};


MBLANG = "ENGLISH";

if Turbine.Engine.GetLanguage() == Turbine.Language.French then
	MBLANG = "FRENCH";
elseif Turbine.Engine.GetLanguage() == Turbine.Language.German then
	MBLANG = "GERMAN";
elseif Turbine.Engine.GetLanguage() == Turbine.Language.Russian then
	MBLANG = "RUSSIAN";
end

-----------------------------------------------------------------------------------------------------------------------

MBOK = "MBOK";
MBCANCEL = "MBCANCEL";
MBYESNO = "MBYESNO";
MBNO = "MBNO";
MBYES = "MBYES";

MessageBox = {};
_mtMessageBox = {};

function MessageBox.Constructor(SENDER,MESSAGE,BUTTON)

	if MESSAGE == nil then MESSAGE = "" end;

	local BUTTONWIDTH = 90;
	local BUTTONTOP = 130;
	local YELLOW = Turbine.UI.Color((210/255),1,0);

	local ScreenBlock = Turbine.UI.Window();
	ScreenBlock:SetSize(Turbine.UI.Display.GetSize());
	ScreenBlock:SetMouseVisible(true);
	ScreenBlock:SetVisible(true);

	local MessageWindow = Turbine.UI.Window();
	MessageWindow:SetParent(ScreenBlock);
	MessageWindow:SetSize(510,170);
	MessageWindow:SetPosition((ScreenBlock:GetWidth()/2)-255,(ScreenBlock:GetHeight()/2)-85);
	MessageWindow:SetBackground(MSGBOXBACKIMAGE);
	MessageWindow:SetBlendMode(4);
	MessageWindow:SetVisible(true);
	MessageWindow:Activate();

	local MessageLabel = Turbine.UI.Label();
	MessageLabel:SetParent(MessageWindow);
	MessageLabel:SetPosition(20,20);
	MessageLabel:SetSize(470,100);
	MessageLabel:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleCenter);
	MessageLabel:SetFont(Turbine.UI.Lotro.Font.TrajanPro16);
	MessageLabel:SetForeColor(YELLOW);
	MessageLabel:SetMultiline(true);
	MessageLabel:SetText(MESSAGE);

	if BUTTON == MBOK then

		local btnOK = Turbine.UI.Lotro.Button();
		btnOK:SetParent(MessageWindow);
		btnOK:SetPosition(255-(BUTTONWIDTH/2),BUTTONTOP);
		btnOK:SetWidth(BUTTONWIDTH);
		btnOK:SetText(_MSGBOXSTRINGS.MBOK[MBLANG]);

		btnOK.Click = function(sender,args)
			ScreenBlock.ButtonClick(sender,{["Button"]="MBOK";});
			ScreenBlock:Close();
		end


	elseif BUTTON == MBCANCEL then

		local btnCancel = Turbine.UI.Lotro.Button();
		btnCancel:SetParent(MessageWindow);
		btnCancel:SetPosition(255-(BUTTONWIDTH/2),BUTTONTOP);
		btnCancel:SetWidth(BUTTONWIDTH);
		btnCancel:SetText(_MSGBOXSTRINGS.MBCANCEL[MBLANG]);

		btnCancel.Click = function (sender, args)
			ScreenBlock.ButtonClick(sender,{["Button"]="MBCANCEL";});
			ScreenBlock:Close();
		end

	elseif BUTTON == MBYESNO then

		local btnYes = Turbine.UI.Lotro.Button();
		btnYes:SetParent(MessageWindow);
		btnYes:SetPosition(255-BUTTONWIDTH-10,BUTTONTOP);
		btnYes:SetWidth(BUTTONWIDTH);
		btnYes:SetText(_MSGBOXSTRINGS.MBYES[MBLANG]);

		local btnNo = Turbine.UI.Lotro.Button();
		btnNo:SetParent(MessageWindow);
		btnNo:SetPosition(265,BUTTONTOP);
		btnNo:SetWidth(BUTTONWIDTH);
		btnNo:SetText(_MSGBOXSTRINGS.MBNO[MBLANG]);

		btnNo.Click = function (sender, args)
			ScreenBlock.ButtonClick(sender,{["Button"]="MBNO";});
			ScreenBlock:Close();
		end

		btnYes.Click = function (sender, args)
			ScreenBlock.ButtonClick(sender,{["Button"]="MBYES";});
			ScreenBlock:Close();
			ScreenBlock = nil;
		end

	else

		--No button entered so OK button only
		local btnOK = Turbine.UI.Lotro.Button();
		btnOK:SetParent(MessageWindow);
		btnOK:SetPosition(215,BUTTONTOP);
		btnOK:SetWidth(BUTTONWIDTH);
		btnOK:SetText(_MSGBOXSTRINGS.MBOK[MBLANG]);

		btnOK.Click = function (sender, args)
			ScreenBlock.ButtonClick(sender,{["Button"]="MBOK";});
			ScreenBlock:Close();
		end

	end

	ScreenBlock.ButtonClick = function() end;

	return ScreenBlock;

end


function _mtMessageBox.__call(...)
    return MessageBox.Constructor(...);
end

setmetatable(MessageBox, _mtMessageBox);
