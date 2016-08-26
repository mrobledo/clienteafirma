/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.local;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.AOPkcs1Signer;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.core.signers.TriphaseDataSigner;
import es.gob.afirma.local.signers.batch.server.BatchPostsigner;
import es.gob.afirma.local.signers.batch.server.BatchPresigner;

/** Cliente del servicio de firma por lote.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class BatchSigner {

	private BatchSigner() {
		// No instanciable
	}

	/** Procesa un lote de firmas.
	 * Los lotes deben proporcionase definidos en un fichero XML con el siguiente esquema XSD:
	 * <pre>
	 * &lt;xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified" xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;
  	 * &lt;xs:element name="signbatch"&gt;
	 *     &lt;xs:complexType&gt;
	 *       &lt;xs:sequence&gt;
	 *         &lt;xs:element name="singlesign" maxOccurs="unbounded" minOccurs="1"&gt;
	 *           &lt;xs:complexType&gt;
	 *             &lt;xs:sequence&gt;
	 *               &lt;xs:element type="xs:string" name="datasource"/&gt;
	 *               &lt;xs:element name="format"&gt;
	 *                 &lt;xs:simpleType&gt;
	 *                   &lt;xs:restriction base="xs:string"&gt;
	 *                     &lt;xs:enumeration value="XAdES"/&gt;
	 *                     &lt;xs:enumeration value="CAdES"/&gt;
	 *                     &lt;xs:enumeration value="PAdES"/&gt;
	 *                   &lt;/xs:restriction&gt;
	 *                 &lt;/xs:simpleType&gt;
	 *               &lt;/xs:element&gt;
	 *               &lt;xs:element name="suboperation"&gt;
	 *                 &lt;xs:simpleType&gt;
	 *                   &lt;xs:restriction base="xs:string"&gt;
	 *                     &lt;xs:enumeration value="sign"/&gt;
	 *                     &lt;xs:enumeration value="cosign"/&gt;
	 *                   &lt;/xs:restriction&gt;
	 *                 &lt;/xs:simpleType&gt;
	 *               &lt;/xs:element&gt;
	 *               &lt;xs:element name="extraparams"&gt;
	 *                 &lt;xs:simpleType&gt;
	 *                  &lt;xs:restriction  base="xs:base64Binary" /&gt;
	 *                 &lt;/xs:simpleType&gt;
	 *               &lt;/xs:element&gt;
	 *               &lt;xs:element name="signsaver"&gt;
	 *                 &lt;xs:complexType&gt;
	 *                   &lt;xs:sequence&gt;
	 *                     &lt;xs:element type="xs:string" name="class"/&gt;
	 *                     &lt;xs:element name="config"&gt;
	 *                       &lt;xs:simpleType&gt;
	 *                         &lt;xs:restriction  base="xs:base64Binary" /&gt;
	 *                       &lt;/xs:simpleType&gt;
	 *                     &lt;/xs:element&gt;
	 *                   &lt;/xs:sequence&gt;
	 *                 &lt;/xs:complexType&gt;
	 *               &lt;/xs:element&gt;
	 *             &lt;/xs:sequence&gt;
	 *             &lt;xs:attribute type="xs:string" name="id" use="required"/&gt;
	 *           &lt;/xs:complexType&gt;
	 *         &lt;/xs:element&gt;
	 *       &lt;/xs:sequence&gt;
	 *       &lt;xs:attribute type="xs:string" name="stoponerror" use="optional"/&gt;
	 *       &lt;xs:attribute type="xs:string" name="algorithm" use="required"&gt;
	 *         &lt;xs:simpleType&gt;
	 *           &lt;xs:restriction base="xs:string"&gt;
	 *             &lt;xs:enumeration value="SHA1withRSA"/&gt;
	 *             &lt;xs:enumeration value="SHA256withRSA"/&gt;
	 *             &lt;xs:enumeration value="SHA384withRSA"/&gt;
	 *             &lt;xs:enumeration value="SHA512withRSA"/&gt;
	 *           &lt;/xs:restriction&gt;
	 *         &lt;/xs:simpleType&gt;
	 *       &lt;xs:attribute&gt;
	 *     &lt;/xs:complexType&gt;
	 *   &lt;/xs:element&gt;
	 * &lt;/xs:schema&gt;
	 * </pre>
	 * Un ejemplo de definici&oacute;n XML de lote de firmas podr&iacute;a ser
	 * este (ejemplo con dos firmas en el lote):
	 * <pre>
	 * &lt;?xml version="1.0" encoding="UTF-8" ?&gt;
	 * &lt;signbatch stoponerror="true" algorithm="SHA1withRSA"&gt;
	 *  &lt;singlesign id="f8526f7b-d30a-4720-9e35-fe3494217944"&gt;
	 *   &lt;datasource&gt;http://google.com&lt;/datasource&gt;
	 *   &lt;format&gt;XAdES&lt;/format&gt;
	 *   &lt;suboperation&gt;sign&lt;/suboperation&gt;
	 *   &lt;extraparams&gt;Iw0KI1RodSBBdW[...]QNCg==&lt;/extraparams&gt;
	 *   &lt;signsaver&gt;
	 *    &lt;class&gt;es.gob.afirma.signers.batch.SignSaverFile&lt;/class&gt;
	 *    &lt;config&gt;Iw0KI1RodSBBdWcgMT[...]wNCg==&lt;/config&gt;
	 *   &lt;/signsaver&gt;
	 *  &lt;/singlesign&gt;
	 *  &lt;singlesign id="0e9cc5de-63ee-45ee-ae02-4a6591ab9a46"&gt;
	 *   &lt;datasource&gt;SG9sYSBNdW5kbw==&lt;/datasource&gt;
	 *   &lt;format&gt;CAdES&lt;/format&gt;
	 *   &lt;suboperation&gt;sign&lt;/suboperation&gt;
	 *   &lt;extraparams&gt;Iw0KI1RodSBBdWc[...]NCg==&lt;/extraparams&gt;
	 *   &lt;signsaver&gt;
	 *    &lt;class&gt;es.gob.afirma.signers.batch.SignSaverFile&lt;/class&gt;
	 *    &lt;config&gt;Iw0KI1RodSBBdWcgMTM[...]Cg==&lt;/config&gt;
	 *   &lt;/signsaver&gt;
	 *  &lt;/singlesign&gt;
	 * &lt;/signbatch&gt;
	 * </pre>
	 * @param batchB64 XML de definici&oacute;n del lote de firmas.
	 * @param certificates Cadena de certificados del firmante.
	 * @param pk Clave privada para realizar las firmas cliente.
	 * @return Registro del resultado general del proceso por lote, en un XML con este esquema:
	 * <pre>
	 * &lt;xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified" xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;
	 *  &lt;xs:element name="signs"&gt;
	 *    &lt;xs:complexType&gt;
	 *      &lt;xs:sequence&gt;
	 *        &lt;xs:element name="sign" maxOccurs="unbounded" minOccurs="1"&gt;
	 *          &lt;xs:complexType&gt;
	 *            &lt;xs:sequence&gt;
	 *              &lt;xs:element name="result"&gt;
	 *                &lt;xs:simpleType&gt;
	 *                  &lt;xs:restriction base="xs:string"&gt;
	 *                    &lt;xs:enumeration value="OK"/&gt;
	 *                    &lt;xs:enumeration value="KO"/&gt;
	 *                    &lt;xs:enumeration value="NP"/&gt;
	 *                  &lt;/xs:restriction&gt;
	 *                &lt;/xs:simpleType&gt;
	 *              &lt;/xs:element&gt;
	 *              &lt;xs:element type="xs:string" name="reason" minOccurs="0"/&gt;
	 *            &lt;/xs:sequence&gt;
	 *            &lt;xs:attribute type="xs:string" name="id" use="required"/&gt;
	 *          &lt;/xs:complexType&gt;
	 *        &lt;/xs:element&gt;
	 *      &lt;/xs:sequence&gt;
	 *    &lt;/xs:complexType&gt;
	 *  &lt;/xs:element&gt;
	 * &lt;/xs:schema&gt;
	 * </pre>
	 * @throws IOException Si hay problemas de red o en el tratamiento de datos.
	 * @throws CertificateEncodingException Si los certificados proporcionados no son v&aacute;lidos.
	 * @throws AOException Si hay errores en las firmas cliente. */
	public static String sign(final String batchB64,
			                  final Certificate[] certificates,
			                  final PrivateKey pk) throws CertificateEncodingException,
			                                              IOException,
			                                              AOException {
		if (batchB64 == null || batchB64.isEmpty()) {
			throw new IllegalArgumentException("El lote de firma no puede ser nulo ni vacio"); //$NON-NLS-1$
		}

		if (certificates == null || certificates.length < 1) {
			throw new IllegalArgumentException(
				"La cadena de certificados del firmante no puede ser nula ni vacia" //$NON-NLS-1$
			);
		}

		final String batchUrlSafe = batchB64.replace("+", "-").replace("/",  "_");  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$

		// Llama a BatchPresigner simulando la funcionalidad del servidor
		byte[] ret = BatchPresigner.doBatchPreSign(batchUrlSafe, getCertChainAsBase64(certificates)).getBytes();

		final TriphaseData td1 = TriphaseData.parser(ret);

		// El cliente hace los PKCS#1 generando TD2, que envia de nuevo al servidor
		final TriphaseData td2 = TriphaseDataSigner.doSign(
			new AOPkcs1Signer(),
			getAlgorithm(batchB64),
			pk,
			certificates,
			td1
		);

		// Llamar a BatchPostsigner simulando la funcionalidad del servidor
		ret = BatchPostsigner.doBatchPostSign(
				batchUrlSafe, 
				getCertChainAsBase64(certificates), 
				Base64.encode(td2.toString().getBytes())
			).getBytes();

		return new String(ret);
	}

	private static String getCertChainAsBase64(final Certificate[] certChain) throws CertificateEncodingException {
		final StringBuilder sb = new StringBuilder();
		for (final Certificate cert : certChain) {
			sb.append(Base64.encode(cert.getEncoded(), true));
			sb.append(";"); //$NON-NLS-1$
		}
		final String ret = sb.toString();

		// Quitamos el ";" final
		return ret.substring(0, ret.length()-1);
	}

	private static String getAlgorithm(final String batch) throws IOException {
		final byte[] xml =  Base64.decode(batch.replace("-", "+").replace("_", "/")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		final Document doc;
		try (
				final InputStream is = new ByteArrayInputStream(xml)
				) {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		}
		catch (final Exception e) {
			Logger.getLogger("es.gob.afirma").severe("Error al cargar el fichero XML de lote: " + e + "\n" + new String(xml)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			throw new IOException("Error al cargar el fichero XML de lote: " + e, e); //$NON-NLS-1$
		}

		final Node signBatchNode = doc.getDocumentElement();
		if (!"signbatch".equalsIgnoreCase(signBatchNode.getNodeName())) { //$NON-NLS-1$
			throw new IllegalArgumentException("No se encontro el nodo 'signbatch' en el XML proporcionado"); //$NON-NLS-1$
		}

		final NamedNodeMap nnm = signBatchNode.getAttributes();
		if (nnm != null) {
			final Node tmpNode = nnm.getNamedItem("algorithm"); //$NON-NLS-1$
			if (tmpNode != null) {
				return tmpNode.getNodeValue();
			}
			throw new IllegalArgumentException(
				"El nodo 'signbatch' debe contener al manos el atributo de algoritmo" //$NON-NLS-1$
			);
		}
		throw new IllegalArgumentException(
			"El nodo 'signbatch' debe contener al manos el atributo de algoritmo" //$NON-NLS-1$
		);

	}

}
