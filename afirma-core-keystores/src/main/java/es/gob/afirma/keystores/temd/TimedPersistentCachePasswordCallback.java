package es.gob.afirma.keystores.temd;

import java.awt.Component;
import java.util.prefs.Preferences;

import javax.security.auth.callback.PasswordCallback;

import es.gob.afirma.keystores.AOKeyStore;

final class TimedPersistentCachePasswordCallback extends PasswordCallback {

	private static final long serialVersionUID = -711589400619807544L;

	private long milisecondsToClose;
	private Component parent;

	private static final String KEY_TEMD_OBJ = "temd-obj"; //$NON-NLS-1$
	private static final String KEY_TEMD_LASTTIME = "temd-lasttime"; //$NON-NLS-1$

	/** Objecto general de preferencias donde se guarda la configuraci&oacute;n de la
	 * aplicaci&oacute;n. */
	private static Preferences preferences;
	static {
		preferences = Preferences.userNodeForPackage(TimedPersistentCachePasswordCallback.class);
	}

	TimedPersistentCachePasswordCallback(final long segsToClose, final Object p) {
		super("dummy", false); //$NON-NLS-1$
		this.milisecondsToClose = segsToClose * 1000;
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

	void setSecondsToClose(final long segsToClose) {
		this.milisecondsToClose = segsToClose * 1000;
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
		final char[] newpin = AOKeyStore.TEMD.getStorePasswordCallback(this.parent).getPassword();
		preferences.put(KEY_TEMD_OBJ, new String(newpin));
		resetTimer();
		return newpin;
	}

	static void resetTimer() {
		preferences.putLong(KEY_TEMD_LASTTIME, System.currentTimeMillis());
	}

	@Override
	public void setPassword(final char[] password) {
		if (password != null && !(password.length < 1)) {
			super.setPassword(password);
			preferences.put(KEY_TEMD_OBJ, new String(password));
			resetTimer();
		}
	}
}
