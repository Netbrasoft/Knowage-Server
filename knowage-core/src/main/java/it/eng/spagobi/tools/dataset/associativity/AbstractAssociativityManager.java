/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.tools.dataset.associativity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jgrapht.graph.Pseudograph;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.DatasetEvaluationStrategy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.cache.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.graph.Tuple;
import it.eng.spagobi.tools.dataset.graph.associativity.Config;
import it.eng.spagobi.tools.dataset.graph.associativity.container.AssociativeDatasetContainerFactory;
import it.eng.spagobi.tools.dataset.graph.associativity.container.IAssociativeDatasetContainer;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicResult;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.parameters.ParametersUtilities;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public abstract class AbstractAssociativityManager implements IAssociativityManager {

	private static Logger logger = Logger.getLogger(AbstractAssociativityManager.class);

	protected IDataSource cacheDataSource;
	protected ICache cache;
	protected Map<String, Map<String, String>> datasetToAssociations;
	protected Pseudograph<String, LabeledEdge<String>> graph;
	protected Map<String, IAssociativeDatasetContainer> associativeDatasetContainers = new HashMap<>();
	protected Set<String> documentsAndExcludedDatasets;
	protected List<SimpleFilter> selections;

	protected AssociativeLogicResult result = new AssociativeLogicResult();

	protected UserProfile userProfile;

	protected abstract void initProcess();

	protected abstract void calculateDatasets(String dataset, EdgeGroup fromEdgeGroup, SimpleFilter filter) throws Exception;

	@Override
	public void process() throws Exception {
		if (cacheDataSource == null) {
			throw new SpagoBIException("Unable to get cache datasource, the value of [dataSource] is [null]");
		}
		if (cache == null) {
			throw new SpagoBIException("Unable to get cache, the value of [cache] is [null]");
		}

		// (1) generate the starting set of values for each associations
		initProcess();

		// (2) user click on widget -> selection!
		for (SimpleFilter selection : selections) {
			if (!documentsAndExcludedDatasets.contains(selection.getDataset().getLabel())) {
				calculateDatasets(selection.getDataset().getLabel(), null, selection);
			}
		}

		// (2) correct result data structure based on actual filters for each dataset
		endProcess();
	}

	protected List<String> getColumnNames(String associationNamesString, String datasetName) {
		String[] associationNames = associationNamesString.split(",");
		List<String> columnNames = new ArrayList<>();
		for (String associationName : associationNames) {
			Map<String, String> associationToColumns = datasetToAssociations.get(datasetName);
			if (associationToColumns != null) {
				String columnName = associationToColumns.get(associationName);
				if (columnName != null) {
					columnNames.add(columnName);
				}
			}
		}
		return columnNames;
	}

	protected void init(Config config, UserProfile userProfile) throws EMFUserError, SpagoBIException {
		this.userProfile = userProfile;
		initGraph(config);
		initDocuments(config);
		initCache();
		initDatasets(config);
	}

	private void initDocuments(Config config) {
		this.documentsAndExcludedDatasets = config.getDocuments();
	}

	private void initDatasets(Config config) throws EMFUserError, SpagoBIException {
		datasetToAssociations = config.getDatasetToAssociations();
		selections = config.getSelections();

		IDataSetDAO dataSetDao = DAOFactory.getDataSetDAO();
		if (userProfile != null) {
			dataSetDao.setUserProfile(userProfile);
		}

		for (String v1 : graph.vertexSet()) {
			if (!documentsAndExcludedDatasets.contains(v1)) {
				// the vertex is the dataset label
				IDataSet dataSet = dataSetDao.loadDataSetByLabel(v1);
				Assert.assertNotNull(dataSet, "Unable to get metadata for dataset [" + v1 + "]");

				if (dataSet.isRealtime()) {
					documentsAndExcludedDatasets.add(v1);
				} else {
					Map<String, String> parametersValues = config.getDatasetParameters().get(v1);
					dataSet.setParamsMap(parametersValues);

					boolean isNearRealtime = config.getNearRealtimeDatasets().contains(v1);
					DatasetEvaluationStrategy evaluationStrategy = dataSet.getEvaluationStrategy(isNearRealtime);

					IAssociativeDatasetContainer container = AssociativeDatasetContainerFactory.getContainer(evaluationStrategy, dataSet, parametersValues,
							userProfile);
					associativeDatasetContainers.put(v1, container);
				}
			}
		}

	}

	private void initGraph(Config config) {
		graph = config.getGraph();
	}

	private void initCache() {
		cacheDataSource = SpagoBICacheConfiguration.getInstance().getCacheDataSource();
		cache = SpagoBICacheManager.getCache();
	}

	protected void addEdgeGroup(String v1, Set<LabeledEdge<String>> edges, IAssociativeDatasetContainer container) {
		EdgeGroup group = AssociativeLogicUtils.getOrCreate(result.getEdgeGroupToDataset().keySet(), new EdgeGroup(edges));
		result.getDatasetToEdgeGroup().get(v1).add(group);

		if (!documentsAndExcludedDatasets.contains(v1)) {
			container.addGroup(group);

			if (!result.getEdgeGroupToDataset().containsKey(group)) {
				result.getEdgeGroupToDataset().put(group, new HashSet<String>());
				result.getEdgeGroupToDataset().get(group).add(v1);
			} else {
				result.getEdgeGroupToDataset().get(group).add(v1);
			}
		}
	}

	protected void addEdgeGroup(String v1, LabeledEdge<String> edge, IAssociativeDatasetContainer container) {
		Set<LabeledEdge<String>> edges = new HashSet<>(1);
		edges.add(edge);
		addEdgeGroup(v1, edges, container);
	}

	public void endProcess() {
		for (String label : associativeDatasetContainers.keySet()) {
			IAssociativeDatasetContainer container = associativeDatasetContainers.get(label);
			result.getDatasetToEdgeGroup().get(label).retainAll(container.getUsedGroups());

			for (EdgeGroup group : result.getEdgeGroupToDataset().keySet()) {
				if (!container.getUsedGroups().contains(group)) {
					result.getEdgeGroupToDataset().get(group).remove(label);
				}
			}
		}
	}

	@Override
	public AssociativeLogicResult getResult() {
		return result;
	}

	@Override
	public Map<String, Map<String, Set<Tuple>>> getSelections() {
		Map<String, Map<String, Set<Tuple>>> selections = new HashMap<>();
		for (String dataset : result.getDatasetToEdgeGroup().keySet()) {
			Set<EdgeGroup> groups = result.getDatasetToEdgeGroup().get(dataset);
			Map<String, Set<Tuple>> groupToValues = new HashMap<>(groups.size());
			for (EdgeGroup group : groups) {
				Set<Tuple> values = result.getEdgeGroupValues().get(group);
				if (values != null) {
					List<String> columns = getColumnNames(group.getOrderedEdgeNames(), dataset);
					String columnsString = StringUtils.join(columns, ",");
					groupToValues.put(columnsString, values);
				}
			}
			for (String edgeName : datasetToAssociations.get(dataset).keySet()) {
				for (EdgeGroup edgeGroup : groups) {
					if (!edgeGroup.getEdgeNames().contains(edgeName)) {
						String missingColumn = datasetToAssociations.get(dataset).get(edgeName);
						if (ParametersUtilities.isParameter(missingColumn)) {
							String missingParameter = ParametersUtilities.getParameterName(missingColumn);
							String value = associativeDatasetContainers.get(dataset).getParameters().get(missingParameter);
							HashSet<Tuple> tuples = new HashSet<Tuple>();
							tuples.add(new Tuple(value));
							groupToValues.put(missingColumn, tuples);
						}
					}
				}
			}
			selections.put(dataset, groupToValues);
		}
		return selections;
	}
}
