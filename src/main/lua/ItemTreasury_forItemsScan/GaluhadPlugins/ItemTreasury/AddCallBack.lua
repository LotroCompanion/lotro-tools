-------------------------------------------------------------------------------------------------------------
--[[


AddCallback() function by Garan
This allows multiple functions to be assigned to control events without one overwriting another.

Usage:

someControl = Turbine.UI.Control();

local controlMouseEnter = function (Sender,Args)
	-- Do something when mouse enters.
end

AddCallback(someControl,"MouseEnter",controlMouseEnter);


--]]
--------------------------------------------------------------------------------------------------------------

function AddCallback(object, event, callback)
    if (object[event] == nil) then
        object[event] = callback;
    else
        if (type(object[event]) == "table") then
            local exists=false;
            local k,v;
            for k,v in ipairs(object[event]) do
                if v==callback then
                    exists=true;
                    break;
                end
            end
            if not exists then
                table.insert(object[event], callback);
            end
        else
            if object[event]~=callback then
                object[event] = {object[event], callback};
            end
        end
    end
    return callback;
end
