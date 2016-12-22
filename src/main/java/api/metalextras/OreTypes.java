package api.metalextras;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

public class OreTypes implements IForgeRegistryEntry<OreTypes>, Iterable<OreType>
{
	private boolean locked = false;
	private ResourceLocation registryName = null;
	private final List<OreType> types = Lists.newArrayList();
	
	public boolean addOreType(OreType type)
	{
		if(this.locked)
			return false;
		if(this.types.contains(type))
			return true;
		if(this.types.size() <= 16)
			this.types.add(type);
		else
			return false;
		return true;
	}
	
	public boolean hasOreType(OreType type)
	{
		return this.types.contains(type);
	}
	
	public List<OreType> getOreTypes()
	{
		return Lists.newArrayList(this.types);
	}
	
	public final void lock()
	{
		this.locked = true;
	}
	
	public final void update()
	{
		for(OreType type : this)
			type.update();
	}
	
	public final OreTypes setRegistryName(String modid, String name)
	{
		return this.setRegistryName(new ResourceLocation(modid, name));
	}
	
	public final OreTypes setRegistryName(String name)
	{
		return this.setRegistryName(new ResourceLocation(name));
	}
	
	@Override
	public final OreTypes setRegistryName(ResourceLocation name)
	{
		if(this.getRegistryName() != null)
			throw new IllegalStateException("Attempted to set registry name with existing registry name! New: " + name + " Old: " + this.getRegistryName());
		this.registryName = name;
		return this;
	}
	
	@Override
	public final ResourceLocation getRegistryName()
	{
		return this.registryName;
	}
	
	@Override
	public final Class<? super OreTypes> getRegistryType()
	{
		return OreTypes.class;
	}
	
	@Override
	public final Iterator<OreType> iterator()
	{
		return this.types.iterator();
	}
}
