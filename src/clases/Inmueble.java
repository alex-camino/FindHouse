package clases;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.Statement;

public class Inmueble {

	//public static Connection conexion;
	
	private int codInmueble,
				categoria, 
				operacion, 
				precioCodigo, 
				habitaciones,
				numImagenes,
				numVecesListado;
	

	private String idInmueble,
				   titulo,
				   url,
				   fecha,
				   webAnuncio,
				   superficie,
				   terreno,
				   vendedor,
				   descripcion,
				   caracteristicas,
				   poblacion;

	public Inmueble(){
		
		//Llamamos al metodo realizar conexion para poder conectarnos a la BD.
		//conexion=Conexiones.realizarConexion();
	}
	
	public Inmueble(int categoria, int operacion, int precioCodigo,
					int habitaciones, int numImagenes, int numVecesListado,
					String idInmueble, String titulo, String url, String fecha, String webAnuncio,
					String superficie, String terreno, String vendedor, String descripcion,
					String caracteristicas, String poblacion){
	
		
		this.categoria=categoria;
		this.operacion=operacion;
		this.precioCodigo=precioCodigo;
		this.habitaciones=habitaciones;
		this.numImagenes=numImagenes;
		this.numVecesListado= numVecesListado;
		
		this.idInmueble=idInmueble;
		this.titulo=titulo;
		this.url=url;
		this.fecha=fecha;
		this.webAnuncio=webAnuncio;
		this.superficie=superficie;
		this.terreno=terreno;
		this.vendedor=vendedor;
		this.descripcion=descripcion;
		this.caracteristicas=caracteristicas;
		this.poblacion=poblacion;
		
	}

	
	
	////////////////// GETTERS Y SETTERS //////////////////////////
	
	public int getCodInmueble() {
		return codInmueble;
	}

	public void setCodInmueble(int codInmueble) {
		this.codInmueble = codInmueble;
	}
	
	public int getCategoria() {
		return categoria;
	}

	public void setCategoria(int categoria) {
		this.categoria = categoria;
	}

	public int getOperacion() {
		return operacion;
	}

	public void setOperacion(int operacion) {
		this.operacion = operacion;
	}

	public int getPrecioCodigo() {
		return precioCodigo;
	}

	public void setPrecioCodigo(int precioCodigo) {
		this.precioCodigo = precioCodigo;
	}

	public int getHabitaciones() {
		return habitaciones;
	}

	public void setHabitaciones(int habitaciones) {
		this.habitaciones = habitaciones;
	}

	public int getNumImagenes() {
		return numImagenes;
	}

	public void setNumImagenes(int numImagenes) {
		this.numImagenes = numImagenes;
	}

	public int getNumVecesListado() {
		return numVecesListado;
	}

	public void setNumVecesListado(int numVecesListado) {
		this.numVecesListado = numVecesListado;
	}

	public String getIdInmueble() {
		return idInmueble;
	}

	public void setIdInmueble(String idInmueble) {
		this.idInmueble = idInmueble;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public String getWebAnuncio() {
		return webAnuncio;
	}

	public void setWebAnuncio(String webAnuncio) {
		this.webAnuncio = webAnuncio;
	}

	public String getSuperficie() {
		return superficie;
	}

	public void setSuperficie(String superficie) {
		this.superficie = superficie;
	}

	public String getTerreno() {
		return terreno;
	}

	public void setTerreno(String terreno) {
		this.terreno = terreno;
	}

	public String getVendedor() {
		return vendedor;
	}

	public void setVendedor(String vendedor) {
		this.vendedor = vendedor;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getCaracteristicas() {
		return caracteristicas;
	}

	public void setCaracteristicas(String caracteristicas) {
		this.caracteristicas = caracteristicas;
	}

	public String getPoblacion() {
		return poblacion;
	}

	public void setPoblacion(String poblacion) {
		this.poblacion = poblacion;
	}
	
	/////////////////// FIN GETTERS Y SETTERS /////////////////////////////
	
	
	public synchronized void insertarPrecios(int mes, int semana, int noche, int normal){
		
		PreparedStatement pstmt;
		
		try {
			
			pstmt = Main.conexion.prepareStatement("insert into precios(precio_mes, precio_semana, precio_noche,precio_normal) values(?,?,?,?)",
									 Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setInt(1, mes);
			pstmt.setInt(2, semana);
			pstmt.setInt(3, noche);
			pstmt.setInt(4, normal);
			pstmt.executeUpdate();
			
			
			ResultSet idPrecios = pstmt.getGeneratedKeys();
			while(idPrecios.next()){
				
				this.precioCodigo=idPrecios.getInt(1);
			}
			idPrecios.close();
			
			//Modificar el inmueble y añadir el ID.Precios
			pstmt= Main.conexion.prepareStatement("update inmuebles set in_precioCodigo="+this.precioCodigo+" where in_codigo="
					+this.codInmueble);
			pstmt.executeUpdate();
			Main.conexion.commit();
			pstmt.close();
			
		} catch (SQLException ex) {
			
			Main.mensajesError.add("\nError al insertar los precios en la Base de Datos, CLASE: Inmuebles."
	                  +"\n"+ex.getMessage()+"\n"+ex.getErrorCode());
		}
		
		
	}
	
	public synchronized void insertarInmueble(){
		
		
		try{
			
			
			PreparedStatement pstmt;			
			
			pstmt= Main.conexion.prepareStatement("insert into inmuebles(in_idInmueble, in_titulo, in_categoria, in_operacion, in_caracteristicas, "
					+ "in_descripcion, in_url, in_fecha, in_web, in_habitaciones, in_superficie, in_terreno, in_vendedor, in_poblacion, "
					+ "in_vecesVisitado) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			
			
			pstmt.setString(1, this.idInmueble);
			pstmt.setString(2, this.titulo);
			pstmt.setInt(3, this.categoria);
			pstmt.setInt(4, this.operacion);
			pstmt.setString(5, this.caracteristicas);
			pstmt.setString(6, this.descripcion);
			pstmt.setString(7, this.url);
			pstmt.setString(8, this.fecha);
			pstmt.setString(9, this.webAnuncio);
			pstmt.setInt(10, this.habitaciones);
			pstmt.setString(11, this.superficie);
			pstmt.setString(12, this.terreno);
			pstmt.setString(13, this.vendedor);
			pstmt.setString(14, this.poblacion);
			pstmt.setInt(15, this.numVecesListado);
			
			
			pstmt.executeUpdate();
				

			ResultSet idDetallesInmueble = pstmt.getGeneratedKeys();
			while(idDetallesInmueble.next()){
				
				this.codInmueble=idDetallesInmueble.getInt(1);
			}
			idDetallesInmueble.close();
			
			pstmt= Main.conexion.prepareStatement("insert into anunciosInsertados (anuncio_codInmueble) values (?)");
			pstmt.setInt(1, this.codInmueble);
			pstmt.executeUpdate();
			
			
			Main.conexion.commit();
			pstmt.close();
			
			
			
		}catch(SQLException ex){
			
			if(ex.getErrorCode()==1062){
				
				Main.mensajesError.add("\nEl anuncio con ID: "+this.idInmueble+" ya existe en la base de datos y no se ha insertado.");
			}else{
				
				Main.mensajesError.add("\nError al insertar el inmueble en la Base de Datos. CLASE: Inmuebles"
		                  +"\n"+ex.getMessage()+"\n"+ex.getErrorCode());
			}
			
			
			try {
				
				Main.conexion.rollback();
				
			} catch (SQLException e) {
				
				Main.mensajesError.add("\nError al realizar el RollBack a la Base de Datos. CLASE: Inmuebles"
		                  +"\n"+e.getMessage()+"\n"+e.getErrorCode());
				
			}
			
		}catch(IndexOutOfBoundsException ex){
			
			Main.mensajesError.add("\nIndice fuera de los limites, los detalles del anuncio no se han insertado, el anuncio se borrará. CLASE: Inmuebles"
	                  +"\n"+ex.getMessage());
			
			try {
				
				Main.conexion.rollback();
			
			} catch (SQLException e) {
				
				Main.mensajesError.add("\nError al realizar el RollBack a la Base de Datos. CLASE: Inmuebles"
		                  +"\n"+e.getMessage()+"\n"+e.getErrorCode());
			}
		}
		
		
	}
	
}
