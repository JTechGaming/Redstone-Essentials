package me.jtech.redstone_essentials.client.editor.windows;

import imgui.ImGui;
import imgui.ImDrawList;
import imgui.ImVec2;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiSliderFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GraphCalculatorWindow {
    private static final List<String> equations = new ArrayList<>();
    private static final List<Integer> colors = new ArrayList<>();
    private static final float[] range = {-10, 10}; // X-axis range
    private static final float[] step = {0.1f}; // Step size for plotting
    private static float zoom = 1.0f; // Zoom factor
    private static ImString equationInput = new ImString(256);

    private static float offsetX = 0.0f; // Horizontal offset for panning
    private static float offsetY = 0.0f; // Vertical offset for panning
    private static int selectedGraph1 = -1; // First graph for intersection
    private static int selectedGraph2 = -1; // Second graph for intersection

    private static boolean hoveredThisFrame = false; // Track if a graph was already hovered this frame

    public static ImBoolean isOpen = new ImBoolean(false);

    public static void render() {
        if (!isOpen.get()) {
            return;
        }
        hoveredThisFrame = false;
        ImGui.begin("Graph Calculator", isOpen);

        // Horizontal layout
        ImGui.beginChild("EquationsList", 200, 0, true); // Fixed width of 200
        ImGui.text("Equations:");
        for (int i = 0; i < equations.size(); i++) {
            ImGui.text((i + 1) + ". " + equations.get(i));
            ImGui.sameLine();
            if (ImGui.checkbox("##Graph" + i, selectedGraph1 == i || selectedGraph2 == i)) {
                if (selectedGraph1 == -1) {
                    selectedGraph1 = i; // Select first graph
                } else if (selectedGraph2 == -1 && selectedGraph1 != i) {
                    selectedGraph2 = i; // Select second graph
                } else if (selectedGraph1 == i) {
                    selectedGraph1 = -1; // Deselect if already selected
                } else if (selectedGraph2 == i) {
                    selectedGraph2 = -1; // Deselect if already selected
                }
            }

            ImGui.sameLine();
            if (ImGui.button("Remove##" + i)) {
                equations.remove(i);
                colors.remove(i);
                break;
            }
        }

        // Input for new equation
        ImGui.separator();
        ImGui.text("Enter Equation:");
        if (ImGui.inputText("##EquationInput", equationInput, ImGuiInputTextFlags.EnterReturnsTrue)) {
            equations.add(equationInput.get());
            colors.add(new Random().nextInt(0xFFFFFF) | 0xFF000000); // Random color
        }
        ImGui.endChild();

        ImGui.sameLine(); // Place the graph next to the equations list

        // Graph rendering
        ImGui.beginChild("GraphArea", 0, 0, false); // Remaining space for the graph

        // Zoom controls
        ImGui.text("Zoom:");
        ImGui.sameLine();
        if (ImGui.button("-")) zoom *= 0.9f;
        ImGui.sameLine();
        if (ImGui.button("+")) zoom *= 1.1f;
        ImGui.sameLine();
        ImGui.spacing();
        ImGui.sameLine();
        ImGui.text("Step Size:");
        ImGui.sameLine();
        ImGui.sliderFloat("##StepSize", step, 0.01f, 1.0f, "%.2f", ImGuiSliderFlags.AlwaysClamp);
        ImGui.sameLine();
        if (ImGui.button("Calculate Intersection") && selectedGraph1 != -1 && selectedGraph2 != -1) {
            calculateIntersection(equations.get(selectedGraph1), equations.get(selectedGraph2));
        }

        ImGui.spacing();
        ImGui.spacing();

        ImVec2 canvasPos = ImGui.getCursorScreenPos();
        ImVec2 canvasSize = ImGui.getContentRegionAvail();
        ImDrawList drawList = ImGui.getWindowDrawList();

        // Handle panning
        if (ImGui.isMouseDragging(1)) { // Right mouse button
            offsetX += ImGui.getIO().getMouseDeltaX();
            offsetY += ImGui.getIO().getMouseDeltaY();
        }

        // Draw grid and equations
        drawGrid(drawList, canvasPos, canvasSize);
        updateRange(canvasSize);
        for (int i = 0; i < equations.size(); i++) {
            plotEquation(drawList, new ImVec2(offsetX, offsetY).plus(canvasPos), canvasSize, equations.get(i), colors.get(i));
        }
        ImGui.endChild();

        ImGui.end();
    }

    private static void drawGrid(ImDrawList drawList, ImVec2 canvasPos, ImVec2 canvasSize) {
        float gridSpacing = 20.0f * zoom; // Adjust grid spacing with zoom
        int gridLinesX = (int) (canvasSize.x / gridSpacing) + 2; // Extra lines for panning
        int gridLinesY = (int) (canvasSize.y / gridSpacing) + 2;

        float centerX = canvasPos.x + canvasSize.x / 2 + offsetX;
        float centerY = canvasPos.y + canvasSize.y / 2 + offsetY;

        // Draw vertical grid lines and labels
        for (int i = -gridLinesX; i <= gridLinesX; i++) {
            float x = centerX + i * gridSpacing;
            drawList.addLine(x, canvasPos.y, x, canvasPos.y + canvasSize.y, 0xFFAAAAAA, 1.0f);
            if (i != 0) {
                float labelX = (i * gridSpacing - offsetX) / (20.0f * zoom);
                ImGui.getWindowDrawList().addText(x + 2, centerY + 2, 0xFFAAAAAA, String.format("%.1f", labelX));
            }
        }

        // Draw horizontal grid lines and labels
        for (int i = -gridLinesY; i <= gridLinesY; i++) {
            float y = centerY - i * gridSpacing;
            drawList.addLine(canvasPos.x, y, canvasPos.x + canvasSize.x, y, 0xFFAAAAAA, 1.0f);
            if (i != 0) {
                float labelY = (offsetY - i * gridSpacing) / (20.0f * zoom);
                ImGui.getWindowDrawList().addText(centerX + 2, y, 0xFFAAAAAA, String.format("%.1f", labelY));
            }
        }

        // Draw axes
        drawList.addLine(centerX, canvasPos.y, centerX, canvasPos.y + canvasSize.y, 0xFFFFFFFF, 3.0f);
        drawList.addLine(canvasPos.x, centerY, canvasPos.x + canvasSize.x, centerY, 0xFFFFFFFF, 3.0f);
    }

    private static void updateRange(ImVec2 canvasSize) {
        float visibleWidth = canvasSize.x / (20.0f * zoom);
        float visibleHeight = canvasSize.y / (20.0f * zoom);

        range[0] = -visibleWidth / 2 - offsetX / (20.0f * zoom);
        range[1] = visibleWidth / 2 - offsetX / (20.0f * zoom);
    }

    private static void plotEquation(ImDrawList drawList, ImVec2 canvasPos, ImVec2 canvasSize, String equation, int color) {
        float centerX = canvasPos.x + canvasSize.x / 2;
        float centerY = canvasPos.y + canvasSize.y / 2;
        float scale = 20.0f * zoom; // Adjust scale with zoom

        if (equation.startsWith("x=")) {
            // Handle vertical line (e.g., x=5)
            try {
                float xValue = Float.parseFloat(equation.substring(2).trim());
                float screenX = centerX + xValue * scale;
                drawList.addLine(screenX, canvasPos.y, screenX, canvasPos.y + canvasSize.y, color, 2.0f);
            } catch (NumberFormatException e) {
                System.err.println("Invalid vertical line equation: " + equation);
            }
            return;
        }

        for (float x = range[0]; x <= range[1]; x += step[0]) {
            float y = evaluateEquation(equation, x);
            float screenX = centerX + x * scale;
            float screenY = centerY - y * scale;

            float nextX = x + step[0];
            float nextY = evaluateEquation(equation, nextX);
            float nextScreenX = centerX + nextX * scale;
            float nextScreenY = centerY - nextY * scale;

            drawList.addLine(screenX, screenY, nextScreenX, nextScreenY, color, 2.0f);

            // Check for hover
            if (ImGui.isMouseHoveringRect(screenX - 5, screenY - 5, screenX + 5, screenY + 5) && !hoveredThisFrame) {
                ImGui.getWindowDrawList().addText(screenX + 5, screenY - 5, 0xFFFFFFFF, String.format("(%.2f, %.2f)", x, y));
                hoveredThisFrame = true; // Mark that a graph was hovered this frame
            }
        }
    }

    private static float evaluateEquation(String equation, float x) {
        try {
            if (equation.startsWith("y=")) {
                equation = equation.substring(2);
            }

            Expression expression = new ExpressionBuilder(equation)
                    .variable("x")
                    .build()
                    .setVariable("x", x);

            return (float) expression.evaluate();
        } catch (Exception e) {
            System.err.println("Invalid equation: " + equation);
            return 0;
        }
    }

    private static void calculateIntersection(String equation1, String equation2) {
        if (equation1.startsWith("x=") && equation2.startsWith("x=")) {
            System.out.println("No intersection: Both are vertical lines.");
            return;
        }

        if (equation1.startsWith("x=") || equation2.startsWith("x=")) {
            // Handle intersection between a vertical line and a regular equation
            String verticalEquation = equation1.startsWith("x=") ? equation1 : equation2;
            String regularEquation = equation1.startsWith("x=") ? equation2 : equation1;

            try {
                float xValue = Float.parseFloat(verticalEquation.substring(2).trim());
                float yValue = evaluateEquation(regularEquation, xValue);
                System.out.printf("Intersection found at: (%.2f, %.2f)%n", xValue, yValue);
            } catch (NumberFormatException e) {
                System.err.println("Invalid vertical line equation: " + verticalEquation);
            }
            return;
        }

        // Handle intersection between two regular equations
        float xStart = range[0];
        float xEnd = range[1];
        float tolerance = 0.001f;

        while (xEnd - xStart > tolerance) {
            float xMid = (xStart + xEnd) / 2;
            float y1 = evaluateEquation(equation1, xMid);
            float y2 = evaluateEquation(equation2, xMid);

            if (Math.abs(y1 - y2) < tolerance) {
                System.out.printf("Intersection found at: (%.2f, %.2f)%n", xMid, y1);
                return;
            }

            if (y1 > y2) {
                xEnd = xMid;
            } else {
                xStart = xMid;
            }
        }

        System.out.println("No intersection found.");
    }
}