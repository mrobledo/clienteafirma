package es.gob.afirma.api;

import java.net.URL;

import org.junit.Test;

/**
 * Pruebas asociadas a incidencias detectadas en el nucleo  */
public final class TestApi {

	/** 
	 * @throws Exception En cualquier error. */
	@Test
	public void testBatchSignWithInterface() throws Exception {
		// Se obtiene el xml de la carpeta resources
		URL xmlURL = this.getClass().getResource("/xml/batch-with-countersign.xml"); //$NON-NLS-1$

		// Se cambia el %20 por espacio
		String filePath = xmlURL.getPath().replace("%20", " "); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Alias y contrasena para firmar con el certificado autofirma.pfx
		String ret = KeyOneUtil.doBatchSign(filePath, "socketautofirma", "654321");  //$NON-NLS-1$ //$NON-NLS-2$
        System.out.println(ret);
	}
}