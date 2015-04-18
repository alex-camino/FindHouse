package clases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Conexiones {

	public static Connection realizarConexion(){
		
		Connection conexion = null;
		
		try {
			
			String servidor = "jdbc:mysql://localhost/findHouse";
			//String usuarioDB = "alex";
			//String passwordDB = "1234";
			String usuarioDB = "adminJJK3Lvr";
			String passwordDB = "KQ7dhcycmAhG";
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
	

