package es.gob.afirma.keystores;

/** Almacenes de claves que se cierran autom&aacute;ticamente pasado un tiempo determinado.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public interface AutoCloseableStore {

	/** Tiempo de inactividad que debe pasar para que el almac&eacute;n de claves se cierre.
	 * @param seconds Segundos de inactividad que deben pasar para cerrar el almac&eacute;n de claves. */
	void closeIn(final int seconds);

	/** Cierra el almacen de claves de inmediato. */
	void close();

	/** Indica si el almacen se encuentra actualmente abierto.
	 * @return {@code true} si el almac&eacute;n est&aacute; abierto, {@code false}
	 * en caso contrario.
	 */
	boolean isOpen();
}
