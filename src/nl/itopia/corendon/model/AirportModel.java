package nl.itopia.corendon.model;

import nl.itopia.corendon.data.Airport;
import nl.itopia.corendon.utils.Log;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * © 2014, Biodiscus.net Robin
 */
public class AirportModel {
    private static final AirportModel _default = new AirportModel();
    private final DatabaseManager dbmanager = DatabaseManager.getDefault();

    private AirportModel() {}

    public Airport getAirport(int id) {
        Airport airport = null;

        try {
            ResultSet result = dbmanager.doQuery("SELECT * FROM airport WHERE id = "+ id);
            if(result.next()) {
                int code = result.getInt("code");
                String name = result.getString("name");
                airport = new Airport(id, code, name);
            }
        } catch (SQLException e) {
            Log.display("SQLEXCEPTION", e.getErrorCode(), e.getSQLState(), e.getMessage());
        }

        return airport;
    }

    public static AirportModel getDefault() {
        return _default;
    }
}