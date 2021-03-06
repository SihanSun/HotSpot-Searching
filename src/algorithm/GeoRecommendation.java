
package algorithm;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;


// Recommendation based on geo distance and similar categories.
public class GeoRecommendation {
	  public List<Item> recommendItems(String userId, double lat, double lon) {
		  
		  List<Item> recommendedItems = new ArrayList<>();
		  DBConnection conn = DBConnectionFactory.getConnection();
		// Step 1 Get all favorited items
		  Set<String> favoriteItemIds = conn.getFavoriteItemIds(userId);
		
		// Step 2 Get all categories of favorited items, sort by count
		  Map<String,Integer>  allCategories = new HashMap();
		  for(String itemId : favoriteItemIds) {
			  Set<String> categories = conn.getCategories(itemId);
			  for(String category : categories) {
				  if(allCategories.containsKey(category)) {
					  allCategories.put(category, allCategories.get(category) + 1);
				  }else {
					  allCategories.put(category,1);
				  }
			  }
		  }
		  
		  List<Entry<String,Integer>> categoryList = 
				  new ArrayList<Entry<String,Integer>>(allCategories.entrySet());
		  Collections.sort(categoryList, new Comparator<Entry<String,Integer>>(){
			  public int compare(Entry<String,Integer>a1, Entry<String,Integer> a2) {
				  return Integer.compare(a1.getValue(), a2.getValue());
			  }
		  });

		// Step 3, do search based on category, filter out favorited events, sort by
		// distance
		  
		  Set<Item> visitedItems = new HashSet<>();
		  
		  for(Entry<String,Integer> category : categoryList) {
			  List<Item> items = conn.searchItems(lat, lon, category.getKey());
			  List<Item> filteredItems = new ArrayList<>();
			  
			  //filter out the items that has already be searched
			  for(Item item : items) {
				  if(!favoriteItemIds.contains(item.getItemId()) && !visitedItems.contains(item.getItemId())) {
					  filteredItems.add(item);
				  }
			  }
			  
			  Collections.sort(filteredItems, new Comparator<Item>() {

				@Override
				public int compare(Item item1, Item item2) {
					return Double.compare(item1.getDistance(), item2.getDistance());
				}
				  
			  });
			  
			  visitedItems.addAll(items);
			  recommendedItems.addAll(filteredItems);
		  }
		  
		  

		  return recommendedItems;
	  }
	  

}

