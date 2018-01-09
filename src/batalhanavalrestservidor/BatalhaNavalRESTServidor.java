package batalhanavalrestservidor;

import batalhanavalrestservidor.util.HttpUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class BatalhaNavalRESTServidor extends JApplet {
    
    private static final int JFXPANEL_WIDTH_INT = 500;
    private static final int JFXPANEL_HEIGHT_INT = 500;
    private static JFXPanel fxContainer;

    private static TextArea logger;

    public static final int PORTA = 12345;
    private static ServerSocket socketServidor;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            }
            
            JFrame frame = new JFrame("Batalha Naval - Servidor");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            JApplet applet = new BatalhaNavalRESTServidor();
            applet.init();
            
            frame.setContentPane(applet.getContentPane());
            
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            applet.start();
        });
    }
    
    @Override
    public void init() {
        fxContainer = new JFXPanel();
        fxContainer.setPreferredSize(new Dimension(JFXPANEL_WIDTH_INT, JFXPANEL_HEIGHT_INT));
        add(fxContainer, BorderLayout.CENTER);

        Platform.runLater(() -> {
            iniciarTela();
        });
        
        new Thread(() -> iniciarServidor()).start();
    }
    
    private void iniciarTela() {
        logger = new TextArea();
        logger.setEditable(false);

        StackPane root = new StackPane();
        root.setPadding(new Insets(30));
        root.getChildren().add(logger);
        fxContainer.setScene(new Scene(root));
    }
    
    private static void iniciarServidor() {
        try {
            socketServidor = new ServerSocket(PORTA);

            appendMensagem("Servidor iniciado");

            while (true) {
                Socket cliente = socketServidor.accept();
                Comunicador comunicador = new Comunicador(cliente);
                
                String mensagemRecebida = comunicador.receberMensagem();
                
                if (validarHeader(mensagemRecebida)) {
                    new Thread(() -> handleMensagem(comunicador, mensagemRecebida)).start();
                } else {
                    comunicador.enviarMensagemParaJogador(HttpUtil.gerarHttp(400, null));
                    comunicador.close();
                }
            }
        } catch (SocketException ex) {
            exibirException(ex);
        } catch (IOException ex) {
            exibirException(ex);
        }
    }
        
    private static boolean validarHeader(String mensagem) {
        switch (mensagem.split(" ")[0]) {
            case "GET":
                return true;
            case "POST":
                return true;
            default:
                return false;
        }
    }
    
    private static void handleMensagem(Comunicador comunicador, String mensagem) {
        String resposta = HttpUtil.gerarHttp(200, "RESPOSTA FON");
        comunicador.enviarMensagemParaJogador(resposta);
        comunicador.close();
    }
    
    public static void appendMensagem(String mensagem) {
        Platform.runLater(() -> {
            logger.appendText(mensagem + "\n");
        });
    }

    public static void exibirException(Exception ex) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Exception");
            alert.setContentText(ex.getMessage());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);

            alert.showAndWait();
        });
    }
}
