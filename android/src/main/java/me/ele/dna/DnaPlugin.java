package me.ele.dna;

import me.ele.dna.model.DnaClassInfo;
import me.ele.dna.model.ParameterInfo;
import me.ele.dna.model.ResultInfo;
import me.ele.dna.util.DnaUtils;
import me.ele.dna.util.GsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * DnaPlugin
 */
public class DnaPlugin implements MethodCallHandler {

    public static final String EXECUTE_NATIVE_CONTEXT = "executeNativeContext";
    public static final String INVOCATION_NODES = "_invocationNodes";
    public static final String OBJECT_JSON_WRAPPERS = "_objectJSONWrappers";
    public static final String RETURN_VAR = "returnVar";
    public static final String OBJECT_ID = "_objectId";
    public static final String DNA_JSON = "json";
    public static final String DNA_CLS = "cls";
    public static final String DNA_OBJECT = "object";
    public static final String DNA_CLS_NAME = "clsName";
    public static final String DNA_METHOD = "method";
    public static final String DNA_ARGS = "args";
    public static final String DNA_CONSTRUCT_CLS = "constructCls";

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "dna");
        channel.setMethodCallHandler(new DnaPlugin());
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals(EXECUTE_NATIVE_CONTEXT)) {
            try {
                excuteNativeMethod(call, result);
            } catch (Exception e) {
                DLog.e(e.getMessage());
                if (DnaClient.getClient().getiResultCallBack() != null) {
                    DnaClient.getClient().getiResultCallBack().onException(e);
                }
            }
        } else {
            result.notImplemented();
        }
    }

    private void excuteNativeMethod(MethodCall call, Result result) throws Exception {
        List<Map<String, Object>> invocationNodes = call.argument(INVOCATION_NODES);
        List<Map<String, Object>> objectJSONs = call.argument(OBJECT_JSON_WRAPPERS);
        Map<String, Object> valueMap = new HashMap<>();// 用于映射id和返回值
        if (DnaUtils.isEmpty(invocationNodes)) {
            return;
        }
        Map<String, String> returnVar = call.argument(RETURN_VAR);
        String finalReturnVarId = getReturnId(returnVar);
        if (!DnaUtils.isEmpty(objectJSONs)) {
            for (Map<String, Object> objectJson : objectJSONs) {
                String objectId = String.valueOf(objectJson.get(OBJECT_ID));
                String content = GsonUtils.toJson(objectJson.get(DNA_JSON));
                String classType = String.valueOf(objectJson.get(DNA_CLS));
                valueMap.put(objectId, new ParameterInfo(content, classType));
            }
        }

        Map<String, Object> idMap;
        String clsName;
        String constructName;
        String nodeId;
        String methodName;
        List<Object> argsMap;
        List<ParameterInfo> parameterInfos;
        Object currentObject = null;
        for (Map<String, Object> node : invocationNodes) {
            idMap = (Map<String, Object>) node.get(DNA_OBJECT);
            nodeId = (String) idMap.get(OBJECT_ID);
            if (idMap.containsKey(DNA_CLS_NAME)) {
                clsName = (String) idMap.get(DNA_CLS_NAME);
                valueMap.put(nodeId, new DnaClassInfo(clsName, false));
            } else if (!valueMap.containsKey(nodeId) && idMap.containsKey(DNA_CONSTRUCT_CLS)) {
                constructName = (String) idMap.get(DNA_CONSTRUCT_CLS);
                valueMap.put(nodeId, new DnaClassInfo(constructName, true));
            }

            methodName = String.valueOf(node.get(DNA_METHOD));
            argsMap = (List<Object>) node.get(DNA_ARGS);
            parameterInfos = getParameters(valueMap, argsMap);
            String returnId = getReturnId((Map<String, String>) node.get(RETURN_VAR));
            Object ownerObject = valueMap.get(nodeId);
            if (ownerObject == null) {
                return;
            } else if (ownerObject instanceof DnaClassInfo) {
                currentObject = ((DnaClassInfo) ownerObject).isConstrcut()
                        ? DnaClient.getClient().invokeConstructorMethod(((DnaClassInfo) ownerObject).getClassName(), parameterInfos)
                        : DnaClient.getClient().invokeMethod(false, ((DnaClassInfo) ownerObject).getClassName(), null, methodName, parameterInfos);
            } else if (ownerObject instanceof ResultInfo) {
                currentObject = DnaClient.getClient().invokeMethod(false, ((ResultInfo) ownerObject).getReturnType(), ((ResultInfo) ownerObject).getObject(), methodName, parameterInfos);
            } else {
                throw new Exception("Abnormal Error");
            }
            valueMap.put(returnId, currentObject);
            if (returnId != null && returnId.equals(finalReturnVarId)) {
                Object o = valueMap.get(returnId);
                result.success(o instanceof ResultInfo ? ((ResultInfo) o).getObject() : o);
            }
        }
        result.success(currentObject instanceof ResultInfo ? ((ResultInfo) currentObject).getObject() : currentObject);

    }


    /**
     * 获取下一个节点的id
     *
     * @param returnVar
     * @return
     */
    private String getReturnId(Map<String, String> returnVar) {
        String returnVarId = null;
        if (returnVar != null && !returnVar.isEmpty()) {
            returnVarId = returnVar.get(OBJECT_ID);
        }
        return returnVarId;
    }

    /**
     * 获取参数信息
     *
     * @param valueMap
     * @param argsMap
     * @return
     */
    private List<ParameterInfo> getParameters(Map<String, Object> valueMap, List<Object> argsMap) {
        String paraId;
        Object paraContent;
        List<ParameterInfo> parameters = new ArrayList<>();
        if (!DnaUtils.isEmpty(argsMap)) {
            for (Object o : argsMap) {
                if (o instanceof Map) {
                    paraId = String.valueOf(((Map) o).get(OBJECT_ID));
                    paraContent = valueMap.get(paraId);
                    if (paraContent instanceof ParameterInfo) {
                        parameters.add((ParameterInfo) paraContent);
                    } else if (paraContent instanceof ResultInfo) {
                        parameters.add(new ParameterInfo(getExcuteParameter(((ResultInfo) paraContent).getObject()), ((ResultInfo) paraContent).getReturnType()));
                    } else if (paraContent != null) {
                        parameters.add(new ParameterInfo(getExcuteParameter(paraContent), paraContent.getClass().getName()));
                    }
                } else if (o != null) {
                    parameters.add(new ParameterInfo(String.valueOf(o), o.getClass().getName()));

                } else {
                    parameters.add(new ParameterInfo(null, null));
                }
            }
        }
        return parameters;
    }

    private String getExcuteParameter(Object result) {
        return DnaUtils.isPrimitiveClass(result.getClass())
                ? String.valueOf(result)
                : GsonUtils.toJson(result);
    }


}
