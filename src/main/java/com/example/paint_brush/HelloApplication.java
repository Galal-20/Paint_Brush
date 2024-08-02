package com.example.paint_brush;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.lang.Object;
//import javafx.embed.swing.SwingFXUtils;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

public class HelloApplication extends Application {
    private double startX, startY;
    private String currentTool = "LINE";
    private boolean isFilled = false;
    private boolean isDotted = false;
    private boolean isBold = false;
    private GraphicsContext gc;
    private final Stack<Shape> shapeStack = new Stack<>();
    private final Stack<Shape> redoStack = new Stack<>();
    private double prevX, prevY;

    private static class Shape {
        String type;
        double startX, startY, endX, endY;
        Color color;
        boolean isFilled;
        boolean isDotted;
        boolean isBold;

        Shape(String type, double startX, double startY, double endX, double endY, Color color, boolean isFilled, boolean isDotted, boolean isBold) {
            this.type = type;
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.color = color;
            this.isFilled = isFilled;
            this.isDotted = isDotted;
            this.isBold = isBold;
        }
    }

    private void redrawShapes() {
        for (Shape shape : shapeStack) {
            gc.setStroke(shape.color);
            gc.setFill(shape.color);
            if (shape.isDotted) {
                gc.setLineDashes(10, 10);
            } else {
                gc.setLineDashes();
            }

            if (shape.isBold) {
                gc.setLineWidth(3);
            } else {
                gc.setLineWidth(1);
            }

            switch (shape.type) {
                case "LINE":
                    gc.strokeLine(shape.startX, shape.startY, shape.endX, shape.endY);
                    break;
                case "OVAL":
                    double ovalWidth = Math.abs(shape.endX - shape.startX);
                    double ovalHeight = Math.abs(shape.endY - shape.startY);
                    if (shape.isFilled) {
                        gc.fillOval(Math.min(shape.startX, shape.endX), Math.min(shape.startY, shape.endY), ovalWidth, ovalHeight);
                    } else {
                        gc.strokeOval(Math.min(shape.startX, shape.endX), Math.min(shape.startY, shape.endY), ovalWidth, ovalHeight);
                    }
                    break;
                case "RECTANGLE":
                    double rectWidth = Math.abs(shape.endX - shape.startX);
                    double rectHeight = Math.abs(shape.endY - shape.startY);
                    if (shape.isFilled) {
                        gc.fillRect(Math.min(shape.startX, shape.endX), Math.min(shape.startY, shape.endY), rectWidth, rectHeight);
                    } else {
                        gc.strokeRect(Math.min(shape.startX, shape.endX), Math.min(shape.startY, shape.endY), rectWidth, rectHeight);
                    }
                    break;
                case "FREE_HAND":
                    gc.strokeLine(shape.startX, shape.startY, shape.endX, shape.endY);
                    break;
            }
        }
    }

    private void clearCanvas() {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        shapeStack.clear();
        redoStack.clear();
    }

    private void undoLastAction() {
        if (!shapeStack.isEmpty()) {
            Shape shape = shapeStack.pop();
            redoStack.push(shape);
            gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());  // Clear the canvas
            redrawShapes();
        }
    }

    private void redoLastAction() {
        if (!redoStack.isEmpty()) {
            Shape shape = redoStack.pop();
            shapeStack.push(shape);
            gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());  // Clear the canvas
            redrawShapes();  // Redraw all shapes including the redone one
        }
    }

private void saveCanvasToFile() {
        WritableImage writableImage = new WritableImage((int) gc.getCanvas().getWidth(), (int) gc.getCanvas().getHeight());
        gc.getCanvas().snapshot(null, writableImage);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
        File file = fileChooser.showSaveDialog(null);

        /*if (file != null) {
            try {
            // error in SwingFXUtils.
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }*/
    }



    @Override
    public void start(Stage stage) {
        stage.setTitle("Paint Brush");

        BorderPane root = new BorderPane();
        Canvas canvas = new Canvas(1500, 600);
        canvas.setStyle("-fx-font-size: 18px; -fx-text-fill: red; -fx-font-weight: bold;");
        gc = canvas.getGraphicsContext2D();

        Label colorLabel = new Label("Color:");
        colorLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: red; -fx-font-weight: bold; -fx-alignment: center;");
        ColorPicker colorPicker = new ColorPicker(Color.BLACK);

        Label functionsLabel = new Label("Functions:");
        functionsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: red; -fx-font-weight: bold; -fx-alignment: center;");
        Button lineButton = new Button("Line");
        lineButton.setStyle("-fx-font-size: 16px; -fx-min-width: 100px; -fx-min-height: 30px; ");
        Button ovalButton = new Button("Oval");
        ovalButton.setStyle("-fx-font-size: 16px; -fx-min-width: 100px; -fx-min-height: 30px; ");
        Button rectangleButton = new Button("Rectangle");
        rectangleButton.setStyle("-fx-font-size: 16px; -fx-min-width: 100px; -fx-min-height: 30px; ");
        Button freeHandButton = new Button("Pencil");
        freeHandButton.setStyle("-fx-font-size: 16px; -fx-min-width: 100px; -fx-min-height: 30px; ");
        Button clearButton = new Button("Clear All");
        clearButton.setStyle("-fx-font-size: 16px; -fx-min-width: 100px; -fx-min-height: 30px; ");
        Button undoButton = new Button("Undo");
        undoButton.setStyle("-fx-font-size: 16px; -fx-min-width: 100px; -fx-min-height: 30px; ");
        Button redoButton = new Button("Redo");
        redoButton.setStyle("-fx-font-size: 16px; -fx-min-width: 100px; -fx-min-height: 30px; ");
        CheckBox fillCheckBox = new CheckBox("Filled");
        fillCheckBox.setStyle("-fx-font-size: 16px; -fx-min-width: 100px; -fx-min-height: 30px; ");
        CheckBox dottedCheckBox = new CheckBox("Dotted");
        dottedCheckBox.setStyle("-fx-font-size: 16px; -fx-min-width: 100px; -fx-min-height: 30px; ");
        CheckBox boldCheckBox = new CheckBox("Bold");
        boldCheckBox.setStyle("-fx-font-size: 16px; -fx-min-width: 100px; -fx-min-height: 30px; ");
        Label Settings = new Label("Settings:");
        Settings.setStyle("-fx-font-size: 18px; -fx-text-fill: red; -fx-font-weight: bold; -fx-alignment: center;");
        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-font-size: 16px; -fx-min-width: 100px; -fx-min-height: 30px; ");


        lineButton.setOnAction(e -> currentTool = "LINE");
        ovalButton.setOnAction(e -> currentTool = "OVAL");
        rectangleButton.setOnAction(e -> currentTool = "RECTANGLE");
        freeHandButton.setOnAction(e -> currentTool = "FREE_HAND");
        clearButton.setOnAction(e -> clearCanvas());
        undoButton.setOnAction(e -> undoLastAction());
        redoButton.setOnAction(e -> redoLastAction());
        fillCheckBox.setOnAction(e -> isFilled = fillCheckBox.isSelected());
        dottedCheckBox.setOnAction(e -> isDotted = dottedCheckBox.isSelected());
        boldCheckBox.setOnAction(e -> isBold = boldCheckBox.isSelected());
        //saveButton.setOnAction(e -> saveCanvasToFile());



        HBox colorBox = new HBox(5, colorLabel, colorPicker);
        HBox functionsBox = new HBox(5, functionsLabel, lineButton, ovalButton, rectangleButton, freeHandButton, clearButton, undoButton, redoButton, fillCheckBox, dottedCheckBox, boldCheckBox, Settings ,saveButton);
        ToolBar toolBar = new ToolBar(colorBox, functionsBox);
        root.setTop(toolBar);
        root.setCenter(canvas);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            startX = e.getX();
            startY = e.getY();
            prevX = startX;
            prevY = startY;

            if (currentTool.equals("FREE_HAND")) {
                gc.setStroke(colorPicker.getValue());
                gc.setFill(colorPicker.getValue());

                if (isDotted) {
                    gc.setLineDashes(10, 10);
                } else {
                    gc.setLineDashes();
                }

                if (isBold) {
                    gc.setLineWidth(3);
                } else {
                    gc.setLineWidth(1);
                }
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            double endX = e.getX();
            double endY = e.getY();

            if (currentTool.equals("FREE_HAND")) {
                gc.setStroke(colorPicker.getValue());
                gc.setFill(colorPicker.getValue());

                if (isDotted) {
                    gc.setLineDashes(10, 10);
                } else {
                    gc.setLineDashes();
                }

                if (isBold) {
                    gc.setLineWidth(3);
                } else {
                    gc.setLineWidth(1);
                }

                gc.strokeLine(prevX, prevY, endX, endY);
                shapeStack.push(new Shape("FREE_HAND", prevX, prevY, endX, endY, colorPicker.getValue(), false, isDotted, isBold)); // Save the segment of the freehand drawing
                prevX = endX;
                prevY = endY;
            } else {
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                redrawShapes();
                gc.setStroke(colorPicker.getValue());
                gc.setFill(colorPicker.getValue());

                if (isDotted) {
                    gc.setLineDashes(10, 10);
                } else {
                    gc.setLineDashes();
                }

                if (isBold) {
                    gc.setLineWidth(3);
                } else {
                    gc.setLineWidth(1);
                }

                switch (currentTool) {
                    case "LINE":
                        gc.strokeLine(startX, startY, endX, endY);
                        break;
                    case "OVAL":
                        double ovalWidth = Math.abs(endX - startX);
                        double ovalHeight = Math.abs(endY - startY);
                        if (isFilled) {
                            gc.fillOval(Math.min(startX, endX), Math.min(startY, endY), ovalWidth, ovalHeight);
                        } else {
                            gc.strokeOval(Math.min(startX, endX), Math.min(startY, endY), ovalWidth, ovalHeight);
                        }
                        break;
                    case "RECTANGLE":
                        double rectWidth = Math.abs(endX - startX);
                        double rectHeight = Math.abs(endY - startY);
                        if (isFilled) {
                            gc.fillRect(Math.min(startX, endX), Math.min(startY, endY), rectWidth, rectHeight);
                        } else {
                            gc.strokeRect(Math.min(startX, endX), Math.min(startY, endY), rectWidth, rectHeight);
                        }
                        break;
                }
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            double endX = e.getX();
            double endY = e.getY();
            gc.setStroke(colorPicker.getValue());
            gc.setFill(colorPicker.getValue());

            if (isDotted) {
                gc.setLineDashes(10, 10);
            } else {
                gc.setLineDashes();
            }

            if (isBold) {
                gc.setLineWidth(3);
            } else {
                gc.setLineWidth(1);
            }

            Shape shape = null;
            switch (currentTool) {
                case "LINE":
                    gc.strokeLine(startX, startY, endX, endY);
                    shape = new Shape("LINE", startX, startY, endX, endY, colorPicker.getValue(), false, isDotted, isBold);
                    break;
                case "OVAL":
                    double ovalWidth = Math.abs(endX - startX);
                    double ovalHeight = Math.abs(endY - startY);
                    if (isFilled) {
                        gc.fillOval(Math.min(startX, endX), Math.min(startY, endY), ovalWidth, ovalHeight);
                    } else {
                        gc.strokeOval(Math.min(startX, endX), Math.min(startY, endY), ovalWidth, ovalHeight);
                    }
                    shape = new Shape("OVAL", startX, startY, endX, endY, colorPicker.getValue(), isFilled, isDotted, isBold);
                    break;
                case "RECTANGLE":
                    double rectWidth = Math.abs(endX - startX);
                    double rectHeight = Math.abs(endY - startY);
                    if (isFilled) {
                        gc.fillRect(Math.min(startX, endX), Math.min(startY, endY), rectWidth, rectHeight);
                    } else {
                        gc.strokeRect(Math.min(startX, endX), Math.min(startY, endY), rectWidth, rectHeight);
                    }
                    shape = new Shape("RECTANGLE", startX, startY, endX, endY, colorPicker.getValue(), isFilled, isDotted, isBold);
                    break;
                case "FREE_HAND":
                    gc.strokeLine(prevX, prevY, endX, endY);
                    shape = new Shape("FREE_HAND", prevX, prevY, endX, endY, colorPicker.getValue(), false, isDotted, isBold);
                    break;
            }

            if (shape != null) {
                shapeStack.push(shape);
                redoStack.clear();
            }
        });


        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
