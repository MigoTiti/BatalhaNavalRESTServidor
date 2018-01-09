package batalhanavalrestservidor.util;

public class HttpUtil {
    
    public static final String CODIGO_110 = "110 \r\n\nLISTA VAZIA";
    public static final String CODIGO_200 = "200 \r\n\n";
    public static final String CODIGO_400 = "400 \r\n\nBAD REQUEST RETARDADO";
    public static final String CODIGO_404 = "404 \r\n\nN ACHOU MANO";
    
    public static String gerarHttp(int codigo, String resposta) {
        StringBuilder sb = new StringBuilder("HTTP/1.1 ");
        
        switch (codigo) {
            case 110:
                sb.append(CODIGO_110);
                break;
            case 200:
                sb.append(CODIGO_200).append(resposta);
                break;
            case 400:
                sb.append(CODIGO_400);
                break;
            case 404:
                sb.append(CODIGO_404);
                break;
        }
        
        return sb.toString();
    }
}
