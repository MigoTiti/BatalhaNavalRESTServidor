package batalhanavalrestservidor;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;

public class Comunicador {

    public static void enviarResposta(HttpExchange t, String resposta, int codigo) {
        try {
            t.sendResponseHeaders(codigo, resposta.getBytes().length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(resposta.getBytes());
                os.close();
            }
        } catch (IOException ex) {
            BatalhaNavalRESTServidor.exibirException(ex);
        }
    }
}
