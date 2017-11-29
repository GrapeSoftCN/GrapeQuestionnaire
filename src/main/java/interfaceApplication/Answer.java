package interfaceApplication;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import JGrapeSystem.rMsg;
import apps.appsProxy;
import interfaceModel.GrapeDBSpecField;
import interfaceModel.GrapeTreeDBModel;
import security.codec;
import session.session;
import string.StringHelper;
import time.TimeHelper;

public class Answer {
    private GrapeTreeDBModel answer;
    private GrapeDBSpecField gDbSpecField;
    
    private session se;
    private JSONObject userInfo;
    private String currentUID;

    public Answer() {
        answer = new GrapeTreeDBModel();
        gDbSpecField = new GrapeDBSpecField();
        gDbSpecField.importDescription(appsProxy.tableConfig("QuestionnaireInfo"));
        answer.descriptionModel(gDbSpecField);
        answer.bindApp();

        se = new session();
        userInfo = se.getDatas();
        if (userInfo!=null && userInfo.size() > 0) {
            currentUID = userInfo.getMongoID("_id");
        }
    }

    /**
     * 提交问卷
     * 
     * @param qid
     *            问卷id
     * @param info
     *            用户答题信息[{"questID":"","userAnswer":"","time":0,}]
     * @return
     */
    public String submitAnswer(String qid, String info) {
        JSONArray array = null;
        JSONObject object;
        String eid = new Examination().getEid(qid); // 获取考场id
        info = codec.encodeFastJSON(info);
        if (StringHelper.InvaildString(info)) {
            array = JSONArray.toJSONArray(info);
            array = getUserResult(eid, array);
        }
        if (array != null && array.size() > 0) {
            for (Object obj : array) {
                object = (JSONObject) obj;
                answer.data(object).insertOnce();
            }
        }
        long count = answer.eq("eid", eid).count(); // 用户已答题数
        if (count == 0) {
            return rMsg.netMSG(0, "还未答题，提交失败");
        }
        return getAnswerResult(eid, count);
    }

    
    /**
     * 评定提交的答案
     * @param aid
     * @param result
     * @return
     */
    @SuppressWarnings("unchecked")
    public String Review(String aid,int result) {
        String rs = rMsg.netMSG(100, "评定答案失败");
        JSONObject object = new JSONObject();
        object.put("userResult", result);
        object.put("reviewer", currentUID);
        object.put("reviewTime", TimeHelper.nowMillis());
        object = answer.eq("_id", aid).data(object).update();
        return (object!=null)?rMsg.netMSG(0, "评定答案成功"):rs;
    }
    
    /**
     * 获取本次问卷的结果
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private String getAnswerResult(String eid, long count) {
        JSONObject object = new JSONObject();
        long rightCount = answer.eq("eid", eid).eq("userResult", 0).count(); // 用户答对题数
        long wrongCount = answer.eq("eid", eid).eq("userResult", 1).count(); // 用户答错题数
        long pendCount = answer.eq("eid", eid).eq("userResult", 2).count(); // 待判定题数
        object.put("anserCount", count); // 用户已答题数
        object.put("rightCount", rightCount); // 答对题数
        object.put("wrongCount", wrongCount); // 答对题数
        object.put("pendCount", pendCount); // 待判定题数
        return new Examination().getResult(eid, object.toJSONString());
    }

    /**
     * 获取答题完整信息
     * 
     * @param eid
     * @param array
     * @return
     */
    @SuppressWarnings("unchecked")
    private JSONArray getUserResult(String eid, JSONArray array) {
        JSONObject rObject = new JSONObject();
        JSONObject object, temp;
        String userAnswer, answer;
        int userResult = 2;
        if (array != null && array.size() > 0) {
            int l = array.size();
            String questID = getQuestionId(array);
            rObject = new QuestionInfo().getQuestInfoById(questID); // 获取答案
            if (rObject != null && rObject.size() > 0) {
                for (int i = 0; i < l; i++) {
                    object = (JSONObject) array.get(i);
                    object.put("eid", eid); // 考场id
                    questID = object.getString("questID");
                    userAnswer = object.getString("userAnswer");
                    temp = rObject.getJson(questID);
                    if (temp != null && temp.size() > 0) {
                        answer = temp.getString("answer");
                        if (!answer.equals("0")) {
                            userResult = answer.equals(userAnswer) ? 0 : 1;
                        }
                    }
                    object.put("userResult", userResult);
                    array.set(i, object);
                }
            }
        }
        return array;
    }

    /**
     * 获取题目id
     * 
     * @param array
     * @return
     */
    private String getQuestionId(JSONArray array) {
        JSONObject obj;
        String temp, qid = "";
        if (array != null && array.size() > 0) {
            for (Object object : array) {
                obj = (JSONObject) object;
                temp = obj.getString("questID");
                if (StringHelper.InvaildString(temp)) {
                    qid += temp + ",";
                }
            }
        }
        return StringHelper.fixString(qid, ',');
    }

}
