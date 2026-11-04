/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NamedParameterStatement implements AutoCloseable {
    private final PreparedStatement statement;
    private Map<String, int[]> indexMap;

    public NamedParameterStatement(Connection connection, String query) throws SQLException {
        String parsedQuery = this.parse(query);
        this.statement = connection.prepareStatement(parsedQuery);
    }

    final String parse(String query) {
        int length = query.length();
        StringBuffer parsedQuery = new StringBuffer(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int index = 1;
        HashMap<String, List<Integer>> indexes = new HashMap(10);

        for(int i = 0; i < length; ++i) {
            char c = query.charAt(i);
            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false;
                }
            } else if (inDoubleQuote) {
                if (c == '"') {
                    inDoubleQuote = false;
                }
            } else if (c == '\'') {
                inSingleQuote = true;
            } else if (c == '"') {
                inDoubleQuote = true;
            } else if (c == ':' && i + 1 < length && Character.isJavaIdentifierStart(query.charAt(i + 1))) {
                int j;
                for(j = i + 2; j < length && Character.isJavaIdentifierPart(query.charAt(j)); ++j) {
                }

                String name = query.substring(i + 1, j);
                c = '?';
                i += name.length();
                List<Integer> indexList = (List)indexes.get(name);
                if (indexList == null) {
                    indexList = new LinkedList();
                    indexes.put(name, indexList);
                }

                ((List)indexList).add(index);
                ++index;
            }

            parsedQuery.append(c);
        }

        this.indexMap = new HashMap(indexes.size());
        Iterator var15 = indexes.entrySet().iterator();

        while(var15.hasNext()) {
            Map.Entry<String, List<Integer>> entry = (Map.Entry)var15.next();
            List<Integer> list = (List)entry.getValue();
            int[] intIndexes = new int[list.size()];
            int i = 0;

            Integer x;
            for(Iterator var13 = list.iterator(); var13.hasNext(); intIndexes[i++] = x) {
                x = (Integer)var13.next();
            }

            this.indexMap.put(entry.getKey(), intIndexes);
        }

        return parsedQuery.toString();
    }

    private int[] getIndexes(String name) {
        int[] indexes = (int[])this.indexMap.get(name);
        if (indexes == null) {
            throw new IllegalArgumentException("Parameter not found: " + name);
        } else {
            return indexes;
        }
    }

    public void setObject(String name, Object value) throws SQLException {
        int[] indexes = this.getIndexes(name);

        for(int i = 0; i < indexes.length; ++i) {
            this.statement.setObject(indexes[i], value);
        }

    }

    public void setString(String name, String value) throws SQLException {
        int[] indexes = this.getIndexes(name);

        for(int i = 0; i < indexes.length; ++i) {
            this.statement.setString(indexes[i], value);
        }

    }

    public void setInt(String name, int value) throws SQLException {
        int[] indexes = this.getIndexes(name);

        for(int i = 0; i < indexes.length; ++i) {
            this.statement.setInt(indexes[i], value);
        }

    }

    public void setLong(String name, long value) throws SQLException {
        int[] indexes = this.getIndexes(name);

        for(int i = 0; i < indexes.length; ++i) {
            this.statement.setLong(indexes[i], value);
        }

    }

    public void setTimestamp(String name, Timestamp value) throws SQLException {
        int[] indexes = this.getIndexes(name);

        for(int i = 0; i < indexes.length; ++i) {
            this.statement.setTimestamp(indexes[i], value);
        }

    }

    public PreparedStatement getStatement() {
        return this.statement;
    }

    public boolean execute() throws SQLException {
        return this.statement.execute();
    }

    public ResultSet executeQuery() throws SQLException {
        return this.statement.executeQuery();
    }

    public int executeUpdate() throws SQLException {
        return this.statement.executeUpdate();
    }

    public void close() throws SQLException {
        this.statement.close();
    }

    public void addBatch() throws SQLException {
        this.statement.addBatch();
    }

    public int[] executeBatch() throws SQLException {
        return this.statement.executeBatch();
    }
}
