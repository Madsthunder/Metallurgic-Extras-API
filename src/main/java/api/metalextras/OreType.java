package api.metalextras;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelUVLock;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class OreType implements Comparable<OreType>
{
	private ResourceLocation name;
	private final OreTypes types;
	private final IBlockState state;
	private final List<OreTypeDictionary> entries;
	private int id = -1;
	
	public OreType(OreTypes types, IBlockState state, OreTypeDictionary... entries)
	{
		this.state = state;
		this.types = types;
		this.entries = Lists.newArrayList(entries);
	}
	
	public IBlockState getState()
	{
		return this.state;
	}
	
	public abstract float getHardness();
	
	public abstract float getResistance();
	
	public abstract String getLanguageKey();
	
	public OreTypes getTypes()
	{
		return this.types;
	}
	
	public SoundType getSoundType()
	{
		return this.getState().getBlock().getSoundType();
	}
	
	public String getHarvestTool()
	{
		return this.getState().getBlock().getHarvestTool(this.getState());
	}
	
	public Material getMaterial()
	{
		return this.getState().getBlock().getMaterial(this.getState());
	}
	
	public int getHarvestLevel()
	{
		return this.getState().getBlock().getHarvestLevel(this.getState());
	}
	
	public boolean canFall()
	{
		return this.getState().getBlock() instanceof BlockFalling;
	}
	
	public int compareTo(OreType o)
	{
		List<OreType> list = OreUtils.getAllOreTypes();
		return list.indexOf(this) - list.indexOf(o);
	}
	
	public void handleEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity)
	{
		
	}
	
	public AxisAlignedBB getSelectionBox(World world, BlockPos pos)
	{
		return Block.FULL_BLOCK_AABB;
	}
	
	@SideOnly(Side.CLIENT)
	public abstract IModel getModel(OreMaterial material);
	
	public final OreType setRegistryName(String modid, String name)
	{
		return this.setRegistryName(new ResourceLocation(modid, name));
	}
	
	public final OreType setRegistryName(String name)
	{
		return this.setRegistryName(new ResourceLocation(name));
	}
	
	public final OreType setRegistryName(ResourceLocation name)
	{
		if(this.getRegistryName() != null)
			throw new IllegalStateException("Attempted to set registry name with existing registry name! New: " + name + " Old: " + this.getRegistryName());
		this.name = name;
		return this;
	}
	
	public final ResourceLocation getRegistryName()
	{
		return this.name;
	}
	
	public final Class<? super OreType> getRegistryType()
	{
		return OreType.class;
	}
	
	public Collection<OreTypeDictionary> getOreTypeDictionaryList()
	{
		return Sets.newHashSet(this.entries);
	}
	
	public final void update()
	{
		this.id = OreUtils.getID(this);
	}
	
	public static class Impl extends OreType
	{
		private float hardness = 0;
		private float resistance = 0;
		private String langKey;
		@SideOnly(Side.CLIENT)
		private ResourceLocation texture;
		
		public Impl(OreTypes types, IBlockState state, OreTypeDictionary... entries)
		{
			super(types, state, entries);
			this.langKey = state.getBlock().getUnlocalizedName() + ".name";
		}
		
		public OreType.Impl setHardness(float hardness)
		{
			this.hardness = hardness;
			return this;
		}
		
		@Override
		public float getHardness()
		{
			return this.hardness;
		}
		
		public OreType.Impl setResistance(float resistance)
		{
			this.resistance = resistance * 3;
			return this;
		}
		
		@Override
		public float getResistance()
		{
			return this.resistance * 5;
		}
		
		public OreType.Impl setLanguageKey(String langKey)
		{
			this.langKey = langKey;
			return this;
		}
		
		public String getLanguageKey()
		{
			return this.langKey;
		}
		
		public OreType.Impl setModelTexture(ResourceLocation texture)
		{
			if(FMLLaunchHandler.side() == Side.CLIENT)
				this.texture = texture;
			return this;
		}
		
		@SideOnly(Side.CLIENT)
		public IModel getModel(OreMaterial material)
		{
			return getModelFromTexture(this.texture);
		}
		
		public static IModel getModelFromTexture(ResourceLocation texture)
		{
			IModel missing = ModelLoaderRegistry.getMissingModel();
			IModel model = ModelLoaderRegistry.getModelOrMissing(new ResourceLocation("minecraft:block/cube_all"));
			if(model != missing && model instanceof IRetexturableModel)
			{
				if(model instanceof IModelUVLock)
					model = ((IModelUVLock)model).uvlock(true);
				return ((IRetexturableModel)model).retexture(new ImmutableMap.Builder().put("all", texture.toString()).build());
			}
			return missing;
		}
	}
}
