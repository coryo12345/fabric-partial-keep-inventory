package partialkeepinv;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class PartialKeepState extends PersistentState {
    private final static RegistryKey<World> STORAGE_WORLD = World.OVERWORLD;

    private final static String ENABLED_KEY = "partialKeepInvEnabled";
    private final static String CHANCE_KEY = "partialKeepInvChance";

    private boolean enabled = false;
    private Integer keepChance = 90;

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.setDirty(true);
    }

    public Integer getKeepChance() {
        return this.keepChance;
    }

    public void setKeepChance(Integer chance) {
        this.keepChance = chance;
        this.setDirty(true);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, WrapperLookup registries) {
        nbt.putBoolean(ENABLED_KEY, this.enabled);
        nbt.putInt(CHANCE_KEY, this.keepChance);
        return nbt;
    }

    public void save(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(STORAGE_WORLD).getPersistentStateManager();
        persistentStateManager.set(PartialKeepInv.MOD_ID, this);
    }

    public static PartialKeepState createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        PartialKeepState state = new PartialKeepState();
        state.setEnabled(tag.getBoolean(ENABLED_KEY));
        state.setKeepChance(tag.getInt(CHANCE_KEY));
        return state;
    }

    private static Type<PartialKeepState> type = new Type<>(
            PartialKeepState::new, // constructor
            PartialKeepState::createFromNbt, // deserializer
            DataFixTypes.POI_CHUNK);

    public static PartialKeepState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(STORAGE_WORLD).getPersistentStateManager();
        PartialKeepState state = persistentStateManager.getOrCreate(type, PartialKeepInv.MOD_ID);
        return state;
    }
}
