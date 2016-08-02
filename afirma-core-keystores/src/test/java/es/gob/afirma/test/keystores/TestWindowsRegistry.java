/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.test.keystores;

import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import es.gob.afirma.keystores.AOKeyStore;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.AOKeyStoreManagerFactory;
import es.gob.afirma.keystores.temd.WindowsRegistry;

/** Pruebas espec&iacute;ficas para el almac&eacute;n DNIe 100% Java.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public class TestWindowsRegistry {

	
    /** Prueba de carga y el almacenado de cadenas en el registro.
     * @throws Exception En cualquier error. */
	@Test
    public void testRegistryReadWrite() throws Exception {
		String passwd = "pass1234"; //$NON-NLS-1$
		System.out.println("Pass a guardar: " + passwd); //$NON-NLS-1$
		WindowsRegistry.writeRegistry("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion", "temd_pass_test", passwd.toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String result = WindowsRegistry.readRegistry("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion", "temd_pass_test"); //$NON-NLS-1$ //$NON-NLS-2$
        System.out.println("Pass cargada: " + result); //$NON-NLS-1$
    }

	/** Main.
	 * @param args No se usa.
	 * @throws Exception En cualquier error. */
	public static void main(final String[] args) throws Exception {
		new TestWindowsRegistry().testRegistryReadWrite();
	}

}
