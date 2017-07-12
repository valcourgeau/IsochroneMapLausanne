package ch.epfl.isochrone.timetable;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class CachedGraph extends LinkedHashMap<Date, Graph>{
		private final int MAX_SIZE;
		
		private LinkedHashMap<Date, Graph> cache = new LinkedHashMap<Date, Graph>() {
			        @Override
			        protected boolean removeEldestEntry(Map.Entry<Date,Graph> e){
			            return size() > MAX_SIZE;
			        }
			};
		
		
		/**
		 * Constructeur par défault qui assigne 10 pour la taille du cache.
		 */
		public CachedGraph() {
			this(10);
		}
		
		/**
		 * 
		 * @param MAX_SIZE Taille maximal du cache
		 */
		public CachedGraph(int MAX_SIZE) {
			this.MAX_SIZE = MAX_SIZE;
		}
		
		@Override
		public Graph put(Date date, Graph graph) {
			Iterator<Date> it = cache.keySet().iterator();
			if (it.hasNext()) {
				Date eldestKey = it.next();
				if (removeEldestEntry(new Entry<Date, Graph>(eldestKey, cache.get(eldestKey))))
					cache.remove(eldestKey);
			}
			return cache.put(date, graph);
		}
		
				
		public Graph get(Date date){
			return cache.get(date);
		}
		
		public boolean containsKey(Date date) {
			return cache.containsKey(date);
		}
		
		/**
		 * 
		 * @author Virgile Neu (224138)
		 * @author Valentin Courgeau (225255)
		 *
		 * @param <Date> 	Type des clés du cache.
		 * @param <Graph>	Type des valeurs du cache.
		 */
		@SuppressWarnings("hiding")
		private final static class Entry<Date, Graph> implements Map.Entry<Date, Graph> {
			private Date key;
			private Graph value;

			private Entry(Date key, Graph value) {
				this.key = key;
				this.value = value;
			}
			
			@Override
			public Date getKey() {
				return this.key;
			}

			@Override
			public Graph getValue() {
				return this.value;
			}

			@Override
			public Graph setValue(Graph arg0) {
				return this.value = arg0;
			}
		}
	}