--------------------------------------------------------------------------------------------------------------------------------------------------------------------
--[[

	Items Database by Galuhad
	Game Version: Update 17.1

	Keys:
	[1] = Item Name (string)
	[2] = Item Description (string)
	[3] = Item Category (number)
	[4] = Item Quality (number)
	[5] = Item Durability (number)
	[6] = Item Is Magic (true/false)
	[7] = Item Is Unique (true/false)
	[8] = Item Icon Image ID (hex number)
	[9] = Item Icon Background Image ID (hex number)

	Item Quantity History:
	17.1 - 94,369
	16.5 - 93,605
	16.0 - 93,125
	15.2 - 92,660
	15.1 - 92,482
	15.0 - 92,425
	14.2 - 90,370
	14.1 - 90,300
	14.0 - 90,287
	13.1 - 87,931
	13.0 - 87,877
	12.3 - 87,544

--]]
--------------------------------------------------------------------------------------------------------------------------------------------------------------------

_ITEMSDB =
{
[1879049233]={[1]="Hengaim";[2]="";[3]=5;[4]=4;[5]=3;[6]=false;[7]=false;[8]=0x410003DF;[9]=0x41000002;};
[1879330079]={[1]="Dirty Drying Cloth";[2]="A drying cloth for those who use the baths in the Great Guest-houses.";[3]=27;[4]=5;[5]=0;[6]=false;[7]=false;[8]=0x410F2D56;[9]=0x410002A9;};
};


--[[
local counter = 0;
for k,v in pairs(_ITEMSDB) do
	counter = counter + 1;
end
print(counter);
--]]
