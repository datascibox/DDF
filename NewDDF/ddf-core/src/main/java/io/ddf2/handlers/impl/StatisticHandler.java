package io.ddf2.handlers.impl;

import io.ddf2.DDFException;
import io.ddf2.IDDF;
import io.ddf2.ISqlResult;
import io.ddf2.analytics.FiveNumSummary;
import io.ddf2.analytics.SimpleSummary;
import io.ddf2.datasource.schema.IColumn;
import io.ddf2.handlers.IStatisticHandler;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public abstract class StatisticHandler implements IStatisticHandler {
    protected IDDF ddf;

    public StatisticHandler(IDDF ddf) {
        this.ddf = ddf;
    }


    //Todo: @Jing check whether we could implement getSumary here. because it's using sql command.
    @Override
    public abstract Summary[] getSummary() throws DDFException;

    @Override
    public abstract SimpleSummary[] getSimpleSummary() throws DDFException;

    @Override
    public abstract FiveNumSummary[] getFiveNumSummary(List<String> columnNames) throws DDFException;

    @Override
    public Double[] getQuantiles(String columnName, Double[] percentiles) throws DDFException {
        assert columnName != null;
        assert percentiles.length > 0;
        Set<Double> setPercentiles = new HashSet(Arrays.asList(percentiles));
        boolean hasZero = setPercentiles.contains(0.0);
        boolean hasOne = setPercentiles.contains(1.0);
        setPercentiles.remove(0.0);
        setPercentiles.remove(1.0);
        List<Double> listPercentiles = new ArrayList(setPercentiles);
        List<String> sqlSelect = new ArrayList<String>();


        if (listPercentiles.size() > 0) {
            IColumn column = ddf.getSchema().getColumn(columnName);
            if (column.isIntegral()) {
                sqlSelect.add(String.format("percentile(%s,array(%s))", columnName, StringUtils.join(listPercentiles, ",")));
            } else if (column.isFractional()) {
                sqlSelect.add(String.format("percentile_approx(%s,array(%s))", columnName, StringUtils.join(listPercentiles, ",")));
            } else {
                throw new DDFException("Only support numeric vectors!!!");
            }
        }
        if (hasZero) sqlSelect.add("min(" + columnName + ")");
        if (hasOne) sqlSelect.add("max(" + columnName + ")");


        String sqlCmd = String.format("SELECT %s FROM %s", StringUtils.join(sqlSelect, ","), ddf.getDDFName());

        ISqlResult sqlResult = ddf.sql(sqlCmd);

        //Todo: @Jing plz help to parse the result. I don't know which kind we want to parse here.
        return new Double[0];
    }

    @Override
    public Double[] getVariance(String columnName) throws DDFException {
        String sqlCmd = String.format("select var_samp(%s) from %s", columnName, ddf.getDDFName());
        ISqlResult result = ddf.sql(sqlCmd);
        if (result.next()) {
            Double tmp = result.getDouble(0);
            Double[] variance = new Double[2];
            variance[0] = tmp;
            variance[1] = Math.sqrt(tmp);
            return variance;
        } else {
            throw new DDFException("Unable to get sql result cmd: " + sqlCmd);
        }
    }

    @Override
    public Double getMean(String columnName) throws DDFException {
        String sqlCmd = String.format("select avg(%s) from %s", columnName, ddf.getDDFName());
        ISqlResult sqlResult = ddf.sql(sqlCmd);
        if (sqlResult.next()) {
            return sqlResult.getDouble(0);
        } else {
            throw new DDFException("Unable to get sql result cmd: " + sqlCmd);
        }
    }

    @Override
    public Double getCor(String xColumnName, String yColumnName) throws DDFException {
        String sqlCmd = String.format("select corr(%s, %s) from %s", xColumnName, yColumnName, ddf.getDDFName());
        ISqlResult sqlResult = ddf.sql(sqlCmd);
        if (sqlResult.next()) {
            return sqlResult.getDouble(0);
        } else {
            throw new DDFException("Unable to get sql result cmd: " + sqlCmd);
        }
    }

    @Override
    public Double getCovariance(String xColumnName, String yColumnName) throws DDFException {
        String sqlCmd = String.format("select covar_samp(%s, %s) from %s", xColumnName, yColumnName, ddf.getDDFName());
        ISqlResult sqlResult = ddf.sql(sqlCmd);
        if (sqlResult.next()) {
            return sqlResult.getDouble(0);
        } else {
            throw new DDFException("Unable to get sql result cmd: " + sqlCmd);
        }
    }

    @Override
    public Double getMin(String columnName) throws DDFException {
        String sqlCmd = String.format("select min(%s) from %s", columnName, ddf.getDDFName());
        ISqlResult sqlResult = ddf.sql(sqlCmd);
        if (sqlResult.next()) {
            return sqlResult.getDouble(0);
        } else {
            throw new DDFException("Unable to get sql result cmd: " + sqlCmd);
        }
    }

    @Override
    public Double getMax(String columnName) throws DDFException {
        String sqlCmd = String.format("select max(%s) from %s", columnName, ddf.getDDFName());
        ISqlResult sqlResult = ddf.sql(sqlCmd);
        if (sqlResult.next()) {
            return sqlResult.getDouble(0);
        } else {
            throw new DDFException("Unable to get sql result cmd: " + sqlCmd);
        }
    }

    @Override
    public IDDF getDDF() {
        return ddf;
    }
}

