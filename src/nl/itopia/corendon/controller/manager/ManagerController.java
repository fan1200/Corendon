package nl.itopia.corendon.controller.manager;

import java.io.File;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import nl.itopia.corendon.controller.HelpFunctionController;
import nl.itopia.corendon.controller.LoginController;
import nl.itopia.corendon.data.Luggage;
import nl.itopia.corendon.data.Status;
import nl.itopia.corendon.data.manager.ChartData;
import nl.itopia.corendon.model.LuggageModel;
import nl.itopia.corendon.model.StatusModel;
import nl.itopia.corendon.mvc.Controller;
import nl.itopia.corendon.pdf.ManagerStatisticsPDF;
import nl.itopia.corendon.utils.DateUtil;

import java.time.LocalDate;
import java.util.*;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;

/**
 * © 2014, Biodiscus.net robin
 */
public class ManagerController extends Controller {
    @FXML
    private LineChart lineDiagram;
    @FXML
    private BarChart barDiagram;

    @FXML
    private Button filterButton, helpButton, logoutButton, printstatisticsButton, lineDiagrambutton, barDiagrambutton,logfilesbutton;
    @FXML
    private CheckBox foundLuggagecheckbox, lostLuggagecheckbox, resolvedLuggagecheckbox;
    @FXML
    private DatePicker datepicker1, datepicker2;
    @FXML private Label userName, userID;
    private LuggageModel luggageModel;

    private ImageView spinningIcon;
    private StackPane iconPane;

    private Chart currentChart;
    private XYChart.Series<Date, Integer> foundSeries, lostSeries, resolvedSeries;
    private XYChart.Series<String, Integer> testSeries;
    private boolean helpFunctionOpened;
    private HelpFunctionController helpController;

    public ManagerController() {
        registerFXML("gui/manager_linediagram.fxml");

        userName.setText("Robin de Jong");
        userID.setText("1337");

        luggageModel = LuggageModel.getDefault();

        foundLuggagecheckbox.setOnAction(this::filterHandle);
        lostLuggagecheckbox.setOnAction(this::filterHandle);
        resolvedLuggagecheckbox.setOnAction(this::filterHandle);
        logoutButton.setOnAction(this::logoutHandler);
        helpButton.setOnAction(this::helpHandler);

        printstatisticsButton.setOnAction(this::printStatisticsHandler);
        lineDiagrambutton.setOnAction(this::lineDiagramHandler);
        barDiagrambutton.setOnAction(this::barDiagramHandler);
        view.fxmlPane.setOnKeyReleased(this::f1HelpFunction);
        helpFunctionOpened = false;
        
        
        currentChart = lineDiagram;


        // TODO: Set the datePicker1 to something else
        datepicker1.setValue(LocalDate.of(1970, 1, 1));
        // Set the datePicker2 to today date
        datepicker2.setValue(LocalDate.now());


        // Show a spinning icon to indicate to the user that we are getting the tableData
        Image image = new Image("img/loader.gif", 24, 16.5, true, false);
        spinningIcon = new ImageView(image);

        iconPane = new StackPane();
        iconPane.setPickOnBounds(false);
        iconPane.getChildren().add(spinningIcon);
        view.fxmlPane.getChildren().add(iconPane);

        // Initialize the Series, give them a name and give them a color
        foundSeries = new XYChart.Series<>();
        foundSeries.setName("Found");

        testSeries = new XYChart.Series<>();
        testSeries.setName("Test");

        lostSeries = new XYChart.Series<>();
        lostSeries.setName("Lost");

        resolvedSeries = new XYChart.Series<>();
        resolvedSeries.setName("Resolved");

        // Make a new thread that will recieve the tableData from the database
        Thread dataThread = new Thread(() -> receiveData());
        dataThread.start();
    }

    
    // Fired when lineDiagrambutton is clicked
    private void lineDiagramHandler(ActionEvent e) {
        lineDiagrambutton.setDisable(true);
        barDiagrambutton.setDisable(false);

        lineDiagram.setVisible(true);
        barDiagram.setVisible(false);
        currentChart = lineDiagram;
    }

    // Fired when barDiagrambutton is clicked
    private void barDiagramHandler(ActionEvent e) {
        lineDiagrambutton.setDisable(false);
        barDiagrambutton.setDisable(true);

        lineDiagram.setVisible(false);
        barDiagram.setVisible(true);
        currentChart = barDiagram;
    }

    private void filterHandle(ActionEvent e) {
        boolean found = foundLuggagecheckbox.isSelected();
        boolean lost = lostLuggagecheckbox.isSelected();
        boolean resolved = resolvedLuggagecheckbox.isSelected();
        showLuggage(found, lost, resolved);
    }

    private void showLuggage(boolean found, boolean lost, boolean resolved) {
        ObservableList<XYChart.Series> lineSeries = lineDiagram.getData();
        ObservableList<XYChart.Series> barSeries = barDiagram.getData();


        if (found) {
            // If the chartSeries is not in the chart, add it
            if (!lineSeries.contains(foundSeries)) {
                lineSeries.add(foundSeries);
            }

//            if(!barSeries.contains(foundSeries)) {
//                barSeries.add(foundSeries);
//            }

        } else {
            lineDiagram.getData().remove(foundSeries);
//            barDiagram.getData().remove(foundSeries);
        }

        if (lost) {
            // If the chartSeries is not in the chart, add it
            if (!lineSeries.contains(lostSeries)) {
                lineSeries.add(lostSeries);
            }

//            if(!barSeries.contains(lostSeries)) {
//                barSeries.add(lostSeries);
//            }
        } else {
            lineDiagram.getData().remove(lostSeries);
//            barDiagram.getData().remove(lostSeries);
        }

        if (resolved) {
            // If the chartSeries is not in the chart, add it
            if (!lineSeries.contains(resolvedSeries)) {
                lineSeries.add(resolvedSeries);
            }

//            if(!barSeries.contains(resolvedSeries)) {
//                barSeries.add(resolvedSeries);
//            }
        } else {
            lineDiagram.getData().remove(resolvedSeries);
//            barDiagram.getData().remove(resolvedSeries);
        }
    }

    // We will call this function in a new thread, so the user can still click buttons
    private void receiveData() {
        StatusModel status = StatusModel.getDefault();
        Status foundStatus = status.getStatus("Found");
        Status lostStatus = status.getStatus("Lost");
        Status resolvedStatus = status.getStatus("Resolved");

        List<ChartData> foundDates = new ArrayList<>();
        List<ChartData> lostDates = new ArrayList<>();
        List<ChartData> resolvedDates = new ArrayList<>();

        // If the date is not set, add it to the list.
        // If the date is set, add one to the count
        List<Luggage> luggages = luggageModel.getAllLuggage();

        for (Luggage luggage : luggages) {
            long uxt = luggage.createDate;
            ChartData contains = null;
            for (ChartData d : foundDates) {
                String date1 = DateUtil.formatDate("MMM yy", uxt);
                String date2 = DateUtil.formatDate("MMM yy", d.timestamp);
                if (date1.equals(date2)) {
                    contains = d;
                    break;
                }
            }

            if (contains == null) {
                for (ChartData d : lostDates) {
                    String date1 = DateUtil.formatDate("MMM yy", uxt);
                    String date2 = DateUtil.formatDate("MMM yy", d.timestamp);
                    if (date1.equals(date2)) {
                        contains = d;
                        break;
                    }
                }
            }

            if (contains == null) {
                for (ChartData d : resolvedDates) {
                    String date1 = DateUtil.formatDate("MMM yy", uxt);
                    String date2 = DateUtil.formatDate("MMM yy", d.timestamp);
                    if (date1.equals(date2)) {
                        contains = d;
                        break;
                    }
                }
            }

            if (contains == null) {
                String statusName = luggage.status.getName();
                if (foundStatus.getName().equals(statusName)) {
                    foundDates.add(new ChartData(uxt, 1));
                } else if (lostStatus.getName().equals(statusName)) {
                    lostDates.add(new ChartData(uxt, 1));
                } else if (resolvedStatus.getName().equals(statusName)) {
                    resolvedDates.add(new ChartData(uxt, 1));
                }
            } else {
                contains.count++;
            }
        }

        //TODO: Refactor

        for (ChartData data : foundDates) {
            int count = data.count;
            Date date = DateUtil.timestampToDate(data.timestamp);
            XYChart.Data<Date, Integer> pointData = new XYChart.Data<>(date, count);
            foundSeries.getData().add(pointData);

            String dateFormat = DateUtil.formatDate("MMM yy", data.timestamp);
            XYChart.Data<String, Integer> testData = new XYChart.Data<>(dateFormat, count);
            testSeries.getData().add(testData);
        }
        for (ChartData data : lostDates) {
            int count = data.count;
            Date date = DateUtil.timestampToDate(data.timestamp);
            XYChart.Data<Date, Integer> pointData = new XYChart.Data<>(date, count);
            lostSeries.getData().add(pointData);
        }
        for (ChartData data : resolvedDates) {
            int count = data.count;
            Date date = DateUtil.timestampToDate(data.timestamp);
            XYChart.Data<Date, Integer> pointData = new XYChart.Data<>(date, count);
            resolvedSeries.getData().add(pointData);
        }

        // Notify the javafx thread to run this next command
        Platform.runLater(() -> {
            // Update the line diagram with our tableData
            lineDiagram.getData().addAll(foundSeries, lostSeries, resolvedSeries);
            barDiagram.getData().add(testSeries);
//            barDiagram.getData().addAll(foundSeries, lostSeries, resolvedSeries);

            // Remove the spinning icon
            view.fxmlPane.getChildren().remove(iconPane);
        });
    }

    private void f1HelpFunction(KeyEvent e) {
        //opens helpfunction with the f1 key
        if(e.getCode() == KeyCode.F1 && e.getEventType() == KeyEvent.KEY_RELEASED) {
            // If it's already openend, close it
            if(helpController == null) {
                openHelp();
            } else {
                removeController(helpController);
                helpController = null;
            }
        }
    }

    private void helpHandler(ActionEvent e) {
        if(helpController == null) {
            openHelp();
        }
        //opens help function
    }

    private void openHelp() {
        helpController = new HelpFunctionController();
        addController(helpController);
    }

    private void logoutHandler(ActionEvent e) {
        changeController(new LoginController());
    }

    private void printStatisticsHandler(ActionEvent e) {
        //SAVE FILE WITH FILECHOOSER
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select location to save PDF.");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        File pdf = fileChooser.showSaveDialog(view.getScene().getWindow());


        if (pdf != null) {
            ManagerStatisticsPDF.generateManagerReportPDF(pdf, currentChart);
            System.out.println("PDF OF MANAGER REPORT SAVED");
        }
    }
}
