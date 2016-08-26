package es.gob.afirma.local.signers.batch;

final class TempStoreFactory {

	private static final TempStore TS = new TempStoreFileSystem();

	static TempStore getTempStore() {
		return TS;
	}

}
