package sample;

import javafx.animation.PathTransition;
import javafx.application.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.scene.shape.ArcTo;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.util.StringConverter;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class Main extends Application {
    final Pane root = new Pane();

    final PathTransition[] pathtransition = new PathTransition[2];

    final Lock lock = new ReentrantLock();
    final Condition[] swap_complete  = { lock.newCondition(), lock.newCondition() };

    @Override
    public void start(Stage stage) {
        int x = 30;
        Random rand = new Random();

        int[] data = new int[x];
        for(int i = 0; i < x; i++)
            data[i] = x - i;

        Label[] labels = createLabels(data);

        ChoiceBox<String> which_sort = new ChoiceBox<String>();
        which_sort.getItems().add("Bubble sort");
        which_sort.getItems().add("Selection sort");
        which_sort.setValue("Bubble sort");
        which_sort.show();

        Button generate_numbers_button = new Button("GENERATE NUMBERS");
        generate_numbers_button.setLayoutX(80);
        generate_numbers_button.setLayoutY(810);
        generate_numbers_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e)
            {
                int n;
                for(int i = 0; i < x; i++)
                {
                    n = rand.nextInt(100);
                    data[i] = n;
                    labels[i].setText(Integer.toString(n));
                }
            }
        });

        Slider animation_speed = new Slider();
        animation_speed.setMinWidth(340);
        animation_speed.setLayoutX(640);
        animation_speed.setLayoutY(810);
        animation_speed.setMin(0.1);
        animation_speed.setMax(3);
        animation_speed.setValue(1.6);
        animation_speed.setMinorTickCount(10);
        animation_speed.setMajorTickUnit(0.75);
        animation_speed.setSnapToTicks(true);
        animation_speed.setShowTickMarks(true);
        animation_speed.setShowTickLabels(true);
        animation_speed.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n)
            {
                if(n < 0.6) return "Very fast";
                if(n < 1.2) return "Fast";
                if(n < 1.8) return "Average";
                if(n < 2.4) return "Slow";
                return "Very slow";
            }

            @Override
            public Double fromString(String s) { return null; }
        });

        Button start_button = new Button("START");
        start_button.setLayoutX(1440);
        start_button.setLayoutY(810);
        start_button.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e) {
                start_button.setDisable(true);
                generate_numbers_button.setDisable(true);
                which_sort.setDisable(true);
                animation_speed.setDisable(true);
                switch(which_sort.getValue())
                {
                    case "Bubble sort":
                        bubble_sort(data, labels, animation_speed.getValue());
                        break;
                    case "Selection sort":
                        selection_sort(data, labels, animation_speed.getValue());
                        break;
                }
            }
        });

        root.getChildren().addAll(labels);
        root.getChildren().add(start_button);
        root.getChildren().add(generate_numbers_button);
        root.getChildren().add(which_sort);
        root.getChildren().add(animation_speed);
        root.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(root, 1600, 900);
        stage.setScene(scene);
        stage.show();
    }

    private void bubble_sort(int[] arr, Label[] circ, double anim_duration)
    {
        Thread thread = new Thread(
                () -> {
                    int n = arr.length;
                    for(int i = 0; i < n - 1; i++) {
                        for(int j = 0; j < n - i - 1; j++)
                        {
                            if(arr[j] > arr[j + 1])
                            {
                                int temp = arr[j];
                                arr[j] = arr[j + 1];
                                arr[j + 1] = temp;

                                final int fj = j;
                                final int fjp1 = j + 1;

                                FutureTask<Void> future = new FutureTask<>(
                                        () -> {
                                            swap(0, circ[fj], circ[fjp1].getLayoutX() - circ[fj].getLayoutX(), Duration.seconds(anim_duration)
                                            );
                                            swap(1, circ[fjp1], circ[fj].getLayoutX() - circ[fjp1].getLayoutX(), Duration.seconds(anim_duration + 0.05)
                                            );

                                            return null;
                                        }
                                );

                                lock.lock();
                                try
                                {
                                    Platform.runLater(future);
                                    future.get();
                                    for (Condition condition : swap_complete)
                                        condition.await();

                                }
                                catch(InterruptedException e)
                                {
                                    Thread.interrupted();
                                    break;
                                }
                                catch(ExecutionException e)
                                {
                                    e.printStackTrace();
                                    break;
                                }
                                finally { lock.unlock(); }

                                Label temporary = circ[j];
                                circ[j] = circ[j + 1];
                                circ[j + 1] = temporary;
                            }
                        }
                        circ[n - i - 1].setStyle(
                                "-fx-background-radius: 12; " +
                                        "-fx-background-color: gold; " +
                                        "-fx-font-family: Verdana; " +
                                        "-fx-font-size: 12pt; " +
                                        "-fx-font-weight: bold;"
                        );
                    }
                    circ[0].setStyle(
                            "-fx-background-radius: 12; " +
                                    "-fx-background-color: gold; " +
                                    "-fx-font-family: Verdana; " +
                                    "-fx-font-size: 12pt; " +
                                    "-fx-font-weight: bold;"
                    );
                }

        );
        thread.start();
    }

    private void selection_sort(int[] arr, Label[] circ, double anim_duration)
    {
        Thread thread = new Thread(
                () -> {
                    int min;
                    for(int i = 0; i < arr.length; i++)
                    {
                        min = i;
                        for(int j = i + 1; j < arr.length; j++)
                            if (arr[j] < arr[min])
                                min = j;
                        if(min != i)
                        {
                            int temp = arr[i];
                            arr[i] = arr[min];
                            arr[min] = temp;
                            final int finalMin = min;
                            final int finalI = i;

                            FutureTask<Void> future = new FutureTask<>(
                                    () -> {
                                        swap(0, circ[finalI], circ[finalMin].getLayoutX() - circ[finalI].getLayoutX(), Duration.seconds(anim_duration));
                                        swap(1, circ[finalMin], circ[finalI].getLayoutX() - circ[finalMin].getLayoutX(), Duration.seconds(anim_duration + 0.05));
                                        return null;
                                    }
                            );

                            lock.lock();
                            try
                            {
                                Platform.runLater(future);
                                future.get();
                                for (Condition condition: swap_complete)
                                    condition.await();
                            }
                            catch(InterruptedException e)
                            {
                                Thread.interrupted();
                                break;
                            }
                            catch(ExecutionException e)
                            {
                                e.printStackTrace();
                                break;
                            }
                            finally { lock.unlock(); }

                            Label temporary = circ[i];
                            circ[i] = circ[min];
                            circ[min] = temporary;
                        }
//                        rect[i].setStyle(
//                                "-fx-background-radius: 12; " +
//                                "-fx-background-color: gold; " +
//                                "-fx-font-family: Verdana; " +
//                                "-fx-font-size: 12pt; " +
//                                "-fx-font-weight: bold;"
//                        );
                    }
                }
        );
        thread.start();
    }


    private Label[] createLabels(int[] arr)
    {
        Label[] rect = new Label[arr.length];

        for(int i = 0; i < arr.length; i++)
            createLabel(i, arr, rect);

        return rect;
    }

    private void createLabel(int i, int[] arr, Label[] rect) {
        rect[i] = new Label(Integer.toString(arr[i]));
        rect[i].setMinSize(40, 40);
        rect[i].setMaxSize(40, 40);
        rect[i].setAlignment(Pos.CENTER);
        rect[i].setShape(new Circle(1.0));
        rect[i].setStyle(
                "-fx-background-radius: 12; " +
                        "-fx-background-color: silver; " +
                        "-fx-font-family: Verdana; " +
                        "-fx-font-size: 12pt; " +
                        "-fx-font-weight: bold;"
        );
        rect[i].relocate(i*50+50, 400);
    }

    void swap(int transition_index, Region node, double destination_x, Duration duration) {
        double source_x = node.getWidth() * 0.5;
        double source_y = node.getHeight() * 0.5;

        MoveTo moveTo = new MoveTo();
        moveTo.setX(source_x);
        moveTo.setY(source_y);

        ArcTo arcTo = new ArcTo();
        arcTo.setX(destination_x + source_x);
        arcTo.setY(source_y);
        arcTo.setRadiusX(1.0);
        arcTo.setRadiusY(1.0);

        Path path = new Path();
        path.getElements().add(moveTo);
        path.getElements().add(arcTo);

        pathtransition[transition_index] = new PathTransition(duration, path, node);

        pathtransition[transition_index].setOnFinished(event -> {
            node.setLayoutX(node.getLayoutX() + node.getTranslateX());
            node.setLayoutY(node.getLayoutY() + node.getTranslateY());
            node.setTranslateX(0);
            node.setTranslateY(0);

            lock.lock();
            try { swap_complete[transition_index].signal(); }
            finally { lock.unlock(); }
        });

        pathtransition[transition_index].play();

    }

    public static void main(String[] args) { launch(args); }
}