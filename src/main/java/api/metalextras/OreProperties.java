package api.metalextras;

import java.util.Collection;
import java.util.Random;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;

public abstract class OreProperties implements Predicate<IBlockState>
{
	public static class SimpleImpl extends OreProperties
	{
		private final OreMaterial material;
		private final WorldGenerator generator;
		private boolean shouldSpawn = false;
		private int spawnTries = 0;
		private int veinSize = 0;
		private int minHeight = 0;
		private int maxHeight = 0;
		private Predicate<Collection<Characteristic>> validTypes;
		
		public SimpleImpl(OreMaterial material)
		{
			this.material = material;
			this.generator = new WorldGenOre(this);
		}
		
		public OreProperties.SimpleImpl setSpawnEnabled(boolean shouldSpawn)
		{
			this.shouldSpawn = shouldSpawn;
			return this;
		}
		
		protected OreProperties.SimpleImpl setSpawnProperties(int spawns, int maxSize, int minHeight, int maxHeight, Predicate<Collection<Characteristic>> validTypes)
		{
			this.spawnTries = spawns;
			this.veinSize = maxSize;
			this.minHeight = minHeight;
			this.maxHeight = maxHeight;
			this.validTypes = Predicates.or(validTypes);
			return this;
		}
		
		@Override
		public OreMaterial getOreMaterial()
		{
			return this.material;
		}
		
		@Override
		public boolean getSpawnEnabled()
		{
			return this.shouldSpawn;
		}
		
		public int getVeinSize(World world, Biome biome)
		{
			return this.veinSize;
		}
		
		@Override
		public int getSpawnTriesPerChunk(World world, Random random)
		{
			return this.spawnTries;
		}
		
		@Override
		public BlockPos getRandomSpawnPos(World world, Random random)
		{
			return new BlockPos(random.nextInt(16), random.nextInt(this.maxHeight - this.minHeight) + this.minHeight, random.nextInt(16));
		}
		
		@Override
		public boolean isValid(OreType type)
		{
			return this.validTypes.apply(type.getCharacteristics());
		}
		
		@Override
		public WorldGenerator getWorldGenerator(World world, BlockPos pos)
		{
			return this.generator;
		}
	}
	
	public abstract OreMaterial getOreMaterial();
	
	public abstract boolean getSpawnEnabled();
	
	public abstract int getSpawnTriesPerChunk(World world, Random random);
	
	public abstract BlockPos getRandomSpawnPos(World world, Random random);
	
	public abstract WorldGenerator getWorldGenerator(World world, BlockPos pos);
	
	public boolean canSpawnAtCoords(World world, BlockPos pos)
	{
		return true;
	}
	
	@Override
	public final boolean apply(IBlockState state)
	{
		OreType type = OreUtils.getOreType(state);
		if(type != null)
			return this.isValid(type);
		return false;
	}
	
	public IBlockState getOre(IBlockState state, World world, BlockPos pos)
	{
		OreType type = OreUtils.getOreType(state);
		if(type == null)
			return state;
		for(BlockOre block : this.getOreMaterial().getBlocks())
		{
			OreTypeProperty property = block.getOreTypeProperty();
			if(property.getTypes() == type.getTypes())
				return block.getDefaultState().withProperty(property, type);
		}
		return state;
	}
	
	public abstract boolean isValid(OreType type);
	
	@Override
	public final int hashCode()
	{
		return this.getOreMaterial().hashCode();
	}
}
