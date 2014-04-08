package vista;
import controlador.ComUtils;
import controlador.ControladorServidor;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import controlador.ComUtils;
import controlador.ControladorServidor;
import controlador.Rules;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import modelo.Ficha;
import modelo.Tablero;
import modelo.Tirada;
import java.util.Scanner;




public class Testing{
public static final byte TIRADA_S_C      = 0x04; 
 private ControladorServidor controlador;
 private ComUtils comUtils;
	    private Socket socket;
	public  Testing(Socket socket){
//        try 
//        {
//            this.socket = socket;
//            this.comUtils = new ComUtils(this.socket);
//            this.controlador = new ControladorServidor(this.socket, this.comUtils);
//            // this.controlador.enviar_tirada(new Tirada(TIRADA_S_C,new Ficha(1,2),'L'),5);
//            // this.controlador.enviar_tirada(new Tirada(TIRADA_S_C,new Ficha(2,2),'R'),5);
//            // this.controlador.enviar_tirada(new Tirada(TIRADA_S_C,new Ficha(3,4),'L'),5);
//            // this.controlador.enviar_tirada(new Tirada(TIRADA_S_C,new Ficha(6,6),'R'),5);
//            // this.controlador.enviar_tirada(new Tirada(TIRADA_S_C,new Ficha(2,3),'L'),5);
//            // this.controlador.enviar_tirada(new Tirada(TIRADA_S_C,new Ficha(6,6),'R'),5);
//            // this.controlador.enviar_tirada(new Tirada(TIRADA_S_C,new Ficha(2,3),'L'),5);
//            while(true){
//               byte header = (byte) this.controlador.recibir_header();
//               switch (header){
//                  case ControladorServidor.TIRADA_C_S:
//                    System.out.println("Recibo tirada");
//                    Tirada tirada = this.controlador.recibir_tirada(ControladorServidor.TIRADA_C_S);
//                    if(tirada==null){
//                      this.controlador.enviar_ficha_robada(new Ficha(6,6));
//                    }else{
//                      System.out.println("Cliente envia "+tirada);
//                    }
//                  break;
//               }
//            }
//       	}
//       	catch(IOException e)
//       	{
//        }
	}
}