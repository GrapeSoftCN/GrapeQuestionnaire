package interfaceApplication;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.interfaceModel.GrapeDBSpecField;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.security.codec;
import common.java.session.session;
import common.java.string.StringHelper;
import common.java.time.TimeHelper;
/**
 * 考场管理
 * 
 *
 */
public class Examination {
    private GrapeTreeDBModel Exam;
    private GrapeDBSpecField gDbSpecField;
    private String pkString;
    private session se;
    private JSONObject userInfo = null;
    private String createUser = null;

    public Examination() {
        Exam = new GrapeTreeDBModel();
        gDbSpecField = new GrapeDBSpecField();
        gDbSpecField.importDescription(appsProxy.tableConfig("Examination"));
        Exam.descriptionModel(gDbSpecField);
        Exam.bindApp();
        pkString = Exam.getPk();

        se = new session();
        userInfo = se.getDatas();
        if (userInfo != null && userInfo.size() > 0) {
            createUser = userInfo.getMongoID("_id");
        }
    }

    // 新增考场信息
    @SuppressWarnings("unchecked")
    public String insert(String ExamInfo) {
        String questionId = null;
        String qid = null;
        ExamInfo = codec.DecodeFastJSON(ExamInfo);
        JSONObject object = JSONObject.toJSON(ExamInfo);
        if (object == null || object.size() <= 0) {
            return rMsg.netMSG(1, "无效参数");
        }
        if (object.containsKey("qid")) {
            qid = object.getString("qid");
        }
        if (!StringHelper.InvaildString(qid)) {
            return rMsg.netMSG(1, "无效问卷id");
        }
        object.put("startTime", TimeHelper.nowMillis());
        object.put("uid", createUser);
        questionId = (String) Exam.data(object).autoComplete().insertOnce();
        return questionId;
    }

    // 获取考场id
    @SuppressWarnings("unchecked")
    protected String getEid(String questionId) {
        String eid = "";
        JSONObject object = new JSONObject();
        if (!StringHelper.InvaildString(questionId)) {
            return rMsg.netMSG(2, "无效类型id");
        }
        object.put("qid", questionId);
        eid = insert(codec.encodeFastJSON(object.toJSONString()));
        return eid;
    }

    /**
     * 生成本次答题结果
     * @param questionId
     * @return
     */
    @SuppressWarnings("unchecked")
    protected String getResult(String eid,String info) {
        if (!StringHelper.InvaildString(eid)) {
            return rMsg.netMSG(false, "无效考场id");
        }
       JSONObject object = new JSONObject();
       object.put("result", info);
       object.put("endTime", TimeHelper.nowMillis());
       object = Exam.eq(pkString, eid).data(object).update();
       object = Exam.eq(pkString, eid).find();
       return rMsg.netMSG(0, object);
    }

    // 分页获取考场信息
    public String page(int idx, int pageSize) {
        long count = 0;
        JSONArray array = null;
        if (idx <= 0) {
            return rMsg.netMSG(false, "页码错误");
        }
        if (pageSize <= 0) {
            return rMsg.netMSG(false, "页长度错误");
        }
        count = Exam.count();
        array = Exam.page(idx, pageSize);
        return rMsg.netPAGE(idx, pageSize, count, array);
    }

    // 根据条件分页获取考场信息
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
            array = Exam.where(condArray).page(idx, pageSize);
        }
        return rMsg.netPAGE(idx, pageSize, count, array);
    }

    // 获取考场数据
    public String get(String questionId) {
        JSONObject object = null;
        if (!StringHelper.InvaildString(questionId)) {
            return rMsg.netMSG(2, "无效类型id");
        }
        object = Exam.eq(pkString, questionId).find();
        return rMsg.netMSG(true, (object != null && object.size() > 0) ? object : new JSONObject());
    }

    // 获取所有考场
    public String getAll() {
        return rMsg.netMSG(true, Exam.select());
    }
}
