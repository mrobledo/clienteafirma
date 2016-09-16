package es.gob.afirma.standalone.ui.envelopes;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.keystores.AOKeyStore;
import es.gob.afirma.keystores.KeyStoreConfiguration;
import es.gob.afirma.standalone.SimpleAfirmaMessages;

/** Utilidades para sobres digitales.
 * @author Mariano Mart&iacute;nez. */
public final class EnvelopesUtils {

	private EnvelopesUtils() {
		// No permitimos la instanciacion
	}

	/** Recupera los almacenes compatibles con el sistema y preparados
     * para contener certificados de firma.
     * @return Listado de almacenes. */
    public static KeyStoreConfiguration[] getKeyStoresToSign() {

        final List<KeyStoreConfiguration> stores = new ArrayList<>();

        stores.add(new KeyStoreConfiguration(AOKeyStore.TEMD, null, null));

        if (Platform.getOS().equals(Platform.OS.WINDOWS)) {
            stores.add(new KeyStoreConfiguration(AOKeyStore.WINDOWS, null, null));
        }
        if (Platform.getOS().equals(Platform.OS.MACOSX)) {
            stores.add(new KeyStoreConfiguration(AOKeyStore.APPLE, null, null));
        }

        stores.add(new KeyStoreConfiguration(AOKeyStore.MOZ_UNI, null, null));
        stores.add(new KeyStoreConfiguration(AOKeyStore.DNIEJAVA, null, null));
        stores.add(new KeyStoreConfiguration(AOKeyStore.PKCS12, null, null));

        return stores.toArray(new KeyStoreConfiguration[stores.size()]);
    }

    /** Recupera los almacenes compatibles con el sistema y preparados
     * para contener certificados para envoltura de datos.
     * @param enableLDAPMDEF <code>true</code> para habilitar el directorio del Ministerio de Defensa.
     * @return Listado de almacenes. */
    public static KeyStoreConfiguration[] getKeyStoresToWrap() {

        final List<KeyStoreConfiguration> stores = new ArrayList<>();

        stores.add(new KeyStoreConfiguration(AOKeyStore.LDAPMDEF, null, null));
        stores.add(new KeyStoreConfiguration(AOKeyStore.SINGLE, null, null));
        stores.add(new KeyStoreConfiguration(AOKeyStore.PKCS12, null, null));
        stores.add(new KeyStoreConfiguration(AOKeyStore.PKCS11, null, null));

        return stores.toArray(new KeyStoreConfiguration[stores.size()]);
    }

    /** Recupera los almacenes compatibles con el sistema y preparados
     * para contener certificados para envoltura de datos. El almac&eacute;n por defecto ser&aacute;
     * @param enableLDAPMDEF <code>true</code> para habilitar el directorio del Ministerio de Defensa.
     * @return Listado de almacenes. */
    public static KeyStoreConfiguration[] getKeyStoresToWrapFirstFrecuentCerts() {

        final List<KeyStoreConfiguration> stores = new ArrayList<>();

        stores.add(new KeyStoreConfiguration(AOKeyStore.FRECUENTCERTS, null, null));
        stores.add(new KeyStoreConfiguration(AOKeyStore.LDAPMDEF, null, null));
        stores.add(new KeyStoreConfiguration(AOKeyStore.SINGLE, null, null));
        stores.add(new KeyStoreConfiguration(AOKeyStore.PKCS12, null, null));
        stores.add(new KeyStoreConfiguration(AOKeyStore.PKCS11, null, null));

        return stores.toArray(new KeyStoreConfiguration[stores.size()]);
    }

	/** Recupera los almacenes compatibles con el sistema y preparados
     * para contener los certificados para la apertura de sobres.
     * @return Listado de almacenes. */
    public static KeyStoreConfiguration[] getKeyStoresToUnwrap() {

        final List<KeyStoreConfiguration> stores = new ArrayList<>();

        stores.add(new KeyStoreConfiguration(AOKeyStore.TEMD, null, null));

        if (Platform.getOS().equals(Platform.OS.WINDOWS)) {
            stores.add(new KeyStoreConfiguration(AOKeyStore.WINDOWS, null, null));
        }
        if (Platform.getOS().equals(Platform.OS.MACOSX)) {
            stores.add(new KeyStoreConfiguration(AOKeyStore.APPLE, null, null));
        }

        stores.add(new KeyStoreConfiguration(AOKeyStore.MOZ_UNI, null, null));
        stores.add(new KeyStoreConfiguration(AOKeyStore.DNIEJAVA, null, null));
        stores.add(new KeyStoreConfiguration(AOKeyStore.PKCS12, null, null));

        return stores.toArray(new KeyStoreConfiguration[stores.size()]);
    }

    /** Obtiene el fichero elegido por el usuario dependiendo de la extension que haya elegido en el combobox.
     * @return El fichero seleccionado. */
    static File addFileSelected(final String[] extension,
    					        final JComboBox<KeyStoreConfiguration> comboDestinatarios,
    					        final Component parent) {
		final File file;
		try {
			if (extension[0] != null) {
				Logger.getLogger("es.gob.afirma").info( //$NON-NLS-1$
					"Anadir fichero de tipo: " + extension[0] //$NON-NLS-1$
				);
			}
			file = AOUIFactory.getLoadFiles(
				SimpleAfirmaMessages.getString("MenuDigitalEnvelope.8"), //$NON-NLS-1$
				null,
				null,
				extension,
				comboDestinatarios.getSelectedItem().toString(),
				false,
				false,
				null,
				parent
			)[0];
		}
		catch (final AOCancelledOperationException e) {
			Logger.getLogger("es.gob.afirma").info( //$NON-NLS-1$
				"Accion cancelada por el usuario: " + e //$NON-NLS-1$
			);
			return null;
		}
		if (!file.canRead()) {
			AOUIFactory.showErrorMessage(
				parent,
				SimpleAfirmaMessages.getString("MenuValidation.6"), //$NON-NLS-1$
				SimpleAfirmaMessages.getString("MenuValidation.5"), //$NON-NLS-1$
				JOptionPane.ERROR_MESSAGE
			);
		}
		return file;
    }

    /** Lectura de fichero.
     * @param filepath Ruta del fichero a leer.
     * @return Array de bytes del contenido del fichero.
     * @throws java.io.FileNotFoundException Excepci&oacute; si no encuentra el fichero.
     * @throws IOException Excepci&oacute; si falla la lectura del fichero. */
    static byte[] readFile(final String filepath) throws IOException {
        try ( final InputStream fileIn = AOUtil.loadFile(AOUtil.createURI(filepath)); ) {
            return AOUtil.getDataFromInputStream(fileIn);
        }
        catch (final URISyntaxException e) {
        	throw new IOException("Ruta hacia el fichero de datos invalida: " + filepath, e); //$NON-NLS-1$
		}
    }
}