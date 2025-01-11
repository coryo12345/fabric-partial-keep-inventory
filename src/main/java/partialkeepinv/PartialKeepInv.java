package partialkeepinv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class PartialKeepInv implements ModInitializer {
	public static final String MOD_ID = "partial-keepinv";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing PartialKeepInventory");
		this.registerCommands();
	}

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("partialkeepinv")
					.requires(source -> source.hasPermissionLevel(2))
					.executes(context -> {
						PartialKeepState state = PartialKeepState.getServerState(context.getSource().getServer());
						context.getSource().sendFeedback(
								() -> Text.literal(String.format("PartialKeepInventory is %s, keep chance is %d",
										state.getEnabled() ? "enabled" : "disabled", state.getKeepChance())),
								false);
						return 0;
					})
					.then(CommandManager.literal("enable")
							.executes((context) -> {
								this.toggleMod(context.getSource().getServer(), true);
								context.getSource().sendFeedback(() -> Text.literal(
										"PartialKeepInventory Enabled (keepInventory gamerule must also be enabled)"),
										false);
								return 0;
							}))
					.then(CommandManager.literal("disable")
							.executes(context -> {
								this.toggleMod(context.getSource().getServer(), false);
								context.getSource().sendFeedback(() -> Text.literal("PartialKeepInventory Disabled"),
										false);
								return 0;
							}))
					.then(CommandManager.literal("setChance")
							.then(CommandManager.argument("value", IntegerArgumentType.integer())
									.executes(context -> {
										final int value = IntegerArgumentType.getInteger(context, "value");
										this.setChance(context.getSource().getServer(), value);
										context.getSource().sendFeedback(
												() -> Text.literal(String.format("Set keep chance to %d", value)),
												false);
										return 0;
									}))));
		});
	}

	private void toggleMod(MinecraftServer server, boolean status) {
		PartialKeepState state = PartialKeepState.getServerState(server);
		state.setEnabled(status);
		state.save(server);
	}

	private void setChance(MinecraftServer server, int chance) {
		PartialKeepState state = PartialKeepState.getServerState(server);
		state.setKeepChance(chance);
		state.save(server);
	}
}