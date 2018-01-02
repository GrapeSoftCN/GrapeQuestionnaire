package CommonModel;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.java.check.checkHelper;
import common.java.database.dbFilter;
import common.java.string.StringHelper;


public class CModel {

	/**
	 * 生成or查询条件
	 * @param pkStrings
	 * @param ids
	 * @return
	 */
	public JSONArray deleteBuildCond(String pkStrings, String[] ids) {
		dbFilter filter = new dbFilter();
		if (ids != null && ids.toString().trim().length() > 0) {
			for (String id : ids) {
				if (StringHelper.InvaildString(id)) {
					if (ObjectId.isValid(id) || checkHelper.isInt(id)) {
						filter.eq(pkStrings, id);
					}
				}
			}
		}
		return filter.build();
	}

	/**
	 * 生成查询条件
	 * @param info
	 * @return
	 */
	public JSONArray searchBuildCond(String info) {
		String key;
		Object value;
		JSONArray condArray = null;
		dbFilter filter = new dbFilter();
		if (StringHelper.InvaildString(info)) {
			JSONObject object = JSONObject.toJSON(info);
			if (object != null && object.size() > 0) {
				for (Object obj : object.keySet()) {
					key = obj.toString();
					value = object.getString(key);
					filter.eq(key, value);
				}
				condArray = filter.build();
			} else {
				condArray = JSONArray.toJSONArray(info);
			}
		}
		return condArray;
	}
	/**
     * JSONArray转换为List
     * 
     * @param array
     *            JSONArray格式：[string,string,string]
     * @return
     */
    public List<String> jsonArray2List(JSONArray array) {
        List<String> list = new ArrayList<>();
        if (array != null && array.size() > 0) {
            for (Object obj : array) {
                list.add((String) obj);
            }
        }
        return list;
    }
}
