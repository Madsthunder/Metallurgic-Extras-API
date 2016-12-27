package api.metalextras;

import java.util.List;

import com.google.common.base.Optional;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockStateContainer.Builder;

public class OreTypeProperty implements IProperty<OreType>
{
	private final OreTypes types;
	private final BlockOre block;
	private final BlockStateContainer container;
	
	public OreTypeProperty(OreTypes types, BlockOre block)
	{
		this.types = types;
		this.block = block;
		if(this.types.getOreTypes().size() > 1)
			this.container = new Builder(this.block).add(this).build();
		else
			this.container = new Builder(this.block).build();
	}
	
	@Override
	public String getName()
	{
		return "type";
	}
	
	@Override
	public final List<OreType> getAllowedValues()
	{
		return this.types.getOreTypes();
	}
	
	@Override
	public Class<OreType> getValueClass()
	{
		return OreType.class;
	}
	
	@Override
	public Optional<OreType> parseValue(String value)
	{
		for(OreType type : this.types.getOreTypes())
			if(type.getRegistryName().toString().replaceAll(":", "_").equals(value))
				return Optional.fromNullable(type);
		return Optional.absent();
	}
	
	@Override
	public String getName(OreType type)
	{
		return type.getRegistryName().toString().replaceAll(":", "_");
	}
	
	public final OreTypes getTypes()
	{
		return this.types;
	}
	
	public final BlockStateContainer getBlockState()
	{
		return this.container;
	}
}
