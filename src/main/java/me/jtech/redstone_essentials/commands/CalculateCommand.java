package me.jtech.redstone_essentials.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.jtech.redstone_essentials.SelectionData;
import me.jtech.redstone_essentials.ServerPlayerLabelStorage;
import me.jtech.redstone_essentials.utility.SelectionHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

enum returnBase {
    DEC,
    HEX,
    BIN,
    OCT
}

public class CalculateCommand { // TODO comment this
    private static returnBase rBase = returnBase.DEC;
    private static boolean copy = false;
    private static boolean vertical = false;

    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("c")
                    .executes(CalculateCommand::noArgs)
                    .then(CommandManager.argument("expression", greedyString())
                            .executes(CalculateCommand::executeCommand)));
            dispatcher.register(CommandManager.literal("calc")
                    .executes(CalculateCommand::noArgs)
                    .then(CommandManager.argument("expression", greedyString())
                            .executes(CalculateCommand::executeCommand)));
            dispatcher.register(CommandManager.literal("calculate")
                    .executes(CalculateCommand::noArgs)
                    .then(CommandManager.argument("expression", greedyString())
                            .executes(CalculateCommand::executeCommand)));
        });
    }

    private static int noArgs(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("You need to provide an expression!"), false);
        return 1;
    }


    private static int executeCommand(CommandContext<ServerCommandSource> context) {
        String expression = StringArgumentType.getString(context, "expression");

        String calcString = dissectFlags(expression, context.getSource().getPlayer());

        String result = Integer.toString((int) evaluateExpression(calcString));

        result = switch (rBase) {
            case BIN -> baseConversion(result, 10, 2);
            case HEX -> baseConversion(result, 10, 16);
            case OCT -> baseConversion(result, 10, 8);
            default -> result;
        };

        if (rBase!=returnBase.DEC) {
            switch (rBase) {
                case BIN -> result = "b"+result;
                case HEX -> result = "0x"+result;
                case OCT -> result = "o"+result;
            }
        }

        if (vertical) {
            String vResult = "";
            for (char c : result.toCharArray()) {
                vResult = vResult.concat("\n");
                vResult = vResult.concat(String.valueOf(c));
            }
            result = vResult;
        }

        String finalResult = result;

        context.getSource().sendFeedback(() -> Text.literal("= " + finalResult).withColor(8251903), false);

        if (copy) {
            if (!GraphicsEnvironment.isHeadless()) {
                StringSelection stringSelection = new StringSelection(finalResult);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
        }
        resetValues();
        return 1;
    }

    public static String baseConversion(String number, int sBase, int dBase) {
        // Parse the number with source radix
        // and return in specified radix(base)
        return Integer.toString(Integer.parseInt(number, sBase), dBase);
    }

    private static void resetValues() {
        rBase = returnBase.DEC;
        copy = false;
        vertical = false;
    }

    private static String dissectFlags(String value, ServerPlayerEntity player) {
        if (value.contains("#")) {
            String flags = value.substring(value.indexOf('#'));
            String calc = value.substring(0, value.indexOf('#'));
            if (flags.contains("d")) {
                rBase = returnBase.DEC;
            } else if (flags.contains("b")) {
                rBase = returnBase.BIN;
            } else if (flags.contains("h")) {
                rBase = returnBase.HEX;
            } else if (flags.contains("o")) {
                rBase = returnBase.OCT;
            }
            if (flags.contains("c")) {
                copy = true;
            }
            if (flags.contains("v")) {
                vertical = true;
            }
            return dissectPlaceholders(calc, player);
        }
        return dissectPlaceholders(value, player);
    }

    private static String dissectPlaceholders(String value, ServerPlayerEntity player) {
        if (value.contains("%")) {
            String[] placeholders = extractVariables(value);
            for (String placeholder : placeholders) {
                List<SelectionData> playerLabels = ServerPlayerLabelStorage.playerLabelList.get(player);
                for (SelectionData rtbo : playerLabels) {
                    if (rtbo.label.equals(placeholder)) {
                        int x = rtbo.blockPos.getX() + rtbo.size.getX() - 1;
                        int y = rtbo.blockPos.getY() + rtbo.size.getY() - 1;
                        int z = rtbo.blockPos.getZ() + rtbo.size.getZ() - 1;

                        SelectionHelper selectionHelper = new SelectionHelper(rtbo.blockPos, new BlockPos(x, y, z), false);
                        int data = selectionHelper.readData(player.getWorld(), 1);
                        value = value.replace("%" + placeholder + "%", data + "");
                    }
                }
            }
        }
        return value;
    }

    public static String[] extractVariables(String input) {
        ArrayList<String> variables = new ArrayList<>();
        Pattern pattern = Pattern.compile("%(.*?)%");  // Regex to match %variable%
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables.toArray(new String[0]);
    }

    private static double evaluateExpression(String expression) {
        return evaluate(expression.replaceAll(" ", ""));
    }

    // Evaluate function using stacks for handling operators and operands
    private static double evaluate(String expression) {
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();
        int length = expression.length();

        for (int i = 0; i < length; i++) {
            char current = expression.charAt(i);

            // If current character is a number
            if (Character.isDigit(current)) {
                StringBuilder buffer = new StringBuilder();
                // Handle multi-digit numbers
                while (i < length && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    buffer.append(expression.charAt(i++));
                }
                i--; // Go back one step since the loop incremented i too far

                if (i-buffer.toString().length() >= 0) {
                    if (isInputBase(expression.charAt(i - buffer.toString().length()))) {
                        buffer = applyInputBase(buffer, expression.charAt(i - buffer.toString().length()));
                    }
                }

                numbers.push(Double.parseDouble(buffer.toString()));
            } else if (current == '(') {
                operators.push(current);
            } else if (current == ')') {
                // Resolve everything inside parentheses
                while (operators.peek() != '(') {
                    numbers.push(applyOperator(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.pop(); // Remove '(' from the stack
            } else if (isOperator(current)) {
                // Process all operators with precedence >= current
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(current)) {
                    numbers.push(applyOperator(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.push(current);
            }
        }

        // Process the remaining operators
        while (!operators.isEmpty()) {
            numbers.push(applyOperator(operators.pop(), numbers.pop(), numbers.pop()));
        }

        // The final result is on the top of the numbers stack
        return numbers.pop();
    }

    private static boolean isInputBase(char iB) {
        return iB == 'b' || iB == 'd' || iB == 'h' || iB == 'o';
    }

    private static boolean isOperator(char op) {
        return op == '+' || op == '-' || op == '*' || op == '/' || op == '^';
    }

    private static int precedence(char op) {
        if (op == '+' || op == '-') {
            return 1;
        } else if (op == '*' || op == '/') {
            return 2;
        }
        return 0;
    }

    private static double applyOperator(char op, double b, double a) {
        return switch (op) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> a / b;
            case '^' -> Math.pow(a, b);
            default -> 0;
        };
    }

    private static StringBuilder applyInputBase(StringBuilder buffer, char base) {
        return switch (base) {
            case 'b' -> new StringBuilder().append(Integer.parseInt(buffer.toString(), 2));
            case 'd' -> new StringBuilder().append(Integer.parseInt(buffer.toString(), 10));
            case 'h' -> new StringBuilder().append(Integer.parseInt(buffer.toString(), 16));
            case 'o' -> new StringBuilder().append(Integer.parseInt(buffer.toString(), 8));
            default -> throw new IllegalStateException("Unexpected value: " + base);
        };
    }
}
