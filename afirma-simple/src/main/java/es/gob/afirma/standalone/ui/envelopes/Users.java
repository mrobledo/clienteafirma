package es.gob.afirma.standalone.ui.envelopes;

final class Users {

	private final String cn;
	private final String email;
	private final String uid;

	String getCn() {
		return this.cn;
	}

	String getEmail() {
		return this.email;
	}

	String getUid() {
		return this.uid;
	}

	Users(final String cn, final String email, final String uid) {
		this.cn = cn;
		this.email = email;
		this.uid = uid;
	}

	@Override
	public String toString() {
		return "cn=" + this.cn + ", email=" + this.email + ", UID=" + this.uid; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
