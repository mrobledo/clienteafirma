package es.gob.afirma.standalone.ui;

import java.awt.Component;
import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.standalone.SimpleAfirmaMessages;

/**
 * Funciones de utilidad para la gesti&oacute;n de certificados en entorno gr&aacute;fico..
 * @author carlos
 *
 */
public class CertificateUtils {

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	/**
	 * Guarda en disco una copia temporal de un certificado y lo abre.
	 * @param cert Certificado.
	 * @param parent Componente padre sobre el que abrir cualquier di&aacute;logo necesario.
	 */
	public static void openCertificate(final X509Certificate cert, final Component parent) {
        try {
            final File tmp = File.createTempFile("afirma", ".cer");  //$NON-NLS-1$//$NON-NLS-2$
            tmp.deleteOnExit();
            try (
        		final OutputStream bos = new BufferedOutputStream(new FileOutputStream(tmp));
    		) {
            	bos.write(cert.getEncoded());
            }
            Desktop.getDesktop().open(tmp);
        }
        catch(final Exception e) {
        	LOGGER.warning(
    			"Error abriendo el fichero con el visor por defecto: " + e //$NON-NLS-1$
			);
        	AOUIFactory.showErrorMessage(
                parent,
                SimpleAfirmaMessages.getString("SignDataPanel.23"), //$NON-NLS-1$
                SimpleAfirmaMessages.getString("SimpleAfirma.7"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
