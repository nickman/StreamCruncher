/*
 * StreamCruncher:  Copyright (c) 2006-2008, Ashwin Jayaprakash. All Rights Reserved.
 * Contact:         ashwin {dot} jayaprakash {at} gmail {dot} com
 * Web:             http://www.StreamCruncher.com
 * 
 * This file is part of StreamCruncher.
 * 
 *     StreamCruncher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     StreamCruncher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 * 
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with StreamCruncher. If not, see <http://www.gnu.org/licenses/>.
 */
package streamcruncher.api.artifact;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.helios.enums.Primitive;
import org.helios.helpers.ClassHelper;

import streamcruncher.api.annotations.Streamed;
import streamcruncher.api.annotations.StreamedAttribute;

/*
 * Author: Ashwin Jayaprakash Date: Feb 18, 2006 Time: 12:25:35 PM
 */
/**
 * The column-set definition of the Table of an Input/Output Event Stream.
 */
public class RowSpec implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * {@value}
     */
    public static final String INFO_SEPARATOR = ":";

    /**
     * {@value}
     */
    public static final String INFO_NAME_VALUE_SEPARATOR = "=";

    /**
     * Additional information can be appended to the Java type.
     *
     * @see RowSpec#addInfo(String, streamcruncher.api.artifact.RowSpec.Info,
     *      Object)
     */
    public static enum Info {
        SIZE;
    }

    protected final String[] columnNames;

    /**
     * If this list carries the Java Types, then Strings will have a size at the
     * end so that they can get converted to varchar. Ex:
     * <code>java.lang.String:SIZE=15</code> becomes <code>varchar(15).</code>
     */
    protected final String[] columnNativeTypes;

    protected final int idColumnPosition;

    protected final int timestampColumnPosition;

    protected final int versionColumnPosition;
    
    protected String name;

    // ------------------------

    private final int hashCode;

    private final String str;

    // ------------------------
    
    private static final Map<Class<?>, RowSpec> rowSpecCache = new ConcurrentHashMap<Class<?>, RowSpec>();
    
    /**
     * Builds a new RowSpec for the passed class
     * @param clazz The class to build a RowSpec for
     * @return a RowSpec
     */
    public static RowSpec buildFor(Class<?> clazz) {
    	if(clazz==null) throw new IllegalArgumentException("The passed class was null", new Throwable());
    	RowSpec rs = rowSpecCache.get(clazz);
    	if(rs==null) {
    		synchronized(rowSpecCache) {
    			if(rs==null) {
	    	    	List<String> names = new ArrayList<String>();
	    	    	List<String> types = new ArrayList<String>();
	    	    	int idCol = -1;
	    	    	int tsCol = -1;
	    	    	Streamed streamed = clazz.getAnnotation(Streamed.class);
	    	    	if(streamed==null) {
	    	    		throw new IllegalArgumentException("The class [" + clazz.getName() + "] is not annotated with @Streamed", new Throwable());
	    	    	}
	    	    	String streamName = streamed.name();
	    	    	if("".equals(streamName)) {
	    	    		streamName = clazz.getSimpleName() + "Stream";
	    	    	}    	
	    	    	int cntr = 0;
	    	    	for(Method method: ClassHelper.getAnnotatedMethods(clazz, StreamedAttribute.class)) {
	    	    		StreamedAttribute sa = method.getAnnotation(StreamedAttribute.class);
	    	    		if(method.getParameterTypes().length>0) {
	    	    			throw new RuntimeException("The method [" + method.toGenericString() + "] is not a valid attribute as it has parameters", new Throwable());    			
	    	    		}
	    	    		if(method.getReturnType().equals(void.class)) {
	    	    			throw new RuntimeException("The method [" + method.toGenericString() + "] is not a valid attribute as it has no return type", new Throwable());
	    	    		}
	    	    		String name = sa.name();
	    	    		if("".equals(name)) {
	    	    			name = method.getName();
	    	    			if(name.startsWith("get")) {
	    	    				name = name.replaceFirst("get", "");
	    	    			}
	    	    		}
	    	    		Class<?> type = sa.type();
	    	    		if(type.equals(Void.class)) {
	    	    			if(Primitive.isPrimitive(method.getReturnType())) {
	    	    				type = Primitive.up(method.getReturnType());
	    	    			} else {
	    	    				type = method.getReturnType();
	    	    			}
	    	    			
	    	    		}
	    	    		if(sa.id()) {
	    	    			if(idCol!=-1) {
	    	    				throw new RuntimeException("Detected multiple annotated attributes with id as true including this one [" + method.toGenericString() + "]. Only one id is allowed per class hierarchy", new Throwable());
	    	    			}
	    	    			idCol = cntr;    			
	    	    		}
	    	    		if(sa.timestamp()) {
	    	    			if(tsCol!=-1) {
	    	    				throw new RuntimeException("Detected multiple annotated attributes with timestamp as true including this one [" + method.toGenericString() + "]. Only one timestamp is allowed per class hierarchy", new Throwable());
	    	    			}
	    	    			tsCol = cntr;    			
	    	    		}
	    	    		names.add(name);
	    	    		types.add(type.getName());
	    	    		cntr++;
	    	    	}
	    	    	// Skipping fields for now
	    	    	rs = new RowSpec(names.toArray(new String[names.size()]), types.toArray(new String[types.size()]), idCol, tsCol, -1);
	    	    	rs.name = streamName;
	    	    	rowSpecCache.put(clazz, rs);
    			}
    		}    		
    	}
    	return rs;
    }
    
    
    /**
     * Creates a RowSpec without {@link #idColumnPosition id-column},
     * {@link #timestampColumnPosition timestamp-column} and
     * {@link #versionColumnPosition version-column} i.e with <code>-1</code>
     * as their positions.
     *
     * @param columnNames
     * @param columnNativeTypes
     * @see #RowSpec(String[], String[], int, int, int)
     */
    public RowSpec(String[] columnNames, String[] columnNativeTypes) {
        this(columnNames, columnNativeTypes, -1, -1, -1);
    }

    /**
     * Creates a RowSpec without {@link #versionColumnPosition version-column}
     * i.e with <code>-1</code> as its position.
     *
     * @param columnNames
     * @param columnNativeTypes
     * @param idColumnPosition
     * @param timestampColumnPosition
     * @see #RowSpec(String[], String[], int, int, int)
     */
    public RowSpec(String[] columnNames, String[] columnNativeTypes, int idColumnPosition,
            int timestampColumnPosition) {
        this(columnNames, columnNativeTypes, idColumnPosition, timestampColumnPosition, -1);
    }

    /**
     * <i>Internal use</i>.
     *
     * @param columnNames
     *            The <b>first</b> column <b>must always</b> hold the
     *            monotonically increasing Row-Ids.
     * @param columnNativeTypes
     *            In the same order and number as the names.
     * @param idColumnPosition
     *            Position of the Id column in the name/type array.
     * @param timestampColumnPosition
     *            Position of a Timestamp-type column in the name/type array,
     *            which will be used as a reference for Time based Windows. Use
     *            <code>-1</code> if the Events in this Stream will not be
     *            used for such Windows.
     * @param versionColumnPosition
     */
    public RowSpec(String[] columnNames, String[] columnNativeTypes, int idColumnPosition,
            int timestampColumnPosition, int versionColumnPosition) {
        this.columnNames = columnNames;
        this.columnNativeTypes = columnNativeTypes;
        this.idColumnPosition = idColumnPosition;
        this.timestampColumnPosition = timestampColumnPosition;
        this.versionColumnPosition = versionColumnPosition;

        this.str = Arrays.asList(columnNames).toString() + ", "
                + Arrays.asList(columnNativeTypes).toString() + ", " + idColumnPosition + ", "
                + timestampColumnPosition + ", " + versionColumnPosition;
        this.hashCode = this.str.hashCode();
    }

    /**
     * @return the idColumnPosition
     */
    public int getIdColumnPosition() {
        return idColumnPosition;
    }

    /**
     * @return the timestampColumnPosition
     */
    public int getTimestampColumnPosition() {
        return timestampColumnPosition;
    }

    /**
     * @return the versionColumnPosition
     */
    public int getVersionColumnPosition() {
        return versionColumnPosition;
    }

    /**
     * @return Returns the columnNames.
     */
    public String[] getColumnNames() {
        return columnNames;
    }

    /**
     * @return Returns the columnNativeTypes.
     */
    public String[] getColumnNativeTypes() {
        return columnNativeTypes;
    }

    /**
     * A convenience method to add additional information to the Type.
     *
     * @param basicJavaType
     *            Ex: java.lang.String
     * @param info
     *            Ex: {@link Info#SIZE}
     * @param value
     *            Ex: 15
     * @return The basic Java Type appended with additional information.
     */
    public static String addInfo(String basicJavaType, Info info, Object value) {
        return basicJavaType + INFO_SEPARATOR + info.name() + INFO_NAME_VALUE_SEPARATOR + value;
    }

    // ------------------------

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RowSpec) {
            RowSpec that = (RowSpec) obj;

            String thisStr = toString();
            String thatStr = that.toString();

            return thisStr.equals(thatStr);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return str;
    }


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
