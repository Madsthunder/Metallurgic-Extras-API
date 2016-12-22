package api.metalextras;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
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
	
	public abstract Item getDrop();
	
	public abstract int getDropCount(Random random);
	
	public abstract int getDropCountWithFortune(int fortune, Random random);
	
	public abstract int getDropMeta();
	
	public abstract int getDropXP(Random random);
	
	@Nullable
	public abstract String getLanguageKey();
	
	@Nonnull
	public BlockOre generateBlock(OreTypes types)
	{
		if(this.blocks.containsKey(types))
			return this.blocks.get(types);
		BlockOre block = new BlockOre(this, types);
		this.blocks.put(types, block);
		return block;
	}
	
	@Nonnull
	public Collection<BlockOre> getBlocks()
	{
		return Sets.newHashSet(this.blocks.values());
	}
	
	@Nullable
	public IBlockState applyBlockState(OreType type)
	{
		for(BlockOre ore : this.blocks.values())
			if(ore.getOreTypeProperty().getTypes().hasOreType(type))
				return ore.withOreType(type);
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
	
	public static class Impl extends OreMaterial
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
		
		public Impl(OreProperties properties)
		{
			this.properties = properties;
		}
		
		public Impl(Function<OreMaterial, OreProperties> materialToProperties)
		{
			this.properties = materialToProperties.apply(this);
		}
		
		public OreProperties getOreProperties()
		{
			return this.properties;
		}
		
		public OreMaterial.Impl setHarvestLevel(int harvestLevel)
		{
			this.harvestLevel = harvestLevel;
			return this;
		}
		
		public int getHarvestLevel()
		{
			return this.harvestLevel;
		}
		
		public OreMaterial.Impl setItemDroppedAsOre()
		{
			return this.setItemDropped(ORE, -1, 0, 0);
		}
		
		public OreMaterial.Impl setItemDropped(Item item, int meta, int xpMin, int xpMax)
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
		
		public OreMaterial.Impl setDropCountRange(int min, int max)
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
		
		public OreMaterial.Impl setFortuneAdditive(int fortuneAdditive)
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
		
		@Override
		public int getDropXP(Random random)
		{
			return MathHelper.getRandomIntegerInRange(random, this.xpMin, this.xpMax);
		}
		
		public OreMaterial.Impl setIngot(Item item, int minXP, int maxXP)
		{
			return this.setIngot(item, minXP, maxXP);
		}
		
		public OreMaterial.Impl setLanguageKey(String langKey)
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
		
		@SideOnly(Side.CLIENT)
		public IModel getModel(OreType type)
		{
			return ModelLoaderRegistry.getModelOrMissing(this.model);
		}
		
		public OreMaterial.Impl setModelType(ModelType type)
		{
			this.modelType = type;
			return this;
		}
		
		@SideOnly(Side.CLIENT)
		public ModelType getModelType()
		{
			return this.modelType;
		}
		
		public OreMaterial.Impl setOverrides(GenerateMinable.EventType... types)
		{
			this.overrides = Sets.newHashSet(types);
			return this;
		}
		
		@Override
		public Collection<GenerateMinable.EventType> getOverrides()
		{
			return this.overrides;
		}
		
		public OreMaterial.Impl setCreativeTab(CreativeTabs tab)
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
