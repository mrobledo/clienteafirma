package es.gob.afirma.api;

import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

import org.junit.Test;

/**
 * Pruebas asociadas a incidencias detectadas en el nucleo  */
public final class TestApi {

	/** 
	 * @throws Exception En cualquier error. */
	@Test
	public static void testBatchSignWithInterface() throws Exception {
		//Se elige el xml
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(new JDialog());
		File file;
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            String ret = KeyOneUtil.doBatchSign(file.getAbsolutePath(), "socketautofirma", "654321");  //$NON-NLS-1$ //$NON-NLS-2$
            System.out.println(ret);
        }
	}
}