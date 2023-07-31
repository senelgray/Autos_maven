package catalogoAutos;

public class Automovil {

	  private String nombre;
	    private String tipo;
	    private String precio;
	    private String imagen;
	    

	    public Automovil (String nombre, String tipo, String precio, String imagen) {
	        this.nombre = nombre;
	        this.tipo = tipo;
	        this.precio = precio;
	        this.imagen = this.imagen = "imagenes/"+imagen;
	    }

	    public String getNombre() {
	        return nombre;
	    }

	    public String getTipo() {
	        return tipo;
	    }
	    
	    public String getPrecio() {
	        return precio;
	    }

	    public String getImagen() {
	        return imagen;
	    }

	    public String toString() {
	        return "Nombre: " + nombre + "\nTipo: " + tipo+ "\nPrecio: "+ precio;
	    }
	}