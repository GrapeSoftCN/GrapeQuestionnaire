package interfaceApplication;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import JGrapeSystem.rMsg;
import apps.appsProxy;
import check.checkHelper;
import interfaceModel.GrapeDBSpecField;
import interfaceModel.GrapeTreeDBModel;
import security.codec;
import string.StringHelper;
import time.TimeHelper;

/**
 * 
 * 题目类型管理
 *
 */
public class QuestionType {
	private GrapeTreeDBModel QuestType;
	private GrapeDBSpecField gDbSpecField;
	private String pkString;
	private String result = rMsg.netMSG(100, "修改失败");

	public QuestionType() {
		QuestType = new GrapeTreeDBModel();
		gDbSpecField = new GrapeDBSpecField();
		gDbSpecField.importDescription(appsProxy.tableConfig("QuestionType"));
		QuestType.descriptionModel(gDbSpecField);
		QuestType.bindApp();
		pkString = QuestType.getPk();
	}

	/**
	 * 新增题目类型
	 * @param info
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String insert(String info) {
		String typeId = null;
		info = codec.DecodeFastJSON(info);
		JSONObject object = JSONObject.toJSON(info);
		if (object == null || object.size() <= 0) {
			return rMsg.netMSG(1, "无效参数");
		}
		object.put("createTime", TimeHelper.nowMillis());
		typeId = (String) QuestType.data(object).autoComplete().insertOnce();
		return get(typeId);
	}

	/**
	 * 修改题目类型
	 * @param typeId
	 * @param typeInfo
	 * @return
	 */
	public String update(String typeId, String typeInfo) {
		typeInfo = codec.DecodeFastJSON(typeInfo);
		if (!StringHelper.InvaildString(typeId)) {
			return rMsg.netMSG(2, "无效类型id");
		}
		JSONObject object = JSONObject.toJSON(typeInfo);
		if (object == null || object.size() <= 0) {
			return rMsg.netMSG(1, "无效参数");
		}
		object = QuestType.eq(pkString, typeId).data(object).update();
		result = object != null ? rMsg.netMSG(0, "修改成功") : result;
		return result;
	}

	/**
	 * 删除题目类型
	 * @param typeIds
	 * @return
	 */
	public String delete(String typeIds) {
		long code = 0;
		String[] value = null;
		if (!StringHelper.InvaildString(typeIds)) {
			return rMsg.netMSG(2, "无效类型id");
		}
		value = typeIds.split(",");
		QuestType.or();
		if (value != null) {
			for (String typeId : value) {
				if (StringHelper.InvaildString(typeId)) {
					if (ObjectId.isValid(typeId) || checkHelper.isInt(typeId)) {
						QuestType.eq(pkString, typeId);
					}
				}
			}
			if (QuestType.getCondCount() > 0) {
				code = QuestType.deleteAll();
			}
		}
		return code > 0 ? rMsg.netMSG(0, "删除成功") : result;
	}

	/**
	 * 分页获取题目类型
	 * @param idx
	 * @param pageSize
	 * @return
	 */
	public String page(int idx, int pageSize) {
		long count = 0;
		JSONArray array = null;
		if (idx <= 0) {
			return rMsg.netMSG(false, "页码错误");
		}
		if (pageSize <= 0) {
			return rMsg.netMSG(false, "页长度错误");
		}
		count = QuestType.count();
		array = QuestType.page(idx, pageSize);
		return rMsg.netPAGE(idx, pageSize, count, array);
	}

	/**
	 * 根据条件分页获取题目类型
	 * @param idx
	 * @param pageSize
	 * @param condString
	 * @return
	 */
	public String pageby(int idx, int pageSize, String condString) {
		long count = 0;
		JSONArray array = null;
		if (idx <= 0) {
			return rMsg.netMSG(false, "页码错误");
		}
		if (pageSize <= 0) {
			return rMsg.netMSG(false, "页长度错误");
		}
		JSONArray condArray = JSONArray.toJSONArray(condString);
		if (condArray != null && condArray.size() > 0) {
			array = QuestType.where(condArray).page(idx, pageSize);
		}
		return rMsg.netPAGE(idx, pageSize, count, array);
	}

	/**
	 * 获取一条题目类型数据
	 * @param typeId
	 * @return
	 */
	public String get(String typeId) {
		JSONObject object = null;
		if (!StringHelper.InvaildString(typeId)) {
			return rMsg.netMSG(2, "无效类型id");
		}
		object = QuestType.eq(pkString, typeId).find();
		return rMsg.netMSG(true, (object != null && object.size() > 0) ? object : new JSONObject());
	}

	/**
	 * 获取所有题目类型
	 * @return
	 */
	public String getAll() {
		return rMsg.netMSG(true, QuestType.select());
	}
}
