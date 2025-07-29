# RDBMS Vendor Strategy Testing Guide

This directory contains comprehensive test suites for RDBMS vendor-specific strategies in the QQQ framework. These tests ensure that each database vendor's unique requirements are properly handled.

## Test Structure

### Unit Tests (No Database Required)
- **Identifier Escaping**: Tests vendor-specific identifier quoting syntax
- **SQL Generation**: Tests SQL clause generation patterns
- **Parameter Binding**: Tests type conversion and binding logic
- **Framework Integration**: Tests interface compliance and strategy selection
- **Edge Cases**: Tests error conditions and boundary cases

### Integration Tests (Database Required)
- **Actual SQL Execution**: Tests real database operations
- **Generated Key Retrieval**: Tests auto-generated primary key handling
- **Performance Testing**: Tests query execution performance
- **Data Type Validation**: Tests actual data type handling

## Usage Instructions

### For New RDBMS Vendors

1. **Copy the Template**: Use `RDBMSVendorStrategyTestTemplate.java` as a starting point
2. **Customize for Your Vendor**: Update expected SQL patterns and vendor-specific logic
3. **Implement Required Methods**: Override methods for vendor-specific functionality
4. **Add Vendor-Specific Tests**: Include tests for unique vendor features
5. **Update Documentation**: Document vendor-specific requirements and behaviors

### Key Areas to Test

#### 1. Identifier Escaping
Each vendor has different rules for quoting identifiers:
- **PostgreSQL**: Double quotes (`"identifier"`)
- **MySQL**: Backticks (`` `identifier` ``)
- **Oracle**: Double quotes or no quotes for simple names
- **SQL Server**: Square brackets (`[identifier]`) or double quotes

#### 2. SQL Syntax
Test vendor-specific SQL dialects and features:
- **IN Clause Handling**: Some vendors require special handling for null values
- **Type Casting**: Vendor-specific type conversion requirements
- **Function Names**: Different function names and syntax
- **Limit/Offset**: Different pagination syntax

#### 3. Parameter Binding
Test type handling and binding requirements:
- **Type Conversions**: String-to-integer conversions for strict databases
- **Null Handling**: Vendor-specific null value handling
- **Date/Time Types**: Different temporal type requirements
- **Large Object Types**: BLOB/CLOB handling differences

#### 4. Generated Keys
Test auto-generated primary key retrieval:
- **PostgreSQL**: Uses `RETURNING` clause
- **MySQL**: Uses `getGeneratedKeys()` method
- **Oracle**: Uses `RETURNING` clause or sequences
- **SQL Server**: Uses `OUTPUT` clause or `SCOPE_IDENTITY()`

## PostgreSQL-Specific Considerations

### Type Checking Issues
PostgreSQL is much stricter about type checking than other databases. Common issues include:

#### 1. String-to-Integer Conversion
**Problem**: `ERROR: operator does not exist: bigint = character varying`

**Cause**: HTTP request parameters come as strings (e.g., `"6"`) but PostgreSQL columns are typed (e.g., `BIGINT`).

**Solution**: Enhanced `bindParamObject()` method in `PostgreSQLRDBMSActionStrategy`:
```java
if(value instanceof String) {
   String stringValue = (String) value;
   try {
      Long longValue = Long.parseLong(stringValue);
      statement.setLong(index, longValue);
      return 1;
   } catch(NumberFormatException e) {
      statement.setString(index, stringValue);
      return 1;
   }
}
```

#### 2. Instant/Timestamp Binding
**Problem**: `Can't infer the SQL type to use for an instance of java.time.Instant`

**Solution**: Explicit type specification:
```java
if(value instanceof Instant) {
   Timestamp timestamp = Timestamp.from((Instant) value);
   statement.setObject(index, timestamp, java.sql.Types.TIMESTAMP);
   return 1;
}
```

#### 3. IN Clauses with Null Values
**Problem**: PostgreSQL strict type checking in IN clauses with null values.

**Solution**: Special handling in `appendCriterionToWhereClause()`:
```java
if(criterion.getOperator() == QCriteriaOperator.IN && values.contains(null)) {
   // Generate: (column IN (values) OR column IS NULL)
   // or: column IS NULL (if all values are null)
}
```

#### 4. INSERT SQL Generation with Hardcoded Backticks
**Problem**: `ERROR: syntax error at or near "`"` - INSERT statements using hardcoded backticks instead of PostgreSQL double quotes.

**Cause**: The `RDBMSInsertAction` was hardcoding backticks for column names instead of using the strategy's `escapeIdentifier` method.

**Solution**: Updated `RDBMSInsertAction.java` to use the strategy's identifier escaping:
```java
// Before (hardcoded backticks):
String columns = insertableFields.stream()
   .map(f -> "`" + getColumnName(f) + "`")
   .collect(Collectors.joining(", "));

// After (uses strategy's escapeIdentifier):
String columns = insertableFields.stream()
   .map(f -> escapeIdentifier(getColumnName(f)))
   .collect(Collectors.joining(", "));
```

## Testing Best Practices

### 1. Database Independence
- **Unit Tests**: Should not require database connections
- **Integration Tests**: Should be clearly separated and optional
- **Mocking**: Use mocks for database-dependent operations in unit tests

### 2. Comprehensive Coverage
- **Happy Path**: Test normal operation scenarios
- **Edge Cases**: Test boundary conditions and error cases
- **Type Safety**: Test type conversion and validation
- **Performance**: Test query generation efficiency

### 3. Maintenance
- **Documentation**: Keep tests well-documented
- **Vendor Notes**: Document vendor-specific requirements
- **Template Updates**: Keep the template current with best practices
- **Regression Testing**: Ensure changes don't break existing functionality

## Common Patterns

### Strategy Selection Testing
```java
@Test
void testStrategySelection() {
   RDBMSBackendMetaData backend = new RDBMSBackendMetaData()
      .withVendor("postgresql");
   
   RDBMSActionStrategyInterface strategy = backend.getActionStrategy();
   assertTrue(strategy instanceof PostgreSQLRDBMSActionStrategy);
}
```

### Identifier Escaping Testing
```java
@Test
void testIdentifierEscaping() {
   String escaped = strategy.escapeIdentifier("table_name");
   assertEquals("\"table_name\"", escaped); // PostgreSQL
   // assertEquals("`table_name`", escaped); // MySQL
}
```

### SQL Generation Testing
```java
@Test
void testSQLGeneration() {
   QFilterCriteria criterion = new QFilterCriteria()
      .withOperator(QCriteriaOperator.EQUALS)
      .withValues(List.of("value"));
   
   StringBuilder clause = new StringBuilder();
   List<Serializable> values = new ArrayList<>();
   
   Integer paramCount = strategy.appendCriterionToWhereClause(
      criterion, clause, "\"column\"", values, field);
   
   assertEquals("\"column\" = ?", clause.toString());
   assertEquals(1, paramCount);
}
```

## Troubleshooting

### Common Issues

1. **Type Mismatch Errors**: Ensure proper type conversion in parameter binding
2. **SQL Syntax Errors**: Verify identifier escaping and SQL generation
3. **Null Handling Issues**: Test null value scenarios thoroughly
4. **Performance Problems**: Monitor query generation and execution times

### Debugging Tips

1. **Enable SQL Logging**: Log generated SQL for analysis
2. **Test Incrementally**: Test one feature at a time
3. **Use Database Tools**: Use vendor-specific tools for SQL validation
4. **Check Documentation**: Refer to vendor documentation for specific requirements

## Future Enhancements

### Planned Improvements
- **Automated Testing**: CI/CD integration for vendor testing
- **Performance Benchmarks**: Standardized performance testing
- **Vendor Comparison**: Automated comparison of vendor implementations
- **Documentation Generation**: Auto-generated vendor-specific documentation

### Contributing
When adding support for new RDBMS vendors:
1. Follow the established patterns
2. Include comprehensive tests
3. Document vendor-specific requirements
4. Update this guide with new information
5. Ensure backward compatibility 