package com.increff.pos.spring;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class SnakeCaseNamingStrategy implements PhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment context) {
        return convert(name);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment context) {
        return convert(name);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        if (name == null) return null;
        // Remove "Pojo" suffix from class name, if present
        String base = name.getText().replaceAll("Pojo$", "");
        return Identifier.toIdentifier(toSnakeCase(base));
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
        return convert(name);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return convert(name);
    }

    private Identifier convert(Identifier name) {
        if (name == null) return null;
        return Identifier.toIdentifier(toSnakeCase(name.getText()));
    }

    private String toSnakeCase(String input) {
        // Convert camelCase or PascalCase to snake_case
        return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
