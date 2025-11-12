/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package medicamentos;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class UtilidadesBoton {

    /**
     * Configura el Key Binding en el botón para que la tecla ENTER simule un clic.
     * Esto anula el comportamiento del "Botón por Defecto" de la ventana para el Enter,
     * permitiendo que cualquier botón enfocado se active al presionarlo.
     * * @param button El JButton a modificar.
     */
    public static void habilitarEnterEnBotonEnfocado(JButton button) {
        
        // 1. Clave de la acción para el mapa
        String actionKey = "clickOnEnter";

        // 2. Mapear la tecla ENTER al actionKey, solo cuando el botón tiene el foco (WHEN_FOCUSED)
        button.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), actionKey
        );

        // 3. Asignar la acción: simular un clic (doClick)
        button.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Llama al listener del botón (simula un clic)
                button.doClick(); 
            }
        });
    }
}