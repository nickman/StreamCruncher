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
package streamcruncher.test;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import streamcruncher.api.InputSession;
import streamcruncher.api.OutputSession;
import streamcruncher.api.ParsedQuery;
import streamcruncher.api.ParserParameters;
import streamcruncher.api.QueryConfig;
import streamcruncher.api.StreamCruncher;
import streamcruncher.api.QueryConfig.QuerySchedulePolicy;
import streamcruncher.api.annotations.Streamed;
import streamcruncher.api.annotations.StreamedAttribute;
import streamcruncher.api.artifact.RowSpec;

/**
 * <p>Title: ExampleRun</p>
 * <p>Description: Test run</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * @version $LastChangedRevision$
 * <p><code>streamcruncher.test.ExampleRun</code></p>
 */

public class ExampleRun extends Thread {
	protected final StreamCruncher cruncher = new StreamCruncher();
	protected final String prop;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File dir = new File("./.artifacts");
		if(dir.exists() && dir.isDirectory()) {
			for(File f: dir.listFiles()) {
				if(f.delete()) {
					log("Deleted file [" + f + "]");
				}
			}
		}
		log("ExampleRun");
		ExampleRun er = new ExampleRun("./src/test/resources/db-configs/sc_config_h2.properties");
		er.start();
		try { er.join(); } catch (Exception e) {}
		log("Shutting Down ....");


	}
	
	public ExampleRun(String prop)  {
		this.prop = prop;
		this.setDaemon(true);
	}
	
	public void run() {
		try {
			cruncher.start(prop);
			
			RowSpec rowSpec = RowSpec.buildFor(HeapUsed.class);
			
			final String inputStreamName = "HeapUsageStream";
			final String queryName = "heapStream";
			try {
				cruncher.unregisterQuery(queryName);
				cruncher.unregisterInStream(inputStreamName); 
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
			cruncher.registerInStream(inputStreamName, rowSpec);
			
			log("RowSpec:\n" + rowSpec);
			String rql = "select type, avg from HeapUsageStream " + 
	              " (partition by type store latest 15 seconds with Average(HeapUsed) as avg) " + 
	              " as HeapStr where HeapStr.$row_status is new;";
			
			//String rql = "select item_sku, item_qty, order_time,order_id from test (partition store last 5 seconds) as mystream	where mystream.$row_status is new;";
			
			
			
			String[] resultColumnTypes = {String.class.getName(), Long.class.getName()}; //, Long.class.getName()};
			
			ParserParameters parameters = new ParserParameters();
			parameters.setQuery(rql);
			parameters.setQueryName(queryName);
			parameters.setResultColumnTypes(resultColumnTypes);
			ParsedQuery parsedQuery = cruncher.parseQuery(parameters);
			QueryConfig config = parsedQuery.getQueryConfig();
			config.setQuerySchedulePolicy(new QueryConfig.QuerySchedulePolicyValue(
			                                  QuerySchedulePolicy.ATLEAST_OR_SOONER,
			                                  5000));

			cruncher.registerQuery(parsedQuery);

			
//			Connection conn = null;
//			PreparedStatement ps = null;
//			ResultSet rset = null;
//			try {
//				DatabaseInterface di = Registry.getImplFor(DatabaseInterface.class);
//				conn = di.createConnection();
//				ps = conn.prepareStatement(rql);
//				rset = ps.executeQuery();
//			} finally {
//				try { rset.close(); } catch (Exception e) {}
//				try { ps.close(); } catch (Exception e) {}
//				try { conn.close(); } catch (Exception e) {}
//			}
			
			log("Cruncher Started");
			
			
			Thread inputThread = new Thread("InputThread"){
				HeapUsed heapUsed = new HeapUsed(true);
				HeapUsed nonHeapUsed = new HeapUsed(true);
				public void run() {
					log("Starting...");
					InputSession inputSession = null;
					try {
						inputSession = cruncher.createInputSession("HeapStream");
						inputSession.start();
					} catch (Exception e) {
						e.printStackTrace(System.err);
					}
					int cnt = 0;
					while(true) {
						heapUsed.update();
						nonHeapUsed.update();
						Object[] event = new Object[]{heapUsed.getHeapUsed(), heapUsed.getTimestamp(), heapUsed.getType(), heapUsed.getId()};
						
						Object[] event2 = new Object[]{nonHeapUsed.getHeapUsed(), nonHeapUsed.getTimestamp(), nonHeapUsed.getType(), nonHeapUsed.getId()};
						inputSession.submitEvent(event);
						inputSession.submitEvent(event2);
						cnt++;
						if(cnt%10==0) {
							log("Inserted 10 Events");
						}
						try { Thread.sleep(2000); } catch (Exception e) {}
					}
				}
				
			};
			inputThread.setDaemon(true);
			inputThread.start();
			Thread outputThread = new Thread("OutputThread"){
				
				public void run() {			
					log("Starting...");
					OutputSession outputSession = null;
					try {
						outputSession = cruncher.createOutputSession(queryName);
						outputSession.start();
					} catch (Exception e) {
						e.printStackTrace(System.err);
					}
					for(;;) {
						try {
							List<Object[]> events = outputSession.readEvents(15, TimeUnit.SECONDS);
						    if (events.size() == 0) {
						        log("Empty Event Result");
						    } else {
						    	for(Object[] event: events) {
						    		log("EVENT:" + Arrays.toString(event));
						    	}
						    }
						} catch (Exception e) {
							e.printStackTrace(System.err);
						}
					}
				}
				
			};
			outputThread.setDaemon(true);
			outputThread.start();
			
			cruncher.keepRunning();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	public static void log(Object msg) {
		System.out.println(String.format("%s - [%s]:%s", new Date(), Thread.currentThread().toString(), msg));
	}

}

@Streamed
class HeapUsed {
	protected static AtomicLong serial = new AtomicLong(0L);
	protected final boolean heap;
	
	protected long heapUsed = -1L;
	protected long ts = 0L;
	protected long id = 0L;
	protected final String type;
	
	@StreamedAttribute
	public long getHeapUsed() {
		return heapUsed;
	}
	@StreamedAttribute
	public String getType() {
		return type;
	}
	
	@StreamedAttribute(timestamp=true)
	public Timestamp getTimestamp() {
		return new Timestamp(ts);
	}
	@StreamedAttribute(id=true)
	public long getId() {
		return id;
	}
	
	public void update() {
		heapUsed = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		ts = System.currentTimeMillis();
		id = serial.incrementAndGet();
	}
	
	public HeapUsed(boolean heap) {
		this.heap = heap;
		type = heap ? "Heap" : "NonHeap";
		update();
	}
}

@Streamed(name="test")
class Order {
	protected static AtomicLong serial = new AtomicLong(0L);
	String country;
	String state;
	String city;
	String item_sku;
	long item_qty = -1;
	Timestamp order_time = null;
	long order_id = -1;
	
	/**
	 * Creates a new Order
	 * @param country
	 * @param state
	 * @param city
	 * @param itemSku
	 * @param itemQty
	 * @param orderTime
	 * @param orderId
	 */
	public Order(String country, String state, String city, String itemSku,
			long itemQty) {
		this.country = country;
		this.state = state;
		this.city = city;
		item_sku = itemSku;
		item_qty = itemQty;
		order_time = new Timestamp(System.currentTimeMillis());
		order_id = serial.incrementAndGet();
	}


	/**
	 * @return the country
	 */
	@StreamedAttribute
	public String getCountry() {
		return country;
	}

	/**
	 * @return the state
	 */
	@StreamedAttribute
	public String getState() {
		return state;
	}

	/**
	 * @return the city
	 */
	@StreamedAttribute
	public String getCity() {
		return city;
	}

	/**
	 * @return the item_sku
	 */
	@StreamedAttribute
	public String getItem_sku() {
		return item_sku;
	}

	/**
	 * @return the item_qty
	 */
	@StreamedAttribute
	public long getItem_qty() {
		return item_qty;
	}

	/**
	 * @return the order_time
	 */
	@StreamedAttribute(timestamp=true)
	public Timestamp getOrder_time() {
		return order_time;
	}

	/**
	 * @return the order_id
	 */
	@StreamedAttribute(id=true)
	public long getOrder_id() {
		return order_id;
	}
	
	
	
}

//{ "country", "state", "city", "item_sku", "item_qty",
//    "order_time", "order_id" };
