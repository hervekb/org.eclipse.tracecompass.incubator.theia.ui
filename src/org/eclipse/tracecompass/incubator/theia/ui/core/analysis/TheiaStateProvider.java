package org.eclipse.tracecompass.incubator.theia.ui.core.analysis;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxPidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxTidAspect;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;



/**
 * An example of a simple state provider for a simple state system analysis
 *
 * This module is also in the developer documentation of Trace Compass. If it is
 * modified here, the doc should also be updated.
 *
 * @author Alexandre Montplaisir
 * @author Geneviève Bastien
 */
public class TheiaStateProvider extends AbstractTmfStateProvider {

    private static final @NonNull String PROVIDER_ID = "org.eclipse.tracecompass.incubator.theia.ui.state.provider"; //$NON-NLS-1$
    private static final int VERSION = 0;

    public static final String OPEN_TRACING_ATTRIBUTE = "openTracingSpans"; //$NON-NLS-1$

    /**
     * Quark name for ust spans
     */
    public static final String UST_ATTRIBUTE = "ustSpans"; //$NON-NLS-1$
    
    Integer id_kept;
    Integer ch_kept;
    int mutex=0;
	private long start_time;
	private Map<String, Integer> timer_tr=new HashMap<String,Integer>();
	private Map<String, Long> timer_latency = new HashMap<String,Long>();
	private Map<String, Long> worker_latency = new HashMap<String,Long>();
	private Map<String, String> worker_tid=new HashMap<String, String>();
    private static Map<String, Integer> thread_tr= new HashMap<String,Integer>();
    private static Map<String, Long> tick_ts= new HashMap<String,Long>();
    
    

    

    /**
     * Constructor
     *
     * @param trace
     *            The trace for this state provider
     */
    public TheiaStateProvider(@NonNull ITmfTrace trace) {
        super(trace, PROVIDER_ID);
        
        //fHandlers =
        //fHandlers.put("OpenTracingSpan", this::handleSpan); //$NON-NLS-1$
        //fHandlers.put("jaeger_ust:start_span", this::handleStart); //$NON-NLS-1$
        //fHandlers.put("jaeger_ust:end_span", this::handleEnd); //$NON-NLS-1$
        
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public @NonNull ITmfStateProvider getNewInstance() {
        return new TheiaStateProvider(getTrace());
    }

    @Override
    protected void eventHandle(ITmfEvent event) {

        /**
         * Do what needs to be done with this event, here is an example that
         * updates the CPU state and TID after a sched_switch
         *
         */


    	ITmfStateSystemBuilder ss = getStateSystemBuilder();
    	//System.out.println(event.getName());



    	//ArrayList tab = new ArrayList();

        //System.out.println(event.getContent().getFieldValue(String.class, IOpenTracingConstants.OPERATION_NAME));

            final long ts = event.getTimestamp().getValue();

            
            String trace_ev = event.getName();
            int q_theia = ss.getQuarkAbsoluteAndAdd("THEIA");
            int comp=0;
            Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
            Integer pid = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), LinuxTidAspect.class, event);
            
           
            
            try {
            	
            	
            	
            	
            	
            	if(event.getName().equals("uv_provider:uv_workerq_remove_event")) {

                    mutex++;

                    if(id_kept!=null || ch_kept!=null) {
                    int q_context_store = ss.getQuarkRelativeAndAdd(q_theia, "Context");
                    int q_context_cpu_store = ss.getQuarkRelativeAndAdd(q_context_store, String.valueOf(cpu));
                    ss.modifyAttribute(ts, "0", q_context_cpu_store);


                    int q_cpu_chid_store = ss.getQuarkRelativeAndAdd(q_theia, "cpu_store");
                    int q_chid_store = ss.getQuarkRelativeAndAdd(q_cpu_chid_store, String.valueOf(cpu));
                    int q_chid_id_store = ss.getQuarkRelativeAndAdd(q_chid_store, "id");
                    ss.modifyAttribute(ts, id_kept, q_chid_id_store);

                    int q_chid_ch_store = ss.getQuarkRelativeAndAdd(q_chid_store, "ch");
                    ss.modifyAttribute(ts, ch_kept, q_chid_ch_store);

                    }



                    if(event.getName().equals("uv_provider:uv_write_stream")) {
                    	
                    }



                }
                
                if(event.getName().equals("uv_provider:uv_timerPhase_event")) {
                	 
                	String tid=event.getContent().getFieldValue(String.class, "context._pthread_id");
                	
                	int q_metrics = ss.getQuarkRelativeAndAdd(q_theia, "Metrics");
                    int q_nextick = ss.getQuarkRelativeAndAdd(q_metrics, "Tick_latency");
                    int q_freq = ss.getQuarkRelativeAndAdd(q_metrics, "Tick_frequency");
                    int q_thread_freq = ss.getQuarkRelativeAndAdd(q_freq , tid);
               	    int q_thread = ss.getQuarkRelativeAndAdd(q_nextick , tid);
               	    if(thread_tr.get(tid)==null)  {
               	    	start_time=ss.getStartTime();
               	    	thread_tr.put(tid, 0);
               	    }
               	    
               	    
               	    long rts=ts-start_time;
               	    
               	    if(rts<=1000000000) {
               	    	thread_tr.put(tid, thread_tr.get(tid)+1);
               	    	
               	    }
               	    else {
               	    	
               	    	ss.modifyAttribute(ts, thread_tr.get(tid), q_thread_freq);
               	    	thread_tr.put(tid, 0);
               	    	start_time=ts;
               	    	
               	    }
               	    
               	    
               	    	
               	  
               	    
               	    if(tick_ts.get(tid)==null) {
               	    
               	    	//ss.modifyAttribute(ts, tid, q_thread);
               	    	
               	    	tick_ts.put(tid,  event.getTimestamp().toNanos());
               	    	
               	    }
               	    else {
                    //String state = String.valueOf(ss.queryOngoing(q_thread));
               	    	if(tick_ts.get(tid)!=null) {
                    ss.modifyAttribute(ts, (event.getTimestamp().toNanos()-tick_ts.get(tid))/1000000, q_thread);
                    //if(!state.equals("null")||!state.equals("nullValue")||state!=null) {
               	    //long cur_ts= ss.getOngoingStartTime(q_thread);
               	    //ss.modifyAttribute(ts, (ts-cur_ts)/1000000, q_thread);
               	    //ss.modifyAttribute(ts, null, q_thread);
               	    tick_ts.remove(tid);
               	    
               	    	}
                    }
               	    
               	    
                	
                	
                }

                
                
                if(event.getName().equals("uv_provider:uv_timersq_remove_event")) {
                	
                	 
                	String tid=event.getContent().getFieldValue(String.class, "context._pthread_id");
                    
             	    int q_metrics = ss.getQuarkRelativeAndAdd(q_theia, "Metrics");
                    int q_timer = ss.getQuarkRelativeAndAdd(q_metrics, "Timers_queue");
                    
                    int q_thread_timer = ss.getQuarkRelativeAndAdd(q_timer , tid);
                    
                    if(timer_tr.get(tid)!=null)
                    	timer_tr.put(tid, timer_tr.get(tid)-1);
                    else
                    	timer_tr.put(tid, 0);
                    
                    ss.modifyAttribute(ts, timer_tr.get(tid), q_thread_timer);
                	
                	
                }
                
               
               
               if(event.getName().equals("uv_provider:uv_timersq_insert_event")) {
            	   String id_timer=event.getContent().getFieldValue(String.class, "data");
            	   
                 
              	    timer_latency.put(id_timer,  ts);
                	
                	
                }
               
               if(event.getName().equals("uv_provider:uv_run_timers_event")) {
            	   String id_timer=event.getContent().getFieldValue(String.class, "backend_fd");
            	   String tid=event.getContent().getFieldValue(String.class, "context._pthread_id");
            	   int q_metrics = ss.getQuarkRelativeAndAdd(q_theia, "Metrics");
                   int q_timer = ss.getQuarkRelativeAndAdd(q_metrics, "Timers_latency");
                   
                   int q_thread_timer = ss.getQuarkRelativeAndAdd(q_timer , tid);
                   if(timer_latency.get(id_timer)!=null) {
                	   
                   ss.modifyAttribute(ts, (ts-timer_latency.get(id_timer))/1000000, q_thread_timer);
              	   timer_latency.remove(id_timer);
                   }
                	
                	
                }
               
               
               if(event.getName().equals("uv_provider:uv_work_submit_event")) {
            	   String id_worker=event.getContent().getFieldValue(String.class, "backend_fd");
            	   String tid=event.getContent().getFieldValue(String.class, "context._pthread_id");
                 
              	    worker_latency .put(id_worker,  ts);
              	    worker_tid.put(id_worker, tid);
                	
                	
                }
               
               
               if(event.getName().equals("uv_provider:uv_worker_event")) {
            	   String id_worker=event.getContent().getFieldValue(String.class, "backend_fd");
            	   String tid=event.getContent().getFieldValue(String.class, "context._pthread_id");
            	   int q_metrics = ss.getQuarkRelativeAndAdd(q_theia, "Metrics");
                   int q_workers = ss.getQuarkRelativeAndAdd(q_metrics, "Workers_latency");
                   
                   
                   if(worker_latency.get(id_worker)!=null) {
                   int q_thread_worker = ss.getQuarkRelativeAndAdd(q_workers , worker_tid.get(id_worker));  
                   ss.modifyAttribute(ts, (ts-worker_latency.get(id_worker))/1000, q_thread_worker);
              	   worker_latency.remove(id_worker);
              	   worker_tid.remove(id_worker);
                   }
                	
                	
                }

            	
            	
            	
            	
            	
            	
            	
            	if(event.getName().equals("uv_provider:uv_async_file_event")) {
            		
            		String tid=event.getContent().getFieldValue(String.class, "context._pthread_id");
            		String id=event.getContent().getFieldValue(String.class, "id");
            		//Integer pid = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), LinuxPidAspect.class, event);
            		//System.out.println(pid);
            		String oper=event.getContent().getFieldValue(String.class, "oper");
            		int q_ws = ss.getQuarkRelativeAndAdd(q_theia, "context_WS");
            		int q_pid_opt = ss.optQuarkRelative(q_ws, String.valueOf(pid));
                  	    if(q_pid_opt<0) {
            		int q_pid = ss.getQuarkRelativeAndAdd(q_theia, "context_pid");
            		int test_pid_str = ss.optQuarkRelative(q_pid, tid);  
            		
            		
               		
            		
            		if(test_pid_str>0) {
            			
               		int q_pid_str = ss.getQuarkRelativeAndAdd(q_pid, tid);  		
               		String pid_state = String.valueOf(ss.queryOngoing(q_pid_str));
               		
               		
               		
               		
               		
               		int q_id= ss.optQuarkRelative(q_theia, "context_WS",pid_state,"id");
     			    String id_state= String.valueOf(ss.queryOngoing(q_id));
     			   
     			    int q_ch= ss.getQuarkRelative(q_theia, "context_WS",pid_state, "ch");
     			    String ch_state= String.valueOf(ss.queryOngoing(q_ch));
     			    
     			  
     			    
     			    if(oper.equals("exit")&& id_state!=null && !id_state.equals("null")&& !id_state.equals("nullValue")&& ch_state !=null && !ch_state.equals("nullValue") && !ch_state.equals("null")&& !id_state.contains("undefine")) {
     			    	
     			    	int q_spans = ss.getQuarkRelative(q_theia, "spans");
     			    	
             		    int q_chid = ss.getQuarkRelative(q_spans, id_state+"_"+ch_state);
             		
             		    int q_chid_sys = ss.getQuarkRelativeAndAdd(q_chid, "Libuv");
             		    ss.modifyAttribute(ts, null, q_chid_sys);
     			    	
     			    }
     			    else {
     			    	
     			    	if(id_state!=null && !id_state.equals("null")&& !id_state.equals("nullValue")&& ch_state !=null && !ch_state.equals("nullValue") && !ch_state.equals("null")&& !id_state.contains("undefine")) {
     			    	
     			    	int q_spans = ss.getQuarkRelative(q_theia, "spans");
             		    int q_chid = ss.getQuarkRelative(q_spans, id_state+"_"+ch_state);
             		
             		    int q_chid_sys = ss.getQuarkRelativeAndAdd(q_chid, "Libuv");
             		    ss.modifyAttribute(ts, oper, q_chid_sys);
             		    
             		    
             		    
     			    	}
     			    	
     			    	
     			    	
     			    }
               		
               		
               		
               		
            		}
            		
            	    }
            	    
            	    else {
            	    	
            	    	int q_id = ss.getQuarkRelative(q_theia, "context_WS",String.valueOf(pid),"id");
          			    
         			    String id_state= String.valueOf(ss.queryOngoing(q_id));
         			   
         			    int q_ch= ss.getQuarkRelative(q_theia, "context_WS",String.valueOf(pid),"ch");
         			    String ch_state= String.valueOf(ss.queryOngoing(q_ch));
         			    
         			    
         		
         			 
            		    
            		    
            		    if(oper.equals("exit")&& id_state!=null && !id_state.equals("null")&& !id_state.equals("nullValue")&& ch_state !=null && !ch_state.equals("nullValue") && !ch_state.equals("null")&& !id_state.contains("undefine")) {
         			    	
            		    	int q_pid = ss.getQuarkRelativeAndAdd(q_theia, "context_pid");
         	            	int test_pid_str = ss.optQuarkRelative(q_pid, tid);
         	            	
         	            	if(test_pid_str>0) {
         	            	
         	            	int q_pid_str = ss.getQuarkRelativeAndAdd(q_pid, tid);  		
         	               	String pid_state = String.valueOf(ss.queryOngoing(q_pid_str));
         	               		
         	               	q_id= ss.optQuarkRelative(q_theia, "context_WS",pid_state,"id");
         	     			id_state= String.valueOf(ss.queryOngoing(q_id));
         	     			   
         	     			q_ch= ss.getQuarkRelative(q_theia, "context_WS",pid_state, "ch");
         	     			ch_state= String.valueOf(ss.queryOngoing(q_ch));
         	     			
         			    	int q_spans = ss.getQuarkRelative(q_theia, "spans");
                 		    int q_chid = ss.getQuarkRelative(q_spans, id_state+"_"+ch_state);
                 		
                 		    int q_chid_sys_ = ss.getQuarkRelativeAndAdd(q_chid, "Libuv");
                 		    ss.modifyAttribute(ts, null, q_chid_sys_);
                 		    
         	            	}
         			    	
         			    }
         			    else {
         			    	
         			    	if(id_state!=null && !id_state.equals("null")&& !id_state.equals("nullValue")&& ch_state !=null && !ch_state.equals("nullValue") && !ch_state.equals("null")&& !id_state.contains("undefine")) {
         			    	
         			    	int q_pid = ss.getQuarkRelativeAndAdd(q_theia, "context_pid");
         	            	int test_pid_str = ss.optQuarkRelative(q_pid, tid);
         	            	
         	            	if(test_pid_str>0) {
         	            	
         	            	int q_pid_str = ss.getQuarkRelativeAndAdd(q_pid, tid);  		
         	               	String pid_state = String.valueOf(ss.queryOngoing(q_pid_str));
         	               		
         	               	q_id= ss.optQuarkRelative(q_theia, "context_WS",pid_state,"id");
         	     			id_state= String.valueOf(ss.queryOngoing(q_id));
         	     			   
         	     			q_ch= ss.getQuarkRelative(q_theia, "context_WS",pid_state, "ch");
         	     			ch_state= String.valueOf(ss.queryOngoing(q_ch));
         	     			
         			    	int q_spans = ss.getQuarkRelative(q_theia, "spans");
                 		    int q_chid = ss.getQuarkRelative(q_spans, id_state+"_"+ch_state);
                 		
                 		    int q_chid_sys_ = ss.getQuarkRelativeAndAdd(q_chid, "Libuv");
                 		    ss.modifyAttribute(ts, oper, q_chid_sys_);
                 		    
         	            	}
                 		    
                 		   
                 		    
         			    	}
         			    	
         			    }
            		    
            		    
            		    
            	    	
            	    }
                	

                }
            	
            	
            	
               if(event.getName().equals("uv_provider:uv_exit_recvmsg_event")) {
            	   int test = ss.optQuarkRelative(q_theia, "context_WS");
            	   String tid=event.getContent().getFieldValue(String.class, "context._pthread_id");
            	   if(test>0) {
            	   String ret=event.getContent().getFieldValue(String.class, "ret");
            	   //Integer pid = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), LinuxTidAspect.class, event);
            	   int q_cont = ss.getQuarkRelative(q_theia, "context_WS");
            	   List <Integer> q_f= ss.getSubAttributes(q_cont, false);
            	   for(int i:q_f) {
            		   int q_ret = ss.optQuarkRelative(i, "ret");
            		   if(q_ret>0) {
            		   int q_ret_ok = ss.getQuarkRelative(i, "ret");
            		   String ret_state = String.valueOf(ss.queryOngoing(q_ret_ok)); 
            		   if(ret_state.equals(ret)) {
            			   
            			int q_pid = ss.getQuarkRelativeAndAdd(q_theia, "context_pid");
                   		int q_pid_str = ss.getQuarkRelativeAndAdd(q_pid, tid);  		
                   		String pid_state = ss.getAttributeName(i);
                   		ss.modifyAttribute(ts, pid_state, q_pid_str);
                   		
            			   
            		   }
            			   
            		   }
            		   
            	   }
            		   
            	   }
                	

                }
               
               
               if(event.getName().startsWith("syscall_exit")) {
             	 //Integer pid = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), LinuxTidAspect.class, event);
             	  //System.out.println(pid);
             	 
             	   
             	   
            		
             	   int q_context = ss.optQuarkRelative(q_theia, "context_WS",String.valueOf(pid));
             	   
             		   
            		   
 	           	   
            		   
            		   //int pid_exist = ss.getQuarkRelative(q_context , String.valueOf(pid));
            		  
            		   if(q_context>0) {
            			   
            			   int q_f = ss.getQuarkRelative(q_theia, "context_WS",String.valueOf(pid));
            			   
            			   int q_ch= ss.optQuarkRelative(q_f, "ch");
            			   String ch_state= String.valueOf(ss.queryOngoing(q_ch));
            			   
            			   int q_id= ss.optQuarkRelative(q_f, "id");
            			   String id_state= String.valueOf(ss.queryOngoing(q_id));
            			  
            			   if(ch_state !=null && !ch_state.equals("nullValue") && !ch_state.equals("null")) {
            				   
            				int q_g = ss.optQuarkRelative(q_theia, "spans",id_state+"_"+ch_state);
            				if(q_g>0) {
            				int q_spans = ss.getQuarkRelative(q_theia, "spans");
                 		    int q_chid = ss.getQuarkRelative(q_spans, id_state+"_"+ch_state);
                 		
                 		    int q_chid_sys = ss.getQuarkRelativeAndAdd(q_chid, "syscall");
                 		    ss.modifyAttribute(ts, null, q_chid_sys);
                 		
                            if(event.getName().equals("syscall_exit_write")) {
                            String ret=event.getContent().getFieldValue(String.class, "ret");
                			int q_ret= ss.getQuarkRelativeAndAdd(q_f, "ret");
                    		ss.modifyAttribute(ts, ret, q_ret);                			
                			
                		}
                 		
            				}
            			   }
            		   
             	   }

                }
                
               
               
               
              if(event.getName().startsWith("syscall_entry")) {
            	  //Integer pid = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), LinuxTidAspect.class, event);
            	  //System.out.println(pid);
            	 
            	   
            	   
           		
            	   int q_context = ss.optQuarkRelative(q_theia, "context_WS",String.valueOf(pid));
            	   
            		   
           		   
	           	   
           		   
           		   //int pid_exist = ss.getQuarkRelative(q_context , String.valueOf(pid));
           		  
           		   if(q_context>0) {
           			   
           			   int q_f = ss.getQuarkRelative(q_theia, "context_WS",String.valueOf(pid));
           			   
           			   int q_ch= ss.optQuarkRelative(q_f, "ch");
           			   String ch_state= String.valueOf(ss.queryOngoing(q_ch));
           			   
           			   int q_id= ss.optQuarkRelative(q_f, "id");
           			   String id_state= String.valueOf(ss.queryOngoing(q_id));
           			  
           			   if(ch_state !=null && !ch_state.equals("nullValue") && !ch_state.equals("null")) {
           				   
           				int q_g = ss.optQuarkRelative(q_theia, "spans",id_state+"_"+ch_state);
           				if(q_g>0) {
           				int q_spans = ss.getQuarkRelative(q_theia, "spans");
                		int q_chid = ss.getQuarkRelative(q_spans, id_state+"_"+ch_state);
                		
                		int q_chid_sys = ss.getQuarkRelativeAndAdd(q_chid, "syscall");
                		ss.modifyAttribute(ts, event.getName(), q_chid_sys);
          		
           				}
           			   }
           		   
            	   }

               }
               
               
               


            if(trace_ev.equals("uv_provider:uv_send_event")) {
            	
            	String met=event.getContent().getFieldValue(String.class, "method");
            	String ch=event.getContent().getFieldValue(String.class, "channel");
            	String id=event.getContent().getFieldValue(String.class, "id");
            	String tid=event.getContent().getFieldValue(String.class, "context._pthread_id");
            	
            	

            			
            	if(!(met.startsWith("ELDHISTOGRAM")||met.startsWith("PerformanceObserver")||met.startsWith("DNSCHANNEL")||met.startsWith("FSEVENTWRAP")||met.startsWith("FSREQCALLBACK")||met.startsWith("GETADDRINFOREQWRAP")||met.startsWith("GETNAMEINFOREQWRAP")||met.startsWith("HTTPINCOMINGMESSAGE")||met.startsWith("HTTPCLIENTREQUEST")||met.startsWith("JSSTREAM")||met.startsWith("PIPECONNECTWRAP")||met.startsWith("PIPEWRAP")||met.startsWith("PROCESSWRAP")||met.startsWith("QUERYWRAP")||met.startsWith("SHUTDOWNWRAP")||met.startsWith("SIGNALWRAP")||met.startsWith("STATWATCHER")||met.startsWith("TCPCONNECTWRAP")||met.startsWith("TCPSERVERWRAP")||met.startsWith("TCPWRAP")||met.startsWith("TTYWRAP")||met.startsWith("UDPSENDWRAP")||met.startsWith("UDPWRAP")||met.startsWith("WRITEWRAP")||met.startsWith("ZLIB")||met.startsWith("SSLCONNECTION")||met.startsWith("PBKDF2REQUEST")||met.startsWith("RANDOMBYTESREQUEST")||met.startsWith("RANDOMBYTESREQUEST")||met.startsWith("TLSWRAP")||met.startsWith("Microtask")||met.startsWith("Timeout")||met.startsWith("Immediate")||met.startsWith("TickObject")||met.startsWith("PROMISE")||met.startsWith("run")||met.startsWith("resolve")||met.startsWith("destoy")||met.startsWith("undefined")||met.startsWith("FSREQPROMISE"))) {
            		
            		String[] tab=met.split("\\.");
                   
                    if(tab.length==2) { 
                    	met=tab[0];
                    	
                    	
            		if(met.startsWith("ServerSends")) {
            			comp=0;
            			int q_spans = ss.getQuarkRelativeAndAdd(q_theia, "spans");
                		int q_chid = ss.getQuarkRelativeAndAdd(q_spans, id+"_"+ch);
                		int q_ws = ss.getQuarkRelativeAndAdd(q_chid, "websocket");
                		ss.modifyAttribute(ts, null, q_ws);
                		
                		
                	
                		
                		
                		
                		
            			
            		}
            		
            		else {
            			
            			 if(!ch.equals("100000") && !ch.equals("200000") && !ch.equals("300000") && !ch.equals("400000")) {
            			comp++;             		
                		int q_spans = ss.getQuarkRelativeAndAdd(q_theia, "spans");
                		int q_chid = ss.getQuarkRelativeAndAdd(q_spans, id+"_"+ch);
                		int q_ws = ss.getQuarkRelativeAndAdd(q_chid, "websocket");              		
                		ss.modifyAttribute(ts, met, q_ws);
                		
                		int q_context = ss.getQuarkRelativeAndAdd(q_theia, "context_WS");
                		int q_context_pid_val = ss.getQuarkRelativeAndAdd(q_context, tab[1]);
                		int q_context_ch = ss.getQuarkRelativeAndAdd(q_context_pid_val, "ch");
                		int q_context_id = ss.getQuarkRelativeAndAdd(q_context_pid_val, "id");
                		ss.modifyAttribute(ts, id, q_context_id);
                		ss.modifyAttribute(ts, ch, q_context_ch);
                		
                		
                		
                		
                		
                		
                		
                		
            			 }
            			 
            			 else {
            				 
            				 
            				 
            				 }
            				 
            				 
            				 
            				 
            			 }
            		}
            			 
            			 
            			 
            			
            		}
             
                    }
            
            	
            
            
            }

        	 catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //$NON-NLS-1$



    }
}




