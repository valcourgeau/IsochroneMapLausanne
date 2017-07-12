package ch.epfl.isochrone.tiledmap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
@SuppressWarnings("serial")
public class TileCache extends LinkedHashMap<Long, Tile> {
	private final int MAX_SIZE;
	
	public LinkedHashMap<Long, Tile> cache = new LinkedHashMap<Long, Tile>() {
		        @Override
		        protected boolean removeEldestEntry(Map.Entry<Long,Tile> e){
		            return size() > MAX_SIZE;
		        }
		};
	
	
	/**
	 * Constructeur par défault qui assigne 100 pour la taille du cache.
	 */
	public TileCache() {
		this(100);
	}
	
	/**
	 * 
	 * @param MAX_SIZE Taille maximal du cache
	 */
	public TileCache(int MAX_SIZE) {
		this.MAX_SIZE = MAX_SIZE;
	}
	
	/**
	 * 
	 * @param zoom	Zoom du point correspondant à la tuile à insérer dans le cache.
	 * @param x 	X du point correspondant à la tuile à insérer dans le cache.
	 * @param y		Y du point correspondant à la tuile à insérer dans le cache.
	 * @param tile	Tuile à insérer dans le cache.
	 */
	public void put(int zoom, int x, int y, Tile tile) {
		Iterator<Long> it = cache.keySet().iterator();
		if (it.hasNext()) {
			Long eldestKey = it.next();
			if (removeEldestEntry(new Entry<Long, Tile>(eldestKey, cache.get(eldestKey))))
				cache.remove(eldestKey);
		}
		cache.put(intsToKey(zoom, x, y), tile);
	}
	
	/**
	 * 
	 * @param zoom	Zoom du point correspondant à la tuile à retourner dans le cache.
	 * @param x		X du point correspondant à la tuile à retourner dans le cache.
	 * @param y		Y du point correspondant à la tuile à retourner dans le cache.
	 * @return		Tuile placée aux coordonnées (zoom, x, y) dans le cache.
	 */
	public Tile get(int zoom, int x, int y){
		return cache.get(intsToKey(zoom, x, y));
	}

	/**
	 * 
	 * @param zoom 	Valeur du zoom de la Tuile.
	 * @param x		Valeur du X de la Tuile souhaiter.
	 * @param y		Valeur du Y de la Tuile souhaiter.
	 * @return		Un long qui contient les trois arguments compressés.
	 */
	private long intsToKey(int zoom, int x, int y) {
		return zoom + x * 100 + y * 10000000 * 100;
	}
	
	/**
	 * 
	 * @author Virgile Neu (224138)
	 * @author Valentin Courgeau (225255)
	 *
	 * @param <Long> 	Type des clés du cache.
	 * @param <Tile>	Type des valeurs du cache.
	 */
	private final static class Entry<K, V> implements Map.Entry<Long, Tile> {
		private Long key;
		private Tile value;

		/**
		 * 
		 * @param key		Clé de l'entré.
		 * @param value		Valeur de l'entré.
		 */
		private Entry(Long key, Tile value) {
			this.key = key;
			this.value = value;
		}
		
		@Override
		public Long getKey() {
			return this.key;
		}

		@Override
		public Tile getValue() {
			return this.value;
		}

		@Override
		public Tile setValue(Tile arg0) {
			return this.value = arg0;
		}
	}
}
