package api.metalextras;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class OreUtils
{
	public static final ResourceLocation ORETYPE_TO_IBLOCKSTATE = new ResourceLocation("metalextras", "oretype_to_iblockstate");
	public static final ResourceLocation ORETYPE_TO_ID = new ResourceLocation("metalextras", "oretype_to_id");
	private static ObjectIntIdentityMap<OreType> ORETYPE_TO_ID_MAP;
	private static OreType[] ORETYPE_TO_IBLOCKSTATE_ARRAY;
	private static IForgeRegistry<OreMaterial> materials;
	private static IForgeRegistry<OreTypes> typeCollections;
	
	public static IForgeRegistry<OreMaterial> getMaterialsRegistry()
	{
		return materials == null ? materials = GameRegistry.findRegistry(OreMaterial.class) : materials;
	}
	
	public static IForgeRegistry<OreTypes> getTypeCollectionsRegistry()
	{
		return typeCollections == null ? typeCollections = GameRegistry.findRegistry(OreTypes.class) : typeCollections;
	}
	
	public static List<OreType> getAllOreTypes()
	{
		List<OreType> result = Lists.newArrayList();
		for(OreTypes types : getTypeCollectionsRegistry())
			for(OreType type : types)
				result.add(type);
		return result;
	}
	
	public static OreType getOreType(IBlockState state)
	{
		return getOreTypeToIBlockStateMap().inverse().get(state);
	}
	
	public static int getID(OreType type)
	{
		return getOreTypeIDMap().get(type);
	}
	
	@Nullable
	public static OreType findOreType(@Nonnull ResourceLocation location)
	{
		if(location == null)
			throw new IllegalStateException("Parameter 'location' Cannot be Null");
		for(OreTypes types : getTypeCollectionsRegistry())
			for(OreType type : types)
				if(location.equals(type.getRegistryName()))
					return type;
		return null;
	}
	
	public static BiMap<OreType, IBlockState> getOreTypeToIBlockStateMap()
	{
		return getTypeCollectionsRegistry().getSlaveMap(ORETYPE_TO_IBLOCKSTATE, BiMap.class);
	}
	
	public static ObjectIntIdentityMap<OreType> getOreTypeIDMap()
	{
		if(ORETYPE_TO_ID_MAP != null)
			return ORETYPE_TO_ID_MAP;
		ORETYPE_TO_ID_MAP = getTypeCollectionsRegistry().getSlaveMap(ORETYPE_TO_ID, ObjectIntIdentityMap.class);
		if(ORETYPE_TO_ID_MAP != null)
			return ORETYPE_TO_ID_MAP;
		return new ObjectIntIdentityMap();
	}
	
	public static void registerMaterialSmeltingRecipe(OreMaterial material, ItemStack ingot, float xp, boolean registerItem)
	{
		for(BlockOre block : material.getBlocks())
			FurnaceRecipes.instance().addSmeltingRecipeForBlock(block, ingot, xp);
		if(registerItem)
		{
			ItemStack item = getItemStackForMaterial(material);
			if(item != null)
				FurnaceRecipes.instance().addSmeltingRecipe(item, ingot, xp);
		}
	}
	
	public static void addMaterialToOreDictionary(OreMaterial material, String name, boolean registerItem)
	{
		for(BlockOre block : material.getBlocks())
			OreDictionary.registerOre(name, block);
		if(registerItem)
		{
			ItemStack item = getItemStackForMaterial(material);
			if(item != null)
				OreDictionary.registerOre(name, item);
		}
	}
	
	@Nullable
	public static ItemStack getItemStackForMaterial(OreMaterial material)
	{
		if(OreMaterial.ORE != null)
		{
			int index = getMaterialsRegistry().getValues().indexOf(material);
			if(index >= 0)
				return new ItemStack(OreMaterial.ORE, 1, index);
		}
		return null;
	}
	
	public static GenerateMinable.EventType getEventType(String name)
	{
		name = name.toUpperCase();
		for(GenerateMinable.EventType type : GenerateMinable.EventType.values())
			if(type.name().equals(name))
				return type;
		return EnumHelper.addEnum(GenerateMinable.EventType.class, name, new Class[0]);
	}
	
	public static boolean generateOres(World world, BlockPos pos, Random random, int blocks, OreProperties properties)
	{
		  float f = random.nextFloat() * (float)Math.PI; double d0 =
		  (double)((float)(pos.getX() + 8) + MathHelper.sin(f) * (float)blocks
		  / 8.0F); double d1 = (double)((float)(pos.getX() + 8) -
		  MathHelper.sin(f) * (float)blocks / 8.0F); double d2 =
		  (double)((float)(pos.getZ() + 8) + MathHelper.cos(f) * (float)blocks
		  / 8.0F); double d3 = (double)((float)(pos.getZ() + 8) -
		  MathHelper.cos(f) * (float)blocks / 8.0F); double d4 =
		  (double)(pos.getY() + random.nextInt(3) - 2); double d5 =
		  (double)(pos.getY() + random.nextInt(3) - 2);
		  
		  for(int i = 0; i < blocks; ++i) { float f1 = (float)i /
		  (float)blocks; double d6 = d0 + (d1 - d0) * (double)f1; double d7 =
		  d4 + (d5 - d4) * (double)f1; double d8 = d2 + (d3 - d2) * (double)f1;
		  double d9 = random.nextDouble() * (double)blocks / 16.0D; double d10
		  = (double)(MathHelper.sin((float)Math.PI * f1) + 1.0F) * d9 + 1.0D;
		  double d11 = (double)(MathHelper.sin((float)Math.PI * f1) + 1.0F) *
		  d9 + 1.0D; int j = MathHelper.floor_double(d6 - d10 / 2.0D); int k =
		  MathHelper.floor_double(d7 - d11 / 2.0D); int l =
		  MathHelper.floor_double(d8 - d10 / 2.0D); int i1 =
		  MathHelper.floor_double(d6 + d10 / 2.0D); int j1 =
		  MathHelper.floor_double(d7 + d11 / 2.0D); int k1 =
		  MathHelper.floor_double(d8 + d10 / 2.0D);
		  
		  for(int l1 = j; l1 <= i1; ++l1) { double d12 = ((double)l1 + 0.5D -
		  d6) / (d10 / 2.0D);
		  
		  if(d12 * d12 < 1.0D) { for(int i2 = k; i2 <= j1; ++i2) { double d13 =
		  ((double)i2 + 0.5D - d7) / (d11 / 2.0D);
		  
		  if(d12 * d12 + d13 * d13 < 1.0D) { for(int j2 = l; j2 <= k1; ++j2) {
		  double d14 = ((double)j2 + 0.5D - d8) / (d10 / 2.0D);
		  
		  if(d12 * d12 + d13 * d13 + d14 * d14 < 1.0D) { BlockPos blockpos =
		  new BlockPos(l1, i2, j2);
		  
		  IBlockState state = world.getBlockState(blockpos);
		  if(properties.canSpawnAtCoords(world, blockpos) &&
		  state.getBlock().isReplaceableOreGen(state, world, blockpos,
		 properties)) { world.setBlockState(blockpos, properties.getOre(state,
		 world, blockpos), 2); } } } } } } } } return true;
	}
}
