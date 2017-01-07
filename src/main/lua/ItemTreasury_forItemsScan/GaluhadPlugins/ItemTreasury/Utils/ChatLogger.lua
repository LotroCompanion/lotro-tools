
-- Chat log filter.
-- Controls chat messages and actions the appropriate functions based on the message.

function InitiateChatLogger()

	CHATLOG = Turbine.Chat;
	CHATLOG.Received = function (sender, args)

		local tempMessage = tostring(args.Message);

		if args.ChatType == Turbine.ChatType.Standard then
			FilterStandard(tempMessage);
		end

	end
end


function FilterStandard(cSender, cMessage)
-- Filters here for use with the combat channel.


end
