package nl.itopia.corendon;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nl.itopia.corendon.controller.LoginController;
import nl.itopia.corendon.controller.MainController;
import nl.itopia.corendon.model.DatabaseManager;
import nl.itopia.corendon.mvc.MVC;
import nl.itopia.corendon.utils.Log;

/**
 * © 2014, Biodiscus.net Robin
 */
public class Launcher extends Application {
    
    public static final String VERSION = "1.0";
    public static final String TITLE = "Corendon - Lost & Found Luggage";

    public static final int WIDTH = 1366;
    public static final int HEIGHT = 768;

    public MVC mvcEngine;

    private Scene scene;
    
    /** database manager */
    private DatabaseManager dbManager;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        stage.setTitle(TITLE + ", V"+VERSION);

        // create and initialize the connectivity
        dbManager = DatabaseManager.getDefault();
        dbManager.openConnection();
        Log.display("Database initialized");

        mvcEngine = new MVC((e)->{
            // scene = new Scene(e, e.getWidth(), e.getHeight());
            scene = new Scene(e, WIDTH, HEIGHT);
            scene.getStylesheets().clear();
            scene.getStylesheets().add("stylesheets/style.css");

            stage.setScene(scene);

            Log.display("Changing view");
        });
//        mvcEngine.setController(new MainController(WIDTH, HEIGHT));
        mvcEngine.setController(new LoginController());
        
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
