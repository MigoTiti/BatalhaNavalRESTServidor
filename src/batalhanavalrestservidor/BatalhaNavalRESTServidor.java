package batalhanavalrestservidor;

import batalhanavalrestservidor.util.Partida;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
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
import org.apache.commons.io.IOUtils;

public class BatalhaNavalRESTServidor extends JApplet {

    private static final int JFXPANEL_WIDTH_INT = 500;
    private static final int JFXPANEL_HEIGHT_INT = 500;
    private static JFXPanel fxContainer;

    private static TextArea logger;

    public static final int PORTA = 12345;

    private static Map<Integer, Partida> partidas = new HashMap<>();

    private static int idIncremental = 1;

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

        partidas = new HashMap<>();

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
            appendMensagem("Servidor iniciado");

            HttpServer server = HttpServer.create(new InetSocketAddress(PORTA), 0);
            server.createContext("/partidas", new Handler());
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
        } catch (SocketException ex) {
            exibirException(ex);
        } catch (IOException ex) {
            exibirException(ex);
        }
    }

    static class Handler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            String mensagemCorpo = IOUtils.toString(is, Charset.defaultCharset());

            String mensagem = t.getRequestURI().toString().trim().substring(1);

            String metodo = t.getRequestMethod();

            switch (metodo) {
                case "GET": {
                    String[] urlDividida = mensagem.split("/");

                    if (urlDividida.length == 1) {
                        Comunicador.enviarResposta(t, listarPartidas(), 200);
                    } else if (isId(urlDividida[1])) {
                        switch (urlDividida[2]) {
                            case "estado_oponente":
                                {
                                    Partida p = partidas.get(Integer.parseInt(urlDividida[1]));
                                    if (p.isPartidaCheia()) {
                                        Comunicador.enviarResposta(t, "" + p.getEstadoOponente(urlDividida[3]), 200);
                                    } else {
                                        Comunicador.enviarResposta(t, "-1", 400);
                                    }       break;
                                }
                            case "estado_jogador":
                                {
                                    Partida p = partidas.get(Integer.parseInt(urlDividida[1]));
                                    Comunicador.enviarResposta(t, "" + p.getEstadoJogador(urlDividida[3]), 200);
                                    break;
                                }
                            case "atualizacao":
                                {
                                    Partida p = partidas.get(Integer.parseInt(urlDividida[1]));
                                    Comunicador.enviarResposta(t, "" + p.getAtualizacao(urlDividida[3]), 200);
                                    break;
                                }
                            default:
                                break;
                        }
                    }

                    break;
                }
                case "POST": {
                    String[] urlDividida = mensagem.split("/");

                    if (urlDividida[1].equals("criar")) {
                        int id = idIncremental++;
                        Partida p = new Partida(id, mensagemCorpo.split("=")[1]);
                        partidas.put(id, p);
                        appendMensagem("Partida " + mensagemCorpo.split("=")[1] + " criada, id: " + id);

                        String token = p.conectarJogador();
                        appendMensagem("Jogador conectado, token: " + token);

                        Comunicador.enviarResposta(t, id + ";" + token, 200);
                    } else if (isId(urlDividida[1])) {
                        switch (urlDividida[2]) {
                            case "conectar": {
                                Partida p = partidas.get(Integer.parseInt(urlDividida[1]));
                                if (!p.isPartidaCheia()) {
                                    String token = p.conectarJogador();
                                    appendMensagem("Jogador conectado, token: " + token);

                                    Comunicador.enviarResposta(t, p.getId() + ";" + token, 200);
                                } else {
                                    Comunicador.enviarResposta(t, "", 400);
                                }

                                break;
                            }
                            case "posicionar": {
                                String[] parametros = mensagemCorpo.split("&");
                                String coordenadas = parametros[0].split("=")[1];
                                String token = parametros[1].split("=")[1];
                                
                                Partida p = partidas.get(Integer.parseInt(urlDividida[1]));

                                Boolean definido = p.definirCampo(token, coordenadas);
                                if (definido != null) {
                                    if (definido) {
                                        Comunicador.enviarResposta(t, "", 200);
                                    }
                                } else {
                                    Comunicador.enviarResposta(t, "", 400);
                                }

                                break;
                            }
                            case "estado_jogador": {
                                String[] parametros = mensagemCorpo.split("&");
                                String estado = parametros[0].split("=")[1];
                                String token = parametros[1].split("=")[1];

                                Partida p = partidas.get(Integer.parseInt(urlDividida[1]));

                                Boolean resultado = p.setEstadoJogador(Integer.parseInt(estado), token);

                                if (resultado != null) {
                                    Comunicador.enviarResposta(t, "", 200);
                                }

                                break;
                            }
                            case "atirar": {
                                String[] parametros = mensagemCorpo.split("&");
                                String coordenadas = parametros[0].split("=")[1];
                                String token = parametros[1].split("=")[1];

                                Partida p = partidas.get(Integer.parseInt(urlDividida[1]));

                                Boolean resultado = p.atirar(token, coordenadas);

                                if (resultado != null) {
                                    Comunicador.enviarResposta(t, resultado ? "a" : "e", 200);
                                }

                                break;
                            }
                            default:
                                break;
                        }
                    }

                    break;
                }
            }
            
            t.close();
        }
    }

    public static boolean isId(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String listarPartidas() {
        StringBuilder listagem = new StringBuilder();

        partidas.entrySet().stream().map((entry) -> entry.getValue()).forEach((partida) -> {
            if (!partida.isPartidaCheia()) {
                listagem.append(partida.getId()).append(",");
                listagem.append(partida.getNome()).append("&");
            }
        });

        listagem.deleteCharAt(listagem.length() - 1);

        return listagem.toString();
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
