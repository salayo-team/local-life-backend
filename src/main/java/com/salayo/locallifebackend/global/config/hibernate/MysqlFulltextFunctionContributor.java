package com.salayo.locallifebackend.global.config.hibernate;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.BasicType;
import org.hibernate.type.StandardBasicTypes;

public class MysqlFulltextFunctionContributor implements FunctionContributor {

	@Override
	public void contributeFunctions(FunctionContributions functionContributions) {

		BasicType<Double> doubleBasicType = functionContributions
			.getTypeConfiguration()
			.getBasicTypeRegistry()
			.resolve(StandardBasicTypes.DOUBLE);

		functionContributions.getFunctionRegistry()
			.registerPattern(
				"match_title_business",
				"match (?1, ?2) against (?3 in boolean mode)",
				doubleBasicType
			);
	}

}
