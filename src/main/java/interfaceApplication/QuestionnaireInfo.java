package interfaceApplication;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import CommonModel.CModel;
import JGrapeSystem.rMsg;
import apps.appsProxy;
import authority.plvDef.UserMode;
import interfaceModel.GrapeDBSpecField;
import interfaceModel.GrapeTreeDBModel;
import security.codec;
import session.session;
import string.StringHelper;
import time.TimeHelper;

/**
 * 问卷管理
 * 
 *
 */
public class QuestionnaireInfo {
    private GrapeTreeDBModel quest;
    private GrapeDBSpecField gDbSpecField;
    private String pkString;
    private CModel model;

    private session se;
    private JSONObject userInfo = null;
    private String currentWeb = null;
    private String createUser = null;
    private int userType = 0;

    public QuestionnaireInfo() {
        quest = new GrapeTreeDBModel();
        gDbSpecField = new GrapeDBSpecField();
        gDbSpecField.importDescription(appsProxy.tableConfig("QuestionnaireInfo"));
        quest.descriptionModel(gDbSpecField);
        quest.bindApp();
        pkString = quest.getPk();

        se = new session();
        userInfo = se.getDatas();
        if (userInfo != null && userInfo.size() > 0) {
            currentWeb = userInfo.getString("currentWeb");
            createUser = userInfo.getMongoID("_id");
            userType = userInfo.getInt("userType");
        }

        model = new CModel();
    }

    /***** --------------------前台接口-------------------- *****/
    /**
     * 分页获取问卷信息
     * 
     * @param wbid
     * @param idx
     * @param pageSize
     * @return
     */
    public String pageFront(String wbid, int idx, int pageSize) {
        long count = 0;
        JSONArray array = null;
        if (idx <= 0) {
            return rMsg.netMSG(false, "页码错误");
        }
        if (pageSize <= 0) {
            return rMsg.netMSG(false, "页长度错误");
        }
        if (!StringHelper.InvaildString(wbid)) {
            return rMsg.netMSG(false, "无效网站id");
        }
        quest.eq("wbid", wbid);
        count = quest.dirty().count();
        array = quest.page(idx, pageSize);
        return rMsg.netPAGE(idx, pageSize, count, getUserName(array));
    }

    /**
     * 根据条件分页获取问卷信息
     * 
     * @param wbid
     * @param idx
     * @param pageSize
     * @param condString
     * @return
     */
    public String pagebyFront(String wbid, int idx, int pageSize, String condString) {
        long count = 0;
        JSONArray array = null;
        if (idx <= 0) {
            return rMsg.netMSG(false, "页码错误");
        }
        if (pageSize <= 0) {
            return rMsg.netMSG(false, "页长度错误");
        }
        if (!StringHelper.InvaildString(wbid)) {
            return rMsg.netMSG(false, "无效wbid");
        }
        quest.eq("wbid", wbid);
        JSONArray condArray = model.searchBuildCond(condString);
        if (condArray != null && condArray.size() > 0) {
            array = quest.where(condArray).page(idx, pageSize);
        }
        return rMsg.netPAGE(idx, pageSize, count, getUserName(array));
    }

    /**
     * 前台获取一条问卷信息
     * 
     * @param qid
     * @return
     */
    public String getFront(String qid) {
        long endTime = 0, startTime = 0;
        if (!StringHelper.InvaildString(qid)) {
            return rMsg.netMSG(false, "无效问卷id");
        }
        JSONObject object = quest.eq(pkString, qid).find();
        if (object != null && object.size() > 0) {
            if (object.containsKey("startTime")) {
                startTime = object.getLong("startTime");
            }
            if (startTime != 0 && startTime > TimeHelper.nowMillis()) {
                return rMsg.netMSG(false, "该问卷还未到答题时间");
            }
            if (object.containsKey("endTime")) {
                endTime = object.getLong("endTime");
            }
            if (endTime != 0 && endTime < TimeHelper.nowMillis()) {
                return rMsg.netMSG(false, "该问卷已结束，暂不能进行作答");
            }
            object = setRandomQuest(object);
        }
        return rMsg.netMSG(true, getUserName(object));
    }

    @SuppressWarnings("unchecked")
    private JSONArray getUserName(JSONArray array) {
        JSONObject object;
        JSONObject userInfos, columnInfo;
        if (array != null && array.size() > 0) {
            userInfos = getUserInfo(getUid(array));
            columnInfo = getColumnInfo(getOgid(array));
            for (int i = 0; i < array.size(); i++) {
                object = (JSONObject) array.get(i);
                array.set(i, FillName(object, userInfos, columnInfo));
            }
        }
        return array;
    }

    private JSONObject getUserName(JSONObject object) {
        JSONObject userInfos, columnInfo;
        if (object != null && object.size() > 0) {
            userInfos = getUserInfo(getUid(object));
            columnInfo = getColumnInfo(getOgid(object));
            object = FillName(object, userInfos, columnInfo);
        }
        return object;
    }

    @SuppressWarnings("unchecked")
    private JSONObject FillName(JSONObject object, JSONObject userInfos, JSONObject columnInfo) {
        String temp, tempName = "", ogid, column = "";
        if (userInfos != null && userInfos.size() > 0) {
            temp = object.getString("createUser");
            if (StringHelper.InvaildString(temp)) {
                tempName = userInfos.getString(temp).split(",")[1];
            }
            object.put("createName", tempName);
            temp = object.getString("editUser");
            if (StringHelper.InvaildString(temp)) {
                tempName = userInfos.getString(temp).split(",")[1];
            }
            if (object.containsKey("ogid")) {
                ogid = object.getString("ogid");
                if (columnInfo != null && columnInfo.size() > 0) {
                    if (StringHelper.InvaildString(ogid)) {
                        column = columnInfo.getString(ogid);
                    }
                }
            }
            object.put("column", column);
        }
        return object;
    }

    private String getUid(JSONArray array) {
        String uid = "";
        JSONObject object;
        if (array != null && array.size() > 0) {
            for (Object obj : array) {
                object = (JSONObject) obj;
                uid = getUid(object) + ",";
            }
        }
        return StringHelper.fixString(uid, ',');
    }

    private String getUid(JSONObject object) {
        String temp, editTemp, uid = "";
        if (object != null && object.size() > 0) {
            temp = object.getString("createUser");
            editTemp = object.getString("editUser");
            if (StringHelper.InvaildString(temp) && StringHelper.InvaildString(editTemp)) {
                if (!uid.contains(temp)) {
                    uid += temp + ",";
                }
                if (!uid.contains(editTemp)) {
                    uid += editTemp + ",";
                }
            }
        }
        return StringHelper.fixString(uid, ',');
    }

    private String getOgid(JSONArray array) {
        String uid = "";
        JSONObject object;
        if (array != null && array.size() > 0) {
            for (Object obj : array) {
                object = (JSONObject) obj;
                uid = getUid(object) + ",";
            }
        }
        return StringHelper.fixString(uid, ',');
    }

    private String getOgid(JSONObject object) {
        String temp, ogid = "";
        if (object != null && object.size() > 0) {
            temp = object.getString("ogid");
            if (StringHelper.InvaildString(temp)) {
                if (!ogid.contains(temp)) {
                    ogid += temp + ",";
                }
            }
        }
        return StringHelper.fixString(ogid, ',');
    }

    /**
     * 获取用户信息
     * 
     * @param uid
     * @return
     */
    private JSONObject getColumnInfo(String ogid) {
        String temp = (String) appsProxy.proxyCall("/GrapeContent/ContentGroup/getColumnName/" + ogid);
        JSONObject object = JSONObject.toJSON(temp);
        return object;
    }

    /**
     * 获取用户信息
     * 
     * @param uid
     * @return
     */
    private JSONObject getUserInfo(String uid) {
        String temp = (String) appsProxy.proxyCall("/GrapeUser/user/getUserInfos/" + uid);
        JSONObject object = JSONObject.toJSON(temp);
        return object;
    }

    /***** --------------------后台接口-------------------- *****/
    /**
     * 新增问卷
     * 
     * @param info
     * @return
     */
    @SuppressWarnings("unchecked")
    public String insert(String info) {
        Object infos = null;
        long questionCount = 0;
        String questionIds = "";
        info = codec.DecodeFastJSON(info);
        JSONObject object = JSONObject.toJSON(info);
        if (object == null || object.size() <= 0) {
            return rMsg.netMSG(1, "无效参数");
        }
        if (object != null && object.size() > 0) {
            if (object.containsKey("questionNum")) {
                questionCount = object.getLong("questionNum");
            }
            if (object.containsKey("questionIds")) {
                questionIds = object.getString("questionIds");
            }
            if (questionIds.split(",").length > questionCount) {
                return rMsg.netMSG(false, "已设置题数大于总题数，添加问卷失败");
            }
            object.put("wbid", currentWeb);
            object.put("createUser", createUser);
            object.put("createTime", TimeHelper.nowMillis());
            object.put("editUser", createUser);
            infos = (String) quest.data(object).autoComplete().insertOnce();
        }
        return (infos == null) ? rMsg.netMSG(100, "添加失败") : rMsg.netMSG(0, "添加成功");
    }

    /**
     * 修改问卷信息
     * 
     * @param id
     * @param info
     * @return
     */
    @SuppressWarnings("unchecked")
    public String update(String id, String info) {
        JSONObject obj = null;
        if (!StringHelper.InvaildString(id)) {
            return rMsg.netMSG(1, "无效问卷id");
        }
        info = codec.DecodeFastJSON(info);
        if (!StringHelper.InvaildString(info)) {
            return rMsg.netMSG(1, "无效参数");
        }
        JSONObject object = JSONObject.toJSON(info);
        if (object != null && object.size() > 0) {
            object.put("editUser", createUser);
            object.put("editTime", TimeHelper.nowMillis());
        }
        obj = quest.eq(pkString, id).data(object).update();
        return (obj == null) ? rMsg.netMSG(100, "修改失败") : rMsg.netMSG(0, "修改成功");
    }

    /**
     * 删除问卷信息
     * 
     * @param ids
     * @return
     */
    public String delete(String ids) {
        long code = 0;
        String[] value = null;
        if (!StringHelper.InvaildString(ids)) {
            return rMsg.netMSG(1, "无效问卷id");
        }
        value = ids.split(",");
        if (value != null) {
            JSONArray condArray = model.deleteBuildCond(pkString, value);
            if (condArray != null && condArray.size() > 0) {
                code = quest.or().where(condArray).deleteAll();
            }
        }
        return code > 0 ? rMsg.netMSG(0, "删除成功") : rMsg.netMSG(100, "删除失败");
    }

    /**
     * 分页获取问卷信息
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
                quest.eq("wbid", currentWeb);
            }
            count = quest.dirty().count();
            array = quest.page(idx, pageSize);
        }
        return rMsg.netPAGE(idx, pageSize, count, getUserName(getQuestionInfo(array)));
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
                quest.eq("wbid", currentWeb);
            }
            if (condArray != null && condArray.size() > 0) {
                array = quest.where(condArray).page(idx, pageSize);
            } else {
                return rMsg.netMSG(false, "无效条件");
            }
        }
        return rMsg.netPAGE(idx, pageSize, count, getQuestionInfo(array));
    }

    /**
     * 非随机问卷显示题目信息
     * 
     * @param array
     * @return
     */
    @SuppressWarnings("unchecked")
    private JSONArray getQuestionInfo(JSONArray array) {
        JSONObject tempObj;
        if (array != null && array.size() > 0) {
            int l = array.size();
            for (int i = 0; i < l; i++) {
                tempObj = (JSONObject) array.get(i);
                array.set(i, getQuestionInfo(tempObj));
            }
        }
        return array;
    }

    /**
     * 非随机问卷显示题目信息
     * 
     * @param array
     * @return
     */
    @SuppressWarnings("unchecked")
    private JSONObject getQuestionInfo(JSONObject object) {
        String[] value = null;
        String questionIds = "";
        long isRandom = 0;
        JSONObject tempObj= null;
        JSONArray questArray = new JSONArray();
        if (object != null && object.size() > 0) {
            if (object.containsKey("isRandom")) {
                isRandom = object.getLong("isRandom");
            }
            if (object.containsKey("questionIds")) {
                questionIds = object.getString("questionIds");
            }
            if (StringHelper.InvaildString(questionIds)) {
                tempObj = new QuestionInfo().getQuestInfoById(questionIds);
                value = questionIds.split(",");
            }
            if (isRandom == 1) {
                object.put("questionIds", new JSONArray());
            } else {
                if (value != null) {
                    for (String qid : value) {
                        questArray.add(getQuestion(qid, tempObj));
                    }
                }
                object.put("questionIds", questArray);
            }
        }
        return object;
    }

    /**
     * 获取题目详细信息
     * @param qid
     * @param tempObj
     * @return
     */
    private JSONObject getQuestion(String qid, JSONObject tempObj) {
        long type;
        JSONObject temp = new JSONObject();
        if (tempObj != null && tempObj.size() > 0) {
            if (tempObj.containsKey(qid)) {
                temp = tempObj.getJson(qid);
                if (temp != null && temp.size() > 0) {
                    type = temp.getLong("type");
                    if (type != 0 || type != 1) {
                        temp.remove("options");
                    }
                }
            }
        }
        return temp;
    }

    /**
     * 前台显示问卷信息，设置随机题目
     * 
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    private JSONObject setRandomQuest(JSONObject object) {
        String[] value = null;
        String questionIds = "";
        long isRandom = 0;
        JSONArray questArray = new JSONArray();
        JSONObject rJsonObject = null;
        if (object != null && object.size() > 0) {
            if (object.containsKey("isRandom")) {
                isRandom = object.getLong("isRandom");
            }
            rJsonObject = getQuestInfo(isRandom, object);
            if (rJsonObject != null && rJsonObject.size() > 0) {
                if (isRandom == 0) {
                    if (object.containsKey("questionIds")) {
                        questionIds = object.getString("questionIds");
                    }
                    value = questionIds.split(",");
                    for (String string : value) {
                        if (rJsonObject.containsKey(string)) {
                            questArray.add(rJsonObject.getJson(string));
                        }
                    }
                } else {
                    for (Object obj : rJsonObject.keySet()) {
                        questArray.add(rJsonObject.getJson(obj.toString()));
                    }
                }
                object.put("questionIds", questArray);
            }
        }
        return (object != null && object.size() > 0) ? object : new JSONObject();
    }

    /**
     * 前台显示 获取题目信息
     * 
     * @param isRandom
     * @param object
     * @return
     */
    private JSONObject getQuestInfo(long isRandom, JSONObject object) {
        JSONObject rJsonObject = null;
        long questionNum = 0;
        String qtype = "", questionIds = "";
        QuestionInfo questionInfo = new QuestionInfo();
        if (isRandom == 1) { // 设置随机题目
            if (object != null && object.size() > 0) {
                if (object.containsKey("qtype")) {
                    qtype = object.getString("qtype");
                }
                if (object.containsKey("questionNum")) {
                    questionNum = object.getLong("questionNum");
                }
                if (StringHelper.InvaildString(qtype) && questionNum > 0) {
                    // 获取题目信息
                    rJsonObject = questionInfo.getRandom(new Long(questionNum).intValue(), qtype);
                }
            }
        } else {
            if (object.containsKey("questionIds")) {
                questionIds = object.getString("questionIds");
                rJsonObject = questionInfo.getQuestInfoById(questionIds);
            }
        }
        return rJsonObject;
    }
}
