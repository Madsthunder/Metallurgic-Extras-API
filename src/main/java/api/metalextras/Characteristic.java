package api.metalextras;

import java.util.Collection;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.world.DimensionType;

public class Characteristic
{
	public static final BiMap<String, Characteristic> characteristics = HashBiMap.create();
	public static final Characteristic ROCKY = byName("ROCKY");
	public static final Characteristic DIRTY = byName("DIRTY");
	public static final Characteristic SANDY = byName("SANDY");
	public static final Characteristic LOOSE = byName("LOOSE");
	public static final Characteristic COMPACT = byName("COMPACT");
	public static final Characteristic DENSE = byName("DENSE");
	public static final Characteristic UNBREAKABLE = byName("UNBREAKABLE");
	public static final Characteristic COLD = byName("COLD");
	public static final Characteristic HOT = byName("HOT");
	public static final Characteristic WET = byName("WET");
	public static final Characteristic DRY = byName("DRY");

	private Characteristic(String name)
	{
		characteristics.put(name, this);
	}

	public String getName()
	{
		return characteristics.inverse().get(this);
	}

	public static Characteristic byName(String name)
	{
		name = name.toUpperCase();
		Characteristic characteristic = allCharacteristics().get(name);
		if (characteristic != null)
			return characteristic;
		return new Characteristic(name);
	}

	public static Characteristic byDimension(DimensionType dimension)
	{
		return byName(dimension.name());
	}

	public static Predicate<Collection<Characteristic>> notAny(final Characteristic... characteristics)
	{
		return new Predicate<Collection<Characteristic>>()
		{
			@Override
			public boolean apply(Collection<Characteristic> characteristics1)
			{
				for (Characteristic characteristic : characteristics)
					if (characteristics1.contains(characteristic))
						return false;
				return true;
			}
		};
	}

	public static Predicate<Collection<Characteristic>> all(final Characteristic... characteristics)
	{
		return new Predicate<Collection<Characteristic>>()
		{
			@Override
			public boolean apply(Collection<Characteristic> characteristics1)
			{
				for (Characteristic characteristic : characteristics)
					if (!characteristics1.contains(characteristic))
						return false;
				return true;
			}
		};
	}

	public static BiMap<String, Characteristic> allCharacteristics()
	{
		return characteristics == null ? HashBiMap.create() : HashBiMap.create(characteristics);
	}
}