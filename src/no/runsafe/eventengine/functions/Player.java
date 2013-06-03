package no.runsafe.eventengine.functions;

import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

public class Player extends TwoArgFunction
{
	public Player() {}

	@Override
	public LuaValue call(LuaValue modName, LuaValue env)
	{
		LuaTable player = tableOf();
		player.set("kill", new kill());

		env.set("Player", player);
		return player;
	}

	static class kill extends OneArgFunction
	{
		@Override
		public LuaValue call(LuaValue playerName)
		{
			RunsafePlayer player = RunsafeServer.Instance.getPlayerExact(playerName.toString());
			player.setHealth(0);
			return null;
		}
		// Sciencee
	}
}