/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.i18n.dao;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.i18n.metadata.SbiI18NMessages;

public class I18NMessagesDAOHibImpl extends AbstractHibernateDAO implements I18NMessagesDAO {

	static private Logger logger = Logger.getLogger(I18NMessagesDAOHibImpl.class);

	@Override
	public String getI18NMessages(Locale locale, String code) throws EMFUserError {
		logger.debug("IN. code=" + code);
		String toReturn = null;

		Session aSession = null;
		Transaction tx = null;

		if (locale == null) {
			logger.warn("No I18n conversion because locale passed as parameter is null");
			return code;
		}

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String qDom = "from SbiDomains dom where dom.valueCd = :valueCd AND dom.domainCd = 'LANG'";
			Query queryDom = aSession.createQuery(qDom);
			String localeId = locale.getISO3Language().toUpperCase();
			logger.debug("localeId=" + localeId);
			queryDom.setString("valueCd", localeId);
			Object objDom = queryDom.uniqueResult();
			if (objDom == null) {
				logger.error("Could not find domain for locale " + locale.getISO3Language());
				return code;
			}
			Integer domId = ((SbiDomains) objDom).getValueId();

			String q = "from SbiI18NMessages att where att.id.languageCd = :languageCd AND att.id.label = :label";
			Query query = aSession.createQuery(q);

			query.setInteger("languageCd", domId);
			query.setString("label", code);

			Object obj = query.uniqueResult();
			if (obj != null) {
				SbiI18NMessages SbiI18NMessages = (SbiI18NMessages) obj;
				toReturn = SbiI18NMessages.getMessage();
			}

			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT.toReturn=" + toReturn);
		return toReturn;
	}

	@Override
	public Map<String, String> getAllI18NMessages(Locale locale) throws EMFUserError {
		logger.debug("IN");

		Map<String, String> toReturn = new HashMap<String, String>();

		Session aSession = null;
		Transaction tx = null;

		if (locale == null) {
			logger.error("No I18n conversion because locale passed as parameter is null");
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String qDom = "from SbiDomains dom where dom.valueCd = :valueCd AND dom.domainCd = 'LANG'";
			Query queryDom = aSession.createQuery(qDom);

			String localeId = null;

			try {
				localeId = locale.getISO3Language().toUpperCase();
			} catch (Exception e) {
				logger.warn("No iso code found for locale, set manually");
			}
			if (localeId == null) {
				if (locale.getLanguage().toUpperCase().equals("US"))
					localeId = "ENG";
				else if (locale.getLanguage().toUpperCase().equals("IT"))
					localeId = "ITA";
				else if (locale.getLanguage().toUpperCase().equals("FR"))
					localeId = "FRA";
				else if (locale.getLanguage().toUpperCase().equals("ES"))
					localeId = "ESP";
			}

			logger.debug("localeId=" + localeId);
			queryDom.setString("valueCd", localeId);
			Object objDom = queryDom.uniqueResult();
			if (objDom == null) {
				logger.error("Could not find domain for locale " + locale.getISO3Language());
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			}

			Integer domId = ((SbiDomains) objDom).getValueId();

			String q = "from SbiI18NMessages att where att.id.languageCd = :languageCd";
			Query query = aSession.createQuery(q);

			query.setInteger("languageCd", domId);

			List objList = query.list();
			if (objList != null && objList.size() > 0) {
				for (Iterator iterator = objList.iterator(); iterator.hasNext();) {
					SbiI18NMessages i18NMess = (SbiI18NMessages) iterator.next();
					toReturn.put(i18NMess.getId().getLabel(), i18NMess.getMessage());
				}
			}

		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT.toReturn=" + toReturn);
		return toReturn;

	}

}
