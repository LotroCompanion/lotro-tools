

function RegisterCommands()

	---------------------------------------------------------------------------------------------

	-- /item
	-- /item <item name or item id>

	itemCommand = Turbine.ShellCommand();

	function itemCommand:Execute(command,args)

		if args == "" then
			if Windows.wMainWin:IsVisible() == false then
				Windows.wMainWin:SetVisible(true);
				Windows.wMainWin:Activate();
			else
				Windows.wMainWin:SetVisible(false);
			end
		else
			Windows.ClearSearchControls();
			if tonumber(args) ~= nil and _ITEMSDB[tonumber(args)] ~= nil then
				_SEARCHRESULTS = {tonumber(args)};
				PAGE = 1;
				Windows.PagenateResults(PAGE);
				Windows.wMainWin:SetVisible(true);
				Windows.wMainWin:Activate();
			else
				Windows.txtSearch:SetText(args);
				Windows.PrepareSearch();
				Windows.wMainWin:SetVisible(true);
				Windows.wMainWin:Activate();
			end
		end
	end

	function itemCommand:GetHelp()
		return _STRINGS[1][5][LANGID];
	end

	function itemCommand:GetShortHelp()
		return _STRINGS[1][5][LANGID];
	end

	Turbine.Shell.AddCommand( "item", itemCommand);

end





