
-- Color Picker Class by Galuhad with some editing and changes by Garan -------------------
--
-- USAGE:
--
-- myColourPicker = ColorPicker.Create();
-- myColourPicker:SetParent(someWindow);
-- myColourPicker:SetSize(width,height);
-- myColourPicker:SetPosition(left,top);
--
-- myColourPicker.LeftClick = function () -- Left-click mouse event
-- myColourPicker.RightClick = function () -- Right-Click mouse event
--
-- myColourPicker:GetTurbineColor(); - returns a Turbine color class
-- myColourPicker:GetHexColor(); -- returns the hex value e.g. FFFFFF
-- myColourPicker:GetRGBColor(); -- returns three number values, red, green, and blue
--
--------------------------------------------------------------------------------------------

-- Set the picker.jpg location for your plugin, can be done within your code if you prefer.
-- 450x70 px  default sizes

PICKER_JPG_DIR = RESOURCEDIR .. "picker.jpg";





-- Please do not modify below this line ----------------------------------------------------

ColorPicker = {};

function ColorPicker.Create()

	-- default sizes
	local PICKERWIDTH = 400;
	local PICKERHEIGHT = 100;

	local Picker = {};


	-- Checks a color is between 0 and 1 ---------------------------------------------
	function ValidColorRange (colorValue)
		if (colorValue < 0) then
			return 0;
		elseif (colorValue > 1) then
			return 1;
		else
			return colorValue;
		end
	end


	-- Converts a color number to hex. -------------------------------------------------
	function DEC_HEX(IN)
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


	-- Draw color selector ---------------------------------------------------------
	local wColorWin = Turbine.UI.Window();
	--wColorWin:SetParent(self.parent);
	wColorWin:SetSize(PICKERWIDTH, PICKERHEIGHT);
	wColorWin:SetPosition(0,0);

	-- Create the color picker with an image background -- G
	-- This would be even more efficient using one of the built in color picker images but this image
	-- serves as a good example
	wColorWin:SetBackground(PICKER_JPG_DIR);
	wColorWin:SetStretchMode(2);

	wColorWin:SetVisible(true);

	-- Draw color preview box ---------------------------------------------------------

	local wPrevCurs = Turbine.UI.Window();
	wPrevCurs:SetSize(26,26);
	wPrevCurs:SetPosition(0,0);
	wPrevCurs:SetBackColor(Turbine.UI.Color(0,0,0));
	wPrevCurs:SetZOrder(100);
	wPrevCurs:SetMouseVisible(false);
	wPrevCurs:SetVisible(false);

	local wColorPrev = Turbine.UI.Window();
	wColorPrev:SetParent(wPrevCurs);
	wColorPrev:SetSize(22,22);
	wColorPrev:SetPosition(2,2);
	wColorPrev:SetBackColor(Turbine.UI.Color(1,1,1,1));
	wColorPrev:SetZOrder(101);
	wColorPrev:SetMouseVisible(false);
	wColorPrev:SetVisible(true);

	-- Event used to get the color at the current mouse position
	wColorWin.GetColorFromCoord = function (sender,X,Y)
		-- Controls the visibility of the cursor window
		local blockXvalue = (math.floor(PICKERWIDTH/6)); -- = 58
		local blockYvalue = (math.floor(PICKERHEIGHT/2)); -- = 50

		local curColor = Turbine.UI.Color();
		curColor.A = 1.0;
		local myX=X
		local myY=Y
		local curRed = 0;
		local curGreen = 0;
		local curBlue = 0;

		if (myX >= 0) and (myX < blockXvalue) then -- myX < 58

			-- First color block = full red, green fade in.
			curRed = 255;
			curGreen = (255/blockXvalue)*myX;
			curBlue = 0;

		elseif (myX >= blockXvalue) and (myX < (2*blockXvalue)) then  -- myX >= 58 and myX < 116

			-- Second color block = full green, red fade out
			curRed = 255-((255/blockXvalue)*(myX - (blockXvalue)));
			curGreen = 255;
			curBlue = 0;

		elseif (myX >= (2*blockXvalue)) and (myX < (3*blockXvalue)) then

			-- Third color block = full green, blue fade in
			curRed = 0;
			curGreen = 255;
			curBlue = (255/blockXvalue)*(myX - (2*blockXvalue));

		elseif (myX >= (3*blockXvalue)) and (myX < (4*blockXvalue)) then

			-- Fourth color block = full blue, green fade out
			curRed = 0;
			curGreen = 255-((255/blockXvalue)*(myX - (3*blockXvalue)));
			curBlue = 255;

		elseif (myX >= (4*blockXvalue)) and (myX < (5*blockXvalue)) then

			-- Fifth color block = full blue, red fade in
			curRed = (255/blockXvalue)*(myX - (4*blockXvalue));
			curGreen = 0;
			curBlue = 255;

		elseif (myX >= (5*blockXvalue)) then

			-- Sixth color block = full red, fade out blue
			curRed = 255;
			curGreen = 0;
			curBlue = 255-((255/blockXvalue)*(myX - (5*blockXvalue)));

		end

		if myY <= blockYvalue then

			-- In the top block, so fade from black to full color
			curRed = curRed * (myY/blockYvalue);
			curGreen = curGreen * (myY/blockYvalue);
			curBlue = curBlue * (myY/blockYvalue);

		else

			-- In the bottom block, so fade from full color to white
			curRed = curRed + ((myY - blockYvalue) * ((255 - curRed)/(blockYvalue)));
			curGreen = curGreen + ((myY - blockYvalue) * ((255 - curGreen)/(blockYvalue)));
			curBlue = curBlue + ((myY - blockYvalue) * ((255 - curBlue)/(blockYvalue)));

		end

		curColor.R = ValidColorRange((1/255) * curRed);
		curColor.G = ValidColorRange((1/255) * curGreen);
		curColor.B = ValidColorRange((1/255) * curBlue);
		return curColor;

	end


	wColorWin.MouseMove = function(sender, args)

		-- When the mouse moves over the control, update the preview box with the new color.

		-- Debug(args.X .. " / "  .. PICKERWIDTH .. "  width = " .. wColorWin:GetWidth());

		if args.X <= PICKERWIDTH then
			mColor = wColorWin:GetColorFromCoord(args.X, args.Y)
			wColorPrev:SetBackColor(mColor);
			wPrevCurs:SetBackColor(Turbine.UI.Color((1-mColor.R),(1-mColor.G),(1-mColor.B)));
			wPrevCurs:SetPosition((Turbine.UI.Display.GetMouseX() - (wPrevCurs:GetWidth() / 2)), (Turbine.UI.Display.GetMouseY() - (wPrevCurs:GetHeight() / 2)));
			wPrevCurs:SetVisible(true);
		else
			wPrevCurs:SetVisible(false);
		end

	end

	wColorWin.MouseLeave = function(sender, args)

		-- When the mouse moves, ensure the preview box is no longer visible

		wPrevCurs:SetVisible(false);

	end

	wColorWin.MouseClick = function(sender, args)

		-- Fire the color picked event..

		colorString = wColorWin:GetColorFromCoord(args.X, args.Y)

		colRGB =
			{
			["R"] = math.floor(255*ValidColorRange(colorString.R));
			["G"] = math.floor(255*ValidColorRange(colorString.G));
			["B"] = math.floor(255*ValidColorRange(colorString.B));
			};

			colHex =
			{
			["R"] = DEC_HEX(math.floor(255*ValidColorRange(colorString.R)));
			["G"] = DEC_HEX(math.floor(255*ValidColorRange(colorString.G)));
			["B"] = DEC_HEX(math.floor(255*ValidColorRange(colorString.B)));
			};

			colTurbine =
			{
			["R"] = ValidColorRange(colorString.R);
			["G"] = ValidColorRange(colorString.G);
			["B"] = ValidColorRange(colorString.B);
			};


		if (args.Button == Turbine.UI.MouseButton.Left) then

			-- Left mouse button event

			-- External event listener .ItemChanged() caller.
			function LeftClickedListener()

				Picker:LeftClick() -- Clicked event link

			end

			if pcall(LeftClickedListener) == true then
				-- External listener function exists, so execute the code.
				--LeftClickedListener(); -- may not be needed, check if statements are executed twice.
			end


		elseif (args.Button == Turbine.UI.MouseButton.Right) then

			-- Right mouse button event

			-- External event listener .ItemChanged() caller.
			function RightClickedListener()

				Picker:RightClick() -- Clicked event link

			end

			if pcall(RightClickedListener) == true then
				-- External listener function exists, so execute the code.
				--RightClickedListener(); -- may not be needed, check if statements are executed twice.
			end


		else

			-- Some other button pressed event

			-- External event listener .ItemChanged() caller.
			function OtherClickedListener()

				Picker:OtherClick() -- Clicked event link

			end

			if pcall(OtherClickedListener) == true then
				-- External listener function exists, so execute the code.
				--OtherClickedListener(); -- may not be needed, check if statements are executed twice.
			end

		end

	end



	-- Functions and attributes

	function Picker:SetHeight(height)

		-- Set the height
		PICKERHEIGHT = height;
		wColorWin:SetHeight(height);

	end


	function Picker:SetWidth(width)

		-- Set the width
		PICKERWIDTH = width;
		wColorWin:SetWidth(width);

	end


	function Picker:SetSize(width,height)

		-- Set the sizes
		PICKERHEIGHT = height;
		PICKERWIDTH = width;

		wColorWin:SetHeight(height);
		wColorWin:SetWidth(width);

	end


	Picker.GetTurbineColor = function ()

		-- Returns the selected color in Turbine color format.
		local tempColor = Turbine.UI.Color(colTurbine.R, colTurbine.G, colTurbine.B);
		return tempColor;

	end


	Picker.GetHexColor = function ()

		-- Returns the selected color's hex value
		local tempHex = tostring(colHex.R .. colHex.G .. colHex.B);
		return tempHex;

	end


	Picker.GetRGBColor = function ()

		-- Returns the selected color's RGB value
		return colRGB.R, colRGB.G, colRGB.B;

	end


	-- This part creates the metatable.
	wColorWin.__index = wColorWin;
	wColorWin.__newindex = wColorWin;

	setmetatable(Picker,wColorWin);

	return Picker;

end



