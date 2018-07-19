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
package it.eng.qbe.statement.jpa;

import it.eng.qbe.query.CriteriaConstants;
import it.eng.qbe.statement.IConditionalOperator;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JPQLStatementConditionalOperators {
	

	
	private static Map<String, IConditionalOperator> conditionalOperators;
	
	public static IConditionalOperator getOperator(String operatorName) {
		return conditionalOperators.get( operatorName );
	}
	
	static {
		conditionalOperators = new HashMap<String, IConditionalOperator>();
		conditionalOperators.put(CriteriaConstants.EQUALS_TO, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.EQUALS_TO;}
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + "=" + rightHandValues[0];
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_EQUALS_TO, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.NOT_EQUALS_TO;}
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + "!=" + rightHandValues[0];
			}
		});
		conditionalOperators.put(CriteriaConstants.GREATER_THAN, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.GREATER_THAN;}
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + ">" + rightHandValues[0];
			}
		});
		conditionalOperators.put(CriteriaConstants.EQUALS_OR_GREATER_THAN, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.EQUALS_OR_GREATER_THAN;}
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + ">=" + rightHandValues[0];
			}
		});
		conditionalOperators.put(CriteriaConstants.LESS_THAN, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.LESS_THAN;}
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + "<" + rightHandValues[0];
			}
		});
		conditionalOperators.put(CriteriaConstants.EQUALS_OR_LESS_THAN, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.EQUALS_OR_LESS_THAN;}
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + "<=" + rightHandValues[0];
			}
		});
		conditionalOperators.put(CriteriaConstants.STARTS_WITH, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.STARTS_WITH;}
			public String apply(String leftHandValue, String[] rightHandValues) {	
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				if (rightHandValue.startsWith("'")) {
					rightHandValue = rightHandValue.substring(1, rightHandValue.length()-1);
					rightHandValue = rightHandValue + "%";
				}else{
					// field reference
					rightHandValue = "' || " + rightHandValue + " || '%";
				}
				return leftHandValue + " like '" + rightHandValue + "'";
			}
		});
		conditionalOperators.put(CriteriaConstants.LIKE, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.LIKE;}
			public String apply(String leftHandValue, String[] rightHandValues) {	
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				if (rightHandValue.startsWith("'")) {
					rightHandValue = rightHandValue.substring(1, rightHandValue.length()-1);
				}else{
					// field reference
					rightHandValue = "' || " + rightHandValue + " || '";
				}
				return leftHandValue + " like '" + rightHandValue + "'";
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_STARTS_WITH, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.NOT_STARTS_WITH;}
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				if (rightHandValue.startsWith("'")) {
					rightHandValue = rightHandValue.substring(1, rightHandValue.length()-1);
					rightHandValue = rightHandValue + "%";
				}else{
					// field reference
					rightHandValue = "' || " +rightHandValue + " || '%";
				}
				return leftHandValue + " not like '" + rightHandValue + "'";
			}
		});
		conditionalOperators.put(CriteriaConstants.ENDS_WITH, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.ENDS_WITH;}
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				if (rightHandValue.startsWith("'")) {
					rightHandValue = rightHandValue.substring(1, rightHandValue.length()-1);
					rightHandValue = "%" + rightHandValue;
				}else{
					// field reference
					rightHandValue = "%' || " + rightHandValue + " || '";
				}
				return leftHandValue + " like '" + rightHandValue + "'";
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_ENDS_WITH, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.NOT_ENDS_WITH;}
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				if (rightHandValue.startsWith("'")) {
					rightHandValue = rightHandValue.substring(1, rightHandValue.length()-1);
					rightHandValue = "%" + rightHandValue;
				}else{
					// field reference
					rightHandValue = "%' || " + rightHandValue + " || '";
				}
				return leftHandValue + " not like '" + rightHandValue + "'";
			}
		});		 
		conditionalOperators.put(CriteriaConstants.CONTAINS, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.CONTAINS;}
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim(); 
				if (rightHandValue.startsWith("'")) {
					rightHandValue = rightHandValue.substring(1, rightHandValue.length()-1);
					rightHandValue = "%" + rightHandValue + "%";
				}else{
					// field reference
					rightHandValue = "%' || " + rightHandValue + " || '%";
				}
				return leftHandValue + " like '" + rightHandValue + "'";
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_CONTAINS, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.NOT_CONTAINS;}
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim(); 
				if (rightHandValue.startsWith("'")) {
					rightHandValue = rightHandValue.substring(1, rightHandValue.length()-1);
					rightHandValue = "%" + rightHandValue + "%";
				}else{
					// field reference
					rightHandValue = "%' || " + rightHandValue + " || '%";
				}
				return leftHandValue + " not like '" + rightHandValue + "'";
			}
		});
		conditionalOperators.put(CriteriaConstants.IS_NULL, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.IS_NULL;}
			public String apply(String leftHandValue, String[] rightHandValue) {
				return leftHandValue + " IS NULL";
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_NULL, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.NOT_NULL;}
			public String apply(String leftHandValue, String[] rightHandValue) {
				return leftHandValue + " IS NOT NULL";
			}
		});
		
		conditionalOperators.put(CriteriaConstants.BETWEEN, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.BETWEEN;}
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues.length == 2, "When BEETWEEN operator is used the operand must contain minValue and MaxValue");
				return leftHandValue + " BETWEEN " + rightHandValues[0] + " AND " + rightHandValues[1];
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_BETWEEN, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.NOT_BETWEEN;}
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues.length == 2, "When BEETWEEN operator is used the operand must contain minValue and MaxValue");
				return leftHandValue + " NOT BETWEEN " + rightHandValues[0] + " AND " + rightHandValues[1];
			}
		});
		
		conditionalOperators.put(CriteriaConstants.IN, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.IN;}
			public String apply(String leftHandValue, String[] rightHandValues) {
				String rightHandValue = StringUtils.join(rightHandValues, ",");
				return leftHandValue + " IN (" +  rightHandValue + ")";
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_IN, new IConditionalOperator() {
			public String getName() {return CriteriaConstants.NOT_IN;}
			public String apply(String leftHandValue, String[] rightHandValues) {
				String rightHandValue = StringUtils.join(rightHandValues, ",");
				return leftHandValue + " NOT IN (" +  rightHandValue + ")";
			}
		});
		// handling spatial operators
		conditionalOperators.put(CriteriaConstants.SPATIAL_EQUALS_TO, new SpatialOperator(CriteriaConstants.SPATIAL_EQUALS_TO, "equals"));
		conditionalOperators.put(CriteriaConstants.SPATIAL_TOUCHES, new SpatialOperator(CriteriaConstants.SPATIAL_TOUCHES, "touches"));
		conditionalOperators.put(CriteriaConstants.SPATIAL_OVERLAPS, new SpatialOperator(CriteriaConstants.SPATIAL_OVERLAPS, "overlaps"));
		conditionalOperators.put(CriteriaConstants.SPATIAL_COVERS, new SpatialOperator(CriteriaConstants.SPATIAL_COVERS, "covers"));
		conditionalOperators.put(CriteriaConstants.SPATIAL_CONTAINS, new SpatialOperator(CriteriaConstants.SPATIAL_CONTAINS, "contains"));
		conditionalOperators.put(CriteriaConstants.SPATIAL_COVERED_BY, new SpatialOperator(CriteriaConstants.SPATIAL_COVERED_BY, "coveredby"));
		conditionalOperators.put(CriteriaConstants.SPATIAL_INTERSECTS, new SpatialOperator(CriteriaConstants.SPATIAL_INTERSECTS, "intersects"));
		conditionalOperators.put(CriteriaConstants.SPATIAL_INSIDE, new SpatialOperator(CriteriaConstants.SPATIAL_INSIDE, "inside"));
		conditionalOperators.put(CriteriaConstants.SPATIAL_DISJOINT, new SpatialOperator(CriteriaConstants.SPATIAL_DISJOINT, "disjoint"));
		conditionalOperators.put(CriteriaConstants.SPATIAL_FILTER, new SpatialOperator(CriteriaConstants.SPATIAL_FILTER, "filter"));
		
		/* not used 
		conditionalOperators.put(CriteriaConstants.SPATIAL_NOT_COVERED_BY, new SpatialOperator(CriteriaConstants.SPATIAL_NOT_COVERED_BY, "coveredby", true));
		conditionalOperators.put(CriteriaConstants.SPATIAL_NOT_CONTAINS, new SpatialOperator(CriteriaConstants.SPATIAL_NOT_CONTAINS, "contains", true));
		conditionalOperators.put(CriteriaConstants.SPATIAL_NOT_COVERS, new SpatialOperator(CriteriaConstants.SPATIAL_NOT_COVERS, "covers", true));
		conditionalOperators.put(CriteriaConstants.SPATIAL_NOT_EQUALS_TO, new SpatialOperator(CriteriaConstants.SPATIAL_NOT_EQUALS_TO, "equals", true));
		conditionalOperators.put(CriteriaConstants.SPATIAL_NOT_TOUCHES, new SpatialOperator(CriteriaConstants.SPATIAL_NOT_TOUCHES,"touches", true));
		conditionalOperators.put(CriteriaConstants.SPATIAL_NOT_OVERLAPS, new SpatialOperator(CriteriaConstants.SPATIAL_NOT_OVERLAPS,"overlaps",true));
		*/
	}
}

class SpatialOperator implements IConditionalOperator{
	String name;
	String hqlname;
	boolean negate;
	public SpatialOperator(String name, String hqlname) {
		this(name, hqlname, false);
	}
	public SpatialOperator(String name, String hqlname, boolean negate) {
		this.name = name;
		this.hqlname = hqlname;
		this.negate = negate;
	}
	public String getName() {return name;}
	public String apply(String leftHandValue, String[] rightHandValues) {
		Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
		return hqlname + "(" + leftHandValue + ", " + rightHandValues[0] + ")" + (negate?"!":"") + "='TRUE'";
	}
	
}
