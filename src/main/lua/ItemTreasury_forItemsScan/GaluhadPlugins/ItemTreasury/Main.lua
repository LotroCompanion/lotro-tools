
PLUGINDIR = "GaluhadPlugins.ItemTreasury";
RESOURCEDIR = "GaluhadPlugins/ItemTreasury/Resources/";
PLUGINNAME = "Item Treasury";

-- Turbine Imports..
import "Turbine";
import "Turbine.Gameplay";
import "Turbine.UI";
import "Turbine.UI.Lotro";

-- Plugin Imports..
import (PLUGINDIR..".Globals");
import (PLUGINDIR..".Images");
import (PLUGINDIR..".Strings");
import (PLUGINDIR..".AddCallBack");
import (PLUGINDIR..".Functions");
import (PLUGINDIR..".Commands");
import (PLUGINDIR..".VerifyData");
import (PLUGINDIR..".17_1_Items");
import (PLUGINDIR..".17_1_NewItems");
import (PLUGINDIR..".Category");

-- Utils Imports..
import (PLUGINDIR..".Utils");

-- Windows..
import (PLUGINDIR..".Windows");


-----------------------------------------------------------------------------------------------------------

function saveData()
	SETTINGS["__VERSION"] = V_SETTINGS;
	Turbine.PluginData.Save(Turbine.DataScope.Character, "ItemTreasury_Settings", SETTINGS);
end


function loadData()
	---------------------------------------------------------------------------------------------------------------------------------
	-- SAVED SETTINGS --
	local SavedSettings = {};

	function GetSavedSettings()
		SavedSettings = Turbine.PluginData.Load(Turbine.DataScope.Character, "ItemTreasury_Settings");
	end

	if pcall(GetSavedSettings) then
		GetSavedSettings();
	else -- Loaded with errors
		SavedSettings = nil;
		printError(_STRINGS[1][1][LANGID]);
	end

	-- Check the saved settings to make sure it is still compatible with newer updates, add in any missing default settings
	if type(SavedSettings) == 'table' then
		local tempSETTINGS = {};
		tempSETTINGS = Utils.deepcopy(DEFAULT_SETTINGS);
		SETTINGS = Utils.mergeTables(tempSETTINGS,SavedSettings);
	else
		SETTINGS = Utils.deepcopy(DEFAULT_SETTINGS);
	end
	----------------------------------------------------------------------------------------------------------------------------------
end


function print(MESSAGE)
	if MESSAGE == nil then return end;
	Turbine.Shell.WriteLine("<rgb=#FF6666>" .. tostring(MESSAGE) .. "</rgb>");
end


function printError(STRING)
	if STRING == nil or STRING == "" then return end;
	Turbine.Shell.WriteLine("<rgb=#FF3333>"..PLUGINNAME..": " .. tostring(STRING) .. "\n" .. _STRINGS[1][2][LANGID] .. "</rgb>");
end


function LoadSequence()
	LANGID = Utils.GetClientLanguage();
	if string.find(PLAYERCHAR:GetName(),"~") then
		Debug(_STRINGS[1][3][LANGID]);
	else
		loadData();
		VerifyData();
		Utils.InitiateChatLogger();
		Windows.DrawWindows();
		RegisterCommands();

		Turbine.Plugin.Unload = function ()
			saveData();
		end

		print("Loaded '" .. PLUGINNAME .. "' by Galuhad [Eldar]");
		print(_STRINGS[1][6][LANGID]);
		print(_STRINGS[1][7][LANGID]);


		--Windows.CheckMissingCats(_NEWITEMS);	-- Only check on new database updates

	end
end


-- Initiate load sequence
LoadSequence();
