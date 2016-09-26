package es.gob.afirma.keystores.temd;

import java.awt.Component;
import java.util.prefs.Preferences;

import javax.security.auth.callback.PasswordCallback;

import es.gob.afirma.keystores.callbacks.UIPasswordCallback;

public final class TimedPersistentCachePasswordCallback extends PasswordCallback {

	private static final long serialVersionUID = -711589400619807544L;

	private final String dialogMsg;
	private long milisecondsToClose;
	private Component parent;

	public static final int INFINITE = -1;

	private static final String KEY_TEMD_OBJ = "temd-obj"; //$NON-NLS-1$
	private static final String KEY_TEMD_LASTTIME = "temd-lasttime"; //$NON-NLS-1$


	/** Objecto general de preferencias donde se guarda la configuraci&oacute;n de la
	 * aplicaci&oacute;n. */
	private static Preferences preferences;
	static {
		preferences = Preferences.userNodeForPackage(TimedPersistentCachePasswordCallback.class);
	}

	public TimedPersistentCachePasswordCallback(final String dialogMsg, final Object p) {
		super("dummy", false); //$NON-NLS-1$
		this.dialogMsg = dialogMsg;
		this.milisecondsToClose = INFINITE;
		if (p instanceof Component) {
			this.parent = (Component) p;
		}
		else {
			this.parent = null;
		}
	}


	public TimedPersistentCachePasswordCallback(final String dialogMsg, final long secsToClose, final Object p) {
		super("dummy", false); //$NON-NLS-1$
		this.dialogMsg = dialogMsg;
		this.milisecondsToClose = secsToClose * 1000;
		if (p instanceof Component) {
			this.parent = (Component) p;
		}
		else {
			this.parent = null;
		}
	}

	void setParent(final Object p) {
		if (p instanceof Component) {
			this.parent = (Component) p;
		}
	}

	public void setSecondsToClose(final long secsToClose) {
		this.milisecondsToClose = secsToClose * 1000;
	}

	@Override
	public void clearPassword() {
		super.clearPassword();
		clear();
	}

	/**
	 * Elimina el PIN del almacen y el registro de la &uacute;ltima
	 * hora en la que se us&oacute;.
	 */
	public static void clear() {
		preferences.remove(KEY_TEMD_OBJ);
		preferences.remove(KEY_TEMD_LASTTIME);
	}

	public boolean isObjectExpired() {

		final long lasttime = preferences.getLong(KEY_TEMD_LASTTIME, -1);
		if (lasttime == -1) {
			return true;
		}

		// Si el tiempo de caducidad es menor que 0, se considera que no hay caducidad
		if (this.milisecondsToClose < 0) {
			return false;
		}

		final boolean expired = System.currentTimeMillis() > lasttime + this.milisecondsToClose;
		if (expired) {
			clearPassword();
		}
		return expired;
	}

	@Override
	public char[] getPassword() {
		final String pin = preferences.get(KEY_TEMD_OBJ, null);
		if (pin != null && !isObjectExpired()) {
			resetTimer();
			return pin.toCharArray();
		}
		final char[] newpin = new UIPasswordCallback(this.dialogMsg, this.parent).getPassword();
		if (this.milisecondsToClose != 0) {
			preferences.put(KEY_TEMD_OBJ, new String(newpin));
		}
		resetTimer();
		return newpin;
	}

	void resetTimer() {
		if (this.milisecondsToClose != 0) {
			preferences.putLong(KEY_TEMD_LASTTIME, System.currentTimeMillis());
		}
	}

	@Override
	public void setPassword(final char[] password) {
		if (password != null && !(password.length < 1)) {
			super.setPassword(password);
			if (this.milisecondsToClose != 0) {
				preferences.put(KEY_TEMD_OBJ, new String(password));
			}
			resetTimer();
		}
	}
}
