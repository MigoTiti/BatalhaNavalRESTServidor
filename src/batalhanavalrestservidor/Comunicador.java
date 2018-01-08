package batalhanavalrestservidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javafx.application.Platform;

public class Comunicador {

    private Socket socket;
    private BufferedReader inputStream;
    private PrintWriter writer;

    public Comunicador(Socket socket) {
        try {
            this.socket = socket;
            this.inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ex) {
            BatalhaNavalRESTServidor.exibirException(ex);
        }
    }

    public void enviarMensagemParaJogador(String mensagemString) {
        try {
            writer.println(mensagemString);
        } catch (Exception ex) {
            Platform.runLater(() -> {
                BatalhaNavalRESTServidor.exibirException(ex);
            });
        }
    }

    public String receberMensagem() {
        try {
            return inputStream.readLine();
        } catch (IOException ex) {
            Platform.runLater(() -> {
                BatalhaNavalRESTServidor.exibirException(ex);
            });
        }

        return null;
    }

    public void close() {
        try {
            inputStream.close();
            writer.close();
            socket.close();
        } catch (IOException ex) {
            Platform.runLater(() -> {
                BatalhaNavalRESTServidor.exibirException(ex);
            });
        }
    }
    
    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
