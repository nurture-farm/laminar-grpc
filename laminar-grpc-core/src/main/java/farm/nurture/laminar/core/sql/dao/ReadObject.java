/*
 *  Copyright 2022 Nurture.Farm
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package farm.nurture.laminar.core.sql.dao;

import static farm.nurture.laminar.core.sql.dao.Constants.RS_NOT_INITIALIZED_MSG;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReadObject<T> extends ReadBase<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ReadObject.class);

    private Class<T> classToFill = null;
    private List<T> records = null;

    public ReadObject(final Class<T> classToFill) {
        this.classToFill = classToFill;
    }

    public ReadObject(final Class<T> classToFill, List<T> records) {
        this.classToFill = classToFill;
        this.records = records;
    }

    private static final Object getObject(final int type, final ResultSet rs, final int colNumber)
        throws SQLException {

        switch (type) {
            case java.sql.Types.VARCHAR:
                return rs.getString(colNumber);

            case java.sql.Types.CHAR:
                return rs.getString(colNumber);

            case java.sql.Types.INTEGER:
                int inte = rs.getInt(colNumber);
                return new Integer(inte);

            case java.sql.Types.DATE:
                return rs.getDate(colNumber);

            case java.sql.Types.TIME:
                return rs.getTime(colNumber);

            case java.sql.Types.DECIMAL:
                return rs.getBigDecimal(colNumber);

            case java.sql.Types.DOUBLE:
                double d = rs.getDouble(colNumber);
                return new Double(d);

            case java.sql.Types.FLOAT:
                float f = rs.getFloat(colNumber);
                return new Float(f);

            case java.sql.Types.NUMERIC:
                return rs.getBigDecimal(colNumber);

            case java.sql.Types.SMALLINT:
                short sh = rs.getShort(colNumber);
                return new Short(sh);

            case java.sql.Types.BIGINT:
                long bigint = rs.getLong(colNumber);
                return new Long(bigint);

            case java.sql.Types.TIMESTAMP:
                return rs.getTimestamp(colNumber);

            case java.sql.Types.DISTINCT:
                return rs.getObject(colNumber);

            case java.sql.Types.JAVA_OBJECT:
                return rs.getObject(colNumber);

            case java.sql.Types.LONGVARBINARY:
                return rs.getBytes(colNumber);

            case java.sql.Types.LONGVARCHAR:
                return rs.getString(colNumber);

            case java.sql.Types.LONGNVARCHAR:
                return rs.getString(colNumber);

            case java.sql.Types.NULL:
                return null;

            case java.sql.Types.OTHER:
                return rs.getObject(colNumber);

            case java.sql.Types.REAL:
                float real = rs.getFloat(colNumber);
                return new Float(real);

            case java.sql.Types.REF:
                return rs.getRef(colNumber);

            case java.sql.Types.STRUCT:
                return rs.getObject(colNumber);

            case java.sql.Types.TINYINT:
                byte b = rs.getByte(colNumber);
                return new Byte(b);

            case java.sql.Types.VARBINARY:
                return rs.getBytes(colNumber);

            case java.sql.Types.ARRAY:
                return rs.getArray(colNumber);

            case java.sql.Types.BINARY:
                return rs.getBytes(colNumber);

            case java.sql.Types.BIT:
                boolean bit = rs.getBoolean(colNumber);
                return new Boolean(bit);

            case java.sql.Types.BLOB:
                return rs.getBlob(colNumber);

            case java.sql.Types.CLOB:
                return rs.getClob(colNumber);

            default:
                return null;
        }
    }

    @Override
    protected final List<T> populate() throws SQLException {
        if (null == records) records = new ArrayList<>(256);
        getOneOrManyRows(records);
        return records;
    }

    @Override
    protected final T getFirstRow() throws SQLException {
        return getOneOrManyRows(null);
    }

    /**
     * Generic Method
     *
     * @param records Populates all found row to this records list
     * @return If records object is null, it returns a single record
     * @throws SQLException
     */
    private final T getOneOrManyRows(final List<T> records) throws SQLException {

        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        /** Get all the columns from the result set. */
        ResultSetMetaData md = rs.getMetaData();
        int totalCol = md.getColumnCount();
        String[] cols = new String[totalCol];
        int[] dataTypes = new int[totalCol];
        getColFromResultSet(md, totalCol, cols, dataTypes);

        /** Get all the public fields form the class. */
        Field[] fields = classToFill.getFields();
        int fieldsT = fields.length;
        List<Field> publicFields = new ArrayList<>(fieldsT);
        getAllPublicFields(fields, fieldsT, publicFields);
        fieldsT = publicFields.size();

        /** Get all the public setters form the class. */
        Method[] methods = classToFill.getMethods();
        int methodsT = methods.length;
        List<Method> publicMethods = new ArrayList<>(methodsT);
        getAllPublicGetters(methods, methodsT, publicMethods);
        methodsT = publicMethods.size();

        /** Get all the set matching fields as column name. */
        Field[] arrangedFields = new Field[totalCol];
        Method[] arrangedMethods = new Method[totalCol];
        boolean fieldFound = false;
        getAllSetMatchingFields(
            totalCol,
            cols,
            fieldsT,
            publicFields,
            methodsT,
            publicMethods,
            arrangedFields,
            arrangedMethods);

        Object[] colValArray = new Object[1];
        return processRecord(
            records, totalCol, dataTypes, arrangedFields, arrangedMethods, colValArray);
    }

    private void getAllSetMatchingFields(
        int totalCol,
        String[] cols,
        int fieldsT,
        List<Field> publicFields,
        int methodsT,
        List<Method> publicMethods,
        Field[] arrangedFields,
        Method[] arrangedMethods) {
        for (int i = 0; i < totalCol; i++) {
            getAllSetMatchingFields(
                cols, fieldsT, publicFields, methodsT, publicMethods, arrangedFields, arrangedMethods, i);
        }
    }

    private void getAllPublicGetters(Method[] methods, int methodsT, List<Method> publicMethods) {
        for (int j = 0; j < methodsT; j++) {
            Method aMethod = methods[j];
            if (Modifier.isPublic(aMethod.getModifiers()) && (aMethod.getName().startsWith("set")))
                publicMethods.add(aMethod);
        }
    }

    private void getAllPublicFields(Field[] fields, int fieldsT, List<Field> publicFields) {
        for (int j = 0; j < fieldsT; j++) {
            Field aField = fields[j];
            if (Modifier.isPublic(aField.getModifiers())) publicFields.add(aField);
        }
    }

    private void getColFromResultSet(
        ResultSetMetaData md, int totalCol, String[] cols, int[] dataTypes) throws SQLException {
        for (int i = 0; i < totalCol; i++) {
            cols[i] = md.getColumnLabel(i + 1);
            dataTypes[i] = md.getColumnType(i + 1);
        }
    }

    private void getAllSetMatchingFields(
        String[] cols,
        int fieldsT,
        List<Field> publicFields,
        int methodsT,
        List<Method> publicMethods,
        Field[] arrangedFields,
        Method[] arrangedMethods,
        int i) {
        boolean fieldFound;
        String col = cols[i];
        fieldFound = false;
        for (int j = 0; j < fieldsT; j++) {
            if (col.equals(publicFields.get(j).getName())) {
                arrangedFields[i] = publicFields.get(j);
                fieldFound = true;
                break;
            }
        }

        if (fieldFound) return;

        for (int j = 0; j < methodsT; j++) {
            String methodName = "set" + Character.toUpperCase(col.charAt(0)) + col.substring(1);
            if (methodName.equals(publicMethods.get(j).getName())) {
                arrangedMethods[i] = publicMethods.get(j);
                break;
            }
        }
    }

    private T processRecord(
        List<T> records,
        int totalCol,
        int[] dataTypes,
        Field[] arrangedFields,
        Method[] arrangedMethods,
        Object[] colValArray)
        throws SQLException {
        try {

            if (null == records) {
                if (rs.next()) {
                    recordsCount++;
                    return createRecord(totalCol, dataTypes, arrangedFields, arrangedMethods, colValArray);
                }
            } else {
                while (this.rs.next()) {
                    recordsCount++;
                    T valueObject =
                        createRecord(totalCol, dataTypes, arrangedFields, arrangedMethods, colValArray);
                    records.add(valueObject);
                }
            }
            return null;
        } catch (IllegalAccessException
            | InstantiationException
            | InvocationTargetException
            | IllegalArgumentException ex) {
            throw new SQLException(ex);
        }
    }

    private final T createRecord(
        final int totalCol,
        final int[] dataTypes,
        final Field[] arrangedFields,
        final Method[] arrangedMethods,
        final Object[] colValArray)
        throws InstantiationException, IllegalAccessException, SQLException,
        InvocationTargetException {

        T valueObject = classToFill.newInstance();
        Object colVal = null;
        for (int i = 0; i < totalCol; i++) {
            colVal = getObject(dataTypes[i], rs, (i + 1));
            if (null == colVal) continue;
            if (null != arrangedFields[i]) {
                arrangedFields[i].set(valueObject, colVal);
            } else if (null != arrangedMethods[i]) {
                colValArray[0] = colVal;
                arrangedMethods[i].invoke(valueObject, colValArray);
            }
        }
        return valueObject;
    }

    @Override
    protected int getRecordsCount() {
        return recordsCount;
    }
}
