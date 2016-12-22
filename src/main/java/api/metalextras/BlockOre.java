package api.metalextras;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoader;

public class BlockOre extends BlockFalling
{
	private final OreTypeProperty property;
	private final OreMaterial material;
	
	public BlockOre(OreMaterial material, OreTypes types)
	{
		super(Material.ROCK);
		this.setRegistryName(material.getRegistryName().toString() + "." + types.getRegistryName().toString().replaceFirst(":", "_"));
		this.property = new OreTypeProperty(types, this);
		this.setDefaultState(this.getBlockState().getBaseState());
		this.setDefaultState(this.getBlockState().getProperties().isEmpty() ? this.getDefaultState() : this.getDefaultState().withProperty(this.getOreTypeProperty(), this.getOreTypeProperty().getAllowedValues().get(0)));
		this.material = material;
	}
	
	public OreMaterial getOreMaterial()
	{
		return this.material;
	}
	
	@Override
	public final Material getMaterial(IBlockState state)
	{
		return this.getOreType(state).getMaterial();
	}
	
	@Override
	public final SoundType getSoundType(IBlockState state, World world, BlockPos pos, Entity entity)
	{
		return this.getOreType(state).getSoundType();
	}
	
	@Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity entity, @Nullable ItemStack stack)
    {
		boolean harvest = false;
		if(stack != null)
			for(String tool : stack.getItem().getToolClasses(stack))
				if(this.isToolEffective(tool, state))
				{
					harvest = true;
					break;
				}
		if(harvest)
			super.harvestBlock(world, player, pos, state, entity, stack);
		else
		{
			OreType type = this.getOreType(state);
			Block block = type.getState().getBlock();
			if(type.getState().getBlock().canHarvestBlock(world, pos, player))
				Block.spawnAsEntity(world, pos, new ItemStack(block.getItemDropped(type.getState(), world.rand, 0), block.quantityDropped(world.rand), block.damageDropped(type.getState())));
		}
    }
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess access, BlockPos pos, IBlockState state, int fortune)
	{
		Random random = access instanceof World ? ((World)access).rand : RANDOM;
		List<ItemStack> list = super.getDrops(access, pos, state, fortune);
		IBlockState typeState = this.getOreType(state).getState();
		Block typeBlock = typeState.getBlock();
		list.add(new ItemStack(typeBlock.getItemDropped(typeState, random, 0), typeBlock.quantityDropped(random), typeBlock.damageDropped(typeState)));
		return list;
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult result, World world, BlockPos pos, EntityPlayer player)
	{
		return new ItemStack(this, 1, this.getMetaFromState(state));
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random random, int fortune)
	{
		return this.material.getDrop();
	}
	
	@Override
	public int quantityDropped(Random random)
	{
		return this.material.getDropCount(random);
	}
	
	@Override
	public int quantityDroppedWithBonus(int fortune, Random random)
	{
		return this.material.getDropCountWithFortune(fortune, random);
	}
	
	@Override
	public int damageDropped(IBlockState state)
	{
		int meta = this.material.getDropMeta();
		return meta == -1 ? OreUtils.getItemStackForMaterial(this.material).getMetadata() : meta;
	}
	
	@Override
	public int getExpDrop(IBlockState state, IBlockAccess access, BlockPos pos, int fortune)
	{
		return this.material.getDropXP(access instanceof World ? ((World)access).rand : Block.RANDOM);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
	{
		tooltip.add((this.getOreType(stack.getMetadata()).getState().getBlock().getLocalizedName()));
	}
	
	@Override
	public boolean canSilkHarvest()
	{
		return true;
	}
	
	@Override
	public float getBlockHardness(IBlockState state, World world, BlockPos pos)
	{
		return this.getOreType(state).getHardness();
	}
	
	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity entity, Explosion explosion)
	{
		return this.getOreType(world.getBlockState(pos)).getResistance();
	}
	
	@Override
	public int getHarvestLevel(IBlockState state)
	{
		int materialHarvest = this.getOreMaterial().getHarvestLevel();
		int typeHarvest = this.getOreType(state).getHarvestLevel();
		return materialHarvest == -1 || typeHarvest == -1 ? -1 : Math.max(materialHarvest, typeHarvest);
	}
	
	@Override
	public String getHarvestTool(IBlockState state)
	{
		return this.getOreType(state).getHarvestTool();
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state)
	{
		if(state.getValue(this.property).canFall())
			world.scheduleUpdate(pos, this, this.tickRate(world));
	}
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos neighbor)
	{
		if(state.getValue(this.property).canFall())
			world.scheduleUpdate(pos, this, this.tickRate(world));
	}
	
	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, NonNullList<ItemStack> list)
	{
		for(int i = 0; i < this.property.getAllowedValues().size(); i++)
			list.add(new ItemStack(item, 1, i));
	}
	
	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
	{
		state.getValue(this.property).handleEntityCollision(world, pos, state, entity);
	}
	
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random random)
	{
		if(state.getValue(this.property).canFall())
			super.updateTick(world, pos, state, random);
	}
	
	@Override
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager)
	{
		IBlockState state = world.getBlockState(pos).getActualState(world, pos);
		List<TextureAtlasSprite> textures = Lists.newArrayList();
		{
			OreType type = this.getOreType(state);
			TextureAtlasSprite texture = this.getOreMaterial().getModel(type).bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()).getParticleTexture();
			if(texture != null)
				textures.add(texture);
			texture = type.getModel(this.getOreMaterial()).bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()).getParticleTexture();
			if(texture != null)
				textures.add(texture);
		}
		if(textures.isEmpty())
			return super.addDestroyEffects(world, pos, manager);
		int i = 4;
		for(int j = 0; j < 4; j++)
			for(int k = 0; k < 4; k++)
				for(int l = 0; l < 4; l++)
				{
					double x = pos.getX() + (j + .5) / 4;
					double y = pos.getY() + (k + .5) / 4;
					double z = pos.getZ() + (l + .5) / 4;
					ParticleDigging particle = (ParticleDigging)new ParticleDigging.Factory().createParticle(0, world, x, y, z, x - pos.getX() - .5, y - pos.getY() - .5, z - pos.getZ() - .5, Block.getStateId(state));
					particle.setBlockPos(pos);
					particle.setParticleTexture(textures.get(world.rand.nextInt(textures.size())));
					manager.addEffect(particle);
				}
			
		return true;
	}
	
	@Override
	public boolean addHitEffects(IBlockState state, World world, RayTraceResult result, ParticleManager manager)
	{
		List<TextureAtlasSprite> textures = Lists.newArrayList();
		{
			OreType type = this.getOreType(state);
			TextureAtlasSprite texture = this.getOreMaterial().getModel(type).bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()).getParticleTexture();
			if(texture != null)
				textures.add(texture);
			texture = type.getModel(this.getOreMaterial()).bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()).getParticleTexture();
			if(texture != null)
				textures.add(texture);
		}
		if(textures.isEmpty())
			return super.addHitEffects(state, world, result, manager);
		BlockPos pos = result.getBlockPos();
		EnumFacing side = result.sideHit;
		int i = pos.getX();
		int j = pos.getY();
		int k = pos.getZ();
		float f = 0.1F;
		AxisAlignedBB axisalignedbb = state.getBoundingBox(world, pos);
		double d0 = (double)i + world.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minX;
		double d1 = (double)j + world.rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minY;
		double d2 = (double)k + world.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minZ;
		
		switch(side)
		{
			case DOWN :
			{
				d1 = j + axisalignedbb.minY - 0.10000000149011612D;
				break;
			}
			case UP :
			{
				d1 = j + axisalignedbb.maxY + 0.10000000149011612D;
				break;
			}
			case NORTH :
			{
				d2 = k + axisalignedbb.minZ - 0.10000000149011612D;
				break;
			}
			case SOUTH :
			{
				d2 = k + axisalignedbb.maxZ + 0.10000000149011612D;
				break;
			}
			case WEST :
			{
				d0 = i + axisalignedbb.minX - 0.10000000149011612D;
				break;
			}
			case EAST :
			{
				d0 = i + axisalignedbb.maxX + 0.10000000149011612D;
				break;
			}
			
			default:
				;
		}
		
		ParticleDigging particle = (ParticleDigging)new ParticleDigging.Factory().createParticle(0, world, d0, d1, d2, 0, 0, 0, Block.getStateId(state));
		particle.setBlockPos(pos);
		particle.multiplyVelocity(0.2F);
		particle.multipleParticleScaleBy(0.6F);
		particle.setParticleTexture(textures.get(world.rand.nextInt(textures.size())));
		manager.addEffect(particle);
		return true;
	}
	
	public boolean addLandingEffects(IBlockState state, WorldServer world, BlockPos pos, IBlockState useless, EntityLivingBase entity, int particles)
	{
		return true;
	}
	
	@Override
	public CreativeTabs getCreativeTabToDisplayOn()
	{
		return this.material.getCreativeTab();
	}
	
	public OreTypeProperty getOreTypeProperty()
	{
		return this.property;
	}
	
	public boolean hasOreType(OreType type)
	{
		return this.getOreTypeProperty().getAllowedValues().contains(type);
	}
	
	public IBlockState withOreType(OreType type)
	{
		return this.getDefaultState().withProperty(this.getOreTypeProperty(), type);
	}
	
	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
	{
		return state.getValue(this.getOreTypeProperty()).getSelectionBox(world, pos);
	}
	
	@Override
	public String getUnlocalizedName()
	{
		String langKey = this.material.getLanguageKey();
		return langKey == null ? this.material.getRegistryName().getResourcePath() : this.material.getLanguageKey();
	}
	
	public final int getMetaFromState(IBlockState state)
	{
		return this.getIndex(this.getOreType(state));
	}
	
	public final IBlockState getStateFromMeta(int meta)
	{
		return this.getBlockState(this.getOreType(meta));
	}
	
	public final BlockStateContainer getBlockState()
	{
		return this.property.getBlockState();
	}
	
	public final OreType getOreType(IBlockState state)
	{
		return this.getBlockState().getProperties().isEmpty() ? this.property.getAllowedValues().get(0) : state.getValue(this.property);
	}
	
	public final IBlockState getBlockState(OreType type)
	{
		return this.getBlockState().getProperties().isEmpty() ? this.getDefaultState() : this.getDefaultState().withProperty(this.property, type);
	}
	
	public final OreType getOreType(int index)
	{
		return this.property.getAllowedValues().get(index);
	}
	
	public final int getIndex(OreType type)
	{
		return type.getTypes() == this.getOreTypeProperty().getTypes() ? this.getBlockState().getProperties().isEmpty() ? 0 : this.property.getAllowedValues().indexOf(type) : -1;
	}
}
