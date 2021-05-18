package io.github.imsejin.mybatis.pagination.support.rebuilder;

import io.github.imsejin.mybatis.pagination.constant.MapperParameterType;
import io.github.imsejin.mybatis.pagination.constant.RebuildMode;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.Configuration;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @see RebuildMode#WRAP
 */
public class MappedStatementRebuilder implements Rebuilder<MappedStatement> {

    private final MappedStatement ms;
    private final RebuildMode rebuildMode;
    private final MapperParameterType mapperParameterType;
    private SqlSource sqlSource;
    private String suffix;
    private ParameterMap parameterMap;
    private List<ResultMap> resultMaps;

    MappedStatementRebuilder(MappedStatement ms, RebuildMode rebuildMode) {
        this.ms = ms;
        this.rebuildMode = rebuildMode;
        this.mapperParameterType = MapperParameterType.from(ms);
    }

    public MappedStatementRebuilder sqlSource(SqlSource sqlSource) {
        this.sqlSource = sqlSource;
        return this;
    }

    public MappedStatementRebuilder suffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    /**
     * Sets parameter map.
     *
     * @return this rebuilder
     */
    public MappedStatementRebuilder parameterMap(ParameterMap parameterMap) {
        if (rebuildMode != RebuildMode.COPY) {
            throw new IllegalArgumentException("This operation only permitted with RebuildType.COPY");
        }

        this.parameterMap = parameterMap;
        return this;
    }

    public MappedStatementRebuilder resultMaps(List<ResultMap> resultMaps) {
        this.resultMaps = resultMaps;
        return this;
    }

    @Override
    public MappedStatement rebuild() {
        String id = this.ms.getId();
        if (this.suffix != null) {
            id += '$' + this.suffix;
        } else {
            String randomValue = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            id += "$MyBatis$" + randomValue;
        }

        // Sets options from original mapped statement.
        if (this.sqlSource == null) this.sqlSource = ms.getSqlSource();
        if (this.parameterMap == null) this.parameterMap = ms.getParameterMap();
        if (this.resultMaps == null) this.resultMaps = ms.getResultMaps();

        // Converts the parameter type of mapper method to "java.util.Map".
        if (this.rebuildMode == RebuildMode.WRAP && this.mapperParameterType == MapperParameterType.SINGLE) {
            Configuration config = this.ms.getConfiguration();
            List<ParameterMapping> parameterMappings = this.ms.getParameterMap().getParameterMappings();
            // ParameterMap's id must be equal to its MappedStatement's id.
            this.parameterMap = new ParameterMap.Builder(config, id, Map.class, parameterMappings).build();
        }

        return new MappedStatement.Builder(ms.getConfiguration(), id, this.sqlSource, ms.getSqlCommandType())
                .resource(ms.getResource())
                .parameterMap(this.parameterMap)
                .resultMaps(this.resultMaps)
                .fetchSize(ms.getFetchSize())
                .timeout(ms.getTimeout())
                .statementType(ms.getStatementType())
                .resultSetType(ms.getResultSetType())
                .cache(ms.getCache())
                .flushCacheRequired(ms.isFlushCacheRequired())
                .useCache(true)
                .resultOrdered(ms.isResultOrdered())
                .keyGenerator(ms.getKeyGenerator())
                .keyColumn(ms.getKeyColumns() == null ? null : String.join(",", ms.getKeyColumns()))
                .keyProperty(ms.getKeyProperties() == null ? null : String.join(",", ms.getKeyProperties()))
                .databaseId(ms.getDatabaseId())
                .lang(ms.getLang())
                .resultSets(ms.getResultSets() == null ? null : String.join(",", ms.getResultSets()))
                .build();
    }

}
