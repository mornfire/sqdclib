package com.mornfire.sqdclib.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.cutscene.Cutscenes;
import com.mornfire.sqdclib.dialogue.Dialogues;
import com.mornfire.sqdclib.quest.QuestStatus;
import com.mornfire.sqdclib.quest.Quests;
import com.mornfire.sqdclib.storystate.Story;
import com.mornfire.sqdclib.storystate.StoryProgress;
import com.mornfire.sqdclib.storystate.WorldState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = SQDCLib.MOD_ID)
public final class StoryCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("story")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("status").executes(StoryCommands::status))
                        .then(Commands.literal("flag")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(StoryCommands::togglePlayerFlag)))
                        .then(Commands.literal("worldflag")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(StoryCommands::toggleWorldFlag)))
                        .then(Commands.literal("counter")
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes(StoryCommands::addCounter))))
                        .then(Commands.literal("phase")
                                .executes(StoryCommands::reportPhase)
                                .then(Commands.argument("phase", StringArgumentType.word())
                                        .executes(StoryCommands::setPhase)))
                        .then(Commands.literal("quest")
                                .then(Commands.argument("id", StringArgumentType.string())
                                        .executes(StoryCommands::reportQuest)
                                        .then(Commands.literal("start").executes(StoryCommands::startQuest))
                                        .then(Commands.literal("complete")
                                                .executes(StoryCommands::completeQuest))
                                        .then(Commands.literal("fail").executes(StoryCommands::failQuest))
                                        .then(Commands.literal("abandon").executes(StoryCommands::abandonQuest))
                                        .then(Commands.literal("reset").executes(StoryCommands::resetQuest))))
                        .then(Commands.literal("talk")
                                .then(Commands.argument("id", StringArgumentType.string())
                                        .executes(StoryCommands::talk)))
                        .then(Commands.literal("cutscene")
                                .then(Commands.argument("id", StringArgumentType.string())
                                        .executes(StoryCommands::cutscene))));
    }

    private static int status(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        StoryProgress progress = StoryProgress.of(player);
        WorldState state = WorldState.get(player.serverLevel());

        String phase = state.getPhase().isEmpty() ? "<unset>" : state.getPhase();
        context.getSource().sendSuccess(() -> Component.literal("phase: " + phase), false);
        context.getSource().sendSuccess(() -> Component.literal("world flags: " + state.getFlags()), false);
        context.getSource().sendSuccess(() -> Component.literal("player flags: " + progress.getFlags()), false);
        context.getSource().sendSuccess(() -> Component.literal("active quests: " + progress.getActiveQuests()), false);
        context.getSource().sendSuccess(() -> Component.literal("quests: " + progress.getCompletedQuests()), false);
        return 1;
    }

    private static int togglePlayerFlag(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String flag = StringArgumentType.getString(context, "name");
        boolean value = Story.setFlag(player, flag, !Story.hasFlag(player, flag));
        context.getSource().sendSuccess(() -> Component.literal("player flag " + flag + " = " + value), true);
        return value ? 1 : 0;
    }

    private static int toggleWorldFlag(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String flag = StringArgumentType.getString(context, "name");
        WorldState state = WorldState.get(player.serverLevel());
        boolean value = state.setFlag(flag, !state.hasFlag(flag));
        context.getSource().sendSuccess(() -> Component.literal("world flag " + flag + " = " + value), true);
        return value ? 1 : 0;
    }

    private static int addCounter(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String key = StringArgumentType.getString(context, "key");
        int amount = IntegerArgumentType.getInteger(context, "amount");
        int value = Story.addCounter(player, key, amount);
        context.getSource().sendSuccess(() -> Component.literal("counter " + key + " = " + value), true);
        return value;
    }

    private static int reportPhase(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String phase = WorldState.get(player.serverLevel()).getPhase();
        context.getSource()
                .sendSuccess(() -> Component.literal("phase: " + (phase.isEmpty() ? "<unset>" : phase)), false);
        return 1;
    }

    private static int setPhase(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String phase = StringArgumentType.getString(context, "phase");
        WorldState.get(player.serverLevel()).setPhase(phase);
        context.getSource().sendSuccess(() -> Component.literal("phase = " + phase), true);
        return 1;
    }

    private static int reportQuest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ResourceLocation id = id(context, "id");
        QuestStatus status = Quests.status(player, id);
        context.getSource().sendSuccess(() -> Component.literal("quest " + id + " = " + status), false);
        return status.ordinal();
    }

    private static int startQuest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ResourceLocation id = id(context, "id");
        boolean started = Quests.start(player, id);
        context.getSource().sendSuccess(() -> Component.literal("start quest " + id + " = " + started), true);
        return started ? 1 : 0;
    }

    private static int completeQuest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ResourceLocation id = id(context, "id");
        boolean completed = Quests.complete(player, id);
        context.getSource().sendSuccess(() -> Component.literal("complete quest " + id + " = " + completed), true);
        return completed ? 1 : 0;
    }

    private static int failQuest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ResourceLocation id = id(context, "id");
        boolean failed = Quests.fail(player, id);
        context.getSource().sendSuccess(() -> Component.literal("fail quest " + id + " = " + failed), true);
        return failed ? 1 : 0;
    }

    private static int abandonQuest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ResourceLocation id = id(context, "id");
        boolean abandoned = Quests.abandon(player, id);
        context.getSource().sendSuccess(() -> Component.literal("abandon quest " + id + " = " + abandoned), true);
        return abandoned ? 1 : 0;
    }

    private static int resetQuest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ResourceLocation id = id(context, "id");
        boolean reset = Quests.reset(player, id);
        context.getSource().sendSuccess(() -> Component.literal("reset quest " + id + " = " + reset), true);
        return reset ? 1 : 0;
    }

    private static int talk(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {        ServerPlayer player = context.getSource().getPlayerOrException();
        ResourceLocation id = id(context, "id");
        boolean started = Dialogues.start(player, id);
        context.getSource().sendSuccess(() -> Component.literal("dialogue " + id + " = " + started), true);
        return started ? 1 : 0;
    }

    private static int cutscene(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ResourceLocation id = id(context, "id");
        boolean started = Cutscenes.play(player, id);
        context.getSource().sendSuccess(() -> Component.literal("cutscene " + id + " = " + started), true);
        return started ? 1 : 0;
    }

    private static ResourceLocation id(CommandContext<CommandSourceStack> context, String name)
            throws CommandSyntaxException {
        String raw = StringArgumentType.getString(context, name);
        try {
            return ResourceLocation.parse(raw);
        } catch (Exception exception) {
            throw new SimpleCommandExceptionType(Component.literal("invalid id: " + raw)).create();
        }
    }

    private StoryCommands() {}
}
