class QueryBuilder {
    private lateinit var _sb: StringBuilder
    private lateinit var _options: QueryOptions
    private lateinit var _query: Query
    private var _tagCounter: Int = 0

    fun build(options: QueryOptions): Query {
        validateOptions(options)

        initBuild(options)

        addSelect()
        addWheres()

        _query.commandText = _sb.toString()
        return _query
    }

    private fun initBuild(options: QueryOptions) {
        _query = Query()
        _sb = StringBuilder()
        _options = options
        _tagCounter = 0
    }

    private fun addWheres() {
        if (_options.wheres.none()) {
            return
        }
        _sb.append(" WHERE ")
        _options.wheres.forEach { addWhereCondition(it) }
    }

    private fun addWhereCondition(where: Where) {
        _sb.append("[${_options.tableName}].[${where.column}] ${getComparator(where.comparator)} ")
        val conditions = castConditions(where)
        val params = getParams(conditions)

        appendWhereStatement(params, where)

        params.zip(conditions).forEach {
            _query.parameters.add(QueryParameter(it.first, it.second))
        }

        if (where != _options.wheres.last()) {
            _sb.append(" AND ")
        }
    }

    private fun appendWhereStatement(params: Iterable<String>, where: Where) {
        val wheres = params.joinToString(separator = ", ") { "@$it" }
        if (where.comparator == WhereComparator.In) {
            _sb.append("($wheres)")
        } else {
            _sb.append(wheres)
        }
    }

    private fun getParams(conditions: Iterable<Any>): Iterable<String> = conditions.map {
        val arg = "arg$_tagCounter"
        _tagCounter += 1
        arg
    }

    private fun castConditions(where: Where): Iterable<Any> = if (where.comparator == WhereComparator.In) {
        where.condition as Iterable<Any>
    } else {
        listOf(where.condition)
    }

    private fun getComparator(comparator: WhereComparator): String = when (comparator) {
        WhereComparator.Equals -> "="
        WhereComparator.In -> "IN"
        else -> throw NotImplementedError("Where comparator not implemented")
    }

    private fun validateOptions(options: QueryOptions) {
        if (options.tableName.isBlank()) {
            throw Error("table name must be set")
        }

        val hasInvalidConditionType = { w: Where -> w.comparator == WhereComparator.In && w.condition !is Iterable<*> }
        if (options.wheres.any(hasInvalidConditionType)) {
            throw Error("When using In Comparator condition must be of type Iterable<>")
        }
    }

    private fun addSelect() {
        _sb.append("SELECT ")

        if (_options.columns.any()) {
            appendColumns()
        } else {
            _sb.append("*")
        }

        _sb.append(" FROM [${_options.tableName}]")
    }

    private fun appendColumns() {
        _options.columns.forEach {
            _sb.append("[$it]")
            if (_options.columns.last() != it) {
                _sb.append(", ")
            }
        }
    }

}