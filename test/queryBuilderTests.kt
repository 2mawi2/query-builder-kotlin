import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails


@Suppress("RemoveRedundantBackticks")
class QueryBuilderTests {
    private fun queryBuilder() = QueryBuilder()

    @Test
    fun `build should build select command`() {
        val result = queryBuilder().build(QueryOptions(
                tableName = "table1"
        ))

        assertEquals("SELECT * FROM [table1]", result.commandText)
    }

    @Test
    fun `build should build multiple select commands`() {
        val result = queryBuilder().build(QueryOptions(
                tableName = "table1",
                columns = arrayListOf("column1", "column2")
        ))

        assertEquals("SELECT [column1], [column2] FROM [table1]", result.commandText)
    }

    @Test
    fun `build should validate table name`() {
        assertFails { queryBuilder().build(QueryOptions(tableName = "")) }
        assertFails { queryBuilder().build(QueryOptions(tableName = " ")) }
    }


    private fun Query.assertParam(index: Int, tag: String, value: Any) {
        assertEquals(tag, this.parameters[index].tag)
        assertEquals(value, this.parameters[index].condition)
    }


    @Test
    fun `build should build where statement`() {
        val result = queryBuilder().build(QueryOptions(
                tableName = "table1",
                wheres = listOf(Where(column = "column1", comparator = WhereComparator.Equals, condition = 2))
        ))

        assertEquals("SELECT * FROM [table1] WHERE [table1].[column1] = @arg0", result.commandText)
        result.assertParam(0, "arg0", 2)
    }

    @Test
    fun `build should build multiple where statements`() {
        val result = queryBuilder().build(QueryOptions(
                tableName = "table1",
                wheres = listOf(
                        Where(column = "column1", comparator = WhereComparator.Equals, condition = 2),
                        Where(column = "column2", comparator = WhereComparator.Equals, condition = 1)
                )
        ))

        assertEquals("SELECT * FROM [table1] WHERE [table1].[column1] = @arg0 AND [table1].[column2] = @arg1", result.commandText)
        result.assertParam(0, "arg0", 2)
        result.assertParam(1, "arg1", 1)
    }

    @Test
    fun `build should build where statement with in comparator`() {
        val result = queryBuilder().build(QueryOptions(
                tableName = "table1",
                wheres = listOf(
                        Where(column = "column1", comparator = WhereComparator.In, condition = listOf(0, 1))
                )
        ))

        assertEquals("SELECT * FROM [table1] WHERE [table1].[column1] IN (@arg0, @arg1)", result.commandText)
        result.assertParam(0, "arg0", 0)
        result.assertParam(1, "arg1", 1)
    }

    @Test
    fun `build should validate where condition`() {
        assertFails {
            queryBuilder().build(QueryOptions(
                    tableName = "table1",
                    wheres = listOf(
                            Where(column = "column1", comparator = WhereComparator.In, condition = 1)
                    )
            ))
        }
    }
}