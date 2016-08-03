package es.gob.afirma.standalone.ui.envelopes;

import es.gob.afirma.standalone.SimpleAfirmaMessages;

enum EnvelopesTypeResources {

		SIGNED(			SimpleAfirmaMessages.getString("MenuDigitalEnvelope.6"), 	1), //$NON-NLS-1$
		AUTHENTICATED(	SimpleAfirmaMessages.getString("MenuDigitalEnvelope.5"), 	0), //$NON-NLS-1$
		SIMPLE(			SimpleAfirmaMessages.getString("MenuDigitalEnvelope.7"), 	2); //$NON-NLS-1$

		private final String envTypeName;
		private final int envTypeIndex;

		private EnvelopesTypeResources(final String name, final int index) {
			this.envTypeName = name;
			this.envTypeIndex = index;
		}

		@Override
		public String toString() {
			return this.envTypeName;
		}

		int getIndex() {
			return this.envTypeIndex;
		}

		static EnvelopesTypeResources getName(final int index) {
			switch (index) {
			case 0:
				return SIGNED;
			case 1:
				return AUTHENTICATED;
			case 2:
				return SIMPLE;
			default:
				throw new IllegalArgumentException();
			}
		}

		static EnvelopesTypeResources[] getAllEnvelopesTypeResources() {
			return new EnvelopesTypeResources[] {
				SIGNED,
				AUTHENTICATED,
				SIMPLE
			};
		}
	}