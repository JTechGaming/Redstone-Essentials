package me.jtech.redstone_essentials.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DynamicCommandHandler {
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("redstone_essentials/custom_commands.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Map<String, CommandData> registeredCommands = new HashMap<>();

    public static void init() {
        registerRegisterCommandCommand();
        loadCommandsFromConfig();
    }

    private static void registerRegisterCommandCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("registerCommand")
                            .then(CommandManager.argument("command", StringArgumentType.string())
                                    .then(CommandManager.argument("baseExecutor", StringArgumentType.string())
                                            .then(CommandManager.argument("arguments", StringArgumentType.greedyString())
                                                    .executes(context -> {
                                                        String command = context.getArgument("command", String.class);
                                                        String baseExecutor = context.getArgument("baseExecutor", String.class);
                                                        String argsJson = context.getArgument("arguments", String.class);

                                                        // Parse arguments JSON
                                                        List<SubCommand> arguments = parseArguments(argsJson);

                                                        // Register the command
                                                        registerCommand(command, baseExecutor, arguments);
                                                        context.getSource().sendFeedback(() -> Text.of("Command registered: " + command), false);
                                                        return Command.SINGLE_SUCCESS;
                                                    }))))
            );
        });
    }

    private static List<SubCommand> parseArguments(String argsJson) {
        try {
            SubCommand[] subCommands = GSON.fromJson(argsJson, SubCommand[].class);
            return Arrays.asList(subCommands);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static void loadCommandsFromConfig() {
        if (!CONFIG_FILE.toFile().exists()) {
            return;
        }
        try {
            CommandData[] commands = GSON.fromJson(Files.newBufferedReader(CONFIG_FILE), CommandData[].class);
            registeredCommands = new HashMap<>();
            for (CommandData commandData : commands) {
                registerCommand(commandData.command, commandData.baseExecutor, commandData.subCommands);
                registeredCommands.put(commandData.command, commandData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveCommandsToConfig() {
        try {
            Files.writeString(CONFIG_FILE, GSON.toJson(registeredCommands.values()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerCommand(String command, String baseExecutor, List<SubCommand> subCommands) {
        CommandData commandData = new CommandData(command, baseExecutor, subCommands);
        registeredCommands.put(command, commandData);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Remove existing command if it exists
            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(command));

            var commandBuilder = CommandManager.literal(command)
                    .executes(context -> executeCommand(dispatcher, context, baseExecutor));

            // Add subcommands directly under the main command
            for (SubCommand subCommand : subCommands) {
                commandBuilder.then(CommandManager.literal(subCommand.name)
                        .executes(context -> executeCommand(dispatcher, context, subCommand.executor)));
            }

            dispatcher.getRoot().addChild(commandBuilder.build());
        });

        saveCommandsToConfig();
    }

    public static class CommandData {
        public String command;
        public String baseExecutor;
        public List<SubCommand> subCommands;

        public CommandData(String command, String baseExecutor, List<SubCommand> subCommands) {
            this.command = command;
            this.baseExecutor = baseExecutor;
            this.subCommands = subCommands;
        }
    }

    public static class SubCommand {
        public String name;
        public String executor;
    }

    private static int executeCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> context, String executor) {
        try {
            dispatcher.execute(executor, context.getSource());
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendError(Text.of("Failed to execute command: " + e.getMessage()));
            return 0;
        }
    }

    public static class ArgumentStructure {
        public String id;
        public List<SubArgument> subs;

        public static class SubArgument {
            public String name;
            public String executor;
        }
    }
}