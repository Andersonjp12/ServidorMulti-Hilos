package ServidorMultiHilos;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

final class SolicitudHttp implements Runnable { // Impementa Runnable para que cada solicitud se ejecute en un hilo separado

    final static String CRLF = "\r\n"; // Secuencia obligatoria para separar líneas en HTTP
    Socket socket;

    public SolicitudHttp(Socket socket) { // Recibe el socket del cliente para procesar la solicitud
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            procesarSolicitud();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void procesarSolicitud() throws Exception {

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        BufferedOutputStream out = new BufferedOutputStream(
                socket.getOutputStream()
        );

        // 1. Leer línea de solicitud
        String lineaSolicitud = in.readLine();
        System.out.println(lineaSolicitud);

        // 2. Leer headers
        String header;
        while ((header = in.readLine()) != null && !header.isEmpty()) {
            System.out.println(header);
        }

        // 3. Extraer recurso solicitado
        StringTokenizer partes = new StringTokenizer(lineaSolicitud);
        partes.nextToken(); // GET
        String nombreArchivo = partes.nextToken();

        // Si piden "/", servir index.html
        if (nombreArchivo.equals("/")) {
            nombreArchivo = "/index.html";
        }

        nombreArchivo = "." + nombreArchivo;
        System.out.println("Buscando: " + nombreArchivo);

        // 4. Buscar archivo
        File archivo = new File(nombreArchivo);
        boolean existe = archivo.exists();

        if (existe) {

            FileInputStream fis = new FileInputStream(archivo);

            out.write(("HTTP/1.0 200 OK" + CRLF).getBytes());
            out.write(("Content-Type: " + contentType(nombreArchivo) + CRLF).getBytes());
            out.write(("Content-Length: " + archivo.length() + CRLF).getBytes());
            out.write(("Connection: close" + CRLF).getBytes());
            out.write(CRLF.getBytes());

            enviarBytes(fis, out);
            fis.close();

        } else {

            String errorHTML = "<html><body><h1>404 Not Found</h1></body></html>";

            out.write(("HTTP/1.0 404 Not Found" + CRLF).getBytes());
            out.write(("Content-Type: text/html; charset=UTF-8" + CRLF).getBytes());
            out.write(("Content-Length: " + errorHTML.length() + CRLF).getBytes());
            out.write(("Connection: close" + CRLF).getBytes());
            out.write(CRLF.getBytes());
            out.write(errorHTML.getBytes());
        }

        out.flush();
        in.close();
        out.close();
        socket.close();
    }

    // Enviar bytes del archivo
    private static void enviarBytes(InputStream fis, OutputStream os) throws Exception {
        byte[] buffer = new byte[1024];
        int bytes;
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    // Detectar tipo MIME
    private static String contentType(String nombreArchivo) {
        if (nombreArchivo.endsWith(".html") || nombreArchivo.endsWith(".htm"))
            return "text/html; charset=UTF-8";
        if (nombreArchivo.endsWith(".jpg"))
            return "image/jpeg";
        if (nombreArchivo.endsWith(".gif"))
            return "image/gif";
        return "application/octet-stream";
    }
}

