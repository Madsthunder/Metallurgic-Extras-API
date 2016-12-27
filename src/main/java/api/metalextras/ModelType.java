package api.metalextras;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class ModelType
{
	private static final BiMap<String, ModelType> types = HashBiMap.create();
	public static final ModelType IRON = byName("IRON");
	public static final ModelType EMERALD = byName("EMERALD");
	public static final ModelType LAPIS = byName("LAPIS");

	private ModelType(String name)
	{
		types.put(name, this);
	}

	public final String getName()
	{
		return types.inverse().get(this);
	}

	public static ModelType byName(String name)
	{
		name = name.toUpperCase();
		ModelType type = allTypes().get(name);
		if (type != null)
			return type;
		return new ModelType(name);
	}

	public static BiMap<String, ModelType> allTypes()
	{
		return HashBiMap.create(types);
	}
}
