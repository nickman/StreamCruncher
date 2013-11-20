/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2007, Helios Development Group and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
 *
 */
package streamcruncher.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Title: StreamedAttribute</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * @version $LastChangedRevision$
 * <p><code>streamcruncher.api.annotations.StreamedAttribute</code></p>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented

public @interface StreamedAttribute {
	/**
	 * The name of this attribute. The default of <code>""</code> means the name is derived from the field or method name.
	 */
	public String name() default "";
	/**
	 * The type of this attribute. The default of <code>Void</code> means the type is derived from the field or method name.
	 */
	public Class<?> type() default Void.class;
	
	/**
	 * Indicates this field or method is the SC Id for the annotated type. Must be a long or Long.
	 */
	public boolean id() default false;
	
	/**
	 * Indicates this field or method is the SC Timestamp for the annotated type. Must be a long or Long.
	 */
	public boolean timestamp() default false;
	
}
