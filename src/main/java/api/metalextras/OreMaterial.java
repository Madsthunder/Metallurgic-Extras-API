package api.metalextras;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class OreMaterial extends IForgeRegistryEntry.Impl<OreMaterial>
{
	@GameRegistry.ObjectHolder("metalextras:ore")
	public static final Item ORE = null;
	
	private ResourceLocation name;
	protected final Map<OreTypes, BlockOre> blocks = Maps.newHashMap();
	
	public abstract int getHarvestLevel();
	
	@Nullable
	public abstract String getLanguageKey();
	
	public final BlockOre getBlock(OreTypes types)
	{
		if(this.blocks.containsKey(types))
			return this.blocks.get(types);
		BlockOre block = this.createBlock(types);
		if(ForgeRegistries.BLOCKS.containsKey(block.getRegistryName()))
		{
			Block block1 = ForgeRegistries.BLOCKS.getValue(block.getRegistryName());
			if(block1 instanceof BlockOre)
				block = (BlockOre)block1;
			else
				throw new IllegalStateException("There Is Already A Block \"" + block.getRegistryName() + "\" Registered That Doesn't Extend BlockOre.");
		}
		this.blocks.put(types, block);
		return block;
	}
	
	public Iterable<BlockOre> getBlocksToRegister(OreTypes types)
	{
		return Sets.newHashSet(this.createBlock(types));
	}
	
	public abstract BlockOre createBlock(OreTypes types);
	
	public Collection<BlockOre> getBlocks()
	{
		for(OreTypes types : OreUtils.getTypeCollectionsRegistry())
			if(!this.blocks.containsKey(types))
				this.getBlock(types);
		return Sets.newHashSet(this.blocks.values());
	}
	
	@Nullable
	public IBlockState applyBlockState(OreType type)
	{
		for(BlockOre ore : this.blocks.values())
			if(ore.getOreTypeProperty().getTypes().hasOreType(type))
				return ore.getBlockState(type);
		return null;
	}
	
	public abstract OreProperties getOreProperties();
	
	public Collection<GenerateMinable.EventType> getOverrides()
	{
		return Sets.newHashSet();
	}
	
	@Nullable
	public CreativeTabs getCreativeTab()
	{
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public abstract IModel getModel(OreType type);
	
	@SideOnly(Side.CLIENT)
	public ModelType getModelType()
	{
		return ModelType.IRON;
	}
	
	@Override
	public final String toString()
	{
		return "OreMaterial{" + this.getRegistryName() + "}";
	}
	
	public static abstract class Impl extends OreMaterial
	{
		public abstract Item getDrop();
		
		public abstract int getDropMeta();
		
		public abstract int getDropCount(Random random);
		
		public abstract int getDropCountWithFortune(int fortune, Random random);
		
		public abstract int getDropXP(Random random);
		
		@Override
		public BlockOre createBlock(OreTypes types)
		{
			return new BlockOre.SimpleImpl(this, types);
		}
		
	}
	
	public static class SimpleImpl extends Impl
	{
		private final OreProperties properties;
		private int harvestLevel = 0;
		private Item drop = null;
		private int dropMeta = 0;
		private int dropMin = 1;
		private int dropMax = 1;
		private int xpMin = 0;
		private int xpMax = 0;
		private int fortuneAdditive;
		private String langKey = null;
		private ResourceLocation model = null;
		private ModelType modelType = ModelType.IRON;
		private Collection<GenerateMinable.EventType> overrides = Sets.newHashSet();
		private CreativeTabs tab = null;
		
		public SimpleImpl(OreProperties properties)
		{
			this.properties = properties;
		}
		
		public SimpleImpl(Function<OreMaterial, OreProperties> materialToProperties)
		{
			this.properties = materialToProperties.apply(this);
		}
		
		@Override
		public OreProperties getOreProperties()
		{
			return this.properties;
		}
		
		public OreMaterial.SimpleImpl setHarvestLevel(int harvestLevel)
		{
			this.harvestLevel = harvestLevel;
			return this;
		}
		
		@Override
		public int getHarvestLevel()
		{
			return this.harvestLevel;
		}
		
		public OreMaterial.SimpleImpl setItemDroppedAsOre()
		{
			return this.setItemDropped(ORE, -1, 0, 0);
		}
		
		public OreMaterial.SimpleImpl setItemDropped(Item item, int meta, int xpMin, int xpMax)
		{
			this.drop = item;
			this.dropMeta = meta;
			this.xpMin = xpMin;
			this.xpMax = xpMax;
			return this;
		}
		
		@Override
		public Item getDrop()
		{
			return this.drop;
		}
		
		@Override
		public int getDropMeta()
		{
			return this.dropMeta;
		}
		
		@Override
		public int getDropXP(Random random)
		{
			return MathHelper.getInt(random, this.xpMin, this.xpMax);
		}
		
		public OreMaterial.SimpleImpl setDropCountRange(int min, int max)
		{
			this.dropMin = min;
			this.dropMax = max;
			return this;
		}
		
		@Override
		public int getDropCount(Random random)
		{
			return this.dropMin >= this.dropMax ? this.dropMin : this.dropMin + random.nextInt(this.dropMax - this.dropMin + 1);
		}
		
		public OreMaterial.SimpleImpl setFortuneAdditive(int fortuneAdditive)
		{
			this.fortuneAdditive = fortuneAdditive;
			return this;
		}
		
		@Override
		public int getDropCountWithFortune(int fortune, Random random)
		{
			int dropCount = this.getDropCount(random);
			if(fortune > 0)
				return dropCount * (Math.max(random.nextInt((fortune + this.fortuneAdditive)) - 1, 0) + 1);
			return dropCount;
		}
		
		public OreMaterial.SimpleImpl setIngot(Item item, int minXP, int maxXP)
		{
			return this.setIngot(item, minXP, maxXP);
		}
		
		public OreMaterial.SimpleImpl setLanguageKey(String langKey)
		{
			this.langKey = langKey;
			return this;
		}
		
		@Override
		public String getLanguageKey()
		{
			return this.langKey;
		}
		
		public OreMaterial setModel(ResourceLocation model)
		{
			this.model = model;
			return this;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public IModel getModel(OreType type)
		{
			return ModelLoaderRegistry.getModelOrMissing(this.model);
		}
		
		public OreMaterial.SimpleImpl setModelType(ModelType type)
		{
			this.modelType = type;
			return this;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public ModelType getModelType()
		{
			return this.modelType;
		}
		
		public OreMaterial.SimpleImpl setOverrides(GenerateMinable.EventType... types)
		{
			this.overrides = Sets.newHashSet(types);
			return this;
		}
		
		@Override
		public Collection<GenerateMinable.EventType> getOverrides()
		{
			return this.overrides;
		}
		
		public OreMaterial.SimpleImpl setCreativeTab(CreativeTabs tab)
		{
			this.tab = tab;
			return this;
		}
		
		@Override
		public CreativeTabs getCreativeTab()
		{
			return this.tab;
		}
	}
}
