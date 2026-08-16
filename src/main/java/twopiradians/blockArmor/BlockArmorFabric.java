package twopiradians.blockArmor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.CommonProxy;
import twopiradians.blockArmor.common.block.ModBlocks;
import twopiradians.blockArmor.common.config.Config;
import twopiradians.blockArmor.common.item.ArmorSet;
import twopiradians.blockArmor.common.item.ModItems;
import twopiradians.blockArmor.common.seteffect.SetEffect;
import twopiradians.blockArmor.common.seteffect.SetEffectHoarder;
import twopiradians.blockArmor.common.seteffect.SetEffectHealth_Boost;
import twopiradians.blockArmor.common.seteffect.SetEffectRespawn;
import twopiradians.blockArmor.common.seteffect.SetEffectUndying;
import twopiradians.blockArmor.common.tileentity.ModTileEntities;
import twopiradians.blockArmor.creativetab.BlockArmorCreativeTab;

/** Fabric common entrypoint. All registrations are intentionally complete before registries freeze. */
public final class BlockArmorFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModTileEntities.register();
        SetEffectHoarder.registerContainers();
        ModItems.discoverGeneratedArmor();
        SetEffect.setup();
        Config.load();
        ModItems.registerDiscoveredArmor();
        BlockArmorCreativeTab.initialize();
        CommonProxy.setup();

		CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, selection) -> {
            twopiradians.blockArmor.common.command.CommandDev.register(dispatcher);
            dispatcher.register(net.minecraft.commands.Commands.literal("blockarmor")
                    .requires(source -> net.minecraft.commands.Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
                    .then(net.minecraft.commands.Commands.literal("reload").executes(context -> {
                        Config.reload();
                        CommonProxy.refreshRecipes(context.getSource().getServer());
                        for (net.minecraft.server.level.ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers())
                            BlockArmor.NETWORK.sendConfig(player);
                        context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Block Armor configuration reloaded"), true);
                        return 1;
                    })));
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (Config.importWorldForgeConfig(server)) CommonProxy.refreshRecipes(server);
            CommonProxy.onServerStarted(server);
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                {
                    CommonProxy.onPlayerJoin(handler.player);
                    SetEffectHealth_Boost.onLogin(handler.player);
                });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ArmorSet.onLogout(handler.player);
            BlockArmor.key.clear(handler.player);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                SetEffectHealth_Boost.onRespawn(newPlayer));
		ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) ->
                BlockArmor.NETWORK.sendCooldowns(player));
        ServerPlayerEvents.ALLOW_DEATH.register((player, source, amount) ->
                !(SetEffectUndying.onDeath(player) || SetEffectRespawn.onDeath(player)));
        ServerTickEvents.START_SERVER_TICK.register(ArmorSet::tickServer);
        ServerTickEvents.END_SERVER_TICK.register(server -> SetEffectHoarder.flushDirtyItems());

        BlockArmor.LOGGER.info("Initialized generated Block Armor sets: {}", ArmorSet.allSets.size());
    }
}
