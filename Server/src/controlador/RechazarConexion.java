/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controlador;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fer
 */
public class RechazarConexion {
    
    /**
     * Constructor de clase que rechaza una conexión.
     * @param socket 
     */
    public RechazarConexion(Socket socket){
        ComUtils comUtils;
        ControladorServidor controlador = null;
        try {
            comUtils = new ComUtils(socket);
            comUtils.init();
            controlador = new ControladorServidor(socket, comUtils);
            controlador.recibir_header();
            controlador.enviar_error(Rules.NOT_ENOUGHT_RESOURCES, "Demasiados clientes conectados.");
        } catch (IOException ex) {
            Logger.getLogger(RechazarConexion.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                controlador.cerrar_conexion();
            } catch (IOException ex) {
                Logger.getLogger(RechazarConexion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
