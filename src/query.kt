data class QueryParameter(
        val tag: String,
        val condition: Any
)

data class Query(
        var commandText: String = "",
        var parameters: ArrayList<QueryParameter> = arrayListOf()
)

enum class WhereComparator { Equals, In }

data class Where(
        val column: String,
        val condition: Any,
        val comparator: WhereComparator
)

data class QueryOptions(
        val tableName: String,
        val columns: Iterable<String> = listOf(),
        val wheres: Iterable<Where> = listOf()
)
