package api.metalextras;

import java.util.Collection;

import com.google.common.base.Predicate;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.util.EnumHelper;

public enum OreTypeDictionary
{
	ROCKY, DIRTY, SANDY, LOOSE, COMPACT, DENSE, UNBREAKABLE, COLD, HOT, WET, DRY;

	public static OreTypeDictionary byName(String name)
	{
		name = name.toUpperCase();
		for (OreTypeDictionary dictionary : OreTypeDictionary.values())
			if (dictionary.name().equals(name))
				return dictionary;
		return EnumHelper.addEnum(OreTypeDictionary.class, name, new Class[0]);
	}

	public static OreTypeDictionary byDimension(DimensionType dimension)
	{
		return byName(dimension.name());
	}

	public static Predicate<Collection<OreTypeDictionary>> notAny(final OreTypeDictionary... entries)
	{
		return new Predicate<Collection<OreTypeDictionary>>()
		{
			@Override
			public boolean apply(Collection<OreTypeDictionary> entries1)
			{
				for (OreTypeDictionary entry : entries)
					if (entries1.contains(entry))
						return false;
				return true;
			}
		};
	}

	public static Predicate<Collection<OreTypeDictionary>> all(final OreTypeDictionary... entries)
	{
		return new Predicate<Collection<OreTypeDictionary>>()
		{
			@Override
			public boolean apply(Collection<OreTypeDictionary> entries1)
			{
				for (OreTypeDictionary entry : entries)
					if (!entries1.contains(entry))
						return false;
				return true;
			}
		};
	}
}
