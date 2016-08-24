package es.gob.afirma.standalone.ui.envelopes;

/**
 * Informaci&oacute;n de un sobre electr&acute;nico.
 */
public class EnvelopInfo {

	/** Tipo de sobre.*/
	private final WrapType type;

	/** Datos contenidos en el sobre. */
	private byte[] data = null;

	public EnvelopInfo(final WrapType type) {
		this.type = type;
	}

	/**
	 * Indica el tipo de sobre.
	 * @return Tipo de sobre o {@code null} si no es un tipo reconocido.
	 */
	public WrapType getType() {
		return this.type;
	}

	public void setData(final byte[] data) {
		this.data = data;
	}

	/**
	 * Datos contenidos en el sobre.
	 * @return Datos extra&iacute;dos.
	 */
	public byte[] getData() {
		return this.data;
	}
}
