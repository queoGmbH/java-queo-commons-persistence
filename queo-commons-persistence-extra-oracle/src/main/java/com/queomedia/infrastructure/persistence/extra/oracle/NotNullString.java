package com.queomedia.infrastructure.persistence.extra.oracle;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SerializationException;
import org.hibernate.usertype.UserType;

/**
 * Hibernate UserType to convert Oracle Emtpy String handling so something usefull:
 * The Class NotNullString returns an empty String and never null, even if actually NULL is stored in the database. 
 * Necessary to avoid problems with null values when working with oracle database, as oracle db 
 * does not distinguish between empty strings and null.
 *
 * <p>
 * It is intended that {@link NotNullExceptForOracle} and {@link NotNullString} are used together if one want to have
 * a String that <b>can be empty but not null<b> in the Java world. In the oracle DB it become null.
 * usage: 
 * </p>
 * <pre><code>
 *  {@literal @}NotNullExceptForOracle
 *  {@literal @}Type(type = "com.queomedia.infrastructure.persistence.NotNullString")
 *  private String comment;
 * </code></pre>
 * 
 * You can use NullWarning.aj to find the fields that requires this annotation.
 */
public class NotNullString implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[] { Types.VARCHAR };
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class returnedClass() {
        return String.class;
    }

    @Override
    public boolean equals(final Object x, final Object y) throws HibernateException {
        if ((x == y) || ((x != null) && (y != null) && x.equals(y))) {
            return true;
        }
        if (((x == null) || ("".equals(x))) && ((y == null) || ("".equals(y)))) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode(final Object x) throws HibernateException {
        return x.hashCode();
    }
    
    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final SharedSessionContractImplementor session,
            final Object owner) throws HibernateException, SQLException {
        if (rs.getString(names[0]) != null) {
            return rs.getString(names[0]);
        } else {
            return "";
        }

    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index,
            final SharedSessionContractImplementor session) throws HibernateException, SQLException {
        st.setString(index, (String) value);

    }

    @Override
    public Object deepCopy(final Object value) throws HibernateException {
        if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(final Object value) throws HibernateException {
        Object deepCopy = deepCopy(value);

        if (!(deepCopy instanceof Serializable)) {
            throw new SerializationException("DeepCopy of " + value + " is not serializable", null);
        }

        return (Serializable) deepCopy;
    }

    @Override
    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return deepCopy(original);
    }

}
