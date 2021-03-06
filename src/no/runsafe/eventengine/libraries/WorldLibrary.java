package no.runsafe.eventengine.libraries;

import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.block.IChest;
import no.runsafe.framework.api.block.ISign;
import no.runsafe.framework.api.chunk.IChunk;
import no.runsafe.framework.api.entity.IEntity;
import no.runsafe.framework.api.lua.FunctionParameters;
import no.runsafe.framework.api.lua.Library;
import no.runsafe.framework.api.lua.RunsafeLuaFunction;
import no.runsafe.framework.api.lua.VoidFunction;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.LegacyMaterial;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.Sound;
import no.runsafe.framework.minecraft.entity.PassiveEntity;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import no.runsafe.worldguardbridge.IRegionControl;
import org.luaj.vm2.LuaTable;

import java.util.ArrayList;
import java.util.List;

public class WorldLibrary extends Library
{
	public WorldLibrary(RunsafePlugin plugin, IRegionControl regionControl)
	{
		super(plugin, "world");
		WorldLibrary.regionControl = regionControl;
	}

	@Override
	protected LuaTable getAPI()
	{
		LuaTable lib = new LuaTable();
		lib.set("setBlock", new SetBlock());
		lib.set("getBlock", new GetBlock());
		lib.set("getPlayers", new GetPlayers());
		lib.set("playSound", new PlaySound());
		lib.set("cloneChestToPlayer", new CloneChestToPlayer());
		lib.set("setSign", new SetSign());
		lib.set("removeItems", new RemoveItems());
		lib.set("getPlayersInRegion", new GetPlayersInRegion());
		lib.set("setTime", new SetTime());
		return lib;
	}

	private static void prepareLocationForEdit(ILocation location)
	{
		IChunk chunk = location.getChunk();
		if (chunk.isUnloaded()) chunk.load();
	}

	private static class SetBlock extends VoidFunction
	{
		@Override
		public void run(FunctionParameters parameters)
		{
			ILocation location = parameters.getLocation(0);
			WorldLibrary.prepareLocationForEdit(location);

			byte damage = 0;
			if (parameters.hasParameter(5))
				damage = (byte) (int) parameters.getInt(5);

			IBlock block = location.getBlock();
			block.set(Item.get(LegacyMaterial.getById(parameters.getInt(4)), damage));
		}
	}

	private static class SetTime extends VoidFunction
	{
		@Override
		public void run(FunctionParameters parameters)
		{
			IWorld world = parameters.getWorld(0);
			world.setTime(parameters.getInt(1));
		}
	}

	private static class SetSign extends VoidFunction
	{
		@Override
		public void run(FunctionParameters parameters)
		{
			ILocation location = parameters.getLocation(0);
			WorldLibrary.prepareLocationForEdit(location);

			IBlock block = location.getBlock();
			if (block instanceof ISign)
			{
				ISign sign = (ISign) block;
				sign.setLines(
						parameters.getString(4),
						parameters.getString(5),
						parameters.getString(6),
						parameters.getString(7)
				);
				sign.update(true);
			}
		}
	}

	private static class PlaySound extends VoidFunction
	{
		@Override
		public void run(FunctionParameters parameters)
		{
			ILocation location = parameters.getLocation(0);
			location.playSound(
					Sound.Get(parameters.getString(4)),
					parameters.getFloat(5),
					parameters.getFloat(6)
			);
		}
	}

	private static class GetBlock extends RunsafeLuaFunction
	{
		@Override
		public List<Object> run(FunctionParameters parameters)
		{
			List<Object> returns = new ArrayList<Object>();
			ILocation location = parameters.getLocation(0);
			WorldLibrary.prepareLocationForEdit(location);

			Item block = location.getBlock().getMaterial();
			returns.add(block.getItemID());
			returns.add(block.getData());

			return returns;
		}
	}

	private static class GetPlayers extends RunsafeLuaFunction
	{
		@Override
		public List<Object> run(FunctionParameters parameters)
		{
			List<Object> returns = new ArrayList<Object>();
			IWorld world = parameters.getWorld(0);

			for (IPlayer player : world.getPlayers())
				returns.add(player.getName());

			return returns;
		}
	}

	private static class GetPlayersInRegion extends RunsafeLuaFunction
	{

		@Override
		public List<Object> run (FunctionParameters parameters)
		{
			List<Object> returns = new ArrayList<Object>();
			IWorld world = parameters.getWorld(0);

			for (IPlayer player : regionControl.getPlayersInRegion(world, parameters.getString(1)))
				returns.add(player.getName());

			return returns;
		}
	}

	private static class CloneChestToPlayer extends VoidFunction
	{
		@Override
		public void run(FunctionParameters parameters)
		{
			ILocation location = parameters.getLocation(0);
			IBlock block = location.getBlock();

			if (block instanceof IChest)
			{
				IChest chest = (IChest) block;
				IPlayer player = parameters.getPlayer(4);

				for (RunsafeMeta item : chest.getInventory().getContents())
					player.give(item);
			}
		}
	}

	private static class RemoveItems extends VoidFunction
	{
		@Override
		public void run(FunctionParameters parameters)
		{
			IWorld world = parameters.getWorld(0);
			for (IEntity entity : world.getEntities())
				if (entity.getEntityType() == PassiveEntity.DroppedItem)
					entity.remove();
		}
	}

	private static IRegionControl regionControl;
}
