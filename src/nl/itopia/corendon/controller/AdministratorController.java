package nl.itopia.corendon.controller;

import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import nl.itopia.corendon.data.Employee;
//import nl.itopia.corendon.data.Luggage;
import nl.itopia.corendon.model.DatabaseManager;
import nl.itopia.corendon.model.EmployeeModel;
//import nl.itopia.corendon.model.LuggageModel;
import nl.itopia.corendon.mvc.Controller;
import nl.itopia.corendon.mvc.View;
import nl.itopia.corendon.utils.Log;

/**
 *
 * @author Erik
 */
public class AdministratorController extends Controller {
    
    //private AdministratorView view;
    private Employee employee;
    private DatabaseManager dbManager;
    
    @FXML private TableColumn <Employee,String>userIDtable;
    @FXML private TableColumn <Employee,String>usernametable;
    @FXML private TableColumn <Employee,String>userLoginIDtable;
    
    @FXML private Button adduserButton;
    
    public AdministratorController() {
        
        registerFXML("gui/Overview_administrator.fxml");
        
        adduserButton.setOnAction(this::createNewEmployee);
    }
    
    public void createNewEmployee(ActionEvent event)
    {
        addController(new CreateUserController());
    }
    
    void initializeTable(){
        //Create columns and set their datatype
        
        userIDtable.setCellValueFactory(new PropertyValueFactory<Employee, String>("ID"));
        usernametable.setCellValueFactory(new PropertyValueFactory<Employee, String>("Username"));
        userLoginIDtable.setCellValueFactory(new PropertyValueFactory<Employee, String>("Login ID"));
        
        //MAAK LIST VAN LUGGAGE OBJECTS EN VUL DE KOLOMMEN
        //LuggageModel luggageModel = LuggageModel.getDefault(); 
        //List<Employee> employeeList = EmployeeModel.getAllEmployee();
        //luggageInfo.getItems().setAll(luggageList);
    }
}
