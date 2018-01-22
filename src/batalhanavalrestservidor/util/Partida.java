package batalhanavalrestservidor.util;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Partida {

    public static final int ESTADO_NAO_ENTROU = 0;
    public static final int ESTADO_PREPARANDO = 1;
    public static final int ESTADO_PRONTO = 2;
    public static final int ESTADO_VEZ = 3;
    public static final int ESTADO_VITORIA = 4;
    public static final int ESTADO_DERROTA = 5;

    private String nome;

    private int id;
    private String tokenJogador1;
    private String tokenJogador2;

    private boolean partidaCheia;

    private String atualizacaoCampoJogador1;
    private String atualizacaoCampoJogador2;

    private final Set<String> campoJogador1;
    private final Set<String> campoJogador2;

    private boolean atualizacaoJogador1;
    private boolean atualizacaoJogador2;

    private int estadoJogador1 = 0;
    private int estadoJogador2 = 0;

    public Partida(int id, String nome) {
        this.id = id;
        this.nome = nome;
        campoJogador1 = new HashSet<>();
        campoJogador2 = new HashSet<>();
        partidaCheia = false;
    }

    public Boolean setEstadoJogador(int estado, String token) {
        if (token.equals(tokenJogador1)) {
            estadoJogador1 = estado;
            return true;
        } else if (token.equals(tokenJogador2)) {
            estadoJogador2 = estado;
            return true;
        }

        return null;
    }

    public int getEstadoJogador(String token) {
        if (token.equals(tokenJogador1)) {
            return estadoJogador1;
        } else if (token.equals(tokenJogador2)) {
            return estadoJogador2;
        }

        return -1;
    }

    public int getEstadoOponente(String token) {
        if (token.equals(tokenJogador1)) {
            return estadoJogador2;
        } else if (token.equals(tokenJogador2)) {
            return estadoJogador1;
        }

        return -1;
    }

    public Boolean atirar(String token, String coordenadas) {
        if (token.equals(tokenJogador1)) {
            if (estadoJogador1 == ESTADO_VEZ) {
                estadoJogador1 = ESTADO_PRONTO;
                estadoJogador2 = ESTADO_VEZ;
                
                if (campoJogador2.contains(coordenadas)) {
                    atualizacaoCampoJogador2 = coordenadas + "&a"; 
                    atualizacaoJogador2 = true;
                    campoJogador2.remove(coordenadas);

                    if (campoJogador2.isEmpty()) {
                        estadoJogador1 = ESTADO_VITORIA;
                        estadoJogador2 = ESTADO_DERROTA;
                    }

                    return true;
                } else {
                    atualizacaoCampoJogador2 = coordenadas + "&e"; 
                    atualizacaoJogador2 = true;
                    return false;
                }
            } else {
                return null;
            }
        } else if (token.equals(tokenJogador2)) {
            if (estadoJogador2 == ESTADO_VEZ) {
                estadoJogador2 = ESTADO_PRONTO;
                estadoJogador1 = ESTADO_VEZ;

                if (campoJogador1.contains(coordenadas)) {
                    atualizacaoCampoJogador1 = coordenadas + "&a"; 
                    atualizacaoJogador1 = true;
                    
                    campoJogador1.remove(coordenadas);

                    if (campoJogador1.isEmpty()) {
                        estadoJogador2 = ESTADO_VITORIA;
                        estadoJogador1 = ESTADO_DERROTA;
                    }

                    return true;
                } else {
                    atualizacaoCampoJogador1 = coordenadas + "&e"; 
                    atualizacaoJogador1 = true;
                    return false;
                }
            } else {
                return null;
            }
        }

        return null;
    }

    public String getAtualizacao(String token) {
        if (token.equals(tokenJogador1)) {
            if (atualizacaoJogador1) {
                String aux = atualizacaoCampoJogador1;
                atualizacaoCampoJogador1 = null;
                return aux;
            } else {
                return "";
            }
        } else if (token.equals(tokenJogador2)) {
            if (atualizacaoJogador2) {
                String aux = atualizacaoCampoJogador2;
                atualizacaoCampoJogador2 = null;
                return aux;
            } else {
                return "";
            }
        }

        return null;
    }

    public Boolean definirCampo(String token, String campo) {
        if (token.equals(tokenJogador1)) {
            String[] coordenadas = campo.split("%26");

            campoJogador1.addAll(Arrays.asList(coordenadas));
            estadoJogador1 = ESTADO_VEZ;

            return true;
        } else if (token.equals(tokenJogador2)) {
            String[] coordenadas = campo.split("%26");

            campoJogador2.addAll(Arrays.asList(coordenadas));
            estadoJogador2 = ESTADO_PRONTO;

            return true;
        }

        return null;
    }

    public String conectarJogador() {
        SecureRandom random = new SecureRandom();

        if (estadoJogador1 == 0) {
            estadoJogador1 = ESTADO_PREPARANDO;
            tokenJogador1 = Long.toString(Math.abs(random.nextLong()), 16);
            return tokenJogador1;
        } else if (estadoJogador2 == 0) {
            estadoJogador2 = ESTADO_PREPARANDO;
            partidaCheia = true;
            tokenJogador2 = Long.toString(Math.abs(random.nextLong()), 16);
            return tokenJogador2;
        }

        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isAtualizacaoJogador1() {
        return atualizacaoJogador1;
    }

    public void setAtualizacaoJogador1(boolean atualizacaoJogador1) {
        this.atualizacaoJogador1 = atualizacaoJogador1;
    }

    public boolean isAtualizacaoJogador2() {
        return atualizacaoJogador2;
    }

    public void setAtualizacaoJogador2(boolean atualizacaoJogador2) {
        this.atualizacaoJogador2 = atualizacaoJogador2;
    }

    public boolean isPartidaCheia() {
        return partidaCheia;
    }

    public void setPartidaCheia(boolean partidaCheia) {
        this.partidaCheia = partidaCheia;
    }

    public int getEstadoJogador1() {
        return estadoJogador1;
    }

    public void setEstadoJogador1(int estadoJogador1) {
        this.estadoJogador1 = estadoJogador1;
    }

    public int getEstadoJogador2() {
        return estadoJogador2;
    }

    public void setEstadoJogador2(int estadoJogador2) {
        this.estadoJogador2 = estadoJogador2;
    }

    public String getTokenJogador1() {
        return tokenJogador1;
    }

    public void setTokenJogador1(String tokenJogador1) {
        this.tokenJogador1 = tokenJogador1;
    }

    public String getTokenJogador2() {
        return tokenJogador2;
    }

    public void setTokenJogador2(String tokenJogador2) {
        this.tokenJogador2 = tokenJogador2;
    }

    public String getAtualizacaoCampoJogador1() {
        return atualizacaoCampoJogador1;
    }

    public void setAtualizacaoCampoJogador1(String atualizacaoCampoJogador1) {
        this.atualizacaoCampoJogador1 = atualizacaoCampoJogador1;
    }

    public String getAtualizacaoCampoJogador2() {
        return atualizacaoCampoJogador2;
    }

    public void setAtualizacaoCampoJogador2(String atualizacaoCampoJogador2) {
        this.atualizacaoCampoJogador2 = atualizacaoCampoJogador2;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
