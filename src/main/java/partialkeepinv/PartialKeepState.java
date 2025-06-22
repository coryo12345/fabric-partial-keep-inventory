package partialkeepinv;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

public class PartialKeepState extends PersistentState {
    private final static RegistryKey<World> STORAGE_WORLD = World.OVERWORLD;

    private final static boolean DEFAULT_ENABLED = false;
    private final static Integer DEFAULT_CHANCE = 90;

    private boolean enabled = false;
    private Integer keepChance = DEFAULT_CHANCE;

    public PartialKeepState() {
        this(DEFAULT_ENABLED, DEFAULT_CHANCE);
    }

    public PartialKeepState(boolean enabled, Integer keepChance) {
        this.enabled = enabled;
        this.keepChance = keepChance;
    }

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

    public void save(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(STORAGE_WORLD).getPersistentStateManager();
        persistentStateManager.set(TYPE, this);
    }

    private static final Codec<PartialKeepState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("enabled").forGetter(PartialKeepState::getEnabled),
            Codec.INT.fieldOf("keepChance").forGetter(PartialKeepState::getKeepChance))
            .apply(instance, PartialKeepState::new));

    private static final PersistentStateType<PartialKeepState> TYPE = new PersistentStateType<>(
            PartialKeepInv.MOD_ID,
            PartialKeepState::new,
            CODEC,
            DataFixTypes.POI_CHUNK);

    public static PartialKeepState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(STORAGE_WORLD).getPersistentStateManager();
        PartialKeepState state = persistentStateManager.getOrCreate(TYPE);
        return state;
    }
}
