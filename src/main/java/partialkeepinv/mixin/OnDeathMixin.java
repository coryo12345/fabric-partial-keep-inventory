package partialkeepinv.mixin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;
import partialkeepinv.PartialKeepState;

@Mixin(ServerPlayerEntity.class)
public class OnDeathMixin {
	@Inject(at = @At("TAIL"), method = "onDeath")
	private void died(DamageSource damageSource, CallbackInfo info) {
		PlayerEntity player = (PlayerEntity) (Object) this;
		MinecraftServer server = player.getServer();
		ServerWorld world = server.getWorld(player.getWorld().getRegistryKey());

		if (this.shouldDoPartialKeep(server, world)) {
			ArrayList<ItemStack> items = this.pickDroppedItems(server, player);
			if (items.size() > 0) {
				Vector3d coords = new Vector3d(player.getX(), player.getY(), player.getZ());
				this.dropItems(world, items, coords);
				this.alertPlayer(player, items, coords);
			}
		}
	}

	private boolean shouldDoPartialKeep(MinecraftServer server, ServerWorld currentWorld) {
		BooleanRule keepInventory = currentWorld.getGameRules().get(GameRules.KEEP_INVENTORY);
		PartialKeepState state = PartialKeepState.getServerState(server);
		return keepInventory.get() == state.getEnabled();
	}

	private ArrayList<ItemStack> pickDroppedItems(MinecraftServer server, PlayerEntity player) {
		PlayerInventory inv = player.getInventory();
		ArrayList<ItemStack> items = new ArrayList<>();
		int keepChance = PartialKeepState.getServerState(server).getKeepChance();

		Consumer<ItemStack> handleItem = (ItemStack item) -> {
			if (item.isEmpty())
				return;

			if (Math.random() * 100 >= keepChance) {
				items.add(item.copy());
				item.setCount(0);
			}
		};
		inv.main.forEach(handleItem);
		inv.armor.forEach(handleItem);
		inv.offHand.forEach(handleItem);
		return items;
	}

	private void dropItems(ServerWorld world, ArrayList<ItemStack> items, Vector3d coords) {
		for (ItemStack itemStack : items) {
			ItemEntity ie = new ItemEntity(world, coords.x, coords.y, coords.z, itemStack);
			world.spawnEntity(ie);
		}
	}

	private void alertPlayer(PlayerEntity player, ArrayList<ItemStack> droppedItems, Vector3d coords) {
		player.sendMessage(Text.literal(String.format("You died at: %d, %d, %d", Math.round(coords.x),
				Math.round(coords.y), Math.round(coords.z))), false);
		player.sendMessage(Text.literal("The following items were dropped:"), false);

		HashMap<String, Integer> countMap = new HashMap<>();
		for (ItemStack itemStack : droppedItems) {
			String key = itemStack.getItemName().getString();
			if (countMap.containsKey(key)) {
				countMap.put(key, countMap.get(key) + itemStack.getCount());
			} else {
				countMap.put(key, itemStack.getCount());
			}
		}

		for (Entry<String, Integer> entry : countMap.entrySet()) {
			player.sendMessage(
					Text.literal(String.format("  - %s (%d)", entry.getKey(), entry.getValue())),
					false);
		}
	}
}