package clases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.Statement;

public class Conexiones {

	public static Connection realizarConexion(){
		
		Connection conexion = null;
		
		try {
			
			String servidor = "jdbc:mysql://localhost/findHouse";
			String usuarioDB = "alex";
			String passwordDB = "1234";
			
			conexion = DriverManager.getConnection(servidor,usuarioDB,passwordDB);
			conexion.setAutoCommit(false);
			
		} catch(SQLException ex){
        	
        	System.out.println(ex + "Error SQLException al intentar conectar con la BD "+ex.getMessage());
            conexion=null;
            
        }catch(Exception ex){
        	
        	System.out.println(ex + "Error Exception al intentar conectar con la BD "+ex.getMessage());
            conexion=null;
        }finally{
        	
            return conexion;
        }
    } 
}
	

