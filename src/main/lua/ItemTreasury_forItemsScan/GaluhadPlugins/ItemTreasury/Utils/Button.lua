
---------------------------------------------------------------------------------------------------------------------
--[[

Custom Button class v.1 by Galuhad
This class allows you to create custom buttons by asigning images for the different button states
This class uses the AddCallback() function by Garan for assigning events. Please make sure you use
the same function if you need to assign any additional events.

usage:
myButton = Button();
myButton:SetParent(control);		-- As normal
myButton:SetSize(width,height);		-- As normal
myButton:SetPosition(left,top);		-- As normal
myButton:SetImageNormal(image);		-- Standard button image
myButton:SetImageOver(image);		-- Image used when mouse is over the control
myButton:SetImageDown(image);		-- Image used when the mouse has clicked down on the control
myButton:SetImageDisabled(image);	-- Image used when control:SetEnabled(false) -- only needed if button will ever be disabled
myButton:SetText("Something");		-- Text to display on the button (optional)

myButton.Click = function (Sender,Args)		-- the event sender, the event args
	-- <do something here>
end

others:
myButton:GetImageNormal();			-- Returns the image used when the button is in it's normal state
myButton:GetImageOver();			-- Returns the image used when the mouse is over the button
myButton:GetImageDown();			-- Returns the image used when the mouse clicks down on the button
myButton:GetImageDisabled();		-- Returns the image used when the button
myButton:GetText();					-- Returns text used in the button's label
mybutton:GetFont();					-- Returns the font used in the button's label
mybutton:GetForeColor();			-- Returns the font color used in the button's label
mybutton:GetTextAlignment();		-- Returns the alignment of the label used in the button's label
mybutton:GetOutlineColor();			-- Returns the outline color used in the button's label
mybutton:GetFontStyle();			-- Returns the font style used in the button's label
mybutton:IsMultiline();				-- Returns whether the label is multilined (true or false)

myButton:SetFont(font);				-- Sets the label's font
myButton:SetForeColor(color);		-- Sets the label's font color
myButton:SetTextAlignment(ContentAlignment);	-- Sets the label's alignment
myButton:SetOutlineColor(color);	-- Sets the label's outline color
myButton:SetFontStyle(FontStyle);	-- Sets the label's font style
myButton:SetMultiline(value);		-- Sets whether the label should be multilined (true or false)

--]]
---------------------------------------------------------------------------------------------------------------------

Button = {};
_mtButton = {};

function Button.Constructor (Sender)

	local _images={};

	local btn = Turbine.UI.Button();
	btn:SetSize(80,22);
	btn:SetBlendMode(4);

	local lbl = Turbine.UI.Label();
	lbl:SetParent(btn);
	lbl:SetSize(btn:GetSize());
	lbl:SetMouseVisible(false);

	local controlEnter = function ()
		if btn:IsEnabled() and _images.Over ~= nil then btn:SetBackground(_images.Over) end;
	end
	AddCallback(btn,"MouseEnter",controlEnter);

	local controlLeave = function ()
		if btn:IsEnabled() and _images.Normal ~= nil then btn:SetBackground(_images.Normal) end;
	end
	AddCallback(btn,"MouseLeave",controlLeave);

	local controlDown = function (Sender,Args)
		if btn:IsEnabled() then
			if _images.Down ~= nil then btn:SetBackground(_images.Down) end;
		end
	end
	AddCallback(btn,"MouseDown",controlDown);

	local controlUp = function ()
		if btn:IsEnabled() and _images.Normal ~= nil then btn:SetBackground(_images.Normal) end;
	end
	AddCallback(btn,"MouseUp",controlUp);

	local controlEnableChanged = function ()
		if btn:IsEnabled() then
			if _images.Normal~= nil then btn:SetBackground(_images.Normal) end;
		else
			if _images.Disabled~= nil then btn:SetBackground(_images.Disabled) end;
		end
	end
	AddCallback(btn,"EnabledChanged",controlEnableChanged);

	-- Calls
	btn.SetImageNormal = function (Sender,Image)
		_images["Normal"] = Image;
		if btn:IsEnabled() then btn:SetBackground(Image) end;
	end

	btn.GetImageNormal = function ()
		if _images.Normal ~= nil then return _images.Normal end;
	end

	btn.SetImageOver = function (Sender,Image)
		_images["Over"] = Image;
	end

	btn.GetImageOver = function ()
		if _images.Over ~= nil then return _images.Over end;
	end

	btn.SetImageDown = function (Sender,Image)
		_images["Down"] = Image;
	end

	btn.GetImageDown = function ()
		if _images.Down ~= nil then return _images.Down end;
	end

	btn.SetImageDisabled = function (Sender,Image)
		_images["Disabled"] = Image;
		if btn:IsEnabled()==false then btn:SetBackground(Image) end;
	end

	btn.GetImageDisabled = function ()
		if _images.Disabled ~= nil then return _images.Disabled end;
	end

	btn.SetText = function (Sender,Text)
		lbl:SetText(Text);
	end

	btn.GetText = function ()
		return lbl:GetText();
	end

	btn.SetFont = function (Sender,Font)
		lbl:SetFont(Font);
	end

	btn.GetFont = function ()
		return lbl:GetFont();
	end

	btn.SetForeColor = function (Sender,Color)
		lbl:SetForeColor(Color);
	end

	btn.GetForeColor = function ()
		return lbl:GetForeColor();
	end

	btn.SetTextAlignment = function (Sender,Align)
		lbl:SetTextAlignment(Align);
	end

	btn.GetTextAlignment = function ()
		return lbl:GetTextAlignment();
	end

	btn.SetOutlineColor = function (Sender,Color)
		lbl:SetOutlineColor(Color);
	end

	btn.GetOutlineColor = function ()
		return lbl:GetOutlineColor();
	end

	btn.SetFontStyle = function (Sender,Style)
		lbl:SetFontStyle(Style);
	end

	btn.GetFontStyle = function ()
		return lbl:GetFontStyle();
	end

	btn.SetMultiline = function (Sender,Value)
		lbl:SetMultiline(Value);
	end

	btn.IsMultiline = function ()
		return lbl:IsMultiline();
	end

	return btn;

end

function _mtButton.__call(...)
    return Button.Constructor(...);
end

setmetatable(Button, _mtButton);
