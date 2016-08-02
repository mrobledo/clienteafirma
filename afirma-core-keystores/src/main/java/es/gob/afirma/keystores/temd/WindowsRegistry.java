package es.gob.afirma.keystores.temd;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.logging.Logger;

/** Clase para leer y escribir claves del registro.
 * @author Sergio Mart&iacute;nez Rico.
 */
public class WindowsRegistry {

	static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$
	
    /** Lee del registro desde la ruta y clave obtenidos.
     * @param location Ruta del registro.
     * @param key Clave del registro.
     * @return registry Valor obtenido o null si no se encuentra
     */
    public static final String readRegistry(String location, String key){
        try {
            // Run reg query, then read output with StreamReader (internal class)
        	String command = "reg query " +  //$NON-NLS-1$
                    '"'+ location + "\" /v " + key; //$NON-NLS-1$
            Process process = Runtime.getRuntime().exec(command); //$NON-NLS-1$
            LOGGER.info("Ejecuta: " + command); //$NON-NLS-1$
            StreamReader reader = new StreamReader(process.getInputStream());
            reader.start();
            process.waitFor();
            reader.join();
            String output = reader.getResult();

            // Output has the following format:
            // \n<Version information>\n\n<key>\t<registry type>\t<value>
            if( ! output.contains("    ")){ //$NON-NLS-1$
                    return null;
            }

            // Parse out the value
            String[] parsed = output.split("    "); //$NON-NLS-1$
            return parsed[parsed.length-1];
        }
        catch (Exception e) {
        	LOGGER.warning("Ha ocurrido un error intentando leer la clave en el registro: " + e); //$NON-NLS-1$
            return null;
        }

    }

    static class StreamReader extends Thread {
        private InputStream is;
        private StringWriter sw= new StringWriter();

        public StreamReader(InputStream is) {
            this.is = is;
        }

        @Override
		public void run() {
            try {
                int c;
                while ((c = this.is.read()) != -1)
                    this.sw.write(c);
            }
            catch (IOException e) { 
            	LOGGER.warning("Ha ocurrido un error intentando realizar una operacion en el registro: " + e); //$NON-NLS-1$
        }
        }

        public String getResult() {
            return this.sw.toString();
        }
    }
    
    /** Escribe el valor obtenido en la ruta del registro definida.
     * @param location Ruta del registro.
     * @param key Clave del registro.
     * @param password Clave del registro.
     */
	public static void writeRegistry(String location, String key, char[] password) {
		try {
			String command = "reg add " +  //$NON-NLS-1$
							location + 
							" /t REG_SZ /v " +  //$NON-NLS-1$
							key + 
							" /d " +  //$NON-NLS-1$
							new String(password) + 
							" /f"; //$NON-NLS-1$
			Runtime.getRuntime().exec(command); 
			LOGGER.info("Ejecuta: " + command);  //$NON-NLS-1$
		} catch (IOException e) {
			LOGGER.warning("Ha ocurrido un error intentando guardar la clave en el registro: " + e); //$NON-NLS-1$
		}
	}
}