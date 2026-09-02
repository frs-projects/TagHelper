package io.github.samarium150.minecraft.mod.taghelper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.samarium150.minecraft.mod.taghelper.platform.ItemData;
import io.github.samarium150.minecraft.mod.taghelper.util.CommandUtil;
import io.github.samarium150.minecraft.mod.taghelper.util.GeneralUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtTagArgument;

import javax.annotation.Nonnull;

/**
 * Builds the whole command tree.
 *
 * <p>Every loader calls this from its own command registration event, so the
 * grammar is identical on all supported Minecraft versions. The tree is built
 * fresh on each call: it must not be cached in a static field, because commands
 * are re-registered on every world load and a shared builder would accumulate
 * duplicate nodes.
 */
public final class TagHelperCommands {
    
    private TagHelperCommands() { }
    
    public static void register(@Nonnull CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(GeneralUtil.MOD_ID)
                .requires(source -> source.hasPermission(2));
        
        root.then(operations(Commands.literal("holding"), Scope.HOLDING));
        root.then(Commands.literal("slot")
                .then(operations(
                        Commands.argument("slot", IntegerArgumentType.integer(CommandUtil.SLOT_MIN, CommandUtil.SLOT_MAX)),
                        Scope.SLOT)));
        root.then(operations(Commands.literal("hotbar"), Scope.HOTBAR));
        root.then(operations(Commands.literal("inventory"), Scope.INVENTORY));
        root.then(operations(Commands.literal("echest"), Scope.ECHEST));
        
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(root);
        dispatcher.register(Commands.literal(GeneralUtil.MOD_ALIAS)
                .requires(source -> source.hasPermission(2))
                .redirect(node));
    }
    
    /**
     * Hangs get/set/remove off a scope node.
     *
     * <p>The key argument type comes from {@link ItemData} because it differs by
     * Minecraft version: pre-1.20.5 keys are arbitrary NBT names, whereas data
     * component ids are resource locations.
     */
    private static <T extends ArgumentBuilder<CommandSourceStack, T>> T operations(@Nonnull T parent, @Nonnull Scope scope) {
        ArgumentType<?> keyType = ItemData.keyArgument();
        return parent
                .then(Commands.literal("get")
                        .executes(context -> Get.run(context, scope)))
                .then(Commands.literal("set")
                        .then(Commands.argument("key", keyType)
                                .suggests(ItemData::suggestKeys)
                                .then(Commands.argument("value", NbtTagArgument.nbtTag())
                                        .executes(context -> Set.entry(context, scope))))
                        .then(Commands.argument("data", CompoundTagArgument.compoundTag())
                                .executes(context -> Set.all(context, scope))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("key", keyType)
                                .suggests(ItemData::suggestKeys)
                                .executes(context -> Remove.entry(context, scope)))
                        .executes(context -> Remove.all(context, scope)));
    }
}
