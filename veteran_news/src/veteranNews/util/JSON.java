/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.util;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 *
 * @author zmc94
 */
public class JSON {
	
	
	/**
	 * compare two list of content. 
	 * @param original the item list collected before
	 * @param newContent the current item list
	 * @return an JsonArray array that has new items in first JsonArray and 
	 * deleted items in second JsonArray
	 */
	public static JsonArray[] JsonArrayCompare(JsonArray original, JsonArray newContent) {
		int[] originalMatch = new int[original.size()];
		for (int i=0; i<originalMatch.length; i++) originalMatch[i] = -1;
		int[] newContentMatch = new int[newContent.size()];
		for (int i=0; i<newContentMatch.length; i++) newContentMatch[i] = -1;
		
		for (int i = 0; i < originalMatch.length; i++) {
			int j = i;
			if (j >= newContent.size()) j = 0;
			int startPoint = j;
			do {
				if (newContentMatch[j] == -1 && jsonSingleCompare(original.get(i), newContent.get(j))) {
					originalMatch[i] = j;
					newContentMatch[j] = i;
					break;
				}
				j++;
				if (j >= newContent.size()) j = 0;
			} while (j != startPoint);
		}
		JsonArrayBuilder deletedItem = Json.createArrayBuilder();
		JsonArrayBuilder createdItem = Json.createArrayBuilder();
		for (int i = 0; i < originalMatch.length; i++) {
			if (originalMatch[i] == -1) {
				deletedItem.add(original.get(i));
			}
		}
		for (int i = 0; i < newContentMatch.length; i++) {
			if (newContentMatch[i] == -1) {
				createdItem.add(newContent.get(i));
			}
		}
		JsonArray[] result = new JsonArray[2];
		result[0] = createdItem.build();
		result[1] = deletedItem.build();
		return result;
	}

	private static boolean jsonSingleCompare(JsonValue a, JsonValue b) {
		if (a.getValueType() == JsonValue.ValueType.STRING && b.getValueType() == JsonValue.ValueType.STRING) {
			return ((JsonString) a).equals(b);
		} else if (a.getValueType() == JsonValue.ValueType.NUMBER && b.getValueType() == JsonValue.ValueType.NUMBER) {
			return ((JsonNumber) a).equals(b);
		} else if (a.getValueType() == JsonValue.ValueType.TRUE && b.getValueType() == JsonValue.ValueType.TRUE) {
			return true;
		} else if (a.getValueType() == JsonValue.ValueType.FALSE && b.getValueType() == JsonValue.ValueType.FALSE) {
			return true;
		} else if (a.getValueType() == JsonValue.ValueType.NULL && b.getValueType() == JsonValue.ValueType.NULL) {
			return true;
		} else if (a.getValueType() == JsonValue.ValueType.OBJECT && b.getValueType() == JsonValue.ValueType.OBJECT) {
			Object[] aKeys = ((JsonObject) a).keySet().toArray();
			Object[] bKeys = ((JsonObject) b).keySet().toArray();
			if (aKeys.length != bKeys.length) {
				return false;
			}
			for (int i = 0; i < aKeys.length; i++) {
				JsonValue temp1 = ((JsonObject) a).get(aKeys[i]);
				JsonValue temp2 = ((JsonObject) b).get(bKeys[i]);
				if (!jsonSingleCompare(temp1, temp2)) {
					return false;
				}
			}
			return true;
		} else if (a.getValueType() == JsonValue.ValueType.ARRAY && b.getValueType() == JsonValue.ValueType.ARRAY) {
			if (((JsonArray) a).size() != ((JsonArray) b).size()) {
				return false;
			}
			for (int i = 0; i < ((JsonArray) a).size(); i++) {
				if (!jsonSingleCompare(((JsonArray) a).get(i), ((JsonArray) b).get(i))) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
}
