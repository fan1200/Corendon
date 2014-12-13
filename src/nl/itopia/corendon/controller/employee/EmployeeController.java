package nl.itopia.corendon.controller.employee;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import nl.itopia.corendon.controller.HelpFunctionController;
import nl.itopia.corendon.controller.LoginController;
import nl.itopia.corendon.data.Luggage;
import nl.itopia.corendon.model.LuggageModel;
import nl.itopia.corendon.mvc.Controller;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import nl.itopia.corendon.data.table.TableLuggage;
import nl.itopia.corendon.utils.Log;

/**
 *  AUTHOR: IGOR
 *  
 */
public class EmployeeController extends Controller {
    @FXML private TableView luggageInfo;

    public ObservableList<TableLuggage> tableData;
    public List<Luggage> luggageList;

    @FXML private TableColumn <Luggage,String> ID, Brand, Dimensions, Color, Airport, Status, Notes, Label;
    @FXML private Label userName, userID;

    @FXML private Button addLuggagebutton, editLuggagebutton, deleteLuggagebutton, searchLuggagebutton, helpButton,
                        logoutButton, detailsLuggagebutton, foundLuggagebutton, lostLuggagebutton, turnedinLuggagebutton,
                        refreshButton;

    private ImageView spinningIcon;
    private StackPane iconPane;

    private LuggageModel luggageModel;
    private HelpFunctionController helpController;
    
    
    public EmployeeController(){
        registerFXML("gui/Overzichtkoffers.fxml");

        luggageModel = LuggageModel.getDefault();
        // Show a spinning icon to indicate to the user that we are getting the tableData
//        Image image = new Image("img/loader.gif", 24, 16.5, true, false);
        Image image = new Image("img/loader.gif");
        spinningIcon = new ImageView(image);
        
        userID.setText("1337");
        userName.setText("Robin de Jong");

        showLoadingIcon();

        //Create buttons
        addLuggagebutton.setOnAction(this::addHandler);
        editLuggagebutton.setOnAction(this::editHandler);
        deleteLuggagebutton.setOnAction(this::deleteHandler);
        searchLuggagebutton.setOnAction(this::searchHandler);
        detailsLuggagebutton.setOnAction(this::detailsHandler);
        helpButton.setOnAction(this::helpHandler);
        logoutButton.setOnAction(this::logoutHandler);
        refreshButton.setOnAction(this::refreshHandler);
        view.fxmlPane.setOnKeyReleased(this::keypressHandler);

        // Set the luggage specific buttons disabled
        editLuggagebutton.setDisable(true);
        deleteLuggagebutton.setDisable(true);
        detailsLuggagebutton.setDisable(true);

        foundLuggagebutton.setDisable(true);
        lostLuggagebutton.setDisable(true);
        turnedinLuggagebutton.setDisable(true);

        
        // Create columns and set their datatype for building the Luggage Table
        ID.setCellValueFactory(new PropertyValueFactory<>("id"));
        Label.setCellValueFactory(new PropertyValueFactory<>("label"));
        Brand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        Dimensions.setCellValueFactory(new PropertyValueFactory<>("dimensions"));
        Color.setCellValueFactory(new PropertyValueFactory<>("color"));
        Airport.setCellValueFactory(new PropertyValueFactory<>("airport"));
        Status.setCellValueFactory(new PropertyValueFactory<>("status"));
        Notes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        luggageInfo.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            editLuggagebutton.setDisable(false);
            deleteLuggagebutton.setDisable(false);
            detailsLuggagebutton.setDisable(false);
        });

        // Make a new thread that will recieve the tableData from the database
        Thread dataThread = new Thread(()-> receiveData());
        dataThread.start();
    }

    private void showLoadingIcon() {
        // Show a spinning icon to indicate to the user that we are getting the tableData
        Image image = new Image("img/loader.gif", 24, 16.5, true, false);
        spinningIcon = new ImageView(image);

        iconPane = new StackPane();
        iconPane.setPickOnBounds(false); // Needed to click trough transparent panes
        iconPane.getChildren().add(spinningIcon);
        view.fxmlPane.getChildren().add(iconPane);
    }

    private void refreshHandler(ActionEvent e) {
        refreshButton.setDisable(true);
        tableData.clear();
        showLoadingIcon();

        Thread dataThread = new Thread(()-> {
            receiveData();
        });
        dataThread.start();
    }

    private void receiveData() {
        luggageList = luggageModel.getAllLuggage();
        tableData = FXCollections.observableArrayList();
        for(Luggage luggage : luggageList) {
            TableLuggage luggageTable = new TableLuggage(
                    luggage.getID(),
                    luggage.label,
                    luggage.dimensions,
                    luggage.notes,
                    luggage.airport.getName(),
                    luggage.brand.getName(),
                    luggage.color.getHex(),
                    luggage.status.getName()
            );

            tableData.add(luggageTable);
        }

        Platform.runLater(() -> {
            luggageInfo.setItems(tableData);
            // Enable the button, remove the loading icon
            refreshButton.setDisable(false);
            view.fxmlPane.getChildren().remove(iconPane);
        });
    }
    
    private void searchHandler(ActionEvent e) {

        SearchLuggageController searchluggagecontroller = new SearchLuggageController();
        
        searchluggagecontroller.setControllerDeleteHandler((o)->{
            /* cast the object to a list */
            List<Luggage> searchList = (List<Luggage>) o;
            
            
            if(null != searchList && searchList.size() >= 1) {
                /* delete all records from the table view */
                tableData.clear();
                /* the search query has atleast one record, continue to fill the table view */
                for(Luggage luggage : searchList){
                    TableLuggage luggageTable = new TableLuggage(luggage.getID(), luggage.label,luggage.dimensions,
                            luggage.notes, luggage.airport.getName(),luggage.brand.getName(), luggage.color.getHex(),
                            luggage.status.getName()
                    );

                    tableData.add(luggageTable);
                }
            }

        });
        
        addController(searchluggagecontroller);
    }

    private void logoutHandler(ActionEvent e) {
        changeController(new LoginController());
    }

    private void addHandler(ActionEvent e) {
        AddLuggageController addLuggage = new AddLuggageController();
        addLuggage.setControllerDeleteHandler((obj)->{
            if(obj == null) return;
            // Update our table with the new tableData
            Luggage luggage = (Luggage)obj;
            TableLuggage tableLuggage = new TableLuggage(
                luggage.getID(),
                luggage.label,
                luggage.dimensions,
                luggage.notes,
                luggage.airport.getName(),
                luggage.brand.getName(),
                luggage.color.getHex(),
                luggage.status.getName()
            );
            tableData.add(tableLuggage);
        });
        addController(addLuggage);
    }

    private void detailsHandler(ActionEvent e) {
        TableLuggage luggage = (TableLuggage) luggageInfo.getSelectionModel().getSelectedItem();
        addController(new DetailLuggageController(luggage.getId()));
    }

    private void editHandler(ActionEvent e) {
        int selectedIndex = luggageInfo.getSelectionModel().getSelectedIndex();
        TableLuggage luggage = (TableLuggage) luggageInfo.getSelectionModel().getSelectedItem();
        EditLuggageController editLuggage = new EditLuggageController(luggage.getId());
        editLuggage.setControllerDeleteHandler((obj)->{
            if(obj == null) return;
            // Update our table with the new tableData
            Luggage lug = (Luggage) obj;
            luggage.setId(lug.getID());
            luggage.setDimensions(lug.dimensions);
            luggage.setNotes(lug.notes);
            luggage.setAirport(lug.airport.getName());
            luggage.setBrand(lug.brand.getName());
            luggage.setColor(lug.color.getHex());
            luggage.setStatus(lug.status.getName());
            tableData.set(selectedIndex, luggage);
        });
        addController(editLuggage);
    }

    private void deleteHandler(ActionEvent e) {
        // TODO: Show dialog with text: Do you really want to delete this luggage?
        TableLuggage luggage = (TableLuggage) luggageInfo.getSelectionModel().getSelectedItem();
        luggageModel.deleteLuggage(luggage.getId());
        tableData.remove(luggage);
    }
    
    private void keypressHandler(KeyEvent e) {
        //opens helpfunction with the f1 key
        if(e.getEventType() == KeyEvent.KEY_RELEASED) {
            if (e.getCode() == KeyCode.F1) {
                // If it's already openend, close it
                if (helpController == null) {
                    openHelp();
                } else {
                    removeController(helpController);
                    helpController = null;
                }
            } else if (e.getCode() == KeyCode.F5) {
                // If the refreshButton is disabled we can't refresh
                if(!refreshButton.isDisabled()) {
                    // We don't need to pass an event
                    refreshHandler(null);
                }
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
}
