package com.brilliancesoft.mushaf.framework.database.helpers

import androidx.sqlite.db.SupportSQLiteDatabase


    /**
     * Helper for altering tables. Handles creating a temporary table with the desired fields,
     * and filling it with values from the old table and then dropping the old table and renaming the new one.
     *
     *  Supported operations: Add, Delete, Rename or Change Scheme of a column.
     *
     *  To Retain an existing column, it has to be defined in [columns] otherwise it will be Deleted.
     *
     * To Add a new column or Retain an existing column, use one of:
     *  - `"foo INTEGER".toNothing() // Add/Retain column with null value (or default value if defined in schema).`
     *  - `"foo INTEGER".toExisting() // Retain column with its existing value from previous version of this table.`
     *  - `"foo INTEGER".toExisting("bar") // Add/Retain column with a value from another existing column.`
     *  - `"foo INTEGER".toExisting("COALESCE(bar, baz)") // Add/Retain column with a value from the first non-null column in the COALESCE statement.`
     *
     * To Delete a column, omit it from the [columns] map.
     *
     * To Rename a column, use: `"foo INTEGER".toExisting("bar")`, which will map the value of `bar` column to the `foo` column.
     *
     * To Change Scheme of a column, specify the new scheme in the [columns]' key, e.g:
     *  - `"foo TEXT NOT NULL".toExisting("bar")
     */

    fun deleteTable(db: SupportSQLiteDatabase,tableName: String){
        db.execSQL("DROP TABLE IF EXISTS $tableName" )
    }

    fun createTable(db: SupportSQLiteDatabase,tableName: String,columns: Map<String, String?>, primaryKeys: List<String>) {
        val createSql = "CREATE TABLE IF NOT EXISTS $tableName"
        val tableColumns = columns.map { it.key }.joinToString() + ", PRIMARY KEY(${primaryKeys.joinToString()})"
        db.execSQL("$createSql($tableColumns)")
    }

    fun updateTable(db: SupportSQLiteDatabase, tableName: String, columns: Map<String, String?>, primaryKeys: List<String>) {
        db.execSQL(
            "CREATE TABLE ${tableName}_temp (" + columns.map { it.key }.joinToString() + ", PRIMARY KEY(${primaryKeys.joinToString()}))"
        )

        /** Filters only columns that want to mapped to another column. */
        val columnsWithMapping = columns.filterValues { it != null }

        db.execSQL(
            "INSERT INTO ${tableName}_temp (" + columnsWithMapping.map { it.key.substringBefore(' ') }.joinToString() + ") " +
                    "SELECT " + columnsWithMapping.map { it.value }.joinToString() + " FROM $tableName"
        )

        db.execSQL("DROP TABLE $tableName")
        db.execSQL("ALTER TABLE ${tableName}_temp RENAME TO $tableName")
    }
    
    /**
     * Indicates that this column should copy the value from an existing column of this table.
     * If [column] is null, the value will be copied from [this] column.
     * If a [column] is specified, value will be copied from it.
     */
    fun String.toExisting(column: String? = null): Pair<String, String> =
        Pair(this, column ?: this.substringBefore(' '))

    /**
     * Creates a pairing to a null value, indicating there is no previous column to copy a value from.
     * Used when adding new columns.
     * */
    private fun String.toNothing(): Pair<String, Nothing?> = Pair(this, null)


