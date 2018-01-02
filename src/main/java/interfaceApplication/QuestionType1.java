package interfaceApplication;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import CommonModel.CModel;
import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.authority.plvDef.UserMode;
import common.java.check.checkHelper;
import common.java.interfaceModel.GrapeDBSpecField;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.security.codec;
import common.java.session.session;
import common.java.string.StringHelper;
import common.java.time.TimeHelper;
/**
 * 
 * 题目类型管理
 *
 */
public class QuestionType1 {
    private GrapeTreeDBModel QuestType;
    private GrapeDBSpecField gDbSpecField;
    private String pkString;
    private CModel model;

    private session se;
    private JSONObject userInfo = null;
    private String currentWeb = null;
    private int userType = 0;

    public QuestionType1() {
        QuestType = new GrapeTreeDBModel();
        gDbSpecField = new GrapeDBSpecField();
        gDbSpecField.importDescription(appsProxy.tableConfig("QuestionType"));
        QuestType.descriptionModel(gDbSpecField);
        QuestType.bindApp();
        pkString = QuestType.getPk();

        model = new CModel();

        se = new session();
        userInfo = se.getDatas();
        if (userInfo != null && userInfo.size() > 0) {
            currentWeb = userInfo.getString("currentWeb");
            userType = userInfo.getInt("userType");
        }
    }

    /**
     * 新增题目类型
     * 
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
        object.put("wbid", currentWeb);
        typeId = (String) QuestType.data(object).autoComplete().insertOnce();
        return get(typeId);
    }

    /**
     * 修改题目类型
     * 
     * @param typeId
     * @param typeInfo
     * @return
     */
    public String update(String typeId, String typeInfo) {
        String result = rMsg.netMSG(100, "修改失败");
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
     * 
     * @param typeIds
     * @return
     */
    public String delete(String typeIds) {
        String result = rMsg.netMSG(100, "修改失败");
        long code = 0;
        String[] value = null;
        if (!StringHelper.InvaildString(typeIds)) {
            return rMsg.netMSG(2, "无效类型id");
        }
        value = typeIds.split(",");
        if (value != null) {
            JSONArray condArray = model.deleteBuildCond(pkString, value);
            if (condArray!=null && condArray.size() > 0) {
                code = QuestType.or().where(condArray).deleteAll(); 
            }
        }
        return code > 0 ? rMsg.netMSG(0, "删除成功") : result;
    }

    /**
     * 分页获取题目类型
     * 
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
        if (userType > 0) {
            if (userType >= UserMode.admin && userType < UserMode.root) {
                QuestType.eq("wbid", currentWeb);
            }
            count = QuestType.count();
            array = QuestType.page(idx, pageSize);
        }
        return rMsg.netPAGE(idx, pageSize, count, array);
    }

    /**
     * 根据条件分页获取题目类型
     * 
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
        JSONArray condArray = model.searchBuildCond(condString);
        if (userType > 0) {
            if (userType >= UserMode.admin && userType < UserMode.root) {
                QuestType.eq("wbid", currentWeb);
            }
            if (condArray != null && condArray.size() > 0) {
                array = QuestType.where(condArray).page(idx, pageSize);
            } else {
                return rMsg.netMSG(false, "无效条件");
            }
        }
        return rMsg.netPAGE(idx, pageSize, count, array);
    }

    /**
     * 获取一条题目类型数据
     * 
     * @param typeId
     * @return
     */
    public String get(String typeId) {
        JSONObject object = null;
        if (!StringHelper.InvaildString(typeId)) {
            return rMsg.netMSG(2, "无效类型id");
        }
        if (ObjectId.isValid(typeId) || checkHelper.isInt(typeId)) {
            object = QuestType.eq(pkString, typeId).find();
        }
        return rMsg.netMSG(true, (object != null && object.size() > 0) ? object : new JSONObject());
    }

    /**
     * 获取所有题目类型
     * 
     * @return
     */
    public String getAll() {
        return rMsg.netMSG(true, QuestType.select());
    }
}
