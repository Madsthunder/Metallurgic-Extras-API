package api.metalextras;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenOre extends WorldGenerator
{
	private final OreProperties.Impl properties;
	
	public WorldGenOre(OreProperties.Impl properties)
	{
		this.properties = properties;
	}
	
	public boolean generate(World world, Random random, BlockPos pos)
	{
		return OreUtils.generateOres(world, pos, random, this.properties.getVeinSize(world, world.getBiome(pos)), this.properties);
	}
}