package ServidorMultiHilos;

import java.net.ServerSocket; // Esto se importa para poder escuchar conexiones TCP 
import java.net.Socket; //Representa la conexion TCP entre el servidor y el cliente

public class ServidorWeb {

    public static void main(String[] args) throws Exception {

        int puerto = 5001; //El servidor escuchara por este puerto 
        ServerSocket serverSocket = new ServerSocket(puerto); //Medienta TCP este empiza a escuchar o reservar este puerto 
        System.out.println("Servidor escuchando en puerto " + puerto); // Indica cuando el servidor empieza 

        while (true) { // Mantiene al servidor escuchando conexiones continuamente
            
            Socket socket = serverSocket.accept(); // Hace el proceso de TCP
            SolicitudHttp solicitud = new SolicitudHttp(socket);
            Thread hilo = new Thread(solicitud);// Un hilo por solicitud 
            hilo.start();
        }
    }
}

