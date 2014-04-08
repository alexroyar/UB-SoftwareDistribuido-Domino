/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controlador;

import java.util.ArrayList;
import modelo.Ficha;

/**
 *
 * @author fer
 */
public class Rules {
    // Cabeceras de mensaje.
    public static final byte ERROR           = 0x00; 
    public static final byte HELLO           = 0x01; 
    public static final byte INIT            = 0x02; 
    public static final byte TIRADA_C_S      = 0x03; 
    public static final byte TIRADA_S_C      = 0x04; 
    public static final byte FICHA_ROBADA    = 0x05;
    public static final byte GAME_FINISH     = 0x06;
    
    // Cabeceras de error.
    public static final byte SYNTAX_ERROR            = 0x00; 
    public static final byte ILLEGAL_ACTION          = 0x01; 
    public static final byte NOT_ENOUGHT_RESOURCES   = 0x02; 
    public static final byte INTERNAL_ERROR          = 0x03; 
    public static final byte UNKNOWN_ERROR           = 0x04;
    
    // Resultados de la partida.
    public static final byte GANA_CLIENTE   = 0x00;
    public static final byte GANA_SERVIDOR  = 0x01;
    public static final byte GANAN_TODOS    = 0x02;
    
    // Valores de timeout (milisegundos).
    public static final int  TIMEOUT_READ = 5000;
    public static final int  TIME_MENU = 25000;
    
    // Reglas y otras constantes del juego/controlador.
    public static final int  MAX_SIZE_MSG_ERROR = 140;
    public static final int  MAX_NUM_TILE       = 6;
    public static final int  MIN_NUM_TILE       = 0;
    public static final int  TILES              = 28;
    public static final int  OFFSET_ASCII_NUM   = 0;
    public static final int  FICHAS_INICIALES   = 7;
    public static final char RIGHT  = 'R';
    public static final char LEFT   = 'L';
    public static final char SPACE  = ' ';
    public static final int  STRSIZE    = 40;
    public static final int  STRMAXSIZE = 140;
    
    // NO FICHA.
    public static final Ficha NO_FICHA = new Ficha('N', 'T');
    
    public static void imprimir_mano(ArrayList<Ficha> fichas,String titulo){
        System.out.println(titulo);
        for(Ficha f:fichas){
            System.out.println(f);
        }
    }
}
